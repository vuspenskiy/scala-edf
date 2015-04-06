package vu.edf

import java.io.InputStream
import java.nio.{ByteOrder, ByteBuffer}
import java.nio.channels.Channels
import EDFConstants._
import scalaz._; import Scalaz._

import scala.util.Try

/**
 * @author v.uspenskiy
 * @since 29/07/14 17:18
 *
 * This is an EDFParser which is capable of parsing files in the formats EDF and EDF+.
 *
 * @see https://github.com/MIOB/EDF4J
 * @see http://www.edfplus.info/
 * @see http://www.edfplus.info/specs/edfplus.html
 */
object EDFParser {

  /**
   * Parse the InputStream which should be at the start of an EDF-File.
   * The method returns an object containing the complete content of the EDF-File.
   */
  def parseEDF(is: InputStream): Option[EDFParserResult] = {
    for {
      header <- parseHeader(is).toOption.flatten
      signal <- parseSignal(is, header).toOption
    } yield {
      val (annotations, cleanHeader, cleanSignal) = parseAnnotation(header, signal)
      EDFParserResult(cleanHeader, cleanSignal, annotations)
    }
  }

  /**
   * Parse the InputStream which should be at the start of an EDF-File.
   * The method returns an object containing the complete header of the EDF-File
   */
  def parseHeader(is: InputStream): Try[Option[EDFHeader]] = Try {

    for {
      idCode <- readASCIIFromStream(is, IDENTIFICATION_CODE_SIZE).filter(_.trim == "0")
      subjectID <- readASCIIFromStream(is, LOCAL_SUBJECT_IDENTIFICATION_SIZE)
      recordingID <- readASCIIFromStream(is, LOCAL_RECORDING_IDENTIFICATION_SIZE)
      startDate <- readASCIIFromStream(is, START_DATE_SIZE)
      startTime <- readASCIIFromStream(is, START_TIME_SIZE)
      bytesInHeader <- readASCIIFromStream(is, HEADER_SIZE).map(_.trim).map(Integer.parseInt)
      formatVersion <- readASCIIFromStream(is, DATA_FORMAT_VERSION_SIZE)
      numberOfRecords <- readASCIIFromStream(is, NUMBER_OF_DATA_RECORDS_SIZE).map(_.trim).map(Integer.parseInt)
      durationOfRecords <- readASCIIFromStream(is, DURATION_DATA_RECORDS_SIZE).map(_.trim).map(java.lang.Double.parseDouble)
      numberOfChannels <- readASCIIFromStream(is, NUMBER_OF_CHANNELS_SIZE).map(_.trim).map(Integer.parseInt)
      channelLabels <- readBulkASCIIFromStream(is, LABEL_OF_CHANNEL_SIZE, numberOfChannels).toList.sequence
      transducerTypes <- readBulkASCIIFromStream(is, TRANSDUCER_TYPE_SIZE, numberOfChannels).toList.sequence
      dimensions <- readBulkASCIIFromStream(is, PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, numberOfChannels).toList.sequence
      minInUnits <- readBulkDoubleFromStream(is, PHYSICAL_MIN_IN_UNITS_SIZE, numberOfChannels).toList.sequence
      maxInUnits <- readBulkDoubleFromStream(is, PHYSICAL_MAX_IN_UNITS_SIZE, numberOfChannels).toList.sequence
      digitalMin <- readBulkIntFromStream(is, DIGITAL_MIN_SIZE, numberOfChannels).toList.sequence
      digitalMax <- readBulkIntFromStream(is, DIGITAL_MAX_SIZE, numberOfChannels).toList.sequence
      prefilterings <- readBulkASCIIFromStream(is, PREFILTERING_SIZE, numberOfChannels).toList.sequence
      numberOfSamples <- readBulkIntFromStream(is, NUMBER_OF_SAMPLES_SIZE, numberOfChannels).toList.sequence
    } yield {

      val reserveds = (0 until numberOfChannels) map { channel =>
        val a = new Array[Byte](RESERVED_SIZE)
        is.read(a)
        a.reverse.dropWhile(_ == 0).reverse.toList
      } toList

      val channelsData = List(channelLabels, transducerTypes, dimensions, minInUnits, maxInUnits,
        digitalMin, digitalMax, prefilterings, numberOfSamples).transpose

      EDFHeader(
        idCode,
        subjectID,
        recordingID,
        startDate,
        startTime,
        bytesInHeader,
        formatVersion,
        numberOfRecords,
        durationOfRecords,
        for((channelData, reserved) <- channelsData zip reserveds) yield {
          ChannelHeader(
            channelData(0).asInstanceOf[String].trim, channelData(1).asInstanceOf[String].trim, channelData(2).asInstanceOf[String].trim,
            channelData(3).asInstanceOf[Double], channelData(4).asInstanceOf[Double],
            channelData(5).asInstanceOf[Int], channelData(6).asInstanceOf[Int],
            channelData(7).asInstanceOf[String].trim, channelData(8).asInstanceOf[Int], reserved)
        }
      )
    }
  }

  /**
   * Parse only data EDF file. This method should be invoked only after
   * parseHeader method.
   */
  def parseSignal(is: InputStream, header: EDFHeader): Try[EDFSignal] = Try {

    val unitsInDigit = header.channels map { channel =>
      channel.maxInUnits - channel.minInUnits / (channel.digitalMax - channel.digitalMin)
    }

    val samplesPerRecord = header.channels.map(_.numberOfSamples).sum

    val ch = Channels.newChannel(is)
    val bytebuf = ByteBuffer.allocate(samplesPerRecord * 2)
    bytebuf.order(ByteOrder.LITTLE_ENDIAN)

    val digitalValues = (0 until header.numberOfRecords).map({ i =>

      bytebuf.rewind()
      ch.read(bytebuf)
      bytebuf.rewind()

      for (channel <- header.channels) yield {
        (0 until channel.numberOfSamples) map { _ => bytebuf.getShort.toInt}
      }
    }).transpose.map {
      _.flatten
    }


    val valuesInUnits = for ((values, units) <- digitalValues zip unitsInDigit) yield {
      values.map(_ * units).toSeq
    }

    EDFSignal(for {
      (units, (values, inUnits)) <- unitsInDigit zip (digitalValues zip valuesInUnits)
    } yield {
      ChannelData(units, values, inUnits)
    })
  }

  def parseAnnotation(header: EDFHeader, signal: EDFSignal): (List[EDFAnnotation], EDFHeader, EDFSignal) = {

    if (!header.formatVersion.startsWith("EDF+"))
      return null

    val annotationIndex = header.channels.indexWhere(_.channelLabel.trim == "EDF Annotations")
    if (annotationIndex == -1)
      return null

    val b = signal.channelsData(annotationIndex).digitalValues flatMap { si =>
      Seq((si % 256).toByte, (si / 256 % 256).toByte)
    }

    val cleanHeader = header.copy(channels = header.channels.take(annotationIndex) ++ header.channels.drop(annotationIndex+1))
    val cleanSignal = signal.copy(channelsData = signal.channelsData.take(annotationIndex) ++ signal.channelsData.drop(annotationIndex+1))

    (parseAnnotations(b), cleanHeader, cleanSignal)
  }

  private val TimestampedAnnotationListsSeparator = new String(Array(20.toChar, 0.toChar))
  private val AnnotationsSeparator = 20.toChar
  private val TimeStampSeparator = 21.toChar

  private def parseAnnotations(b: Seq[Byte]): List[EDFAnnotation] = {

    new String(b.toArray).split(TimestampedAnnotationListsSeparator).filterNot(_.isEmpty) flatMap { annotationPiece =>

      val parts = annotationPiece.split(AnnotationsSeparator)
      val timeStamp = parts.head.split(TimeStampSeparator).filterNot(_.trim.isEmpty)
      val onSet = timeStamp.headOption.map(java.lang.Double.parseDouble)
      val duration = timeStamp.drop(1).headOption.map(java.lang.Double.parseDouble)
      val annotations = parts.tail

      for(onSetValue <- onSet if annotations.nonEmpty)
        yield EDFAnnotation(onSetValue, duration.getOrElse(0.00), annotations)
    } toList
  }

  private def readBulkASCIIFromStream(is: InputStream, size: Int, length: Int): Seq[Option[String]] = {
    (0 until length) map { _ => readASCIIFromStream(is, size) }
  }

  private def readBulkDoubleFromStream(is: InputStream, size: Int, length: Int): Seq[Option[Double]] = {
    (0 until length) map { _ => readASCIIFromStream(is, size).map(_.trim).map(java.lang.Double.parseDouble) }
  }

  private def readBulkIntFromStream(is: InputStream, size: Int, length: Int): Seq[Option[Int]] = {
    (0 until length) map { _ => readASCIIFromStream(is, size).map(_.trim).map(Integer.parseInt) }
  }

  private def readASCIIFromStream(is: InputStream, size: Int): Option[String] = {
    val data = new Array[Byte](size)
    val len = is.read(data)

    if (len == data.length)
      Some(new String(data, EDFConstants.CHARSET))
    else None
  }
}

package vu.edf

import EDFConstants._
import java.io.{IOException, OutputStream}
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import vu._

/**
 * @author v.uspenskiy
 * @since 29/07/14 15:30
 *
 * @see https://github.com/MIOB/EDF4J
 */
object EDFWriter {

  implicit def channelHeader2Traversable(channelHeader: ChannelHeader): Seq[Any] = { import channelHeader._
    Seq(channelLabel, transducerType, dimensions, minInUnits, maxInUnits,
      digitalMin, digitalMax, prefilterings, numberOfSamples, reserveds)
  }

  /**
   * Writes the EDFHeader into the OutputStream.
   *
   * @param header The header to write
   * @param outputStream The OutputStream to write into
   * @throws IOException Will be thrown if it is not possible to write into the outputStream
   */
  def writeIntoOutputStream(header: EDFHeader, outputStream: OutputStream) {
    val df = new DecimalFormat("#0.0", make(new DecimalFormatSymbols()) {
      _.setDecimalSeparator('.')
    })

    val bb = ByteBuffer.allocate(header.bytesInHeader)
    putIntoBuffer(bb, IDENTIFICATION_CODE_SIZE, header.idCode)
    putIntoBuffer(bb, LOCAL_SUBJECT_IDENTIFICATION_SIZE, header.subjectID)
    putIntoBuffer(bb, LOCAL_RECORDING_IDENTIFICATION_SIZE, header.recordingID)
    putIntoBuffer(bb, START_DATE_SIZE, header.startDate)
    putIntoBuffer(bb, START_TIME_SIZE, header.startTime)
    putIntoBuffer(bb, HEADER_SIZE, String.valueOf(header.bytesInHeader))
    putIntoBuffer(bb, DATA_FORMAT_VERSION_SIZE, header.formatVersion)
    putIntoBuffer(bb, NUMBER_OF_DATA_RECORDS_SIZE, String.valueOf(header.numberOfRecords))
    putIntoBuffer(bb, DURATION_DATA_RECORDS_SIZE, header.durationOfRecords, df)
    putIntoBuffer(bb, NUMBER_OF_CHANNELS_SIZE, String.valueOf(header.channels.size))

    val channelsData = header.channels.transpose

    putIntoBuffer(bb, LABEL_OF_CHANNEL_SIZE, channelsData(0).asInstanceOf[Seq[String]])
    putIntoBuffer(bb, TRANSDUCER_TYPE_SIZE, channelsData(1).asInstanceOf[Seq[String]])
    putIntoBuffer(bb, PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, channelsData(2).asInstanceOf[Seq[String]])
    putIntoBuffer(bb, PHYSICAL_MIN_IN_UNITS_SIZE, channelsData(3).asInstanceOf[Seq[Double]], df)
    putIntoBuffer(bb, PHYSICAL_MAX_IN_UNITS_SIZE, channelsData(4).asInstanceOf[Seq[Double]], df)
    putIntoBuffer(bb, DIGITAL_MIN_SIZE, channelsData(5).map(String.valueOf))
    putIntoBuffer(bb, DIGITAL_MAX_SIZE, channelsData(6).map(String.valueOf))
    putIntoBuffer(bb, PREFILTERING_SIZE, channelsData(7).asInstanceOf[Seq[String]])
    putIntoBuffer(bb, NUMBER_OF_SAMPLES_SIZE, channelsData(8).map(String.valueOf))
    bb.put(channelsData(9).asInstanceOf[Seq[Seq[Byte]]].flatten.toArray)

    outputStream.write(bb.array())
  }

  private def putIntoBuffer(bb: ByteBuffer, lengthPerValue: Int, values: Seq[Double], df: DecimalFormat) {
    values.foreach(value => putIntoBuffer(bb, lengthPerValue, value, df))
  }

  private def putIntoBuffer(bb: ByteBuffer, length: Int, value: Double, df: DecimalFormat) {
    if (Math.floor(value) == value) {
      putIntoBuffer(bb, length, String.valueOf(value.intValue()))
    } else {
      putIntoBuffer(bb, length, df.format(value))
    }
  }

  private def putIntoBuffer(bb: ByteBuffer, lengthPerValue: Int, values: Seq[String]) {
    values.foreach(value => putIntoBuffer(bb, lengthPerValue, value))
  }

  private def putIntoBuffer(bb: ByteBuffer, length: Int, value: String) {
    bb.put(make(ByteBuffer.allocate(length)) { buffer =>
      buffer.put(value.getBytes(EDFConstants.CHARSET))
      buffer.rewind()
    })
  }
}

package vu.edf

/**
 * @author v.uspenskiy
 * @since 29/07/14 16:00
 *
 * @see https://github.com/MIOB/EDF4J
 */

case class EDFAnnotation(onSet: Double, duration: Double, annotations: Array[String])

case class EDFHeader (
  idCode: String,
  subjectID: String,
  recordingID: String,
  startDate: String,
  startTime: String,
  bytesInHeader: Int = 0,
  formatVersion: String,
  numberOfRecords: Int = 0,
  durationOfRecords: Double = 0.00,
  channels: Seq[ChannelHeader]
)

case class ChannelHeader(
  channelLabel: String,
  transducerType: String = "",
  dimensions: String = "",
  minInUnits: Double,
  maxInUnits: Double,
  digitalMin: Int,
  digitalMax: Int,
  prefilterings: String,
  numberOfSamples: Int,
  reserveds: Seq[Byte]
)

case class EDFSignal(channelsData: Seq[ChannelData])

case class ChannelData(unitsInDigit: Double, digitalValues: Seq[Int], valuesInUnits: Seq[Double])

case class EDFParserResult(header: EDFHeader, signal: EDFSignal, annotations: Seq[EDFAnnotation])

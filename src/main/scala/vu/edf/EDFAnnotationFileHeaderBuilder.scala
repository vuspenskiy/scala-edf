package vu.edf

import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author v.uspenskiy
 * @since 29/07/14 16:24
 *
 * @see https://github.com/MIOB/EDF4J
 */

class EDFAnnotationFileHeaderBuilder {

  private var recordingStartDate : String = null
  private var startDate: String = null
  private var startTime: String = null
  private var durationOfRecord: Int = 0
  private var numberOfSamples: Int = 0

  private var patientCode = "X"
  private var patientSex = "X"
  private var patientBirthdate = "X"
  private var patientName = "X"
  private var recordingHospital = "X"
  private var recordingTechnician = "X"
  private var recordingEquipment = "X"

  def startOfRecording(startOfRecording: Date) = {
    assert(startOfRecording != null)
    recordingStartDate = new SimpleDateFormat("dd-MMM-yyyy").format(startOfRecording).toUpperCase
    startDate = new SimpleDateFormat("dd.MM.yy").format(startOfRecording)
    startTime = new SimpleDateFormat("HH.mm.ss").format(startOfRecording)
    this
  }

  def durationOfRecord(value: Int): EDFAnnotationFileHeaderBuilder = {
    assert(value > 0)
    durationOfRecord = value
    this
  }

  def numberOfSamples(value: Int): EDFAnnotationFileHeaderBuilder = {
    assert(value > 0)
    numberOfSamples = value
    this
  }

  def patientCode(value: String): EDFAnnotationFileHeaderBuilder = {
    assert(value != null)
    patientCode = nonSpaceString(value)
    this
  }

  def patientIsMale(value: Boolean): EDFAnnotationFileHeaderBuilder = {
    patientSex = if(value) "M" else "F"
    this
  }

  def patientBirthdate(birthdate: Date): EDFAnnotationFileHeaderBuilder = {
    assert(birthdate != null)
    patientBirthdate = new SimpleDateFormat("dd-MMM-yyyy").format(birthdate).toUpperCase
    this
  }

  def patientName(value: String): EDFAnnotationFileHeaderBuilder = {
    assert(value != null)
    patientName = nonSpaceString(value)
    this
  }

  def recordingHospital(value: String): EDFAnnotationFileHeaderBuilder = {
    assert(value != null)
    recordingHospital = nonSpaceString(value)
    this
  }

  def recordingTechnician(value: String): EDFAnnotationFileHeaderBuilder = {
    assert(value != null)
    recordingTechnician = nonSpaceString(value)
    this
  }

  def recordingEquipment(value: String): EDFAnnotationFileHeaderBuilder = {
    assert(value != null)
    recordingEquipment = nonSpaceString(value)
    this
  }

  private def nonSpaceString(value: String) = value.replaceAll(" ", "_")

  def build(): EDFHeader = {
    assert(recordingStartDate != null)
    assert(startDate != null)
    assert(startTime != null)
    assert(durationOfRecord > 0)
    assert(numberOfSamples > 0)

    EDFHeader(
      idCode = String.valueOf(0),
      subjectID = buildPatientString(),
      recordingID = buildRecordingString(),
      startDate = startDate,
      startTime = startTime,
      formatVersion = "EDF+C",
      bytesInHeader = EDFConstants.HEADER_SIZE_RECORDING_INFO + EDFConstants.HEADER_SIZE_PER_CHANNEL,
      numberOfRecords = 1,
      durationOfRecords = durationOfRecord,
      channels = Seq(ChannelHeader(
        "EDF Annotations", "", "", 0.00, 1.00, -32768, 32767, "",
        numberOfSamples, Seq(EDFConstants.RESERVED_SIZE.toByte)))
    )
  }

  private def buildPatientString(): String = {
    s"$patientCode $patientSex $patientBirthdate $patientName"
  }

  private def buildRecordingString(): String = {
    s"Startdate $recordingStartDate $recordingHospital $recordingTechnician $recordingEquipment"
  }
}

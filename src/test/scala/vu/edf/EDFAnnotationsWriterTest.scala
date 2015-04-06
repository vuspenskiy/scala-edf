package vu.edf

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Date
import org.scalatest._


/**
 * @author v.uspenskiy
 * @since 04/04/15
 *
 * @see https://github.com/MIOB/EDF4J
 */

class EDFAnnotationsWriterTest extends FlatSpec with Matchers {

  "Write and read header" should "return the same header" in {
    val header = new EDFAnnotationFileHeaderBuilder()
      .startOfRecording(new Date()).durationOfRecord(1000).numberOfSamples(100)
      .patientCode("1234").patientIsMale(value = true).patientBirthdate(new Date()).patientName("The patient")
      .recordingHospital("Hosp.").recordingTechnician("Techn.").recordingEquipment("Equ.").build()

    val out = new ByteArrayOutputStream()
    EDFWriter.writeIntoOutputStream(header, out)
    val parsedHeader = EDFParser.parseHeader(new ByteArrayInputStream(out.toByteArray)).toOption.flatten.getOrElse(
      fail("Header could not be parsed!")
    )

    assert(header.idCode == parsedHeader.idCode.trim)
    assert(header.subjectID == parsedHeader.subjectID.trim)
    assert(header.recordingID == parsedHeader.recordingID.trim)
    assert(header.startDate == parsedHeader.startDate)
    assert(header.startTime == parsedHeader.startTime)
    assert(header.bytesInHeader == parsedHeader.bytesInHeader)
    assert(header.formatVersion == parsedHeader.formatVersion.trim)
    assert(header.numberOfRecords == parsedHeader.numberOfRecords)
    assert(0 == java.lang.Double.compare(header.durationOfRecords, parsedHeader.durationOfRecords))
    assert(header.channels.size == parsedHeader.channels.size)

    for ((headerChannel, parsedHeaderChannel) <- header.channels zip parsedHeader.channels) {
      assert(headerChannel == parsedHeaderChannel)
    }
  }
}
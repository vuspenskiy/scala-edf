package vu.edf

import java.nio.charset.Charset

/**
 * @author v.uspenskiy
 * @since 29/07/14 15:37
 *
 * @see http://www.edfplus.info/specs/edfplus.html
 * @see https://github.com/MIOB/EDF4J
 */
object EDFConstants {

  protected[edf] val CHARSET = Charset.forName("ASCII")
  protected[edf] val IDENTIFICATION_CODE_SIZE = 8
  protected[edf] val LOCAL_SUBJECT_IDENTIFICATION_SIZE = 80
  protected[edf] val LOCAL_RECORDING_IDENTIFICATION_SIZE = 80
  protected[edf] val START_DATE_SIZE = 8
  protected[edf] val START_TIME_SIZE = 8
  protected[edf] val HEADER_SIZE = 8
  protected[edf] val DATA_FORMAT_VERSION_SIZE = 44
  protected[edf] val DURATION_DATA_RECORDS_SIZE = 8
  protected[edf] val NUMBER_OF_DATA_RECORDS_SIZE = 8
  protected[edf] val NUMBER_OF_CHANNELS_SIZE = 4
  protected[edf] val LABEL_OF_CHANNEL_SIZE = 16
  protected[edf] val TRANSDUCER_TYPE_SIZE = 80
  protected[edf] val PHYSICAL_DIMENSION_OF_CHANNEL_SIZE = 8
  protected[edf] val PHYSICAL_MIN_IN_UNITS_SIZE = 8
  protected[edf] val PHYSICAL_MAX_IN_UNITS_SIZE = 8
  protected[edf] val DIGITAL_MIN_SIZE = 8
  protected[edf] val DIGITAL_MAX_SIZE = 8
  protected[edf] val PREFILTERING_SIZE = 80
  protected[edf] val NUMBER_OF_SAMPLES_SIZE = 8
  protected[edf] val RESERVED_SIZE = 32

  /** The size of the EDF-Header-Record containing information about the recording */
  protected[edf] val HEADER_SIZE_RECORDING_INFO =
    IDENTIFICATION_CODE_SIZE + LOCAL_SUBJECT_IDENTIFICATION_SIZE + LOCAL_RECORDING_IDENTIFICATION_SIZE +
      START_DATE_SIZE + START_TIME_SIZE + HEADER_SIZE + DATA_FORMAT_VERSION_SIZE + DURATION_DATA_RECORDS_SIZE +
      NUMBER_OF_DATA_RECORDS_SIZE + NUMBER_OF_CHANNELS_SIZE

  /** The size per channel of the EDF-Header-Record containing information a channel of the recording */
  protected[edf] val HEADER_SIZE_PER_CHANNEL =
    LABEL_OF_CHANNEL_SIZE + TRANSDUCER_TYPE_SIZE + PHYSICAL_DIMENSION_OF_CHANNEL_SIZE +
      PHYSICAL_MIN_IN_UNITS_SIZE + PHYSICAL_MAX_IN_UNITS_SIZE + DIGITAL_MIN_SIZE + DIGITAL_MAX_SIZE +
      PREFILTERING_SIZE + NUMBER_OF_SAMPLES_SIZE + RESERVED_SIZE
}

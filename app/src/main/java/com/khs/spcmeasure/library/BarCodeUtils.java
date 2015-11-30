package com.khs.spcmeasure.library;

/**
 * Created by Mark on 11/30/2015.
 */
public class BarCodeUtils {
    private static final String TAG = "BarCodeUtils";

    // barcode prefixes
    public static final String SERIAL_NUMBER_PREFIX = "S";
    public static final String SERIAL_PREFIX_HONDA = "H";
    public static final String SERIAL_PREFIX_NISSAN = "NE";

    // honda barcode destination data
    // TODO is this destination?
    public static final String DEST_PREFIX_HONDA_1 = "05110911";
    public static final String DEST_PREFIX_HONDA_2 = "00259500";
    public static final String DEST_SEPARATOR_HONDA = "-";

    // Karmax duns number
    // TODO should be a param lookup
    public static final String KARMAX_DUNS = "246439921";
    public static final String PREFIX_DUNS = "JUN";

    // barcode sizes
    public static final int LOT_NUMBER_LENGTH = 6;
    public static final int SERIAL_NUMBER_LENGTH = 9;
    public static final int SERIAL_LENGTH_HONDA = 8;
    public static final int SERIAL_LENGTH_NISSAN = 7;

    // extracts the serial number from a barcode (without any preamble) or null if not found
    public static String getSerialNumber(String barcode) {
        String serial = null;
        String prefix = null;

        if (barcode != null) {
            // check for serial number
            if (barcode.toUpperCase().indexOf(SERIAL_NUMBER_PREFIX) != -1) {
                // loop serial number prefix numeric
                for (int iPre = 1; iPre <= 20; iPre++) {
                    // build prefix to search for
                    prefix = Integer.toString(iPre) + SERIAL_NUMBER_PREFIX;

                    // check for matching prefix
                    if (barcode.startsWith(prefix)) {
                        // remove prefix to get base serial
                        serial = barcode.substring(prefix.length());

                        // check for honda serial
                        if (serial.startsWith(DEST_PREFIX_HONDA_1) || serial.startsWith(DEST_PREFIX_HONDA_2)) {
                            if (serial.indexOf(DEST_SEPARATOR_HONDA) != -1) {
                                // invalid honda serial
                                return null;
                            } else {
                                // get honda serial
                                serial = SERIAL_PREFIX_HONDA + serial.substring(DEST_PREFIX_HONDA_1.length() - 1);
                            }
                        }

                        // check for nissan serial
                        if (serial.length() == SERIAL_LENGTH_NISSAN) {
                            serial = SERIAL_PREFIX_NISSAN + serial;
                        }

                        return serial;
                    }
                }
            }

            // check for joint Karmax duns number
            for (int iPre = 1; iPre <= 20; iPre++) {
                // build prefix to search for
                prefix = Integer.toString(iPre) + PREFIX_DUNS + KARMAX_DUNS;

                // check for matching prefix
                if (barcode.startsWith(prefix)) {
                    // remove prefix to get serial
                    serial = barcode.substring(prefix.length());
                    return serial;
                }
            }

            // check non-numeric prefix
            prefix = SERIAL_NUMBER_PREFIX;
            if (barcode.startsWith(prefix)) {
                // remove prefix to base serial
                serial = barcode.substring(prefix.length());

                // check for nissan serial
                if (serial.length() == SERIAL_LENGTH_NISSAN) {
                    serial = SERIAL_PREFIX_NISSAN + serial;
                }

                return serial;
            }
        }

        return serial;
    }

}

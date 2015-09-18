import java.io.UnsupportedEncodingException;

/**
 * Created by viacheslav on 18.09.2015.
 */
public class ByteUtls {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Converts byte array to string, using hexadecimal representation.
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * converts byte array to string, separating each byte with dots.
     *
     * @param bytes
     * @return
     */
    public static String bytesToDec(byte[] bytes) {
        String s = "" + ((int) bytes[0] + 127);

        for (int j = 1; j < bytes.length; j++) {
            s = s + "." + ((int) bytes[j] + 127);
        }
        return s;
    }

    /**
     * Converts string to utf-8 byte array.
     *
     * @param string
     * @return
     */
    public static byte[] stringToBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("COULD NEVER HAPPEN!! " + e.getMessage());
            return new byte[]{};
        }
    }


    /**
     * converts long unix time to 8 byte char array.
     *
     * @param unixTime
     * @return
     */
    public static byte[] unixTimeToBytes(long unixTime) {
        return new byte[]{
                (byte) (unixTime >> 56),
                (byte) (unixTime >> 48),
                (byte) (unixTime >> 40),
                (byte) (unixTime >> 32),
                (byte) (unixTime >> 24),
                (byte) (unixTime >> 16),
                (byte) (unixTime >> 8),
                (byte) unixTime
        };
    }

}

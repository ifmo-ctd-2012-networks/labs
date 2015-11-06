package tr.core;

public class Token {
    private byte[] mac;
    private int version;

    public Token(byte[] mac, int version) {
        this.mac = mac;
        this.version = version;
    }

    public byte[] getMac() {
        return mac;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token && version == ((Token) obj).version) {
            for (int i = 0; i < mac.length; i++) {
                if (mac[i] != ((Token) obj).mac[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String result = "";
        for (byte b : mac) {
            result += String.format("%02X:", b);
        }
        return result.substring(0, result.length() - 1) + " :: " + version;
    }
}

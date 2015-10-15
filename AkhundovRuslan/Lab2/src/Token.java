import java.io.IOException;
import java.io.Serializable;

/**
 * @author korektur
 *         15/10/2015
 */
public class Token implements Serializable {

    private static final int MAC_ADDRESS_LEN = 5;

    private byte[] macAddress;
    private int version;
    private int nDigits;
    private String pi;


    public Token(byte[] macAddress, int version, int nDigits, String pi) {
        this.macAddress = macAddress;
        this.version = version;
        this.nDigits = nDigits;
        this.pi = pi;
    }

    public byte[] getMacAddress() {
        return macAddress;
    }

    public int getVersion() {
        return version;
    }

    public int getnDigits() {
        return nDigits;
    }

    public String getPi() {
        return pi;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeByte(0x03);
        out.write(macAddress);
        out.writeInt(version);
        out.writeInt(nDigits);
        out.writeChars(pi);

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        if (in.readByte() != 0x03)
            throw new IllegalArgumentException("Wrong body start marker");


        macAddress = new byte[MAC_ADDRESS_LEN];
        if (in.read(macAddress) != MAC_ADDRESS_LEN)
            throw new IllegalArgumentException("Couldn't read MAC address");

        version = in.readInt();

        nDigits = in.readInt();

        pi = "3.";
        for(int i = 0; i < nDigits; ++i) {
            pi += in.readByte();
        }
    }
}

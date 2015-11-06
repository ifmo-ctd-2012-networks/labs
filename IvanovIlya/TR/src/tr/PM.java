package tr;

import tr.core.PayloadManager;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class PM implements PayloadManager {
    @Override
    public Object readPayload(Scanner sc) {
        return sc.next();
    }

    @Override
    public void writePayload(Object payload, Writer writer) throws IOException {
        writer.write(payload + "\n");
    }

    @Override
    public int compare(Object first, Object second) {
        return Integer.compare(((String) second).length(), ((String) first).length());
    }
}

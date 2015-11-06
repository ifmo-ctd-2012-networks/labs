package tr.core;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public interface PayloadManager {
    Object readPayload(Scanner sc);

    void writePayload(Object payload, Writer writer) throws IOException;

    int compare(Object first, Object second);
}

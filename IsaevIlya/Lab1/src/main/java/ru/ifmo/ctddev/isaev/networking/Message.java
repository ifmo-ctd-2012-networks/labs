package ru.ifmo.ctddev.isaev.networking;

import dnl.utils.text.table.TextTable;

/**
 * @author Ilya Isaev
 */
public class Message {

    public static final String[] columnNames = {
            "MAC address",
            "Hostname",
            "Timestamp"};
    String mac;
    String hostname;
    long timestamp;
    boolean ok;

    public void printAsTable() {
        Object[][] data = {
                {mac, hostname, timestamp}
        };
        TextTable tt = new TextTable(columnNames, data);
        tt.setAddRowNumbering(false);
        tt.printTable();
    }
}

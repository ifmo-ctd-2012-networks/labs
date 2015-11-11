package sender.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpDispatcher extends NetDispatcher {

    @Override
    protected void submit(SendInfo sendInfo) {
        try {
            DatagramSocket socket;
            if (sendInfo.address == null) {
                socket = new DatagramSocket(1234, );
                socket.setBroadcast(true);
            } else {
                socket = new DatagramSocket(sendInfo.address.getPort(), sendInfo.address.getAddress());
            }
            DatagramPacket packet = new DatagramPacket(sendInfo.data, sendInfo.data.length);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

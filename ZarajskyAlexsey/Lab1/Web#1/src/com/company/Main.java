package com.company;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
	public static String broadcastIP;
	public static int port;

	public static void main(String[] args) {
		if (args.length != 2) throw new IllegalArgumentException();
		broadcastIP = args[0];
		port = Integer.parseInt(args[1]);
		Log.sendMessage(
				"Main",
				"Address IP : " + broadcastIP,
				"Port : " + port
		);
		new Client().start();
		new Server().start();
	}

	static class Server extends Thread {

		public void run() {
			try {
				List<Data> bufferDataMessage = new ArrayList<>();
				TreeMap<byte[], Integer> listDevices = new TreeMap<>((o, t1) -> {
					for (int i = 0; i < o.length; i++) {
						int c = Byte.compare(o[i], t1[i]);
						if (c != 0) return c;
					}
					return 0;
				});

				Thread receiver = new Thread(() -> {
					try {
						byte[] buffer = new byte[64];
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						DatagramSocket dsocket = new DatagramSocket(port);
						while (!isInterrupted()) {
							dsocket.receive(packet);
							synchronized (bufferDataMessage) {
								Data message = new Data(Arrays.copyOf(packet.getData(), packet.getLength()));
								Log.sendMessage("Server", "Get Message : " + Arrays.toString(message.getBytes()));
								bufferDataMessage.add(message);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				receiver.start();

				while (!isInterrupted()) {
					synchronized (bufferDataMessage) {
						for (byte[] keyBytes : listDevices.keySet())
							listDevices.replace(keyBytes, listDevices.get(keyBytes) + 1);
						for (Data d : bufferDataMessage) {
							byte[] macAddress = d.macAddress;
							if (!listDevices.containsKey(macAddress)) listDevices.put(macAddress, 1);
							listDevices.replace(macAddress, listDevices.get(macAddress) - 1);
						}
						for (byte[] keyBytes : listDevices.keySet())
							if (listDevices.get(keyBytes) >= 5) listDevices.remove(keyBytes);

						Log.sendMessage(
								"Server",
								"Connected devices : " + listDevices.size(),
								"Buffer of messages is clear"
						);

						bufferDataMessage.clear();
					}
					sleep(5000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	static class Client extends Thread {

		public void run() {
			try {
				while (!isInterrupted()) {
					Data data = new Data();
					byte[] message = data.getBytes();

					Log.sendMessage(
							"Client",
							//"Time : " + data.unixTimeStamp,
							"Send Message : " + Arrays.toString(message)
					);
					DatagramPacket packet = new DatagramPacket(message, message.length,
							InetAddress.getByName(broadcastIP), port);

					DatagramSocket dsocket = new DatagramSocket();
					dsocket.send(packet);
					dsocket.close();
					sleep(5000);
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	static class Data {
		public byte[] macAddress;
		public String hostName;
		public long unixTimeStamp;

		public Data(byte[] data) {
			macAddress = Arrays.copyOf(data, 6);
			int length = data[6];
			hostName = new String(Arrays.copyOfRange(data, 7, 7 + length), StandardCharsets.UTF_8);
			unixTimeStamp = 0;
			unixTimeStamp += (data[7 + length] & 0xffL) << 24;
			unixTimeStamp += (data[7 + length + 1] & 0xffL) << 16;
			unixTimeStamp += (data[7 + length + 2] & 0xffL) << 8;
			unixTimeStamp += (data[7 + length + 3] & 0xffL);
		}

		public Data() throws UnknownHostException, SocketException {
			macAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			hostName = InetAddress.getLocalHost().getHostName();
			unixTimeStamp = System.currentTimeMillis() / 1000L;
		}

		public byte[] getBytes() {
			ArrayList<Byte> bytes = new ArrayList<>();
			for (byte b : macAddress) bytes.add(b);
			byte[] byteHostName = hostName.getBytes(StandardCharsets.UTF_8);
			bytes.add((byte) byteHostName.length);
			for (byte b : byteHostName) bytes.add(b);
			byte[] productionDate = new byte[]{
					(byte) (unixTimeStamp >> 24),
					(byte) (unixTimeStamp >> 16),
					(byte) (unixTimeStamp >> 8),
					(byte) unixTimeStamp

			};

			for (byte b : productionDate) bytes.add(b);
			byte[] answer = new byte[bytes.size()];
			for (int i = 0; i < bytes.size(); i++) answer[i] = bytes.get(i);
			return answer;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Data)) return false;
			Data oData = (Data) o;
			return
					Arrays.equals(macAddress,((Data) o).macAddress) &&
					hostName.equals(oData.hostName) &&
					Long.compare(unixTimeStamp, oData.unixTimeStamp) == 0;
		}
	}

	static class Log {
		private static long count = 0;

		public static void sendMessage(String name, String... message) {
			new Thread(() -> {
				count++;
				for (int i = 0; i < message.length; i++) {
					System.out.printf("%s [%s]  (%d / %d mes # %d)  %s\n", new Date(), name, i + 1, message.length, count, message[i]);
				}
			}).start();
		}
	}
}

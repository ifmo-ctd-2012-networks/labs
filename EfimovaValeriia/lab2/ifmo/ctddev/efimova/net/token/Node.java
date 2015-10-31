package ifmo.ctddev.efimova.net.token;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Node {
    private byte[] macBytes;
    private String macAddr;
    private DatagramSocket serverSocket;
    private final Map<String, String> macIP;
    private List<Byte> piBackup;

    private volatile NavigableSet<String> neighbours;
    private volatile int version;
    private volatile State state;
    private volatile String nextInRing;
    private volatile String prevInRing;

    private volatile boolean isThread1ShouldStop;
    private volatile boolean isThread2ShouldStop;

    public Node() {
        this.macIP    = new TreeMap<>();
        this.version  = 0;
        this.state    = State.CONFIG;
        this.piBackup = new ArrayList<>();
    }

    private void setThisNodeMacBytesAndAddr() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;

            getIP:
            while (interfaces.hasMoreElements()) {
                NetworkInterface element = interfaces.nextElement();
                if (element.isLoopback() || !element.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = element.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress i = addresses.nextElement();
                    if (i instanceof Inet4Address) {
                        ip = i;
                        System.out.println("Current IP address : " + ip.getHostAddress());
                        break getIP;
                    }
                }
            }

            macBytes = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        this.macAddr = NetUtils.macAddrFromBytes(macBytes);
        System.out.println("Current MAC address : " + macAddr);
    }

    public void run() {
        setThisNodeMacBytesAndAddr();
        setServerSocket();

        new Thread(this::receiveReconfigure).start();
        new Thread(this::receiveToken).start();
        initReconfigure(version + 1);
    }

    private void setServerSocket() {
        try {
            serverSocket = new DatagramSocket(Constants.BROADCAST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void receiveReconfigure() {
        try {
            while (true) {
                byte[] receiveData = new byte[Constants.CONF_MSG_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String ip = receivePacket.getAddress().getHostAddress();
                Message message = new Message(receiveData);
                System.out.println("Receive reconfigure: "
                        + "myversion = " + this.version
                        + ", version = " + message.version
                        + ", MAC = " + message.macAddr
                        + ", IP = " + ip);

                if (message.version > this.version) {
                    macIP.put(message.macAddr, ip);
                    reconfigure(message.version, message.macAddr);
                } else if ((message.version < this.version) || (message.version == this.version) && (state != State.CONFIG)) {
                    reconfigure(this.version + 1, this.macAddr);
                } else {
                    macIP.put(message.macAddr, ip);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reconfigure(int newVersion, String initializerMacAddr) {
        System.out.println("Reconfigure: version = " + newVersion + ", MAC = " + initializerMacAddr);

        this.state = State.CONFIG;
        this.version = newVersion;
        this.neighbours = new TreeSet<>(Arrays.asList(this.macAddr, initializerMacAddr));
        this.isThread1ShouldStop = false;
        this.isThread2ShouldStop = false;

        Thread thread1 = new Thread(() -> sendReconfigure(this.macBytes, version));

        final boolean[] isBadConfig = new boolean[1];

        Runnable thread2 = () -> {
            try {
                while (!isThread2ShouldStop) {
                    byte[] receiveData = new byte[Constants.CONF_MSG_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    String ip = receivePacket.getAddress().getHostAddress();
                    Message message = new Message(receiveData);

                    if (message.macAddr.equals(this.macAddr)) {
                        continue;
                    }

                    System.out.println("    in thread2: receive reconfigure: "
                            + "myversion = " + this.version
                            + ", version = " + message.version
                            + ", MAC = " + message.macAddr
                            + ", IP = " + ip);
                    if (message.version == this.version) {
                        neighbours.add(message.macAddr);
                        macIP.put(message.macAddr, ip);
                    } else {
                        if (message.version > this.version) { //
                            this.version = message.version; //
                        } //
                        isBadConfig[0] = true;
                        isThread1ShouldStop = true;
                        isThread2ShouldStop = true;
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        thread1.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(thread2).get(Constants.TICK, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            isThread2ShouldStop = true;
        }
        executor.shutdownNow();

        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isBadConfig[0] |= neighbours.size() <= 1;

        if (isBadConfig[0]) {
            reconfigure(version + 1, this.macAddr);
        } else {
            state = State.WORKING;
            updateNextAndPrev();
            System.out.println("State = WORKING, prev = " + prevInRing + ", next = " + nextInRing);

            if (neighbours.first().equals(this.macAddr)) {
                try {
                    Thread.sleep(Constants.TICK);
                    sendToken(new Token(version, macBytes, piBackup.size(), piBackup));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendReconfigure(byte[] mac, int newVersion) {
        try (DatagramSocket broadcastAnnounceSocket = new DatagramSocket()) {
            broadcastAnnounceSocket.setBroadcast(true);

            InetAddress IPAddress = InetAddress.getByName(Constants.BROADCAST_ADDRESS);
            byte data[] = new Message(newVersion, mac).toBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, Constants.BROADCAST_PORT);

            System.out.println("    Send reconfigure: version = " + newVersion + ", MAC = " + NetUtils.macAddrFromBytes(mac));

            for (int i = 0; i < Constants.BROADCASTS_COUNT; i++) {
                if (isThread1ShouldStop) {
                    break;
                }
                broadcastAnnounceSocket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNextAndPrev() {
        nextInRing = neighbours.higher(this.macAddr);
        if (nextInRing == null) {
            nextInRing = neighbours.first();
        }

        prevInRing = neighbours.lower(this.macAddr);
        if (prevInRing == null) {
            prevInRing = neighbours.last();
        }
    }

    private void receiveToken() {
        while(true) {
            try {
                ServerSocket serverTCPSocket = new ServerSocket(Constants.TCP_PORT);
                Thread th1 = new Thread(() -> {
                    try {
                        Thread.sleep(Constants.TICK);
                        if (!serverTCPSocket.isClosed()) {
                            serverTCPSocket.close();
                            initReconfigure(version + 1);
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                        initReconfigure(version + 1);
                    }
                });
                //th1.start();

                Socket prevSocket = serverTCPSocket.accept();
                DataInputStream is = new DataInputStream(prevSocket.getInputStream());
                Token token = new Token(is);
                serverTCPSocket.close();

                System.out.println("Receive token: "
                        + "myversion = " + version
                        + ", version = " + token.version
                        + ", MAC = " + token.macAddr
                        + ", nDigits = " + token.nDigits);

                if ((state == State.CONFIG) || (token.version != this.version) || (token.nDigits < piBackup.size()) || (!token.macAddr.equals(prevInRing))) {
                    initReconfigure(Math.max(token.version, this.version) + 1);
                } else {
                    savePi(token);
                    sendToken(token);
                }
            } catch (IOException e) {
                e.printStackTrace();
                initReconfigure(version + 1);
            }
        }
    }

    private void savePi(Token token) {
        for (int i = piBackup.size(); i < token.digits.length; i++) {
            piBackup.add(token.digits[i]);
        }
    }

    private void sendToken(Token token) {
        Socket nextSocket = new Socket();
        try {
            nextSocket.connect(new InetSocketAddress(macIP.get(nextInRing), Constants.TCP_PORT), Constants.TICK * 1000);
            DataOutputStream os = new DataOutputStream(nextSocket.getOutputStream());

            token.macBytes = this.macBytes;
            token.nDigits = token.digits.length;
            PiHolder.getInstance().addNDigits(token.digits);

            System.out.println("Send token: "
                    + "myversion = " + version
                    + ", version = " + token.version
                    + ", MAC = " + token.macAddr
                    + ", nDigits = " + token.nDigits);
            //savePi(token);
            os.write(token.toBytes());
        } catch (IOException e) {
            //e.printStackTrace();
            initReconfigure(version + 1);
        } finally {
            try {
                nextSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initReconfigure(int newVersion) {
        while (this.version < newVersion) {
            System.out.println("Init reconfigure: version = " + newVersion + ", MAC = " + macAddr);
            sendReconfigure(this.macBytes, newVersion);
            try {
                Thread.sleep(Constants.TICK);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

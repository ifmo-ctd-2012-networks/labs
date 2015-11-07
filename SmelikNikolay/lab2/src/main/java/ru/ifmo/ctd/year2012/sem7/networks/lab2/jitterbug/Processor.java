package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;

/**
 * States:
 * * leader: token_id > 0
 * * waiter: token_id < 0 && (System.currentTimeMillis() <= {renew_timeout} + lastLivenessEventTime)
 * * orphan: token_id < 0 && (System.currentTimeMillis() > {renew_timeout} + lastLivenessEventTime)
 *
 * @param <D> type of data (application-defined)
 */
class Processor<D extends Data<D>> extends Thread implements State<D> {
    private static final Logger log = LoggerFactory.getLogger(Processor.class);
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Context<D> context;
    private final BlockingQueue<Event> eventQueue;
    private final NodeList nodes;
    private HashMap<Integer, Penalty> penalties;
    private final Set<Node> rememberedNodes;
    private long lastUpdate;
    private final Random random;

    private volatile boolean isReceivedGrater, isWaitTR2;


    @Getter
    private volatile D data;

    /**
     * Token id
     * Positive value means that we are a leader
     * Negative - that we don't
     */
    @Getter
    private volatile int tokenId;

    private MessageService<D> messageService;

    Processor(Context<D> context) {
        this.context = context;
        messageService = context.getMessageService();
        eventQueue = new ArrayBlockingQueue<>(context.getSettings().getQueueCapacity());
        data = context.getSettings().getInitialData();
        tokenId = generateTokenId();
        nodes = new NodeList();
        penalties = new HashMap<>();
        rememberedNodes = Collections.newSetFromMap(new ConcurrentHashMap<>());
        lastUpdate = System.currentTimeMillis();
        random = new Random();
    }

    private void rememberNode(Node node) {
        if (rememberedNodes.add(node)) {
            log.info("New node was added: " + node);
        }
    }

    @Override
    public void rememberNode(int hostId, InetAddress address, int tcpPort) {
        rememberNode(new Node(hostId, address, tcpPort));
    }

    @Override
    public void reportTR2(InetAddress senderAddress, int tokenId) {
        isReceivedGrater = isReceivedGrater || tokenId > getTokenId();
        log.info("our token id: " + getTokenId());
        log.info("received token id: " + tokenId + " from " + senderAddress.getHostName() + " is " + (tokenId > getTokenId() ? "grater " : "less ") + "than ours");
    }

    @Override
    public void handleSocketConnection(Socket socket) {
        try {
            eventQueue.put(new TPLeaderReceived(socket));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private int generateTokenId() {
        int randInt = 0;
        while (randInt == 0) {
            randInt = context.getRandom().nextInt();
        }
        return (randInt > 0) ? -randInt : randInt;
    }

    @Override
    public void run() {
        scheduledExecutor.scheduleWithFixedDelay(this::becomeOrphan, 0, context.getSettings().getTrInitTimeout(), TimeUnit.MILLISECONDS);

        while (true) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                log.debug("Processor was interrupted");
                break;
            }
            Event event;
            try {
                event = eventQueue.take();
                if (event instanceof TRInitiateEvent) {
                    log.info("Initialize token restore procedure");
                    tokenRestoreTry(tokenId, new TRCheckTR2FirstMessage());
                } else if (event instanceof TPLeaderReceived) {
                    log.info("token leader received");
                    TPLeaderReceived leaderEvent = (TPLeaderReceived) event;
                    try (Socket socket = leaderEvent.socket;
                         DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                         DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                        socket.setSoTimeout(context.getSettings().getTpTimeout());
                        new TPReceiver(dis, dos).process();
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                } else if (event instanceof TRCheckTR2FirstMessage) {
                    log.info("Process first tr2 messages");
                    if (isWaitTR2) {
                        if (!isReceivedGrater) {
                            tokenId = generateTokenId();
                            isReceivedGrater = false;
                            tokenRestoreTry(tokenId, new TRCheckTR2SecondMessage());
                        } else {
                            becomeWaiter();
                        }
                    }
                } else if (event instanceof TRCheckTR2SecondMessage) {
                    log.info("Process second tr2 message");
                    if (isWaitTR2) {
                        if (!isReceivedGrater) {
                            becomeLeader();
                        } else {
                            becomeWaiter();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("Processor was interrupted");
                break;
            }
        }
    }

    private void becomeWaiter() {
        log.info("Now we are waiter");
        if (tokenId > 0) {
            tokenId *= -1;
        }
    }

    private void becomeLeader() {
        log.info("Now we are leader");
        if (tokenId < 0) {
            tokenId *= -1;
        }
        actAsLeader();
    }

    private void becomeOrphan() {
        if (tokenId < 0 && (System.currentTimeMillis() > context.getSettings().getTrInitTimeout() + lastUpdate)) {
            try {
                log.info("Now we are orphan");
                eventQueue.put(new TRInitiateEvent());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tokenRestoreTry(int tryoutTokenId, Event event) {
        scheduledExecutor.schedule(() -> {
            try {
                eventQueue.put(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, context.getSettings().getTrPhaseTimeout(), TimeUnit.MILLISECONDS);
        context.getMessageService().sendTR1MessageRepeatedly(tryoutTokenId);
        isWaitTR2 = true;
        isReceivedGrater = false;
    }

    public void actAsLeader() {
        boolean success = false;
        while (!success) {
            success = tokenPass();
            if (!success) {
                log.info("Repeating leader phase");
            }
        }
        becomeWaiter();
    }

    public boolean tokenPass() {
        int lostTokenThreshold = (int)(context.getSettings().getTokenLooseProbBase() * rememberedNodes.size());
        boolean needToLostToken = random.nextInt(lostTokenThreshold) == 0;
        if (needToLostToken) {
            eventQueue.offer(new TRInitiateEvent());
            return true;
        } else {
            data = data.next();
            log.info("TOKEN PASS " + data, "new computed value: " + data);
            boolean isTokenPassed = false;
            rememberedNodes.forEach(nodes::add);
            lastUpdate = System.currentTimeMillis();
            int myHostId = context.getHostId();
            for (int i = nodes.getByHostId(myHostId) + 1; i < nodes.size() && !isTokenPassed; ++i) {
                isTokenPassed = tryTokenPass(nodes.get(i));
            }
            for (int i = 0; i < nodes.getByHostId(myHostId) && !isTokenPassed; ++i) {
                isTokenPassed = tryTokenPass(nodes.get(i));
            }
            return isTokenPassed;
        }
    }

    private boolean tryTokenPass(Node candidate) {
        boolean isAllowedForRound = true;
        if (!penalties.containsKey(candidate.getHostId())) {
            penalties.put(candidate.getHostId(), new Penalty(candidate.getHostId()));
        }
        Penalty penalty = penalties.get(candidate.getHostId());
        if (penalty.getPenaltyCount() >= Math.pow(2, penalty.getPenaltyThreshold()) - 1) {
            penalty.resetCount();
        } else {
            isAllowedForRound = false;
        }
        if (isAllowedForRound) {
            boolean res = tokenPassForCandidate(candidate);
            if (res) {
                log.info("Pass token to candidate " + candidate.getAddress().getHostName() + " was success");
                if (penalty.getPenaltyThreshold() > 0) penalty.decreaseThreshold();
                return true;
            } else {
                log.info("Pass token to candidate " + candidate.getAddress().getHostName() + " failure");
                penalty.increaseThreshold();
            }
        } else {
            penalty.increaseCount();
        }
        return false;
    }

    public boolean tokenPassForCandidate(Node candidate) {
        try (
                Socket socket = new Socket(candidate.getAddress(), candidate.getPort());
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            socket.setSoTimeout(context.getSettings().getTpTimeout());
            messageService.sendTP1Message(dos, getTokenId(), nodes.getHash());
            messageService.handleTPMessage(dis, new TPHandler() {
                @Override
                public void handleTP2() throws IOException, ParseException {
                    context.getMessageService().sendTP4Message(dos, nodes.size(), nodes.getBytes());
                }

                @Override
                public void handleTP3() throws IOException, ParseException {
                }
            });
            messageService.sendTP5Message(dos, data);
            return true;
        } catch (IOException | ParseException e) {
            log.debug("Exception caught while trying to pass token to {}", candidate, e);
            return false;
        }
    }

    private interface Event {
    }

    private static class TPLeaderReceived implements Event {
        @Getter
        private final Socket socket;

        public TPLeaderReceived(Socket socket) {
            this.socket = socket;
        }
    }

    private static class TRCheckTR2FirstMessage implements Event {
    }

    private static class TRCheckTR2SecondMessage implements Event {
    }

    private static class TRInitiateEvent implements Event {
    }

    private class TPReceiver {
        private int leaderToken;
        private List<Node> receivedNodes;

        private final DataInputStream dis;
        private final DataOutputStream dos;

        private TPReceiver(DataInputStream dis, DataOutputStream dos) {
            this.dis = dis;
            this.dos = dos;
        }

        void process() throws IOException, ParseException {
            messageService.handleTPMessage(dis, new TPHandler() {
                @Override
                public void handleTP1(int tokenId, int nodeListHash) throws IOException, ParseException {
                    leaderToken = tokenId;
                    isWaitTR2 = false;
                    lastUpdate = System.currentTimeMillis();
                    if (nodeListHash != nodes.getHash()) {
                        messageService.sendTP2Message(dos);
                        messageService.handleTPMessage(dis, new TPHandler() {
                            @Override
                            public void handleTP4(List<Node> nodes) throws IOException, ParseException {
                                receivedNodes = nodes;
                            }
                        });
                    } else {
                        messageService.sendTP3Message(dos);
                    }
                }
            });
            messageService.handleTPMessage(dis, new TPHandler() {
                @Override
                public void handleTP5(DataInputStream dataStream) throws IOException, ParseException {
                    ObjectInputStream ois = new ObjectInputStream(dis);
                    try {
                        D newData = (D) ois.readObject();
                        if (getTokenId() == leaderToken) {
                            data = newData;
                        } else {
                            Processor.this.tokenId = leaderToken;
                            data = newData.mergeWith(data);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new IOException(e);
                    }

                    if (receivedNodes != null) {
                        Set<Node> toRemember = nodes.replace(receivedNodes);
                        toRemember.forEach(rememberedNodes::add);
                    }
                    becomeLeader();
                }
            });
        }


    }

    private static class Penalty {
        @Getter
        private final int hostId;
        @Getter
        private int penaltyThreshold;
        @Getter
        private int penaltyCount;
        private static final int MAX_THRESHOLD = 10;

        public Penalty(int hostId) {
            this.hostId = hostId;
            penaltyCount = 0;
            penaltyThreshold = 0;
        }

        public void resetCount() {
            penaltyCount = 0;
        }

        public void increaseCount() {
            penaltyCount++;
        }

        public void increaseThreshold() {
            if (penaltyThreshold < MAX_THRESHOLD)
                penaltyThreshold++;
        }

        public void decreaseThreshold() {
            if (penaltyThreshold > 0)
                penaltyThreshold--;
        }
    }

}

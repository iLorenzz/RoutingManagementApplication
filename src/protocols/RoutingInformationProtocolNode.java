import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RoutingInformationProtocolNode extends RoutingInformationProtocol implements UnicastServiceUserInterface, Runnable {
    private final short nodeId;

    private final int[][] distanceTable;
    private final Map<Short, Integer> linkCosts;
    private final List<Short> allNodes;
    private final List<Short> neighbors;

    private final int propagationTimeout;
    private Timer propagationTimer;
    private volatile boolean running = true;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public RoutingInformationProtocolNode(
            short nodeId,
            String hostname,
            int portNumber,
            int propagationTimeout,
            Map<Short, Integer> initialLinkCosts,
            List<Short> allNodes
    ) {
        super(nodeId, hostname, portNumber);

        this.nodeId = getId();
        this.propagationTimeout = propagationTimeout;
        this.linkCosts = initialLinkCosts;

        this.allNodes = allNodes;
        Collections.sort(this.allNodes);

        this.neighbors = new ArrayList<>(initialLinkCosts.keySet());
        Collections.sort(this.neighbors);

        this.distanceTable = initializeDistanceTable();

        unicastThreadInitialization();
    }

    @Override
    public void upDataInd(short source, String message) {
        String[] brokenPDU = message.split(" ");

        switch (brokenPDU[2]) {
            case "RIPRQT":
                handleDistanceTableRequest(source);
                break;

            case "RIPGET":
                int getNodeAId = Integer.parseInt(brokenPDU[3]);
                int getNodeBId = Integer.parseInt(brokenPDU[4]);

                handleGetLinkCost(source, getNodeAId, getNodeBId);
                break;

            case "RIPSET":
                int setNodeAId = Integer.parseInt(brokenPDU[3]);
                int setNodeBId = Integer.parseInt(brokenPDU[4]);
                int newCost = Integer.parseInt(brokenPDU[5]);

                handleSetLinkCost(source, setNodeAId, setNodeBId, newCost);
                break;

            case "RIPIND":
                short neighborId = Short.parseShort(brokenPDU[3]);
                String neighborDistanceVector = brokenPDU[4];

                handleDistanceVector(neighborId, neighborDistanceVector);
                break;
        }
    }

    @Override
    public void run() {

    }

    private int[][] initializeDistanceTable() {
        int numRows = neighbors.size() + 1;
        int numCols = allNodes.size();

        int[][] distanceTable = new int[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            Arrays.fill(distanceTable[i], -1);
        }

        for (int j = 0; j < numCols; j++) {
            short currentNode = allNodes.get(j);

            if (currentNode == nodeId) {
                distanceTable[0][j] = 0;
                continue;
            }

            if (linkCosts.containsKey(currentNode)) {
                distanceTable[0][j] = linkCosts.get(currentNode);
            }
        }

        for (int i = 1; i < numRows; i++) {
            short neighborId = neighbors.get(i - 1);

            int neighborCol = allNodes.indexOf(neighborId);

            distanceTable[i][neighborCol] = 0;
        }


        return distanceTable;
    }

    private void unicastThreadInitialization() {
        Thread thrd = new Thread(getUnicastProtocol());
        thrd.start();
    }

    private void handleDistanceTableRequest(short source) {
        rwLock.readLock().lock();
        try {
            String responseMessage = createDistanceTableResponsePDU();
            getUnicastProtocol().upDataReq(source, responseMessage);
        } finally {
            rwLock.readLock().unlock();
        }

    }

    private void handleGetLinkCost(short source, int nodeAId, int nodeBId) {
        rwLock.readLock().lock();
        try {
            String responseMessage = createLinkCostPDU((short) nodeAId, (short) nodeBId);
            getUnicastProtocol().upDataReq(source, responseMessage);
        } finally {
            rwLock.readLock().unlock();
        }

    }

    private void handleSetLinkCost(short source, int nodeAId, int nodeBId, int cost) {
        boolean changed;
        rwLock.writeLock().lock();
        try {
            short neighborId = (short) nodeBId;

            linkCosts.put(neighborId, cost);

            int neighborIndex = allNodes.indexOf(neighborId);
            distanceTable[0][neighborIndex] = cost;

            changed = recalculateDistanceVector();

            String responseMessage = createLinkCostPDU((short) nodeAId, (short) nodeBId);
            getUnicastProtocol().upDataReq(source, responseMessage);
        } finally {
            rwLock.writeLock().unlock();
        }

        if (changed) {
            propagateDistanceVector();
        }
    }

    private void handleDistanceVector(short neighborId, String neighborDistanceVectorStr){
        rwLock.writeLock().lock();

        try{
            int neighborRow = neighbors.indexOf(neighborId) + 1;
            String[] costsStr = neighborDistanceVectorStr.split(":");

            for(int i = 0; i < costsStr.length; i++){
                try {
                    int cost = Integer.parseInt(costsStr[i]);
                    distanceTable[neighborRow][i] = cost;
                } catch (NumberFormatException nfe){
                    distanceTable[neighborRow][i] = -1;
                }
            }

            recalculateDistanceVector();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private boolean recalculateDistanceVector() {
        boolean changed = false;
        int minCost = -1;

        for (int i = 0; i < allNodes.size(); i++) {
            short node = allNodes.get(i);

            if (node == nodeId) {
                continue;
            }

            for (Short neighbor : neighbors) {
                Integer directLinkCost = linkCosts.get(neighbor);

                int neighborRow = neighbors.indexOf(neighbor) + 1;
                int fromNeighborToDestinationNode = distanceTable[neighborRow][i];
                if (fromNeighborToDestinationNode == -1) {
                    continue;
                }

                int totalCost = directLinkCost + fromNeighborToDestinationNode;

                if (minCost == -1 || totalCost < minCost) {
                    minCost = totalCost;
                }
            }

            if (minCost != distanceTable[0][i]) {
                distanceTable[0][i] = minCost;
                changed = true;
            }
        }
        return changed;
    }

    private void propagateDistanceVector() {
        rwLock.readLock().lock();
        try {
            String propagateVectorMessage = createDistanceVectorMessage();
            for (Short neighbor : neighbors) {
                getUnicastProtocol().upDataReq(neighbor, propagateVectorMessage);
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private String createDistanceTableResponsePDU() {
        String responseMessage = "RIPRSQ" + nodeId;
        StringBuilder formatDistanceTable = new StringBuilder();

        for (int i = 0; i < distanceTable.length; i++) {
            for (int j = 0; j < distanceTable[0].length; j++) {
                if (j == distanceTable[0].length - 1) {
                    formatDistanceTable.append(distanceTable[i][j]);
                    break;
                }

                formatDistanceTable.append(distanceTable[i][j]).append(":");
            }

            formatDistanceTable.append(" ");
        }

        return responseMessage + " " + formatDistanceTable;
    }

    private String createLinkCostPDU(int nodeAId, int nodeBId) {
        String responseMessage = "RIPNFT" + " " + nodeAId + " " + nodeBId;

        return responseMessage + " " + linkCosts.get((short) nodeAId);
    }

    private String createDistanceVectorMessage() {
        StringBuilder propagateMessage = new StringBuilder();

        for (int i = 0; i < allNodes.size(); i++) {
            if (i > 0) {
                propagateMessage.append(":");
            }

            propagateMessage.append(distanceTable[0][i]);
        }

        return "RIPIND " + nodeId + " " + propagateMessage;
    }
}

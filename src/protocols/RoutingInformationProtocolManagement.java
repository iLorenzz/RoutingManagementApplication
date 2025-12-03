import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoutingInformationProtocolManagement extends RoutingInformationProtocol implements UnicastServiceUserInterface, RoutingProtocolManagementInterface{
    private final RoutingManagementApplication routingManagementApplication;
    private final Map<Short, List<Short>> nodesNeighbors;

    private String requestLinkState;
    private boolean waitingMessage = false;

    private final int timeout;
    private final ScheduledExecutorService retryScheduler;

    public RoutingInformationProtocolManagement(String hostname, int portNumber, int timeout, RoutingManagementApplication routingManagementApplication) {
        super((short) 0, hostname, portNumber);

        this.timeout = timeout;
        this.routingManagementApplication = routingManagementApplication;
        this.nodesNeighbors = new HashMap<>();
        this.retryScheduler = Executors.newScheduledThreadPool(1);

        unicastThreadInitialization();
    }

    @Override
    public void upDataInd(short source, String unicastMessage){
        if(waitingMessage){
            stopRetryScheduler();
            waitingMessage = false;
        }

        String[] brokenUnicastPDU = unicastMessage.split(" ", 3);
        String[] brokenMessagePDU = brokenUnicastPDU[2].split(" ");

        switch(brokenMessagePDU[0]){
            case "RIPRSP":

                short nodeId = Short.parseShort(brokenMessagePDU[1]);

                StringBuilder distanceTableStr = new StringBuilder();
                for(int i = 2; i < brokenMessagePDU.length; i++){
                    distanceTableStr.append(brokenMessagePDU[i]);
                    distanceTableStr.append(" ");
                }

                int[][] distanceTable = generateDistanceTableFromMessage(String.valueOf(distanceTableStr));
                routingManagementApplication.distanceTableIndication(nodeId, distanceTable);
                break;

            case "RIPNFT":
                short nodeAId = Short.parseShort(brokenMessagePDU[1]);
                short nodeBId = Short.parseShort(brokenMessagePDU[2]);
                int cost = Integer.parseInt(brokenMessagePDU[3]);

                if(requestLinkState.equals("LinkCostSetRequest1")){
                    requestLinkState = "LinkCostSetRequest2";
                    boolean success = setLinkSendMessage(nodeBId, nodeAId, cost);
                    if(!success){
                        setLinkRequest2RetryScheduler(nodeBId, nodeAId, cost);
                    }
                    break;
                }

                routingManagementApplication.linkCostIndication(nodeBId, nodeAId, cost);
                break;
        }
    }

    @Override
    public boolean getDistanceTable(short nodeId){
        boolean success = getDistanceTableSendMessage(nodeId);

        if(success){
            waitingMessage = true;
            getTableRequestRetryScheduler(nodeId);
            return true;
        }

        return false;
    }

    @Override
    public boolean getLinkCost(short nodeAId, short nodeBId){
        if(checkIfNodesAreNotNeighbors(nodeAId, nodeBId)){
            return false;
        }

        requestLinkState = "LinkCostGetRequest";
        boolean success = getLinkSendMessage(nodeAId, nodeBId);

        if(!success){
            waitingMessage = true;
            getLinkRequestRetryScheduler(nodeAId, nodeBId);
        }

        return success;
    }

    @Override
    public boolean setLinkCost(short nodeAId, short nodeBId, int cost){
        if(checkIfNodesAreNotNeighbors(nodeAId, nodeBId)){
            return false;
        }

        requestLinkState = "LinkCostSetRequest1";
        boolean success = setLinkSendMessage(nodeAId, nodeAId, cost);

        if(!success){
            waitingMessage = true;
            setLinkRequest1RetryScheduler(nodeAId, nodeBId, cost);
        }

        return success;
    }

    private boolean getDistanceTableSendMessage(short nodeId){
        String message = "RIPRQT";
        return getUnicastProtocol().upDataReq(nodeId, message);
    }

    private boolean getLinkSendMessage(short nodeAId, short nodeBId){
        String message = "RIPGET" + " " + nodeAId + " " + nodeBId;

        return getUnicastProtocol().upDataReq(nodeAId, message);
    }

    private boolean setLinkSendMessage(short nodeAId, short nodeBId, int cost){
        String message = "RIPSET" + " " + nodeAId + " " + nodeBId + " " + cost;

        return getUnicastProtocol().upDataReq(nodeAId, message);
    }

    private void setLinkRequest1RetryScheduler(short nodeAId, short nodeBId, int cost){
        retryScheduler.scheduleAtFixedRate(() -> setLinkSendMessage(nodeAId, nodeBId, cost), timeout, timeout, TimeUnit.SECONDS);
    }

    private void setLinkRequest2RetryScheduler(short nodeBId, short nodeAId, int cost){
        retryScheduler.scheduleAtFixedRate(() -> setLinkSendMessage(nodeBId, nodeAId, cost), timeout, timeout, TimeUnit.SECONDS);
    }

    private void getLinkRequestRetryScheduler(short nodeAId, short nodeBId){
        retryScheduler.scheduleAtFixedRate(() -> getLinkSendMessage(nodeAId, nodeBId), timeout, timeout, TimeUnit.SECONDS);
    }

    private void getTableRequestRetryScheduler(short nodeId){
        retryScheduler.scheduleAtFixedRate(() -> getDistanceTableSendMessage(nodeId), timeout, timeout, TimeUnit.SECONDS);
    }

    private void stopRetryScheduler(){
        retryScheduler.shutdown();
        try{
            if(!retryScheduler.awaitTermination(1, TimeUnit.SECONDS)){
                retryScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Timer was forced to interrupt!");
        }
    }

    private void unicastThreadInitialization() {
        Thread thrd = new Thread(getUnicastProtocol());
        thrd.start();
    }

    private boolean checkIfNodesAreNotNeighbors(short nodeAId, short nodeBId){
        return !nodesNeighbors.get(nodeAId).contains(nodeBId);
    }

    public void addNodeNeighbor(short nodeAId, short nodeBId){
        List<Short> nodeNeighbors = new ArrayList<>();

        nodesNeighbors.putIfAbsent(nodeAId, nodeNeighbors);

        nodeNeighbors.add(nodeBId);
        nodesNeighbors.put(nodeAId, nodeNeighbors);
    }

    private int[][] generateDistanceTableFromMessage(String distanceTableStr){
        int[][] distanceTable;

        String[] distanceVectorsStr = distanceTableStr.split(" ");
        String[] nodeDistanceVector = distanceVectorsStr[0].split(":");

        distanceTable = new int[distanceVectorsStr.length][nodeDistanceVector.length];
        String[] neighborDistanceVector;

        for(int i = 0; i < distanceTable.length; i++){
            for(int j = 0; j < distanceTable[0].length; j++){
                if(i == 0){
                    distanceTable[i][j] = Integer.parseInt(nodeDistanceVector[j]);
                    continue;
                }

                neighborDistanceVector = distanceVectorsStr[i].split(":");
                distanceTable[i][j] = Integer.parseInt(neighborDistanceVector[j]);
            }
        }

        return distanceTable;
    }
}


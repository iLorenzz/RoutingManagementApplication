import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RoutingInformationProtocolManagement extends RoutingInformationProtocol implements UnicastServiceUserInterface, RoutingProtocolManagementInterface{
    private final RoutingManagementApplication routingManagementApplication;
    private final Map<Short, List<Short>> nodesNeighbors;
    private ScheduledFuture<?> currentRetryTask;

    private String requestLinkState;
    private boolean waitingMessage = false;

    private final int timeout;
    private final ScheduledExecutorService retryScheduler;

    public RoutingInformationProtocolManagement(String hostname, int portNumber, String unicastConfigFilePath, int timeout, RoutingManagementApplication routingManagementApplication) {
        super((short) 0, hostname, portNumber, unicastConfigFilePath);

        this.timeout = timeout;
        this.routingManagementApplication = routingManagementApplication;
        this.nodesNeighbors = new HashMap<>();

        this.retryScheduler = Executors.newScheduledThreadPool(4);
        unicastThreadInitialization();
    }

    @Override
    public void upDataInd(short source, String unicastMessage){
        if(waitingMessage){
            stopRetrySchedulerForRequest();
            waitingMessage = false;
        }

        String[] brokenUnicastPDU = unicastMessage.split(" ", 3);
        String[] brokenMessagePDU = brokenUnicastPDU[2].split(" ");

        switch(brokenMessagePDU[0].trim()){
            case "RIPRSP":
                short nodeId = Short.parseShort(brokenMessagePDU[1].trim());

                StringBuilder distanceTableStr = new StringBuilder();
                for(int i = 2; i < brokenMessagePDU.length; i++){
                    distanceTableStr.append(brokenMessagePDU[i].trim());
                    distanceTableStr.append(" ");
                }

                int[][] distanceTable = generateDistanceTableFromMessage(String.valueOf(distanceTableStr));
                routingManagementApplication.distanceTableIndication(nodeId, distanceTable);
                break;

            case "RIPNTF":
                short nodeAId = Short.parseShort(brokenMessagePDU[1].trim());
                short nodeBId = Short.parseShort(brokenMessagePDU[2].trim());
                int cost = Integer.parseInt(brokenMessagePDU[3].trim());

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
        System.out.println("entrou aqui");
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

        if(success){
            waitingMessage = true;
            getLinkRequestRetryScheduler(nodeAId, nodeBId);
            return true;
        }

        return false;
    }

    @Override
    public boolean setLinkCost(short nodeAId, short nodeBId, int cost){
        if(checkIfNodesAreNotNeighbors(nodeAId, nodeBId)){
            return false;
        }

        requestLinkState = "LinkCostSetRequest1";
        boolean success = setLinkSendMessage(nodeAId, nodeBId, cost);

        if(success){
            waitingMessage = true;
            setLinkRequest1RetryScheduler(nodeAId, nodeBId, cost);
            return true;
        }

        return false;
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
        currentRetryTask = retryScheduler.scheduleAtFixedRate(() -> setLinkSendMessage(nodeAId, nodeBId, cost), timeout, timeout, TimeUnit.SECONDS);
    }

    private void setLinkRequest2RetryScheduler(short nodeBId, short nodeAId, int cost){
        currentRetryTask = retryScheduler.scheduleAtFixedRate(() -> setLinkSendMessage(nodeBId, nodeAId, cost), timeout, timeout, TimeUnit.SECONDS);
    }

    private void getLinkRequestRetryScheduler(short nodeAId, short nodeBId){
        currentRetryTask = retryScheduler.scheduleAtFixedRate(() -> getLinkSendMessage(nodeAId, nodeBId), timeout, timeout, TimeUnit.SECONDS);
    }

    private void getTableRequestRetryScheduler(short nodeId){
        currentRetryTask = retryScheduler.scheduleAtFixedRate(() -> getDistanceTableSendMessage(nodeId), timeout, timeout, TimeUnit.SECONDS);
    }

    private void stopRetrySchedulerForRequest() {
        if (currentRetryTask != null && !currentRetryTask.isCancelled()) {
            currentRetryTask.cancel(false);
            currentRetryTask = null;
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
        if(nodesNeighbors.get(nodeAId) == null){
            List<Short> neighbors = new ArrayList<>();
            neighbors.add(nodeBId);

            nodesNeighbors.put(nodeAId, neighbors);
            return;
        }

        List<Short> neighbors = nodesNeighbors.get(nodeAId);

        neighbors.add(nodeBId);
        nodesNeighbors.put(nodeAId, neighbors);

        System.out.println(nodesNeighbors);
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


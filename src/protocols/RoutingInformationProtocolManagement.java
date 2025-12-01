import java.util.*;

public class RoutingInformationProtocolManagement extends RoutingInformationProtocol implements UnicastServiceUserInterface, RoutingProtocolManagementInterface, Runnable{
    private final RoutingManagementApplication routingManagementApplication;
    private final Map<Short, List<Short>> nodesNeighbors;

    public RoutingInformationProtocolManagement(String hostname, int portNumber, RoutingManagementApplication routingManagementApplication) {
        super((short) 0, hostname, portNumber);

        this.routingManagementApplication = routingManagementApplication;
        this.nodesNeighbors = new HashMap<>();
    }

    @Override
    public void upDataInd(short source, String unicastMessage){
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
                routingManagementApplication.distanceTableIndication(source, distanceTable);
        }
    }

    @Override
    public boolean getDistanceTable(short nodeId){
        String message = "RIPRQT";

        return getUnicastProtocol().upDataReq(nodeId, message);
    }

    @Override
    public boolean getLinkCost(short nodeAId, short nodeBId){
        if(checkIfNodesAreNotNeighbors(nodeAId, nodeBId)){
            return false;
        }

        String message = "RIPGET" + " " + nodeAId + " " + nodeBId;

        return getUnicastProtocol().upDataReq(nodeAId, message);
    }

    @Override
    public boolean setLinkCost(short nodeAId, short nodeBId, int cost){
        if(checkIfNodesAreNotNeighbors(nodeAId, nodeBId)){
            return false;
        }

        String message = "RIPSET" + " " + nodeAId + " " + nodeBId + " " + cost;

        return getUnicastProtocol().upDataReq(nodeAId, message);
    }

    public void run(){

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


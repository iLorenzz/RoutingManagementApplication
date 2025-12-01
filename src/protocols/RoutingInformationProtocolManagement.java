import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingInformationProtocolManagement extends RoutingInformationProtocol implements UnicastServiceUserInterface, RoutingProtocolManagementInterface, Runnable{
    private final RoutingManagementApplication routingManagementApplication;
    private Map<Short, List<Short>> nodesNeighbors;

    public RoutingInformationProtocolManagement(String hostname, int portNumber) {
        super((short) 0, hostname, portNumber);

        this.routingManagementApplication = new RoutingManagementApplication();
        this.nodesNeighbors = new HashMap<>();
    }

    @Override
    public void upDataInd(short source, String message){

    }

    @Override
    public boolean getDistanceTable(short nodeId){
        String message = "RIPRQT";

        return getUnicastProtocol().upDataReq(nodeId, message);
    }

    @Override
    public boolean getLinkCost(short nodeAId, short nodeBId){
        if(!checkIfNodesAreNeighbors(nodeAId, nodeBId)){
            return false;
        }

        String
        return true;
    }

    @Override
    public boolean setLinkCost(short nodeAId, short nodeBId, int cost){
        return true;
    }

    public void run(){

    }

    private boolean checkIfNodesAreNeighbors(short nodeAId, short nodeBId){
        return nodesNeighbors.get(nodeAId).contains(nodeBId);
    }
}


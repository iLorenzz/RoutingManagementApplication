import java.util.SortedMap;
import java.util.SortedSet;

public class RoutingManagementApplication implements RoutingProtocolManagementServiceUserInterface, Runnable{
    private RoutingInformationProtocolManagement routingInformationProtocolManagement;

    public RoutingManagementApplication(String hostname, int portNumber){
        this.routingInformationProtocolManagement = new RoutingInformationProtocolManagement(hostname, portNumber, this);
    }

    @Override
    public void distanceTableIndication(short nodeId, int[][] distanceTable){

    }

    @Override
    public void linkCostIndication(short nodeIdA, short nodeIdB, int cost){

    }

    @Override
    public void run(){

    }
}

import java.util.SortedMap;
import java.util.SortedSet;

public class RoutingManagementApplication implements RoutingProtocolManagementServiceUserInterface, Runnable{
    private final short ucsapId;
    private final String hostName;
    private final int portNumber;

    public RoutingManagementApplication(String hostName, int portNumber){
        this.ucsapId = 0;
        this.hostName = hostName;
        if (portNumber <= 1024 || portNumber > 65535) {
            throw new IllegalArgumentException("Invalid port number " + portNumber + " at id " + ucsapId);
        }
        this.portNumber = portNumber;
    }

    @Override
    public void distanceTableIndication(short nodeId, SortedMap<Short, SortedSet<Double>> distanceTable){

    }

    @Override
    public void linkCostIndication(short nodeIdA, short nodeIdB, int cost){

    }

    @Override
    public void run(){

    }
}

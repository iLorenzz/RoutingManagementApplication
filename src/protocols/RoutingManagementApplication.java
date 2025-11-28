import java.util.SortedMap;
import java.util.SortedSet;

public class RoutingManagementApplication implements RoutingProtocolManagementServiceUserInterface, Runnable{


    public RoutingManagementApplication(){

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

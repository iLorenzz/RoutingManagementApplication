import java.util.SortedMap;
import java.util.SortedSet;

public interface RoutingProtocolManagementServiceUserInterface {
	void distanceTableIndication(short nodeId, SortedMap<Short, SortedSet<Double>> distanceTable);
	void linkCostIndication(short nodeIdA, short nodeIdB, double cost);

}

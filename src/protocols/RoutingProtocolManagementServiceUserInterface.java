import java.util.SortedMap;
import java.util.SortedSet;

public interface RoutingProtocolManagementServiceUserInterface {
	void distanceTableIndication(short nodeId, int[][] distanceTable);
	void linkCostIndication(short nodeIdA, short nodeIdB, int cost);

}

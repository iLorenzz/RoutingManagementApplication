public interface RoutingProtocolManagementInterface {
	boolean getDistanceTable(short nodeId);
	boolean getLinkCost(short nodeIdA, short nodeIdB);
	boolean setLinkCost(short nodeIdA, short nodeIdB, double cost);
}

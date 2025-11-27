import java.util.SortedMap;
import java.util.SortedSet;

public class Node {
	private final short ucsapId;
	private final String hostName;
	private final int portNumber;

	private SortedMap<Short, SortedSet<Double>> neighborCosts;
	private SortedMap<Short, SortedSet<Double>> distanceTable;
	// 'this' distance vector is accessed by using the node's own ucsapId as a Key
	// Neighbor's distance vectors are accessed by using their ucsapIds

	private final UnicastProtocol unicastProtocol;


	public Node(short ucsapId, String hostName, int portNumber) {
		this.ucsapId = ucsapId;
		this.hostName = hostName;
		if (portNumber <= 1024 || portNumber > 65535) {
			throw new IllegalArgumentException("Invalid port number " + portNumber + " at id " + ucsapId);
		}
		this.portNumber = portNumber;

		String[] entityInformation = {
			hostName,
			Integer.toString(portNumber),
		};

		this.unicastProtocol = new UnicastProtocol();
	}
}

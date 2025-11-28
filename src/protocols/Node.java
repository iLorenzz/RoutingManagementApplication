import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.SortedSet;

public class Node implements Runnable{
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
        UnicastProtocol.setEntity_map(ucsapId, entityInformation);
	}

    public void send_message(short destination, String message) {
        try {
            if (!unicastProtocol.up_data_req(destination, message)) {
                System.out.println("Error: illegal message");
            }
            //lastEntitySender = this.ucsap_id;
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        unicastProtocol.receiveMessage(portNumber);
    }

    /*public static short getLastSender() {
        return lastEntitySender;
    }*/
}

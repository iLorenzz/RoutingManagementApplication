import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.SortedSet;

public class Node implements Runnable{
    private static short lastNodeSender = -1;

	private final short ucsapId;
	private final String hostName;
	private final int portNumber;

	private SortedMap<Short, SortedSet<Double>> neighborCosts;
	private SortedMap<Short, SortedSet<Double>> distanceTable;
	// 'this' distance vector is accessed by using the node's own ucsapId as a Key
	// Neighbor's distance vectors are accessed by using their ucsapIds

	private final UnicastProtocol unicastProtocol;
    private final RoutingInformationProtocol routingInformationProtocol;


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

        this.routingInformationProtocol = new RoutingInformationProtocol();
	}

    public void sendMessage(short destination, String message) {
        try {
            if (!unicastProtocol.up_data_req(destination, message)) {
                System.out.println("Error: illegal message");
            }
            lastNodeSender = this.ucsapId;
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        //unicastProtocol.receiveMessage(portNumber);

        try(DatagramSocket datagram = new DatagramSocket(portNumber)){
            byte[] buffer = new byte[1024];

            DatagramPacket requestPack = new DatagramPacket(buffer, buffer.length);
            datagram.receive(requestPack);

            //short source = UnicastEntity.getLastSender();

            String message = new String(requestPack.getData());
            InetAddress source = requestPack.getAddress();

            routingInformationProtocol.upDataInd(lastNodeSender, message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*public static short getLastSender() {
        return lastEntitySender;
    }*/
}

import java.net.UnknownHostException;

public class UnicastEntity implements Runnable {

	private static short lastEntitySender = -1;

	private final short ucsap_id;
	private final String host_name;
	private final int port_number;

	private final UnicastProtocol unicast_protocol;

	public UnicastEntity(short ucsap_id, String host_name, int port_number) {
		this.ucsap_id = ucsap_id;
		this.host_name = host_name;

		if (port_number <= 1024 || port_number > 65535) {
			throw new IllegalArgumentException("Invalid port number " + port_number + " at id " + ucsap_id);
		}

		this.port_number = port_number;

		String[] entity_information = {
			host_name,
			Integer.toString(port_number),
		};

		unicast_protocol = new UnicastProtocol();
		UnicastProtocol.setEntity_map(ucsap_id, entity_information);
	}

	public short getUcsap_id() {
		return ucsap_id;
	}

	public int getPort_number() {
		return port_number;
	}

	public String getHost_name() {
		return host_name;
	}

	public void send_message(short destination, String message) {
		try {
			if (!unicast_protocol.up_data_req(destination, message)) {
				//Todo exception
				System.out.println("Error: illegal message");
			}
			lastEntitySender = this.ucsap_id;
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void run() {
		//unicast_protocol.receiveMessage(port_number);
	}

	public static short getLastSender() {
		return lastEntitySender;
	}
}

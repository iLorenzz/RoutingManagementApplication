import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class UnicastProtocol implements UnicastServiceInterface{
    private static ConcurrentMap<Short, String[]> entity_map = new ConcurrentHashMap<>();

    private final RoutingInformationProtocol routingInformationProtocol;

    public UnicastProtocol(){
        routingInformationProtocol = new RoutingInformationProtocol();
    }

    public static void setEntity_map(Short id, String[] entity_information) {
        entity_map.put(id, entity_information);
    }

    @Override
    public boolean up_data_req(short destination, String message) throws UnknownHostException {
        try(DatagramSocket datagram = new DatagramSocket()){

            byte[] buffer = create_message(message);

            if(buffer.length > 1024){
                return false;
            }

            InetAddress address = InetAddress.getByName(entity_map.get(destination)[0]);
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, address, Integer.parseInt(entity_map.get(destination)[1]));

            datagram.send(requestPacket);

            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] create_message(String message){
        int message_size = message.length();
        String format_message = "UPDREQPDU" + " " + message_size + " " + message;

        return format_message.getBytes();
    }

    public void receiveMessage(int port){
        try(DatagramSocket datagram = new DatagramSocket(port)){
            byte[] buffer = new byte[1024];

            DatagramPacket requestPack = new DatagramPacket(buffer, buffer.length);
            datagram.receive(requestPack);

            String request_address = requestPack.getAddress().toString();
            String request_port = Integer.toString(requestPack.getPort());
            String[] request_entity_information = {request_address, request_port};

            short source = -1;
            for(Map.Entry<Short, String[]> entry : entity_map.entrySet()){
                if(Arrays.equals(entry.getValue(), request_entity_information)){
                    source = entry.getKey();
                }
            }

            String message = new String(requestPack.getData());

            routingInformationProtocol.up_data_ind(source, message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

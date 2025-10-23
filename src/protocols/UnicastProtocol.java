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
        System.out.println(Arrays.toString(entity_map.get(id)));
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

            short source = UnicastEntity.getLastSender();

            String message = new String(requestPack.getData());

            routingInformationProtocol.up_data_ind(source, message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

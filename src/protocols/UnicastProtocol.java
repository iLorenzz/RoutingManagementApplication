import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.exit;

public class UnicastProtocol implements UnicastServiceInterface, Runnable{
    private static final ConcurrentMap<Short, String[]> entityMap = new ConcurrentHashMap<>();

    private final short ucsapId;
    private final String hostName;
    private final int portNumber;
    private volatile boolean onNodeRunning = true;

    private final DatagramSocket datagramSocket;

    private final RoutingInformationProtocol routingInformationProtocol;

    public UnicastProtocol(short ucsapId, String hostname, int portNumber, RoutingInformationProtocol routingInformationProtocol){
        this.ucsapId = ucsapId;
        this.hostName = hostname;
        if (portNumber <= 1024 || portNumber > 65535) {
            throw new IllegalArgumentException("Invalid port number " + portNumber + " at id " + ucsapId);
        }
        this.portNumber = portNumber;
        this.routingInformationProtocol = routingInformationProtocol;

        try {
            InetAddress address = InetAddress.getByName(hostname);

            String[] entityInformation = {
                address.getHostAddress(),
                Integer.toString(portNumber),
            };

            setEntityMap(ucsapId, entityInformation);

            this.datagramSocket = new DatagramSocket(portNumber, address);
            System.out.println("socket open");

        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void setEntityMap(Short id, String[] entityInformation) {
        entityMap.put(id, entityInformation);
    }

    @Override
    public boolean upDataReq(short destination, String message) {
        System.out.println("tentando enviar algo");

        try{
            byte[] buffer = createMessage(message);

            if(buffer.length > 1024){
                return false;
            }

            String[] destinationInfo = entityMap.get(destination);
            if(destinationInfo == null){
                return false;
            }

            InetAddress address = InetAddress.getByName(destinationInfo[0]);
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, address, Integer.parseInt(destinationInfo[1]));

            datagramSocket.send(requestPacket);

            return true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public void run(){
        while(onNodeRunning){
            try{
                byte[] buffer = new byte[1024];

                DatagramPacket requestPack = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(requestPack);

                System.out.println("recebeu o pacote");

                String message = new String(requestPack.getData());
                InetAddress sourceAddress = requestPack.getAddress();
                int sourcePort = requestPack.getPort();

                String[] sourceEntityInformation = {
                        sourceAddress.getHostAddress(),
                        Integer.toString(sourcePort)
                };

                System.out.println(Arrays.toString(sourceEntityInformation));

                short senderUcsapId = getSenderUcsapId(sourceEntityInformation);
                routingInformationProtocol.upDataInd(senderUcsapId, message);
            } catch (Exception e) {
                System.err.println("Unicast thread error, ending program");
                System.exit(-1);
            }
        }
    }

    private byte[] createMessage(String message){
        String trimmedString = message.trim();

        int messageSize = trimmedString.length();
        String formatMessage = "UPDREQPDU" + " " + messageSize + " " + trimmedString;

        System.out.println(formatMessage);

        return formatMessage.getBytes();
    }

    private short getSenderUcsapId(String[] sourceEntityInformation){
        for(Map.Entry<Short, String[]> entry : entityMap.entrySet()){
            String[] entityInfo = entry.getValue();

            //System.out.println(Arrays.toString(entityInfo));

            if(entityInfo[0].equals(sourceEntityInformation[0]) &&
                    entityInfo[1].equals(sourceEntityInformation[1])) {
                System.out.println(entry.getKey());
                return entry.getKey();
            }
        }

        System.out.println("estive aqui");
        return -1;
    }

    public void stopRunning(){
        datagramSocket.close();
        onNodeRunning = false;
    }
}

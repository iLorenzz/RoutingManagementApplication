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
                address.toString(),
                Integer.toString(portNumber),
            };

            setEntityMap(ucsapId, entityInformation);

            this.datagramSocket = new DatagramSocket(portNumber, address);

        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setEntityMap(Short id, String[] entityInformation) {
        entityMap.put(id, entityInformation);
    }

    @Override
    public boolean upDataReq(short destination, String message) {
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

        } catch (IOException ioe) {
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

                String message = new String(requestPack.getData());
                InetAddress sourceAddress = requestPack.getAddress();
                int sourcePort = requestPack.getPort();

                String[] sourceEntityInformation = {
                        sourceAddress.toString(),
                        Integer.toString(sourcePort)
                };

                short senderUcsapId = getSenderUcsapId(sourceEntityInformation);

                routingInformationProtocol.upDataInd(senderUcsapId, message);
            } catch (Exception e) {
                stopRunning();
                exit(-1);
            }
        }
    }

    private byte[] createMessage(String message){
        String trimmedString = message.trim();

        int messageSize = trimmedString.length();
        String formatMessage = "UPDREQPDU" + " " + messageSize + " " + trimmedString;

        return formatMessage.getBytes();
    }

    private short getSenderUcsapId(String[] sourceEntityInformation){
        for(Map.Entry<Short, String[]> entry : entityMap.entrySet()){
            if(Arrays.equals(entry.getValue(), sourceEntityInformation)) {
                return entry.getKey();
            }
        }

        return -1;
    }

    private void stopRunning(){
        datagramSocket.close();
        onNodeRunning = false;
    }
}

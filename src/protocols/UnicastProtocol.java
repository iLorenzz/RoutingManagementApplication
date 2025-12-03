import java.io.File;
import java.net.*;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UnicastProtocol implements UnicastServiceInterface, Runnable{
    private final ConcurrentMap<Short, String[]> entityMap = new ConcurrentHashMap<>();

    private final DatagramSocket datagramSocket;

    private final RoutingInformationProtocol routingInformationProtocol;

    public UnicastProtocol(short ucsapId, String hostname, int portNumber, String unicastConfigFilePath, RoutingInformationProtocol routingInformationProtocol){
        if (portNumber <= 1024 || portNumber > 65535) {
            throw new IllegalArgumentException("Invalid port number " + portNumber + " at id " + ucsapId);
        }
        this.routingInformationProtocol = routingInformationProtocol;

        readUnicastConfigFile(unicastConfigFilePath);

        try {
            InetAddress address = InetAddress.getByName(hostname);

            this.datagramSocket = new DatagramSocket(portNumber, address);

        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e.getMessage());
        }
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

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public void run(){
        boolean onNodeRunning = true;
        while(onNodeRunning){
            try{
                byte[] buffer = new byte[1024];

                DatagramPacket requestPack = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(requestPack);

                String message = new String(requestPack.getData());
                InetAddress sourceAddress = requestPack.getAddress();
                int sourcePort = requestPack.getPort();

                String[] sourceEntityInformation = {
                        sourceAddress.getHostAddress(),
                        Integer.toString(sourcePort)
                };

                short senderUcsapId = getSenderUcsapId(sourceEntityInformation);
                routingInformationProtocol.upDataInd(senderUcsapId, message);

            } catch (Exception e) {
                System.out.println("Unicast thread finishing, ending program");
                System.exit(0);
            }

            stopRunning();
        }
    }

    private byte[] createMessage(String message){
        String trimmedString = message.trim();

        int messageSize = trimmedString.length();
        String formatMessage = "UPDREQPDU" + " " + messageSize + " " + trimmedString;

        return formatMessage.getBytes();
    }

    private void readUnicastConfigFile(String configFilePath) {
        try (Scanner scanner = new Scanner(new File(configFilePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid line: " + line);
                }

                short id = Short.parseShort(parts[0]);
                String host = parts[1];
                int port = Integer.parseInt(parts[2]);

                InetAddress address = InetAddress.getByName(host);
                String[] entityInfo = {
                        address.getHostAddress(),
                        Integer.toString(port)
                };
                entityMap.put(id, entityInfo);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read config file: " + configFilePath, e);
        }
    }

    private short getSenderUcsapId(String[] sourceEntityInformation){
        for(Map.Entry<Short, String[]> entry : entityMap.entrySet()){
            String[] entityInfo = entry.getValue();

            if(entityInfo[0].equals(sourceEntityInformation[0]) &&
                    entityInfo[1].equals(sourceEntityInformation[1])) {
                return entry.getKey();
            }
        }

        return -1;
    }

    public void stopRunning(){
        datagramSocket.close();
    }
}

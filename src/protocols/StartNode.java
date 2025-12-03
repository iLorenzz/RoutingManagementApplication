import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class StartNode {
    public static void main(String[] args){
        String unicastConfigurationPath = "src/protocols/unicast_configuration.txt";
        Scanner sc = new Scanner(System.in);

        try {
            List<Short> allNodesId = getAllNodes(unicastConfigurationPath);
            List<RoutingInformationProtocolNode> nodes = createAllNodes(10, allNodesId, unicastConfigurationPath);

            System.out.println("Enter 'exit' to exit: ");
            String status = sc.nextLine();

            if(status.equals("exit")){
                nodes.forEach(RoutingInformationProtocolNode::stop);
            }
        }catch (FileNotFoundException fnfe){
            System.err.println("Configuration file not found!");
        }
    }

    private static List<RoutingInformationProtocolNode> createAllNodes(
            int propagationTimeout,
            List<Short> allNodesId,
            String unicastConfigurationPath
    ) throws FileNotFoundException {

        List<RoutingInformationProtocolNode> nodes = new ArrayList<>();
        String topologyConfigurationPath = "src/protocols/topology_configuration.txt";

        Scanner unicastScanner = new Scanner(new File(unicastConfigurationPath));
        short lastNode = 0;

        while(unicastScanner.hasNextLine()){
            String unicastConfig = unicastScanner.nextLine();
            String[] splitUnicastConfig = unicastConfig.split(" ");

            short nodeId = Short.parseShort(splitUnicastConfig[0]);

            if(nodeId < 0 || nodeId > 15){
                throw new IllegalNodeId("Node ID must be between 1 - 15!");
            }

            if(nodeId == 0){
                continue;
            }

            if((lastNode - nodeId) != -1){
                throw new IllegalNodeId("Node IDs must be incremental!");
            }
            lastNode = nodeId;

            Map<Short, Integer> nodeNeighborsLinkCost = getNodeNeighborsLinkCost(nodeId, topologyConfigurationPath);

            String hostname = splitUnicastConfig[1];
            int portNumber = Integer.parseInt(splitUnicastConfig[2]);
            try{
                nodes.add(
                        new RoutingInformationProtocolNode(
                                nodeId,
                                hostname,
                                portNumber,
                                propagationTimeout,
                                nodeNeighborsLinkCost,
                                allNodesId
                        )
                );
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }

        return nodes;
    }

    private static Map<Short, Integer> getNodeNeighborsLinkCost(short nodeId, String topologyConfiguration) throws FileNotFoundException {
        Map<Short, Integer> nodeNeighborsLinkCost = new HashMap<>();
        Scanner topologyScanner = new Scanner(new File(topologyConfiguration));

        while(topologyScanner.hasNextLine()){
            String topologyConfig = topologyScanner.nextLine();
            String[] splitTopologyConfig = topologyConfig.split(" ");

            short nodeA = Short.parseShort(splitTopologyConfig[0]);
            short nodeB = Short.parseShort(splitTopologyConfig[1]);
            int cost = Integer.parseInt(splitTopologyConfig[2]);

            if (nodeA == nodeId) {
                nodeNeighborsLinkCost.put(nodeB, cost);
            } else if (nodeB == nodeId) {
                nodeNeighborsLinkCost.put(nodeA, cost);
            }
        }

        return nodeNeighborsLinkCost;
    }

    private static List<Short> getAllNodes(String unicastConfigurationPath) throws FileNotFoundException {
        Scanner unicastScanner = new Scanner(new File(unicastConfigurationPath));
        List<Short> allNodesId = new ArrayList<>();

        short lastNode = 0;
        while(unicastScanner.hasNextLine()){
            String unicastConfig = unicastScanner.nextLine();
            String[] splitUnicastConfig = unicastConfig.split(" ");
            short nodeId = Short.parseShort(splitUnicastConfig[0]);

            if(nodeId == 0){
                continue;
            }

            if(nodeId < 0 || nodeId > 15){
                throw new IllegalNodeId("Node ID must be between 1 - 15!");
            }

            if((lastNode - nodeId) != -1){
                throw new IllegalNodeId("Node IDs must be incremental!");
            }
            lastNode = nodeId;

            if(allNodesId.contains(nodeId)){
                throw new IllegalNodeId("There cannot be two nodes with the same ID!");
            }

            allNodesId.add(nodeId);
        }

        return allNodesId;
    }
}
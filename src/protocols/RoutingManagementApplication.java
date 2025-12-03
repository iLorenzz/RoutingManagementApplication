import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.SortedSet;

public class RoutingManagementApplication implements RoutingProtocolManagementServiceUserInterface{
    private final RoutingInformationProtocolManagement routingInformationProtocolManagement;

    public RoutingManagementApplication(String hostname, int portNumber, int timeout, String unicastConfigFilePath){
        this.routingInformationProtocolManagement = new RoutingInformationProtocolManagement(hostname, portNumber, unicastConfigFilePath, timeout, this);
    }

    @Override
    public void distanceTableIndication(short nodeId, int[][] distanceTable){
        System.out.println();
        System.out.println("---------------------------------------");
        System.out.println("Node " + nodeId + " " + "Distance Table");
        System.out.println("---------------------------------------");

        for(int[] distanceVector : distanceTable){
            for(int j = 0; j < distanceVector.length; j++){
                if(j == distanceVector.length-1){
                    System.out.println(distanceVector[j]);
                    break;
                }
                System.out.print(distanceVector[j]);
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    @Override
    public void linkCostIndication(short nodeAId, short nodeBId, int cost){
        System.out.println();
        System.out.println("-----------------------------------------------");
        System.out.println("Link " + nodeAId + "-" + nodeBId + " " + "cost = " + cost);
        System.out.println("----------------------------------------------=");
        System.out.println();
    }

    public RoutingInformationProtocolManagement getRoutingInformationProtocolManagement() {
        return routingInformationProtocolManagement;
    }

    private void addNodeNeighborsMap(String topologyConfigurationPath) throws FileNotFoundException {
        Scanner topologyConfigurationScanner = new Scanner(new File(topologyConfigurationPath));

        while(topologyConfigurationScanner.hasNextLine()){
            String topologyIConfig = topologyConfigurationScanner.nextLine();
            String[] splitTopologyConfig = topologyIConfig.split(" ");

            short nodeAId = Short.parseShort(splitTopologyConfig[0]);
            short nodeBId = Short.parseShort(splitTopologyConfig[1]);

            this.routingInformationProtocolManagement.addNodeNeighbor(nodeAId, nodeBId);
            this.routingInformationProtocolManagement.addNodeNeighbor(nodeBId, nodeAId);
        }
    }

    public static void main(String[] args){
        String topologyConfigurationPath = "src/protocols/topology_configuration.txt";
        String unicastConfigurationPath = "src/protocols/unicast_configuration.txt";
        boolean running = true;

        Scanner sc = new Scanner(System.in);
        String operation;

        try {
            RoutingManagementApplication routingManagementApplication = newRoutingManagementApplication(unicastConfigurationPath);
            routingManagementApplication.addNodeNeighborsMap(topologyConfigurationPath);

            while(running){
                System.out.println("""
                       -------------------------------------------------------
                        Choose an operation:
                       -------------------------------------------------------
                        -> getLinkCost: to get a cost link between two nodes
                        -> setLinkCost: to set a cost link between two nodes
                        -> getDistanceTable: to get a node distance table
                        -> exit: to close management
                       """);
                System.out.println();

                operation = sc.nextLine();

                switch(operation){

                    case "getLinkCost":
                        System.out.println("Choose two nodes:");
                        short getLinkNodeAId = Short.parseShort(sc.nextLine());
                        short getLinkNodeBId = Short.parseShort(sc.nextLine());

                        if(!routingManagementApplication
                                .getRoutingInformationProtocolManagement()
                                .getLinkCost(getLinkNodeAId, getLinkNodeBId)){

                            System.err.println(getLinkNodeAId + " and " + getLinkNodeBId + " " + "are not neighbors or node was not found!");
                            System.out.println("Select nodes that are neighbors!");
                            System.out.println();
                        }

                        break;

                    case "setLinkCost":
                        System.out.println("Choose two nodes:");
                        short setLinkNodeAId = Short.parseShort(sc.nextLine());
                        short setLinkNodeBId = Short.parseShort(sc.nextLine());

                        System.out.println("Enter new cost value:");
                        int cost = Integer.parseInt(sc.nextLine());

                        if(!routingManagementApplication
                                .getRoutingInformationProtocolManagement()
                                .setLinkCost(setLinkNodeAId, setLinkNodeBId, cost)){

                            System.err.println("Nodes " + setLinkNodeAId + " and " + setLinkNodeBId + " " + "are not neighbors or node was not found!");
                            System.out.println("Select nodes that are neighbors!");
                        }

                        break;

                    case "getDistanceTable":
                        System.out.println("Choose a node: ");
                        short nodeId = Short.parseShort(sc.nextLine());

                        if(!routingManagementApplication
                                .getRoutingInformationProtocolManagement()
                                .getDistanceTable(nodeId)){

                            System.out.println("Node was not found!");
                        }
                        break;

                    case "exit":
                        System.out.println("Finishing...");
                        routingManagementApplication
                                .getRoutingInformationProtocolManagement()
                                .getUnicastProtocol()
                                .stopRunning();

                        running = false;
                        break;

                    default:
                        System.err.println("No such operation " + operation + "!");
                }
            }
        }catch (FileNotFoundException fnfe){
            System.err.println("Configuration file not found!");
        }
   }

   private static RoutingManagementApplication newRoutingManagementApplication(String unicastConfigurationPath) throws FileNotFoundException {
       Scanner unicastConfigurationScanner =  new Scanner(new File(unicastConfigurationPath));

       String managementInfo = unicastConfigurationScanner.nextLine();
       String[] splitManagementInfo = managementInfo.split(" ");

       String hostname = splitManagementInfo[1];
       int portNumber = Integer.parseInt(splitManagementInfo[2]);

       return new RoutingManagementApplication(hostname, portNumber, 10, unicastConfigurationPath);
   }

}

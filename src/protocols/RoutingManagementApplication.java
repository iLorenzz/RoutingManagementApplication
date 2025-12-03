import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.SortedSet;

public class RoutingManagementApplication implements RoutingProtocolManagementServiceUserInterface{
    private final RoutingInformationProtocolManagement routingInformationProtocolManagement;

    public RoutingManagementApplication(String hostname, int portNumber, int timeout){
        this.routingInformationProtocolManagement = new RoutingInformationProtocolManagement(hostname, portNumber, timeout,this);
    }

    @Override
    public void distanceTableIndication(short nodeId, int[][] distanceTable){
        System.out.println();
        System.out.println("-----------------------------------");
        System.out.println("Node " + nodeId + "Distance Table");
        System.out.println("-----------------------------------");

        for(int[] distanceVector : distanceTable){
            for(int j = 0; j < distanceVector.length; j++){
                if(j == distanceVector.length-1){
                    System.out.println(distanceVector[j]);
                }
                System.out.println(distanceVector[j]);
                System.out.println(" ");
            }
        }
    }

    @Override
    public void linkCostIndication(short nodeAId, short nodeBId, int cost){
        System.out.println();
        System.out.println("-----------------------------------------------");
        System.out.println("Link " + nodeAId + "-" + nodeBId + " " + "cost");
        System.out.println("----------------------------------------------=");
    }

    public RoutingInformationProtocolManagement getRoutingInformationProtocolManagement() {
        return routingInformationProtocolManagement;
    }

    public static void main(String[] args){
        String topologyConfigurationPath = "src/protocols/topology_configuration.txt";
        String unicastConfigurationPath = "src/protocols/unicast_configuration.txt";
        boolean running = true;

        Scanner sc = new Scanner(System.in);
        String operation;

        try {
            RoutingManagementApplication routingManagementApplication = newRoutingManagementApplication(unicastConfigurationPath);
            while(running){
                System.out.println("""
                        ----------------- Chose an operation -----------------
                        -> getLinkCost: to get a link between two nodes
                        ->  
                        """);

                operation = sc.nextLine();
                switch(operation){

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

       return new RoutingManagementApplication(hostname, portNumber, 10);
   }

   //private static void add
}

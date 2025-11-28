import java.net.UnknownHostException;

public class RoutingInformationProtocol implements UnicastServiceUserInterface, RoutingProtocolManagementInterface{

    private final UnicastProtocol unicastProtocol;
    private final RoutingManagementApplication routingManagementApplication;

    public RoutingInformationProtocol() {
        this.unicastProtocol = new UnicastProtocol();
        this.routingManagementApplication= new RoutingManagementApplication("localhost", 8088);
    }

    @Override
    public void upDataInd(short source, String message){
        //System.out.println("\nMessage from " + source + ":" + " \"" + message + "\"\n");

    }

    @Override
    public boolean getDistanceTable(short nodeId){
        String message = "RIPRQT";

        try{
            unicastProtocol.up_data_req(nodeId, message);
        } catch (UnknownHostException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean getLinkCost(short nodeAId, short nodeBId){
        return true;
    }

    @Override
    public boolean setLinkCost(short nodeAId, short nodeBId, int cost){
        return true;
    }
}

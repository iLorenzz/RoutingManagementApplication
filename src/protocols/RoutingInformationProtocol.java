public class RoutingInformationProtocol implements UnicastServiceUserInterface{

    public RoutingInformationProtocol() {
    }

    @Override
    public void up_data_ind(short source, String message){
        System.out.println("\nMessage from " + source + ":" + " \"" + message + "\"\n");
    }
}

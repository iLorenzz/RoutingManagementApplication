public abstract class RoutingInformationProtocol implements UnicastServiceUserInterface{
    private final UnicastProtocol unicastProtocol;
    private final short id;

    public RoutingInformationProtocol(short id, String hostName, int portNumber, String unicastConfigFilePath){
        this.id = id;
        this.unicastProtocol = new UnicastProtocol(this.id, hostName, portNumber, unicastConfigFilePath,this);
    }

    @Override
    public void upDataInd(short source, String message){
    }

    protected UnicastProtocol getUnicastProtocol(){
        return unicastProtocol;
    }

    protected short getId(){
        return id;
    }
}

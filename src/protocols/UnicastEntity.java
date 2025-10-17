public class UnicastEntity {
    private final String host_name;
    private final int port_number;

    public UnicastEntity(String host_name, int port_number)throws Exception{
        this.host_name = host_name;
        if(port_number <= 1024 || port_number > 65535){
            throw new Exception();
        }

        this.port_number = port_number;
    }

    public int getPort_number() {
        return port_number;
    }

    public String getHost_name() {
        return host_name;
    }
}

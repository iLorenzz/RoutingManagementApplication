import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class UnicastProtocol implements UnicastServiceInterface, Runnable{
    private static ConcurrentMap<Short, List<String>> entity_map;

    private short ucsap_id;
    private InetAddress host_name;
    private int port_number;

    public UnicastProtocol(short ucsap_id, String host_name, int port_number) throws Exception{
        this.ucsap_id = ucsap_id;
        try {
            this.host_name = InetAddress.getByName(host_name);
        }catch(UnknownHostException e) {
            System.out.println(e.getMessage());
        }

        if(port_number <= 1024 || port_number > 65535){
            throw new Exception();
        }

        this.port_number = port_number;

    }

    @Override
    public boolean up_data_req(short destination, String message){
        return true;
    }

    @Override
    public void run() {

    }
}

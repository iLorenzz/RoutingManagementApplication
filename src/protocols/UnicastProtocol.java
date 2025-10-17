import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentMap;

public class UnicastProtocol implements UnicastServiceInterface{
    private static ConcurrentMap<Short, UnicastEntity> entity_map;

    private InetAddress host_name;

    public UnicastProtocol(InetAddress host_name) throws Exception{

        this.host_name = host_name;
    }

    @Override
    public boolean up_data_req(short destination, String message){
        return true;
    }
}

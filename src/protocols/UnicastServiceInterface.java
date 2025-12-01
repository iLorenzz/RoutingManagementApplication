import java.net.UnknownHostException;

public interface UnicastServiceInterface {
    boolean upDataReq(short destination, String message) throws UnknownHostException;
}

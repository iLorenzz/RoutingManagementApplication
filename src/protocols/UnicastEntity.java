import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class UnicastEntity implements  Runnable{

    private short ucsap_id;
    private final String host_name;
    private final int port_number;

    public UnicastEntity(String host_name, int port_number)throws Exception{
        this.host_name = host_name;
        if(port_number <= 1024 || port_number > 65535){
            throw new Exception();
        }

        /*try {
            this.host_name = InetAddress.getByName(host_name);
        }catch(UnknownHostException e) {
            System.out.println(e.getMessage());
        }*/

        this.port_number = port_number;
    }

    public int getPort_number() {
        return port_number;
    }

    public String getHost_name() {
        return host_name;
    }

    @Override
    public void run() {

    }

    //put in main file
    private UnicastEntity read_config_file() throws Exception{
        String configuration_file_path = "configuration.txt";
        String[] found_correct_configuration = null;

        try(Scanner sc = new Scanner(new File(configuration_file_path))){
            while(sc.hasNextLine()){
                String config = sc.nextLine();

                String[] separated_configs = config.split("");
                if(Short.parseShort(separated_configs[0]) == ucsap_id){
                    found_correct_configuration = separated_configs;
                    break;
                }
            }

            if(found_correct_configuration != null){
                return new UnicastEntity(found_correct_configuration[1], Integer.parseInt(found_correct_configuration[2]));
            }

        }catch(Exception e){
            System.err.println(e.getMessage());
        }

        //TODO  implement exception if this ucsap_id was not found
        throw new Exception();
    }
}

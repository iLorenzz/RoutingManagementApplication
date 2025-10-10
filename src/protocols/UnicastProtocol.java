import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentMap;

public class UnicastProtocol implements UnicastServiceInterface, Runnable{
    private static ConcurrentMap<Short, EntityConfig> entity_map;

    private short ucsap_id;
    private InetAddress host_name;

    public UnicastProtocol(short ucsap_id, String host_name) throws Exception{
        this.ucsap_id = ucsap_id;
        try {
            this.host_name = InetAddress.getByName(host_name);
        }catch(UnknownHostException e) {
            System.out.println(e.getMessage());
        }

        EntityConfig this_unicast_entity_config = read_config_file();
        entity_map.put(ucsap_id, this_unicast_entity_config);
    }

    private EntityConfig read_config_file() throws Exception{
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
                return new EntityConfig(found_correct_configuration[1], Integer.parseInt(found_correct_configuration[2]));
            }

        }catch(Exception e){
            System.err.println(e.getMessage());
        }

        //TODO  implement exception if this ucsap_id was not found
        throw new Exception();
    }

    @Override
    public boolean up_data_req(short destination, String message){
        return true;
    }

    @Override
    public void run() {

    }
}

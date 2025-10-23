import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test {
    public static void main(String[] args){
        List<Thread> thread_entities = new ArrayList<>();
        String selected_host;

        try {
            List<UnicastEntity> allConfiguredEntities = read_config_file();

            for (UnicastEntity allConfiguredEntity : allConfiguredEntities) {
                Thread newThread = new Thread(allConfiguredEntity);
                thread_entities.add(newThread);
            }

            for(Thread threadEntity : thread_entities){
                threadEntity.start();
            }
            Scanner sc = new Scanner(System.in);

            do {
                System.out.println("----------------Select an host to send a message----------------");
                for (UnicastEntity entity : allConfiguredEntities) {
                    System.out.println("ID = " + entity.getUcsap_id());
                }
                System.out.println("exit: to finish the program");

                selected_host = sc.nextLine();

                System.out.println("Enter a message");
                String message = sc.nextLine();

                System.out.println("Select an host to receive the message");
                short host_to_receive = Short.parseShort(sc.nextLine());

                allConfiguredEntities.get(Integer.parseInt(selected_host)).send_message(host_to_receive, message);

                Thread.sleep(500);

            }while(!selected_host.equals("exit"));

        }catch (Exception e){
            //todo: fix to correct exception
            e.getStackTrace();
        }

    }

    private static List<UnicastEntity> read_config_file() throws Exception{
        String configuration_file_path = "/home/lorenz/Documentos/USP/projects/redes_project/RoutingManagementApplication/src/protocols/configuration.txt";
        List<UnicastEntity> allConfiguredEntities = new ArrayList<>();

        try(Scanner sc = new Scanner(new File(configuration_file_path))){
            while(sc.hasNextLine()){
                String config = sc.nextLine();

                String[] separated_configs = config.split(" ");
                allConfiguredEntities.add(new UnicastEntity(Short.parseShort(separated_configs[0]), separated_configs[1], Integer.parseInt(separated_configs[2])));
            }

            return allConfiguredEntities;

        }catch(Exception e){
            System.err.println(e.getMessage());
        }

        //TODO  implement exception if this ucsap_id was not found
        throw new Exception();
    }
}

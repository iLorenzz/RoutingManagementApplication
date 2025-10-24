import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test {

	public static void main(String[] args) {
		List<Thread> thread_entities = new ArrayList<>();
		short selected_host = 0;
		short selected_receiver = 0;
		String message;
		String input;

		List exitCommands = List.of("exit", "q", "quit", "sair");

		try {
			List<UnicastEntity> allConfiguredEntities = read_config_file();

			for (UnicastEntity allConfiguredEntity : allConfiguredEntities) {
				Thread newThread = new Thread(allConfiguredEntity);
				thread_entities.add(newThread);
				newThread.start();
			}

			Scanner sc = new Scanner(System.in);

			while(true) {
				System.out.println("==================================");
				System.out.println("Available Hosts");
				System.out.println("----------------------------------");
				System.out.println("  id | 'host name' : 'port'");
				System.out.println("----------------------------------");

				allConfiguredEntities.forEach(entity -> {System.out.println("-> " + entity.getUcsap_id() + " | " + entity.getHost_name() + " : " + entity.getPort_number());});
				System.out.println("----------------------------------");

				System.out.print("Sender ID: ");
				input = sc.nextLine();
				try {
					selected_host = Short.parseShort(input);
					final short atomicSelectedSender = selected_host;
					if (allConfiguredEntities.stream().noneMatch(entity -> {return entity.getUcsap_id() == atomicSelectedSender;})) {
						throw new NumberFormatException();
					};
				} catch (NumberFormatException e) {
					if (exitCommands.contains(input)) {break;}
					System.out.println("\n==================================");
					System.out.println("!!! Invalid Host ID !!!");
					System.out.println("==================================\n");
					continue;
				}

				System.out.print("Receiver ID: ");
				input = sc.nextLine();
				try {
					selected_receiver = Short.parseShort(input);
					final short atomicSelectedReceiver = selected_receiver;
					if (allConfiguredEntities.stream().noneMatch(entity -> {return entity.getUcsap_id() == atomicSelectedReceiver;})) {
						throw new NumberFormatException();
					};
				} catch (NumberFormatException e) {
					if (exitCommands.contains(input)) {break;}
					System.out.println("\n==================================");
					System.out.println("!!! Invalid Host ID !!!");
					System.out.println("==================================\n");
					continue;
				}

				System.out.print("Message: ");
				message = sc.nextLine();
				if (exitCommands.contains(message)) {break;}

				allConfiguredEntities
					.get(selected_host)
					.send_message(selected_receiver, message);

				Thread.sleep(500);
			}
		} catch (Exception e) {
			//todo: fix to correct exception
			e.getStackTrace();
		}
		System.out.println("Goodbye");
		thread_entities.forEach(Thread::interrupt);
		System.exit(0);
	}

	private static List<UnicastEntity> read_config_file() throws FileNotFoundException {
		String configuration_file_path = "src/protocols/configuration.txt";
		List<UnicastEntity> allConfiguredEntities = new ArrayList<>();

		Scanner sc = new Scanner(new File(configuration_file_path));
		while (sc.hasNextLine()) {
			String config = sc.nextLine();
			String[] separated_configs = config.split(" ");
			try {
				allConfiguredEntities.add(
						new UnicastEntity(
								Short.parseShort(separated_configs[0]),
								separated_configs[1],
								Integer.parseInt(separated_configs[2])
						)
				);
			} catch (IllegalArgumentException exception){
				System.out.println("\n========================================================");
				System.err.println(exception.getMessage());
				System.out.println("Proceeding with operation; faulty data will NOT be used.");
				System.out.println("========================================================\n");
			}
		}

		return allConfiguredEntities;
	}
}

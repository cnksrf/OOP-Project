import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {

        XmlDocument doc = XmlParser.parse("src/input.xml");
        CommandProcessor processor = new CommandProcessor(doc);

        Scanner scanner = new Scanner(System.in);

        while(true){
            System.out.println("> ");
            String command = scanner.nextLine();

            if(command.equals("exit")) break;

            processor.process(command);
        }

    }
}

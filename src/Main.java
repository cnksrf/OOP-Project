import java.util.*;
import java.io.*;

public class Main {
    static XmlDocument doc = null;
    static String currentPath = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("XML Parser ready. Type 'help' for available commands.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = tokenize(line);
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "open":
                    if (parts.length < 2) { System.out.println("Usage: open <file>"); break; }
                    try {
                        doc = XmlDocument.load(parts[1]);
                        currentPath = parts[1];
                        System.out.println("Successfully opened " + parts[1]);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        doc = null;
                    }
                    break;

                case "close":
                    if (doc == null) { System.out.println("Error: No file is open."); break; }
                    doc = null;
                    currentPath = null;
                    System.out.println("Successfully closed file.");
                    break;

                case "save":
                    if (doc == null) { System.out.println("Error: No file is open."); break; }
                    try {
                        doc.save(currentPath);
                        System.out.println("Successfully saved " + currentPath);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "saveas":
                    if (doc == null) { System.out.println("Error: No file is open."); break; }
                    if (parts.length < 2) { System.out.println("Usage: saveas <file>"); break; }
                    try {
                        doc.save(parts[1]);
                        System.out.println("Successfully saved " + parts[1]);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "help":
                    System.out.println("The following commands are supported:");
                    System.out.println("  open <file>              opens <file>");
                    System.out.println("  close                    closes currently opened file");
                    System.out.println("  save                     saves the currently open file");
                    System.out.println("  saveas <file>            saves the currently open file in <file>");
                    System.out.println("  print                    displays the XML content");
                    System.out.println("  select <id> <key>        outputs attribute value by element id and key");
                    System.out.println("  set <id> <key> <value>   assigns an attribute value");
                    System.out.println("  children <id>            lists nested elements");
                    System.out.println("  child <id> <n>           accesses the nth child of an element");
                    System.out.println("  text <id>                accesses the text of an element");
                    System.out.println("  delete <id> <key>        deletes an element attribute");
                    System.out.println("  newchild <id>            adds a new child element");
                    System.out.println("  xpath <id> <XPath>       executes XPath queries on an element");
                    System.out.println("  help                     prints this information");
                    System.out.println("  exit                     exits the program");
                    break;

                case "exit":
                    System.out.println("Exiting the program...");
                    return;

                default:
                    if (doc == null) {
                        System.out.println("Error: No file is open. Use 'open <file>' first.");
                    } else {
                        new CommandProcessor(doc).process(line);
                    }
            }
        }
    }

    private static String[] tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = '"';
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!inQuote && (c == '"' || c == '\'')) { inQuote = true; quoteChar = c; }
            else if (inQuote && c == quoteChar) { inQuote = false; }
            else if (!inQuote && c == ' ') {
                if (sb.length() > 0) { tokens.add(sb.toString()); sb.setLength(0); }
            } else { sb.append(c); }
        }
        if (sb.length() > 0) tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}

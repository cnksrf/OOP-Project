import java.io.*;
import java.util.*;

public class Main {

    private static XmlElement root = null;
    private static Map<String, XmlElement> idMap = new HashMap<>();
    private static String currentFilePath = null;
    private static boolean modified = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("XML Parser CLI. Type 'help' for available commands.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = tokenize(line);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "open"    -> handleOpen(parts);
                case "close"   -> handleClose();
                case "save"    -> handleSave();
                case "saveas"  -> handleSaveAs(parts);
                case "print"   -> handlePrint();
                case "select"  -> handleSelect(parts);
                case "set"     -> handleSet(parts);
                case "children"-> handleChildren(parts);
                case "child"   -> handleChild(parts);
                case "text"    -> handleText(parts);
                case "delete"  -> handleDelete(parts);
                case "newchild"-> handleNewChild(parts);
                case "xpath"   -> handleXPath(parts);
                case "help"    -> handleHelp();
                case "exit"    -> { System.out.println("Exiting the program..."); return; }
                default        -> System.out.println("Unknown command: " + command + ". Type 'help' for available commands.");
            }
        }
    }

    // ---- Command Handlers ----

    private static void handleOpen(String[] parts) {
        if (parts.length < 2) { System.out.println("Usage: open <file>"); return; }
        String path = parts[1];
        XmlParser parser = new XmlParser();
        try {
            root = parser.parse(path);
            idMap = parser.getIdMap();
            currentFilePath = path;
            modified = false;
            System.out.println("Successfully opened " + path);
        } catch (Exception e) {
            System.out.println("Error: Could not open file - " + e.getMessage());
            root = null;
        }
    }

    private static void handleClose() {
        requireOpen();
        if (root == null) return;
        root = null;
        idMap.clear();
        currentFilePath = null;
        modified = false;
        System.out.println("Successfully closed file.");
    }

    private static void handleSave() {
        if (!requireOpen()) return;
        XmlParser parser = new XmlParser();
        try {
            parser.write(root, currentFilePath);
            modified = false;
            System.out.println("Successfully saved " + currentFilePath);
        } catch (Exception e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private static void handleSaveAs(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 2) { System.out.println("Usage: saveas <file>"); return; }
        String path = parts[1];
        XmlParser parser = new XmlParser();
        try {
            parser.write(root, path);
            System.out.println("Successfully saved " + path);
        } catch (Exception e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private static void handlePrint() {
        if (!requireOpen()) return;
        System.out.println(root.print(0));
    }

    private static void handleSelect(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 3) { System.out.println("Usage: select <id> <key>"); return; }
        String id = parts[1];
        String key = parts[2];
        XmlElement el = idMap.get(id);
        if (el == null) { System.out.println("Error: No element with id '" + id + "'"); return; }
        String val = el.getAttribute(key);
        if (val == null) { System.out.println("Error: Attribute '" + key + "' not found"); return; }
        System.out.println(val);
    }

    private static void handleSet(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 4) { System.out.println("Usage: set <id> <key> <value>"); return; }
        String id = parts[1];
        String key = parts[2];
        String value = parts[3];
        XmlElement el = idMap.get(id);
        if (el == null) { System.out.println("Error: No element with id '" + id + "'"); return; }
        el.setAttribute(key, value);
        modified = true;
        System.out.println("Attribute '" + key + "' set to '" + value + "' on element '" + id + "'");
    }

    private static void handleChildren(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 2) { System.out.println("Usage: children <id>"); return; }
        String id = parts[1];
        XmlElement el = idMap.get(id);
        if (el == null) { System.out.println("Error: No element with id '" + id + "'"); return; }
        List<XmlElement> children = el.getChildren();
        if (children.isEmpty()) { System.out.println("(no children)"); return; }
        for (XmlElement child : children) {
            System.out.println("  id=" + child.getId() + "  tag=" + child.getTagName() +
                "  attrs=" + child.getAttributes());
        }
    }

    private static void handleChild(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 3) { System.out.println("Usage: child <id> <n>"); return; }
        String id = parts[1];
        int n;
        try { n = Integer.parseInt(parts[2]); }
        catch (NumberFormatException e) { System.out.println("Error: n must be an integer"); return; }
        XmlElement el = idMap.get(id);
        if (el == null) { System.out.println("Error: No element with id '" + id + "'"); return; }
        List<XmlElement> children = el.getChildren();
        if (n < 0 || n >= children.size()) {
            System.out.println("Error: Index " + n + " out of range (0-" + (children.size()-1) + ")");
            return;
        }
        XmlElement child = children.get(n);
        System.out.println(child.print(0));
    }

    private static void handleText(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 2) { System.out.println("Usage: text <id>"); return; }
        String id = parts[1];
        XmlElement el = idMap.get(id);
        if (el == null) { System.out.println("Error: No element with id '" + id + "'"); return; }
        String text = el.getText();
        System.out.println(text != null ? text : "(no text)");
    }

    private static void handleDelete(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 3) { System.out.println("Usage: delete <id> <key>"); return; }
        String id = parts[1];
        String key = parts[2];
        XmlElement el = idMap.get(id);
        if (el == null) { System.out.println("Error: No element with id '" + id + "'"); return; }
        if (el.getAttribute(key) == null) {
            System.out.println("Error: Attribute '" + key + "' not found");
            return;
        }
        el.removeAttribute(key);
        modified = true;
        System.out.println("Attribute '" + key + "' deleted from element '" + id + "'");
    }

    private static void handleNewChild(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 2) { System.out.println("Usage: newchild <id>"); return; }
        String parentId = parts[1];
        XmlElement parent = idMap.get(parentId);
        if (parent == null) { System.out.println("Error: No element with id '" + parentId + "'"); return; }
        // Generate unique id for new element
        String newId = "gen_" + idMap.size();
        while (idMap.containsKey(newId)) newId = "gen_" + (idMap.size() + 1);
        XmlElement child = new XmlElement("element");
        child.setId(newId);
        child.setAttribute("id", newId);
        parent.addChild(child);
        idMap.put(newId, child);
        modified = true;
        System.out.println("New child created with id '" + newId + "'");
    }

    private static void handleXPath(String[] parts) {
        if (!requireOpen()) return;
        if (parts.length < 3) { System.out.println("Usage: xpath <id> <XPath>"); return; }
        String id = parts[1];
        String xpathExpr = parts[2];
        XmlElement startEl = idMap.get(id);
        if (startEl == null) { System.out.println("Error: No element with id '" + id + "'"); return; }

        XPathEngine engine = new XPathEngine(startEl, idMap);
        try {
            List<Object> results = engine.evaluate(xpathExpr);
            if (results.isEmpty()) {
                System.out.println("(no results)");
            } else {
                for (Object obj : results) {
                    if (obj instanceof XmlElement el) {
                        System.out.println(el.print(0));
                    } else {
                        System.out.println(obj.toString());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("XPath error: " + e.getMessage());
        }
    }

    private static void handleHelp() {
        System.out.println("The following commands are supported:");
        System.out.println("  open <file>              opens <file>");
        System.out.println("  close                    closes currently opened file");
        System.out.println("  save                     saves the currently open file");
        System.out.println("  saveas <file>            saves the currently open file in <file>");
        System.out.println("  print                    displays the XML content");
        System.out.println("  select <id> <key>        outputs attribute value by element id and key");
        System.out.println("  set <id> <key> <value>   assigns an attribute value");
        System.out.println("  children <id>            lists attributes of nested elements");
        System.out.println("  child <id> <n>           accesses the nth successor of an element");
        System.out.println("  text <id>                accesses the text of an item");
        System.out.println("  delete <id> <key>        deletes an element attribute by key");
        System.out.println("  newchild <id>            adds a NEW item successor");
        System.out.println("  xpath <id> <XPath>       executes XPath 2.0 queries on an item");
        System.out.println("  help                     prints this information");
        System.out.println("  exit                     exits the program");
    }

    // ---- Utilities ----

    private static boolean requireOpen() {
        if (root == null) {
            System.out.println("Error: No file is open. Use 'open <file>' first.");
            return false;
        }
        return true;
    }

    /**
     * Tokenize respecting quoted strings (strips quotes).
     */
    private static String[] tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = '"';
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!inQuote && (c == '"' || c == '\'')) {
                inQuote = true;
                quoteChar = c;
            } else if (inQuote && c == quoteChar) {
                inQuote = false;
            } else if (!inQuote && c == ' ') {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}

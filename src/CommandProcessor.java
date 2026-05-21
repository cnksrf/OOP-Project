import java.util.*;

public class CommandProcessor {
    XmlDocument doc;

    public CommandProcessor(XmlDocument doc) {
        this.doc = doc;
    }

    public void process(String input) {
        String[] parts = tokenize(input);
        if (parts.length == 0) return;
        String cmd = parts[0];
        XmlNode node;

        switch (cmd) {
            case "print":
                print(doc.root, 0);
                break;

            case "select":
                if (parts.length < 3) { System.out.println("Usage: select <id> <key>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                String val = node.attributes.get(parts[2]);
                System.out.println(val != null ? val : "Error: Attribute '" + parts[2] + "' not found");
                break;

            case "set":
                if (parts.length < 4) { System.out.println("Usage: set <id> <key> <value>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                node.attributes.put(parts[2], parts[3]);
                System.out.println("Attribute '" + parts[2] + "' set to '" + parts[3] + "'");
                break;

            case "children":
                if (parts.length < 2) { System.out.println("Usage: children <id>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                if (node.children.isEmpty()) { System.out.println("(no children)"); break; }
                for (XmlNode c : node.children) {
                    System.out.println(c.tagName + " (" + c.id + ")");
                }
                break;

            case "child":
                if (parts.length < 3) { System.out.println("Usage: child <id> <n>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                int index = Integer.parseInt(parts[2]);
                if (index < 0 || index >= node.children.size()) {
                    System.out.println("Error: Index out of range"); break;
                }
                print(node.children.get(index), 0);
                break;

            case "text":
                if (parts.length < 2) { System.out.println("Usage: text <id>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                System.out.println(node.text.isEmpty() ? "(no text)" : node.text);
                break;

            case "delete":
                if (parts.length < 3) { System.out.println("Usage: delete <id> <key>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                if (!node.attributes.containsKey(parts[2])) {
                    System.out.println("Error: Attribute '" + parts[2] + "' not found"); break;
                }
                node.attributes.remove(parts[2]);
                System.out.println("Attribute '" + parts[2] + "' deleted");
                break;

            case "newchild":
                if (parts.length < 2) { System.out.println("Usage: newchild <id>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                XmlNode newNode = new XmlNode("element");
                doc.registerNode(newNode);
                node.addChild(newNode);
                System.out.println("New child created with id '" + newNode.id + "'");
                break;

            case "xpath":
                if (parts.length < 3) { System.out.println("Usage: xpath <id> <XPath>"); break; }
                node = doc.getNode(parts[1]);
                if (node == null) { System.out.println("Error: No element with id '" + parts[1] + "'"); break; }
                List<Object> xresult = XPathEngine.evaluateFull(node, parts[2]);
                if (xresult.isEmpty()) { System.out.println("(no results)"); break; }
                for (Object r : xresult) {
                    if (r instanceof XmlNode n) print(n, 0);
                    else System.out.println(r.toString());
                }
                break;

            default:
                System.out.println("Unknown command: " + cmd + ". Type 'help' for available commands.");
        }
    }

    private void print(XmlNode node, int indent) {
        String space = "    ".repeat(indent);
        System.out.print(space + "<" + node.tagName);
        for (Map.Entry<String, String> e : node.attributes.entrySet()) {
            System.out.print(" " + e.getKey() + "=\"" + e.getValue() + "\"");
        }
        if (node.children.isEmpty() && !node.text.isEmpty()) {
            System.out.println(">" + node.text + "</" + node.tagName + ">");
            return;
        }
        System.out.println(">");
        if (!node.text.isEmpty()) System.out.println(space + "    " + node.text);
        for (XmlNode child : node.children) print(child, indent + 1);
        System.out.println(space + "</" + node.tagName + ">");
    }

    // Tokenize respecting quoted strings
    private String[] tokenize(String line) {
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

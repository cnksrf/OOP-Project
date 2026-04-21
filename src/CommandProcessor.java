import java.util.*;

public class CommandProcessor {

    XmlDocument doc;

    public CommandProcessor(XmlDocument doc) {
        this.doc = doc;
    }

    public void process(String input) {
        String[] parts = input.split(" ");
        String cmd = parts[0];

        XmlNode node;

        switch (cmd) {
            case "print":
                print(doc.root, 0);
                break;

            case "select":
                node = doc.getNode(parts[1]);
                System.out.println(node.attributes.get(parts[2]));
                break;

            case "set":
                node = doc.getNode(parts[1]);
                node.attributes.put(parts[2], parts[3]);
                break;

            case "children":
                node = doc.getNode(parts[1]);
                for (XmlNode c : node.children) {
                    System.out.println(c.tagName + " (" + c.id + ")");
                }
                break;

            case "child":
                node = doc.getNode(parts[1]);
                int index = Integer.parseInt(parts[2]);
                System.out.println(node.children.get(index).tagName);
                break;

            case "text":
                node = doc.getNode(parts[1]);
                System.out.println(node.text);
                break;

            case "delete":
                node = doc.getNode(parts[1]);
                node.attributes.remove(parts[2]);
                break;

            case "newchild":
                node = doc.getNode(parts[1]);
                XmlNode newNode = new XmlNode("newNode");
                doc.registerNode(newNode);
                node.addChild(newNode);
                break;

            case "xpath":
                List<XmlNode> result = XPathEngine.evaluate(doc.root, parts[1]);
                for (XmlNode r : result) {
                    System.out.println(r.tagName + " (" + r.id + ")");
                }
                break;
        }
    }

    private void print(XmlNode node, int indent) {
        String space = " ".repeat(indent);

        System.out.print(space + "<" + node.tagName);

        for (String key : node.attributes.keySet()) {
            System.out.print(" " + key + "=\"" + node.attributes.get(key) + "\"");
        }


        if (node.children.isEmpty() && !node.text.isEmpty()) {
            System.out.println(">" + node.text + "</" + node.tagName + ">");
            return;
        }

        System.out.println(">");

        if (!node.text.isEmpty()) {
            System.out.println(space + "  " + node.text);
        }

        for (XmlNode child : node.children) {
            print(child, indent + 2);
        }

        System.out.println(space + "</" + node.tagName + ">");
    }
}
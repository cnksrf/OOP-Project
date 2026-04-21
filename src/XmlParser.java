import java.io.*;
import java.util.*;

public class XmlParser {

    public static XmlDocument parse(String filename) throws Exception {
        XmlDocument doc = new XmlDocument();
        Stack<XmlNode> stack = new Stack<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty()) continue;


            if (line.matches("<[^/][^>]*>.*</[^>]+>")) {

                int startTagEnd = line.indexOf(">");
                int endTagStart = line.lastIndexOf("<");

                if (startTagEnd < endTagStart) {

                    String tagContent = line.substring(1, startTagEnd);
                    String text = line.substring(startTagEnd + 1, endTagStart).trim();

                    String[] parts = tagContent.split(" ");
                    XmlNode node = new XmlNode(parts[0]);

                    // attributes parse
                    for (int i = 1; i < parts.length; i++) {
                        String[] kv = parts[i].split("=");
                        if (kv.length == 2) {
                            String key = kv[0];
                            String value = kv[1].replace("\"", "");
                            node.attributes.put(key, value);
                        }
                    }

                    node.text = text;

                    if (!stack.isEmpty()) {
                        stack.peek().addChild(node);
                    } else {
                        doc.root = node;
                    }

                    doc.registerNode(node);
                }
            }


            else if (line.startsWith("</")) {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }


            else if (line.startsWith("<")) {

                String tagContent = line.substring(1, line.indexOf(">"));
                String[] parts = tagContent.split(" ");

                XmlNode node = new XmlNode(parts[0]);

                // attributes parse
                for (int i = 1; i < parts.length; i++) {
                    String[] kv = parts[i].split("=");
                    if (kv.length == 2) {
                        String key = kv[0];
                        String value = kv[1].replace("\"", "");
                        node.attributes.put(key, value);
                    }
                }

                if (!stack.isEmpty()) {
                    stack.peek().addChild(node);
                } else {
                    doc.root = node;
                }

                doc.registerNode(node);
                stack.push(node);
            }


            else {
                if (!stack.isEmpty()) {
                    stack.peek().text = line.trim();
                }
            }
        }

        br.close();
        return doc;
    }
}
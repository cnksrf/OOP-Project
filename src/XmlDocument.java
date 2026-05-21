import java.util.*;
import java.io.*;
import java.nio.file.*;

public class XmlDocument {
    XmlNode root;
    Map<String, XmlNode> idIndex = new LinkedHashMap<>();
    Map<String, Integer> idCounter = new HashMap<>();
    String filePath;

    public void registerNode(XmlNode node) {
        String id = node.attributes.get("id");
        if (id == null) {
            // Generate a short deterministic id based on tag + index
            id = node.tagName + "_" + idIndex.size();
            // Make sure it's unique
            while (idIndex.containsKey(id)) id = node.tagName + "_" + idIndex.size() + "_" + (int)(Math.random()*100);
        } else {
            if (idIndex.containsKey(id)) {
                int count = idCounter.getOrDefault(id, 1);
                String newId = id + "_" + count;
                idCounter.put(id, count + 1);
                id = newId;
            } else {
                idCounter.put(id, 1);
            }
        }
        node.id = id;
        idIndex.put(id, node);
    }

    public XmlNode getNode(String id) {
        return idIndex.get(id);
    }

    // ---- Parser ----
    public static XmlDocument load(String path) throws IOException {
        String content = Files.readString(Path.of(path));
        XmlDocument doc = new XmlDocument();
        doc.filePath = path;
        Parser p = new Parser(content, doc);
        doc.root = p.parse();
        return doc;
    }

    // ---- Serializer ----
    public void save(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writeNode(root, sb, 0);
        Files.writeString(Path.of(path), sb.toString());
    }

    private void writeNode(XmlNode node, StringBuilder sb, int indent) {
        String pad = "    ".repeat(indent);
        sb.append(pad).append("<").append(node.tagName);
        for (Map.Entry<String, String> e : node.attributes.entrySet()) {
            sb.append(" ").append(e.getKey()).append("=\"").append(e.getValue()).append("\"");
        }
        sb.append(">");
        if (node.children.isEmpty() && !node.text.isEmpty()) {
            sb.append(node.text).append("</").append(node.tagName).append(">\n");
        } else {
            sb.append("\n");
            if (!node.text.isEmpty()) {
                sb.append(pad).append("    ").append(node.text).append("\n");
            }
            for (XmlNode child : node.children) {
                writeNode(child, sb, indent + 1);
            }
            sb.append(pad).append("</").append(node.tagName).append(">\n");
        }
    }

    // ---- Inner Parser ----
    static class Parser {
        String content;
        int pos;
        XmlDocument doc;

        Parser(String content, XmlDocument doc) {
            this.content = content;
            this.pos = 0;
            this.doc = doc;
        }

        XmlNode parse() {
            skipWhitespace();
            if (content.startsWith("<?", pos)) {
                while (pos < content.length() && !content.startsWith("?>", pos)) pos++;
                pos += 2;
                skipWhitespace();
            }
            return parseNode();
        }

        XmlNode parseNode() {
            expect('<');
            String tag = readName();
            XmlNode node = new XmlNode(tag);
            skipWhitespace();
            while (pos < content.length() && content.charAt(pos) != '>' && !content.startsWith("/>", pos)) {
                String key = readName();
                skipWhitespace();
                expect('=');
                skipWhitespace();
                char q = content.charAt(pos++);
                StringBuilder val = new StringBuilder();
                while (pos < content.length() && content.charAt(pos) != q) val.append(content.charAt(pos++));
                expect(q);
                node.attributes.put(key, val.toString());
                skipWhitespace();
            }
            doc.registerNode(node);
            if (content.startsWith("/>", pos)) { pos += 2; return node; }
            expect('>');
            StringBuilder text = new StringBuilder();
            while (pos < content.length()) {
                if (content.startsWith("</", pos)) break;
                if (content.charAt(pos) == '<') {
                    String t = text.toString().trim();
                    if (!t.isEmpty()) node.text = t;
                    text.setLength(0);
                    node.addChild(parseNode());
                } else {
                    text.append(content.charAt(pos++));
                }
            }
            String t = text.toString().trim();
            if (!t.isEmpty()) node.text = t;
            expect('<'); expect('/'); readName(); skipWhitespace(); expect('>');
            return node;
        }

        void skipWhitespace() {
            while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) pos++;
        }

        String readName() {
            StringBuilder sb = new StringBuilder();
            while (pos < content.length()) {
                char c = content.charAt(pos);
                if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ':' || c == '.') { sb.append(c); pos++; }
                else break;
            }
            return sb.toString();
        }

        void expect(char c) {
            if (pos >= content.length() || content.charAt(pos) != c)
                throw new RuntimeException("Expected '" + c + "' at pos " + pos + ", got '" + (pos < content.length() ? content.charAt(pos) : "EOF") + "'");
            pos++;
        }
    }
}

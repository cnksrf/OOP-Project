import java.io.*;
import java.nio.file.*;
import java.util.*;

public class XmlParser {

    private String content;
    private int pos;
    private Map<String, XmlElement> idMap;
    private Map<String, Integer> idCount;  // for making duplicate ids unique

    public XmlParser() {
        idMap = new LinkedHashMap<>();
        idCount = new HashMap<>();
    }

    public Map<String, XmlElement> getIdMap() { return idMap; }

    public XmlElement parse(String filePath) throws IOException {
        content = Files.readString(Path.of(filePath));
        pos = 0;
        idMap = new LinkedHashMap<>();
        idCount = new HashMap<>();
        skipWhitespace();
        // Skip XML declaration if present
        if (pos < content.length() && content.startsWith("<?", pos)) {
            while (pos < content.length() && !content.startsWith("?>", pos)) pos++;
            pos += 2;
            skipWhitespace();
        }
        XmlElement root = parseElement();
        return root;
    }

    private XmlElement parseElement() {
        expect('<');
        String tagName = readName();
        XmlElement element = new XmlElement(tagName);

        skipWhitespace();
        // Read attributes
        while (pos < content.length() && content.charAt(pos) != '>' && !content.startsWith("/>", pos)) {
            String attrName = readName();
            skipWhitespace();
            expect('=');
            skipWhitespace();
            char quote = content.charAt(pos++);
            StringBuilder attrVal = new StringBuilder();
            while (pos < content.length() && content.charAt(pos) != quote) {
                attrVal.append(content.charAt(pos++));
            }
            expect(quote);
            element.setAttribute(attrName, attrVal.toString());
            skipWhitespace();
        }

        // Register ID
        registerElement(element);

        if (content.startsWith("/>", pos)) {
            pos += 2;
            return element;
        }

        expect('>');

        // Read children or text
        StringBuilder textContent = new StringBuilder();
        while (pos < content.length()) {
            if (content.startsWith("</", pos)) {
                break;
            } else if (content.charAt(pos) == '<') {
                // Child element
                String txt = textContent.toString().trim();
                if (!txt.isEmpty()) element.setText(txt);
                textContent.setLength(0);
                element.addChild(parseElement());
            } else {
                textContent.append(content.charAt(pos++));
            }
        }

        String txt = textContent.toString().trim();
        if (!txt.isEmpty()) element.setText(txt);

        // Closing tag
        expect('<');
        expect('/');
        readName(); // consume closing tag name
        skipWhitespace();
        expect('>');

        return element;
    }

    private void registerElement(XmlElement element) {
        String rawId = element.getAttribute("id");
        if (rawId != null) {
            if (!idMap.containsKey(rawId)) {
                // First occurrence: use as-is but track it
                idMap.put(rawId, element);
                idCount.put(rawId, 1);
            } else {
                // Duplicate: rename existing if first time we see duplicate
                int count = idCount.get(rawId);
                if (count == 1) {
                    // Rename first occurrence
                    XmlElement first = idMap.remove(rawId);
                    first.setId(rawId + "_1");
                    idMap.put(rawId + "_1", first);
                    idCount.put(rawId, 2);
                }
                // Give this one a unique suffix
                int newCount = idCount.get(rawId);
                String uniqueId = rawId + "_" + newCount;
                element.setId(uniqueId);
                idMap.put(uniqueId, element);
                idCount.put(rawId, newCount + 1);
            }
        } else {
            // No id: generate one
            String generated = "gen_" + idMap.size();
            element.setId(generated);
            idMap.put(generated, element);
        }
    }

    private void skipWhitespace() {
        while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) pos++;
    }

    private String readName() {
        StringBuilder sb = new StringBuilder();
        while (pos < content.length()) {
            char c = content.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ':' || c == '.') {
                sb.append(c);
                pos++;
            } else break;
        }
        return sb.toString();
    }

    private void expect(char c) {
        if (pos >= content.length() || content.charAt(pos) != c) {
            throw new RuntimeException("Expected '" + c + "' at position " + pos +
                " but got '" + (pos < content.length() ? content.charAt(pos) : "EOF") + "'");
        }
        pos++;
    }

    // ---- Serializer ----
    public void write(XmlElement root, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(root.print(0));
        Files.writeString(Path.of(filePath), sb.toString());
    }
}

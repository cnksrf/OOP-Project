import java.util.*;

public class XmlElement {
    private String id;
    private Map<String, String> attributes;
    private List<XmlElement> children;
    private String text;
    private String tagName;

    public XmlElement(String tagName) {
        this.tagName = tagName;
        this.attributes = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        this.text = null;
        this.id = null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTagName() { return tagName; }

    public Map<String, String> getAttributes() { return attributes; }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
        if (key.equals("id")) this.id = value;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
        if (key.equals("id")) this.id = null;
    }

    public List<XmlElement> getChildren() { return children; }

    public void addChild(XmlElement child) {
        children.add(child);
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String print(int indent) {
        StringBuilder sb = new StringBuilder();
        String pad = " ".repeat(indent * 2);

        sb.append(pad).append("<").append(tagName);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            sb.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
        }
        sb.append(">");

        if (text != null && !text.isBlank() && children.isEmpty()) {
            sb.append(text).append("</").append(tagName).append(">");
        } else {
            if (text != null && !text.isBlank()) {
                sb.append("\n").append(pad).append("  ").append(text);
            }
            for (XmlElement child : children) {
                sb.append("\n").append(child.print(indent + 1));
            }
            if (!children.isEmpty() || (text != null && !text.isBlank())) {
                sb.append("\n").append(pad);
            }
            sb.append("</").append(tagName).append(">");
        }

        return sb.toString();
    }
}

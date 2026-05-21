import java.util.*;
public class XmlNode {
    String tagName;
    String id;
    Map<String, String> attributes;
    List<XmlNode> children;
    String text = "";
    XmlNode parent;
    public XmlNode(String tagName) {
        this.tagName = tagName;
        this.children = new ArrayList<>();
        this.attributes = new LinkedHashMap<>();
    }
    public void addChild(XmlNode child) {
        child.parent = this;
        children.add(child);
    }
}

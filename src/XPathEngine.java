import java.util.*;

public class XPathEngine {

    public static List<XmlNode> evaluate(XmlNode root, String xpath){
        String[] parts = xpath.split("/");
        List<XmlNode>current = new ArrayList<>();
        current.add(root);

        for(String part : parts){
            List<XmlNode> next = new ArrayList<>();

            for(XmlNode node : current){
                for(XmlNode child : node.children){
                    if(child.text.equals(part)){
                        next.add(child);
                    }
                }
            }
            current  = next;
        }
        return current;
    }
}

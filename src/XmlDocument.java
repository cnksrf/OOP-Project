import java.util.*;

public class XmlDocument {

    XmlNode root;
    Map<String, XmlNode> idIndex = new  HashMap<>();
    Map<String, Integer> idCounter = new HashMap<>();

    public void registerNode(XmlNode node) {
        String id = node.attributes.get("id");

        if(id == null){
            id = generateId();
        } else {
            if(idIndex.containsKey(id)){
                int count = idCounter.getOrDefault(id, 1);
                String newId = id+" "+count;
                idCounter.put(id,count + 1);
                id =  newId;
            } else {
                idCounter.put(id,1);
            }
        }
        node.id = id;
        idIndex.put(id, node);
    }
    private String generateId(){
        return UUID.randomUUID().toString().substring(0,8);
    }

    public XmlNode getNode(String id){
        return idIndex.get(id);
    }
}

import java.util.*;

public class XPathEngine {

    public static List<XmlNode> evaluate(XmlNode root, String xpath) {
        xpath = xpath.trim();
        List<Object> result = evalPath(xpath, Collections.singletonList((Object) root));
        List<XmlNode> nodes = new ArrayList<>();
        for (Object o : result) {
            if (o instanceof XmlNode) nodes.add((XmlNode) o);
        }
        return nodes;
    }

    // Returns List<Object> which can be XmlNode or String (for @attr)
    public static List<Object> evaluateFull(XmlNode root, String xpath) {
        return evalPath(xpath.trim(), Collections.singletonList((Object) root));
    }

    private static List<Object> evalPath(String xpath, List<Object> context) {
        if (xpath.isEmpty()) return context;
        List<String> steps = splitSteps(xpath);
        List<Object> current = context;
        for (String step : steps) {
            current = applyStep(step, current);
        }
        return current;
    }

    private static List<String> splitSteps(String xpath) {
        List<String> steps = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < xpath.length(); i++) {
            char c = xpath.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            else if (c == '/' && depth == 0) { steps.add(cur.toString()); cur.setLength(0); continue; }
            cur.append(c);
        }
        if (cur.length() > 0) steps.add(cur.toString());
        return steps;
    }

    private static List<Object> applyStep(String step, List<Object> context) {
        List<Object> result = new ArrayList<>();

        // @attr
        if (step.startsWith("@")) {
            String attr = step.substring(1);
            for (Object o : context) {
                if (o instanceof XmlNode node) {
                    String val = node.attributes.get(attr);
                    if (val != null) result.add(val);
                }
            }
            return result;
        }

        // tagName(attr="val") or tagName(childTag="val")
        String tagName = step;
        String compPred = null;
        int paren = step.indexOf('(');
        if (paren != -1 && step.endsWith(")")) {
            tagName = step.substring(0, paren);
            compPred = step.substring(paren + 1, step.length() - 1);
        }

        // tagName[n]
        int bracket = tagName.indexOf('[');
        int indexFilter = -1;
        if (bracket != -1) {
            indexFilter = Integer.parseInt(tagName.substring(bracket + 1, tagName.indexOf(']')).trim());
            tagName = tagName.substring(0, bracket);
        }

        final String tag = tagName;
        for (Object o : context) {
            if (o instanceof XmlNode node) {
                List<XmlNode> matches = new ArrayList<>();
                for (XmlNode child : node.children) {
                    if (child.tagName.equals(tag)) matches.add(child);
                }
                if (indexFilter >= 0) {
                    if (indexFilter < matches.size()) result.add(matches.get(indexFilter));
                } else {
                    result.addAll(matches);
                }
            }
        }

        // Apply comparison predicate
        if (compPred != null && !compPred.isEmpty()) {
            result = applyPredicate(result, compPred);
        }

        return result;
    }

    private static List<Object> applyPredicate(List<Object> elements, String pred) {
        List<Object> result = new ArrayList<>();
        int eq = pred.indexOf('=');
        if (eq == -1) return elements;
        String key = pred.substring(0, eq).trim();
        String val = pred.substring(eq + 1).trim();
        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'")))
            val = val.substring(1, val.length() - 1);
        final String value = val;
        for (Object o : elements) {
            if (o instanceof XmlNode node) {
                // Check attribute
                if (value.equals(node.attributes.get(key))) { result.add(node); continue; }
                // Check child text
                for (XmlNode child : node.children) {
                    if (child.tagName.equals(key) && value.equals(child.text)) { result.add(node); break; }
                }
            }
        }
        return result;
    }
}

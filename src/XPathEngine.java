import java.util.*;

public class XPathEngine {

    private XmlElement root;
    private Map<String, XmlElement> idMap;

    public XPathEngine(XmlElement root, Map<String, XmlElement> idMap) {
        this.root = root;
        this.idMap = idMap;
    }

    /**
     * Evaluate an XPath expression. Returns a list of matching XmlElements or strings.
     */
    public List<Object> evaluate(String xpath) {
        // Trim
        xpath = xpath.trim();

        // Handle comparison operator: expression=value
        // e.g. person(address="USA")/name
        // We'll detect it inside parsePath

        return evaluatePath(xpath, Collections.singletonList(root));
    }

    private List<Object> evaluatePath(String xpath, List<Object> context) {
        if (xpath.isEmpty()) return context;

        // Split on first '/'
        // But careful about predicates like [0]
        // We process step by step

        List<String> steps = splitSteps(xpath);
        List<Object> current = context;

        for (String step : steps) {
            current = applyStep(step, current);
        }
        return current;
    }

    private List<String> splitSteps(String xpath) {
        List<String> steps = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < xpath.length(); i++) {
            char c = xpath.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            else if (c == '/' && depth == 0) {
                steps.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        if (cur.length() > 0) steps.add(cur.toString());
        return steps;
    }

    private List<Object> applyStep(String step, List<Object> context) {
        List<Object> result = new ArrayList<>();

        // @ attribute access: @id → returns attribute values
        if (step.startsWith("@")) {
            String attrName = step.substring(1);
            for (Object obj : context) {
                if (obj instanceof XmlElement el) {
                    String val = el.getAttribute(attrName);
                    if (val != null) result.add(val);
                }
            }
            return result;
        }

        // Check for comparison predicate: tagName(attr="val") e.g. person(address="USA")
        String tagName = step;
        String compPredicate = null;
        int parenOpen = step.indexOf('(');
        if (parenOpen != -1 && step.endsWith(")")) {
            tagName = step.substring(0, parenOpen);
            compPredicate = step.substring(parenOpen + 1, step.length() - 1);
        }

        // Check for index predicate: tagName[n]
        int bracketOpen = tagName.indexOf('[');
        int indexFilter = -1;
        if (bracketOpen != -1) {
            String idx = tagName.substring(bracketOpen + 1, tagName.indexOf(']'));
            indexFilter = Integer.parseInt(idx.trim());
            tagName = tagName.substring(0, bracketOpen);
        }

        final String finalTag = tagName;

        // Gather matching children from all context elements
        for (Object obj : context) {
            if (obj instanceof XmlElement el) {
                List<XmlElement> matches = new ArrayList<>();
                for (XmlElement child : el.getChildren()) {
                    if (child.getTagName().equals(finalTag)) {
                        matches.add(child);
                    }
                }
                // Apply index filter
                if (indexFilter >= 0) {
                    if (indexFilter < matches.size()) {
                        result.add(matches.get(indexFilter));
                    }
                } else {
                    result.addAll(matches);
                }
            }
        }

        // Apply comparison predicate (e.g. person(address="USA") filters elements
        // whose *child* tag matches value)
        if (compPredicate != null && !compPredicate.isEmpty()) {
            result = applyChildValuePredicate(result, compPredicate);
        }

        return result;
    }

    /**
     * Filter elements where a child element's tag text equals a value.
     * Supports both attribute predicates (attr="val") and child-text predicates (tag="val").
     */
    private List<Object> applyChildValuePredicate(List<Object> elements, String predicate) {
        List<Object> result = new ArrayList<>();
        int eqIdx = predicate.indexOf('=');
        if (eqIdx == -1) return elements;

        String key = predicate.substring(0, eqIdx).trim();
        String value = predicate.substring(eqIdx + 1).trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        final String finalValue = value;

        for (Object obj : elements) {
            if (obj instanceof XmlElement el) {
                // Check attribute match first
                String attrVal = el.getAttribute(key);
                if (finalValue.equals(attrVal)) {
                    result.add(el);
                    continue;
                }
                // Check child text match (e.g. address child has text "USA")
                for (XmlElement child : el.getChildren()) {
                    if (child.getTagName().equals(key)) {
                        String childText = child.getText();
                        if (finalValue.equals(childText)) {
                            result.add(el);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }


}

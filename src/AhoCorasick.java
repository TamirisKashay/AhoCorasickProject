
import java.util.*;

public class AhoCorasick {

    private static class Node {
        Map<Character, Node> children = new HashMap<>();

        Node failureLink;
        Node outputLink;

        List<String> outputs = new ArrayList<>();
    }

    private final Node root = new Node();

    public AhoCorasick(List<String> patterns) {
        buildTrie(patterns);
        buildFailureLinks();
    }

    private void buildTrie(List<String> patterns) {

        for (String pattern : patterns) {

            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException(
                        "Patterns cannot be null or empty."
                );
            }

            Node current = root;

            for (char ch : pattern.toCharArray()) {
                current.children.putIfAbsent(ch, new Node());
                current = current.children.get(ch);
            }

            current.outputs.add(pattern);
        }
    }

    private void buildFailureLinks() {

        Queue<Node> queue = new LinkedList<>();

        root.failureLink = root;

        for (Node child : root.children.values()) {
            child.failureLink = root;
            child.outputLink = null;
            queue.add(child);
        }

        while (!queue.isEmpty()) {

            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry
                    : current.children.entrySet()) {

                char ch = entry.getKey();
                Node child = entry.getValue();

                Node fallback = current.failureLink;

                while (fallback != root
                        && !fallback.children.containsKey(ch)) {
                    fallback = fallback.failureLink;
                }

                if (fallback.children.containsKey(ch)) {
                    child.failureLink = fallback.children.get(ch);
                } else {
                    child.failureLink = root;
                }

                Node failure = child.failureLink;

                if (!failure.outputs.isEmpty()) {
                    child.outputLink = failure;
                } else {
                    child.outputLink = failure.outputLink;
                }

                queue.add(child);
            }
        }
    }

    public Map<String, List<Integer>> search(String text) {

        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null.");
        }

        Map<String, List<Integer>> results = new LinkedHashMap<>();

        collectPatterns(root, results);

        Node current = root;

        for (int i = 0; i < text.length(); i++) {

            char ch = text.charAt(i);

            while (current != root
                    && !current.children.containsKey(ch)) {
                current = current.failureLink;
            }

            if (current.children.containsKey(ch)) {
                current = current.children.get(ch);
            }

            reportMatches(current, i, results);
        }

        return results;
    }

    private void collectPatterns(
            Node node,
            Map<String, List<Integer>> results) {

        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();

        queue.add(node);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (!visited.add(current)) {
                continue;
            }

            for (String pattern : current.outputs) {
                results.putIfAbsent(pattern, new ArrayList<>());
            }

            queue.addAll(current.children.values());
        }
    }

    private void reportMatches(
            Node current,
            int endIndex,
            Map<String, List<Integer>> results) {

        Node node = current;

        while (node != null) {

            for (String pattern : node.outputs) {

                int startIndex =
                        endIndex - pattern.length() + 1;

                results.get(pattern).add(startIndex);
            }

            node = node.outputLink;
        }
    }
}

import java.util.HashMap;

public class Labels {
    private HashMap<String, Integer> labels = new HashMap<>();

    public int get(String label) {
        return labels.get(label);
    }

    public void set(String label, int line) {
        labels.put(label, line);
    }
}

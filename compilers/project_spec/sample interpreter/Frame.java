import java.util.HashMap;

public class Frame {

    private HashMap<String, Object> map = new HashMap<>();

    public Object get(String key) {
        return map.get(key);
    }

    public void set(String key, Object value) {
        map.put(key, value);
    }
}

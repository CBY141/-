package main.java.com.tankbattle.system;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 存档数据类
 */
public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String saveName;
    private long timestamp;
    private Map<String, Object> data = new HashMap<>();

    public SaveData(String saveName) {
        this.saveName = saveName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSaveName() {
        return saveName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Object get(String key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        Object value = data.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }

    @Override
    public String toString() {
        return String.format("SaveData[name=%s, time=%tF %tT, size=%d]",
                saveName, timestamp, timestamp, data.size());
    }
}
package battlecode.crossplay;

import org.json.*;

public class CrossPlayObject {
    public final CrossPlayObjectType type;

    public CrossPlayObject(CrossPlayObjectType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "CrossPlayObject(type=" + type + ")";
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", this.type.ordinal());
        return json;
    }

    public static CrossPlayObject fromJson(JSONObject json) {
        CrossPlayObjectType type = CrossPlayObjectType.values[json.getInt("type")];
        return new CrossPlayObject(type);
    }
}

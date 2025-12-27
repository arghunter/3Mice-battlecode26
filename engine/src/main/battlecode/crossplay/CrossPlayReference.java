package battlecode.crossplay;

import org.json.JSONObject;

public class CrossPlayReference extends CrossPlayObject {
    public int objectId;

    public CrossPlayReference(CrossPlayObjectType type, int objectId) {
        super(type);
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "CrossPlayObject(type=" + this.type + ", oid=" + this.objectId + ")";
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        json.put("oid", this.objectId);
        return json;
    }

    public static CrossPlayReference fromJson(JSONObject json) {
        CrossPlayObjectType type = CrossPlayObjectType.values[json.getInt("type")];
        int objectId = json.getInt("oid");
        return new CrossPlayReference(type, objectId);
    }
}

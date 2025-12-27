package battlecode.crossplay;

import org.json.*;

public class CrossPlayLiteral extends CrossPlayObject {
    public final Object value;

    public CrossPlayLiteral(CrossPlayObjectType type, Object value) {
        super(type);
        this.value = value;
    }

    @Override
    public String toString() {
        return "CrossPlayLiteral(type=" + type + ", value=" + value + ")";
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        json.put("value", this.value);
        return json;
    }
}

package battlecode.crossplay;

import org.json.*;

public class CrossPlayMessage extends CrossPlayObject {
    public final CrossPlayMethod method;
    public final CrossPlayObject[] params;

    public CrossPlayMessage(CrossPlayMethod method, CrossPlayObject[] params) {
        super(CrossPlayObjectType.CALL);
        this.method = method;
        this.params = params;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", this.type.ordinal());
        json.put("method", this.method.ordinal());

        JSONArray paramsJson = new JSONArray();

        for (CrossPlayObject param : this.params) {
            JSONObject paramJson = param.toJson();
            paramsJson.put(paramJson);
        }

        json.put("params", paramsJson);

        return json;
    }

    public static CrossPlayMessage fromJson(JSONObject json) {
        CrossPlayObjectType messageType = CrossPlayObjectType.values[json.getInt("type")];

        if (messageType != CrossPlayObjectType.CALL) {
            System.err.println("Received non-call cross-play message!");
        }

        CrossPlayMethod method = CrossPlayMethod.values[json.getInt("method")];
        JSONArray paramsJson = json.getJSONArray("params");
        int numParams = paramsJson.length();
        CrossPlayObject[] params = new CrossPlayObject[numParams];

        for (int i = 0; i < numParams; i++) {
            JSONObject paramJson = paramsJson.getJSONObject(i);
            CrossPlayObjectType type = CrossPlayObjectType.values[paramJson.getInt("type")];

            switch (type) {
                case INVALID:
                    System.err.println("Received invalid cross-play object type!");
                    break;
                case CALL:
                    CrossPlayMessage nestedMessage = CrossPlayMessage.fromJson(paramJson);
                    params[i] = nestedMessage;
                    break;
                default:
                    params[i] = new CrossPlayObject(type);
                    break;
            }
        }

        CrossPlayMessage message = new CrossPlayMessage(method, params);
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CrossPlayMessage(method=");
        sb.append(method);
        sb.append(", params=[");
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].toString());
            if (i < params.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("])");
        return sb.toString();
    }

    // public static void main(String[] args) {
    //     String jsonTest =
    //     """
    //     {
    //         "type": 1,
    //         "method": 1,
    //         "params": [
    //             {
    //                 "type": 1,
    //                 "method": 2,
    //                 "params": []
    //             },
    //             {
    //                 "type": 2,
    //                 "oid": 0
    //             },
    //             {
    //                 "type": 2,
    //                 "oid": 1
    //             }
    //         ]
    //     }
    //     """;
    //     JSONObject json = new JSONObject(jsonTest);
    //     CrossPlayMessage message = CrossPlayMessage.fromJson(json, 2);
    //     System.out.println("\n" + message.toString());
    // }
}

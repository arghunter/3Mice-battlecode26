package battlecode.crossplay;

import java.util.ArrayList;
import org.json.*;

import java.io.IOException;
import java.nio.file.*;

import battlecode.common.*;

/**
 * Allows bots written in different languages to be run by the Java engine using a message-passing system.
 * Any language can be supported as long as a file analogous to runner.py is written.
 * Battlecode 2026 supports Java and Python.
 */
public class CrossPlay {
    public static final String
        CROSS_PLAY_DIR = "crossplay", // temporary directory for cross-play files
        MESSAGE_FILE_JAVA = "messages_java.json", // messages from the java engine
        MESSAGE_FILE_OTHER = "messages_other.json", // messages from the other language's runner script
        LOCK_FILE_JAVA = "lock_java.txt", // lock file created by the java engine
        LOCK_FILE_OTHER = "lock_other.txt"; // lock file created by the other language's runner script

    ArrayList<Object> objects;

    public CrossPlay() {
        this.objects = new ArrayList<>();
    }

    private void clearObjects() {
        this.objects.clear();
    }

    private void resetFiles() {
        try {
            Path crossPlayDir = Paths.get(CROSS_PLAY_DIR);

            if (!Files.exists(crossPlayDir) || !Files.isDirectory(crossPlayDir)) {
                Files.createDirectory(crossPlayDir);
            }

            Files.deleteIfExists(crossPlayDir.resolve(MESSAGE_FILE_JAVA));
            Files.deleteIfExists(crossPlayDir.resolve(MESSAGE_FILE_OTHER));
            Files.deleteIfExists(crossPlayDir.resolve(LOCK_FILE_JAVA));
            Files.deleteIfExists(crossPlayDir.resolve(LOCK_FILE_OTHER));
        } catch (Exception e) {
            throw new CrossPlayException("Failed to clear cross-play lock files.");
        }
    }

    private void clearTempFiles() {
        try {
            Path crossPlayDir = Paths.get(CROSS_PLAY_DIR);

            if (Files.exists(crossPlayDir)) {
                Files.deleteIfExists(crossPlayDir.resolve(MESSAGE_FILE_JAVA));
                Files.deleteIfExists(crossPlayDir.resolve(MESSAGE_FILE_OTHER));
                Files.deleteIfExists(crossPlayDir.resolve(LOCK_FILE_JAVA));
                Files.deleteIfExists(crossPlayDir.resolve(LOCK_FILE_OTHER));
                Files.delete(crossPlayDir);
            }
        } catch (Exception e) {
            throw new CrossPlayException("Failed to clear cross-play lock files.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getLiteralValue(CrossPlayObject obj) {
        if (obj instanceof CrossPlayLiteral lit) {
            Object value = lit.value;

            try {
                return (T) value;
            } catch (ClassCastException e) {
                throw new CrossPlayException("Tried to get object of type " + obj.type + " but it does not match expected type.");
            }
        } else {
            throw new CrossPlayException("Tried to get value of non-literal cross-play object");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getObject(CrossPlayObject obj) {
        if (obj instanceof CrossPlayReference ref) {
            Object rawObj = this.objects.get(ref.objectId);

            try {
                return (T) rawObj;
            } catch (ClassCastException e) {
                throw new CrossPlayException("Tried to get object of type " + obj.type + " but it does not match expected type.");
            }
        } else {
            throw new CrossPlayException("Tried to retrieve Java value of non-reference cross-play object");
        }
    }

    private void setObject(CrossPlayReference ref, Object value) {
        if (ref.objectId >= this.objects.size()) {
            // extend the array
            for (int i = this.objects.size(); i <= ref.objectId; i++) {
                this.objects.add(null);
            }
        }

        this.objects.set(ref.objectId, value);
    }

    private CrossPlayReference setNextObject(CrossPlayObjectType type, Object value) {
        CrossPlayReference ref = new CrossPlayReference(type, this.objects.size());
        setObject(ref, value);
        return ref;
    }

    public void run() {
        clearObjects();
        resetFiles();

        Path crossPlayDir = Paths.get(CROSS_PLAY_DIR);
        Path javaMessagePath = crossPlayDir.resolve(MESSAGE_FILE_JAVA);
        Path otherMessagePath = crossPlayDir.resolve(MESSAGE_FILE_OTHER);
        Path javaLockPath = crossPlayDir.resolve(LOCK_FILE_JAVA);
        Path otherLockPath = crossPlayDir.resolve(LOCK_FILE_OTHER);

        while (true) {
            try {
                if (!Files.exists(otherMessagePath) || Files.exists(javaMessagePath) || Files.exists(otherLockPath)) {
                    Thread.sleep(0, 100000); // sleep for 0.1 ms
                    continue;
                }

                if (Files.exists(javaLockPath)) {
                    throw new CrossPlayException("Detected existing java lock file while waiting for other language's message."
                        + " This should never happen under normal operation.");
                }

                // create java lock file
                Files.createFile(javaLockPath);
                // read other language's messages
                String messageContent = Files.readString(otherMessagePath);
                JSONObject messageJson = new JSONObject(messageContent);
                CrossPlayMessage message = CrossPlayMessage.fromJson(messageJson);
                CrossPlayObject result = processMessage(message);
                // delete other language's message file
                Files.delete(otherMessagePath);

                // write to java message file
                String resultContent = result.toJson().toString();
                Files.writeString(javaMessagePath, resultContent);

                // delete java lock file
                Files.delete(javaLockPath);
                break;
            } catch (InterruptedException e) {
                throw new CrossPlayException("Cross-play message passing thread was interrupted.");
            } catch (IOException e) {
                throw new CrossPlayException("Failed to read other language's cross-play message file.");
            }
        }

        clearTempFiles();
    }

    // private JSONArray processJsonMessages(JSONArray json) {
    //     System.out.println("Starting cross-play message processing...");
    //     int length = json.length();
    //     JSONObject[] resultsArr = new JSONObject[length];

    //     for (int i = 0; i < length; i++) {
    //         JSONObject messageJson = json.getJSONObject(i);
    //         CrossPlayMessage message = CrossPlayMessage.fromJson(messageJson);
    //         CrossPlayObject result = processMessage(message);
    //         JSONObject resultJson = result.toJson();
    //         resultsArr[i] = resultJson;
    //     }

    //     JSONArray resultsJson = new JSONArray(resultsArr);
    //     System.out.println("Finished cross-play message processing.");
    //     return resultsJson;
    // }

    private CrossPlayObject processMessage(CrossPlayMessage message) {
        CrossPlayObject[] computedParams = new CrossPlayObject[message.params.length];

        for (int i = 0; i < message.params.length; i++) {
            CrossPlayObject param = message.params[i];

            if (param instanceof CrossPlayMessage mess) {
                CrossPlayObject innerResult = processMessage(mess);
                computedParams[i] = getObject(innerResult);
            } else {
                computedParams[i] = param;
            }
        }

        CrossPlayObject result;
        RobotController rc;

        // TODO add cases for all methods
        switch (message.method) {
            case INVALID:
                throw new CrossPlayException("Received invalid cross-play method!");
            case RC_GET_ROUND_NUM:
                rc = this.<RobotController>getObject(computedParams[0]);
                result = new CrossPlayLiteral(CrossPlayObjectType.INTEGER, rc.getRoundNum());
                break;
            case RC_GET_MAP_WIDTH:
                rc = this.<RobotController>getObject(computedParams[0]);
                result = new CrossPlayLiteral(CrossPlayObjectType.INTEGER, rc.getMapWidth());
                break;
            case RC_GET_MAP_HEIGHT:
                rc = this.<RobotController>getObject(computedParams[0]);
                result = new CrossPlayLiteral(CrossPlayObjectType.INTEGER, rc.getMapHeight());
                break;
            case LOG:
                String msg = getLiteralValue(computedParams[0]);
                System.out.println(msg);
            default:
                throw new CrossPlayException("Received unknown cross-play method: " + message.method);
        }

        return result;
    }
}

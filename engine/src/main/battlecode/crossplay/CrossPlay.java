package battlecode.crossplay;

import java.util.ArrayList;
import org.json.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;

import battlecode.common.*;
import battlecode.instrumenter.stream.RoboPrintStream;

/**
 * Allows bots written in different languages to be run by the Java engine using a message-passing system.
 * Any language can be supported as long as a file analogous to runner.py is written.
 * Battlecode 2026 supports Java and Python.
 */
public class CrossPlay {
    public static final String
        CROSS_PLAY_DIR = "crossplay_temp", // temporary directory for cross-play files
        MESSAGE_FILE_JAVA = "messages_java.json", // messages from the java engine
        MESSAGE_FILE_OTHER = "messages_other.json", // messages from the other language's runner script
        LOCK_FILE_JAVA = "lock_java.txt", // lock file created by the java engine
        LOCK_FILE_OTHER = "lock_other.txt"; // lock file created by the other language's runner script

    private final boolean finalizer;
    private boolean initialized;
    private ArrayList<Object> objects;
    private RobotController rc;
    private CrossPlayReference rcRef;
    private int roundNum;
    private Team team;
    private int id;
    private OutputStream out;

    public CrossPlay() {
        this.objects = new ArrayList<>();
        this.finalizer = false;
        this.initialized = false;
    }

    public CrossPlay(boolean finalizer) {
        this.objects = new ArrayList<>();
        this.finalizer = finalizer;
        this.initialized = false;
    }

    private void clearObjects() {
        this.objects.clear();
    }

    public static void resetFiles() {
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

    public static void clearTempFiles() {
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

    public int runMessagePassing() throws GameActionException {
        Path crossPlayDir = Paths.get(CROSS_PLAY_DIR);
        Path javaMessagePath = crossPlayDir.resolve(MESSAGE_FILE_JAVA);
        Path otherMessagePath = crossPlayDir.resolve(MESSAGE_FILE_OTHER);
        Path javaLockPath = crossPlayDir.resolve(LOCK_FILE_JAVA);
        Path otherLockPath = crossPlayDir.resolve(LOCK_FILE_OTHER);
        // System.out.println("Waiting for message Python -> Java...");

        while (true) {
            try {
                if (!Files.exists(otherMessagePath) || Files.exists(javaMessagePath) || Files.exists(otherLockPath)) {
                    Thread.sleep(0, 100000); // TODO currently 0.1 ms, maybe make shorter
                    // System.out.println("Still waiting for message Python -> Java...");
                    continue;
                }

                if (Files.exists(javaLockPath)) {
                    throw new CrossPlayException("Detected existing java lock file while waiting for other language's message."
                        + " This should never happen under normal operation.");
                }

                Files.createFile(javaLockPath);
                String messageContent = Files.readString(otherMessagePath);
                JSONObject messageJson = new JSONObject(messageContent);
                CrossPlayMessage message = CrossPlayMessage.fromJson(messageJson);

                // System.out.println("Received message Python -> Java: " + messageJson.toString());

                CrossPlayObject result = processMessage(message);
                String resultContent = result.toJson().toString();
                Files.writeString(javaMessagePath, resultContent);

                Files.delete(otherMessagePath);
                Files.delete(javaLockPath);

                if (message.method == CrossPlayMethod.END_TURN) {
                    // System.out.println("Received terminate message, ending cross-play message passing.");
                    int bytecodeUsed = getLiteralValue(message.params[0]);
                    return bytecodeUsed;
                } else if (this.finalizer && message.method == CrossPlayMethod.START_TURN) {
                    // System.out.println("Finalizer received start turn message, ending cross-play message passing.");
                    return 0;
                }

                // System.out.println("Sent response Java -> Python: " + resultContent);
                // System.out.println("Waiting for message Python -> Java...");
            } catch (InterruptedException e) {
                throw new CrossPlayException("Cross-play message passing thread was interrupted.");
            } catch (IOException e) {
                throw new CrossPlayException("Failed to read other language's cross-play message file.");
            }
        }
    }

    private CrossPlayObject processMessage(CrossPlayMessage message) {
        CrossPlayObject result;
        RobotController rc;

        // TODO add cases for all methods
        switch (message.method) {
            case INVALID:
                throw new CrossPlayException("Received invalid cross-play method!");
            case START_TURN:
                // System.out.println("Processing START_TURN");
                if (this.finalizer) {
                    result = new CrossPlayLiteral(CrossPlayObjectType.ARRAY, new CrossPlayObject[] {
                        CrossPlayLiteral.NULL,
                        CrossPlayLiteral.NULL,
                        CrossPlayLiteral.NULL,
                        CrossPlayLiteral.NULL,
                        CrossPlayLiteral.TRUE
                    });
                } else {
                    result = new CrossPlayLiteral(CrossPlayObjectType.ARRAY, new CrossPlayObject[] {
                        this.rcRef,
                        CrossPlayLiteral.ofInt(this.roundNum),
                        CrossPlayLiteral.ofTeam(this.team),
                        CrossPlayLiteral.ofInt(this.id),
                        CrossPlayLiteral.FALSE
                    });
                }
                break;
            case END_TURN:
                // System.out.println("Processing END_TURN");
                result = new CrossPlayLiteral(CrossPlayObjectType.NULL, null);
                break;
            case RC_GET_ROUND_NUM:
                // System.out.println("Processing RC_GET_ROUND_NUM");
                rc = this.<RobotController>getObject(message.params[0]);
                result = new CrossPlayLiteral(CrossPlayObjectType.INTEGER, rc.getRoundNum());
                break;
            case RC_GET_MAP_WIDTH:
                // System.out.println("Processing RC_GET_MAP_WIDTH");
                rc = this.<RobotController>getObject(message.params[0]);
                result = new CrossPlayLiteral(CrossPlayObjectType.INTEGER, rc.getMapWidth());
                break;
            case RC_GET_MAP_HEIGHT:
                // System.out.println("Processing RC_GET_MAP_HEIGHT");
                rc = this.<RobotController>getObject(message.params[0]);
                result = new CrossPlayLiteral(CrossPlayObjectType.INTEGER, rc.getMapHeight());
                break;
            case LOG:
                // System.out.println("Processing LOG");
                String msg = getLiteralValue(message.params[0]);

                if (this.out instanceof RoboPrintStream rps) {
                    rps.println(msg);
                }

                result = new CrossPlayLiteral(CrossPlayObjectType.NULL, null);
                break;
            default:
                throw new CrossPlayException("Received unknown cross-play method: " + message.method);
        }

        return result;
    }

    public int playTurn(RobotController rc, OutputStream systemOut) throws GameActionException {
        // System.out.println("playTurn called for CrossPlay instance " + this.hashCode());

        if (this.rc == rc && this.roundNum == rc.getRoundNum()) {
            // System.out.println("playTurn returned early for CrossPlay instance " + this.hashCode());
            return 0;
        }

        this.rc = rc;
        this.roundNum = rc.getRoundNum();
        this.team = rc.getTeam();
        this.id = rc.getID();
        this.out = systemOut;

        if (this.initialized) {
            this.rcRef = new CrossPlayReference(CrossPlayObjectType.ROBOT_CONTROLLER, 0);
        } else {
            clearObjects();
            this.rcRef = setNextObject(CrossPlayObjectType.ROBOT_CONTROLLER, this.rc);
            this.initialized = true;
            // System.out.println("Cross-play bot initialized!");
        }

        // System.out.println("Running message passing for CrossPlay instance " + this.hashCode());
        int bytecodeUsed = runMessagePassing();
        // System.out.println("playTurn finished for CrossPlay instance " + this.hashCode());
        return bytecodeUsed;
    }
}

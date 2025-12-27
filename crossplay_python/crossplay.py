import os
import json
import time

from enum import Enum

class CrossPlayException(Exception):
    def __init__(self, message):
        super().__init__(message + " (If you are a competitor, please report this to the Battlecode staff."
                         " This is not an error in your code.)")

class CrossPlayObjectType(Enum):
    INVALID = 0
    CALL = 1
    INTEGER = 2
    STRING = 3
    BOOLEAN = 4
    DOUBLE = 5
    ARRAY = 6
    DIRECTION = 7
    MAP_LOCATION = 8
    MESSAGE = 9
    ROBOT_CONTROLLER = 10
    ROBOT_INFO = 11
    TEAM = 12
    # TODO add more types

class CrossPlayMethod(Enum):
    INVALID = 0
    RC_GET_ROUND_NUM = 1
    RC_GET_MAP_WIDTH = 2
    RC_GET_MAP_HEIGHT = 3
    LOG = 4
    # TODO add more methods

class CrossPlayObject:
    def __init__(self, object_type, object_id):
        self.object_type = object_type
        self.object_id = object_id
    
    def to_json(self):
        return {
            "type": self.object_type.value,
            "oid": self.object_id
        }
    
    @staticmethod
    def from_json(json_data):
        object_type = json_data["type"]
        object_id = json_data["oid"]
        return CrossPlayObject(object_type, object_id)
    
class CrossPlayLiteral(CrossPlayObject):
    def __init__(self, object_type, object_id, value):
        super().__init__(object_type, object_id)
        self.value = value
    
    def to_json(self):
        json_data = super().to_json()
        json_data["value"] = self.value
        return json_data
    
    @staticmethod
    def from_json(json_data):
        object_type = json_data["type"]
        object_id = json_data["oid"]
        value = CrossPlayLiteral.from_json(json_data["value"])
        return CrossPlayLiteral(object_type, object_id, value)

class CrossPlayMessage(CrossPlayObject):
    def __init__(self, method, params, object_id):
        super().__init__(CrossPlayObjectType.CALL, object_id)
        self.method = method
        self.params = params
    
    def to_json(self):
        json_data = super().to_json()
        json_data["method"] = self.method.value
        json_data["params"] = [param.to_json() for param in self.params]
        return json_data
    
    @staticmethod
    def from_json(json_data):
        if json_data["type"] != CrossPlayObjectType.CALL.value:
            raise CrossPlayException("Tried to parse non-call as CrossPlayMessage!")
        
        object_id = json_data["oid"]
        method = json_data["method"]
        params = [CrossPlayObject.from_json(param) for param in json_data["params"]]
        return CrossPlayMessage(method, params, object_id)
    
MESSAGE_DIR = "crossplay_temp"

def wait(message: CrossPlayMessage, timeout=1, timestep=0.0001, message_dir=MESSAGE_DIR):
    read_file = os.path.join(message_dir, "message_java.json")
    write_file = os.path.join(message_dir, "message_other.json")
    java_lock_file = os.path.join(message_dir, "lock_java.txt")
    other_lock_file = os.path.join(message_dir, "lock_other.txt")

    json_message = message.to_json()
    time_limit = time.time() + timeout

    while not (os.path.exists(read_file) or os.path.exists(write_file) or os.path.exists(java_lock_file)):
        time.sleep(timestep)

        if time.time() > time_limit:
            raise CrossPlayException("Cross-play message passing timed out (Python -> Java).")

    if not os.path.exists(other_lock_file):
        with open(other_lock_file, 'x') as f:
            f.write('')

    with open(write_file, 'w') as f:
        json.dump(json_message, f)

    if os.path.exists(other_lock_file):
        os.remove(other_lock_file)

    time_limit = time.time() + timeout

    while not (not os.path.exists(read_file) or os.path.exists(write_file) or os.path.exists(java_lock_file)):
        time.sleep(timestep)

        if time.time() > time_limit:
            raise CrossPlayException("Cross-play message passing timed out (Python -> Java).")

    if not os.path.exists(other_lock_file):
        with open(other_lock_file, 'x') as f:
            f.write('')

    with open(read_file, 'r') as f:
        json_data = json.load(f)
        result = CrossPlayObject.from_json(json_data)

    os.remove(read_file)
    
    if os.path.exists(other_lock_file):
        os.remove(other_lock_file)

    print(f"Received message from Java: {result}")

    if isinstance(result, CrossPlayLiteral):
        return result.value
    else:
        return result

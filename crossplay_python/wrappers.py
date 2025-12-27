from crossplay_python.crossplay import CrossPlayMessage, CrossPlayLiteral, \
    CrossPlayMethod as m, CrossPlayObjectType as ot
import inspect

class Wrappers:
    @staticmethod
    def get_round_num():
        return CrossPlayMessage(m.RC_GET_ROUND_NUM, [], -1).wait()
    
    @staticmethod
    def get_map_width():
        return CrossPlayMessage(m.RC_GET_MAP_WIDTH, [], -1).wait()
    
    @staticmethod
    def get_map_height():
        return CrossPlayMessage(m.RC_GET_MAP_HEIGHT, [], -1).wait()
    
    @staticmethod
    def log(message):
        return CrossPlayMessage(m.LOG, [CrossPlayLiteral(ot.STRING, -1, message)], -1).wait()

game_methods = inspect.getmembers(Wrappers, predicate=inspect.isfunction)

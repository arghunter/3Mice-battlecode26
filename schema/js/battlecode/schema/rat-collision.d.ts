import * as flatbuffers from 'flatbuffers';
/**
 * Rat Collision
 */
export declare class RatCollision {
    bb: flatbuffers.ByteBuffer | null;
    bb_pos: number;
    __init(i: number, bb: flatbuffers.ByteBuffer): RatCollision;
    id(): number;
    loc(): number;
    static sizeOf(): number;
    static createRatCollision(builder: flatbuffers.Builder, id: number, loc: number): flatbuffers.Offset;
}

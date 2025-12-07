import { BreakDirt } from '../../battlecode/schema/break-dirt';
import { CatFeed } from '../../battlecode/schema/cat-feed';
import { CatPounce } from '../../battlecode/schema/cat-pounce';
import { CatScratch } from '../../battlecode/schema/cat-scratch';
import { CheesePickup } from '../../battlecode/schema/cheese-pickup';
import { CheeseSpawn } from '../../battlecode/schema/cheese-spawn';
import { DamageAction } from '../../battlecode/schema/damage-action';
import { DieAction } from '../../battlecode/schema/die-action';
import { IndicatorDotAction } from '../../battlecode/schema/indicator-dot-action';
import { IndicatorLineAction } from '../../battlecode/schema/indicator-line-action';
import { IndicatorStringAction } from '../../battlecode/schema/indicator-string-action';
import { PlaceDirt } from '../../battlecode/schema/place-dirt';
import { PlaceTrap } from '../../battlecode/schema/place-trap';
import { RatAttack } from '../../battlecode/schema/rat-attack';
import { RatCollision } from '../../battlecode/schema/rat-collision';
import { RatNap } from '../../battlecode/schema/rat-nap';
import { SpawnAction } from '../../battlecode/schema/spawn-action';
import { TriggerTrap } from '../../battlecode/schema/trigger-trap';
export declare enum Action {
    NONE = 0,
    CatFeed = 1,
    RatAttack = 2,
    RatNap = 3,
    RatCollision = 4,
    PlaceDirt = 5,
    BreakDirt = 6,
    CheesePickup = 7,
    CheeseSpawn = 8,
    CatScratch = 9,
    CatPounce = 10,
    PlaceTrap = 11,
    TriggerTrap = 12,
    DamageAction = 13,
    SpawnAction = 14,
    DieAction = 15,
    IndicatorStringAction = 16,
    IndicatorDotAction = 17,
    IndicatorLineAction = 18
}
export declare function unionToAction(type: Action, accessor: (obj: BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceDirt | PlaceTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerTrap) => BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceDirt | PlaceTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerTrap | null): BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceDirt | PlaceTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerTrap | null;
export declare function unionListToAction(type: Action, accessor: (index: number, obj: BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceDirt | PlaceTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerTrap) => BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceDirt | PlaceTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerTrap | null, index: number): BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceDirt | PlaceTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerTrap | null;

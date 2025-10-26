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
import { PlaceCatTrap } from '../../battlecode/schema/place-cat-trap';
import { PlaceDirt } from '../../battlecode/schema/place-dirt';
import { PlaceRatTrap } from '../../battlecode/schema/place-rat-trap';
import { RatAttack } from '../../battlecode/schema/rat-attack';
import { RatCollision } from '../../battlecode/schema/rat-collision';
import { RatNap } from '../../battlecode/schema/rat-nap';
import { SpawnAction } from '../../battlecode/schema/spawn-action';
import { TriggerCatTrap } from '../../battlecode/schema/trigger-cat-trap';
import { TriggerRatTrap } from '../../battlecode/schema/trigger-rat-trap';
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
    PlaceRatTrap = 11,
    PlaceCatTrap = 12,
    TriggerRatTrap = 13,
    TriggerCatTrap = 14,
    DamageAction = 15,
    SpawnAction = 16,
    DieAction = 17,
    IndicatorStringAction = 18,
    IndicatorDotAction = 19,
    IndicatorLineAction = 20
}
export declare function unionToAction(type: Action, accessor: (obj: BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceCatTrap | PlaceDirt | PlaceRatTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerCatTrap | TriggerRatTrap) => BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceCatTrap | PlaceDirt | PlaceRatTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerCatTrap | TriggerRatTrap | null): BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceCatTrap | PlaceDirt | PlaceRatTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerCatTrap | TriggerRatTrap | null;
export declare function unionListToAction(type: Action, accessor: (index: number, obj: BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceCatTrap | PlaceDirt | PlaceRatTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerCatTrap | TriggerRatTrap) => BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceCatTrap | PlaceDirt | PlaceRatTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerCatTrap | TriggerRatTrap | null, index: number): BreakDirt | CatFeed | CatPounce | CatScratch | CheesePickup | CheeseSpawn | DamageAction | DieAction | IndicatorDotAction | IndicatorLineAction | IndicatorStringAction | PlaceCatTrap | PlaceDirt | PlaceRatTrap | RatAttack | RatCollision | RatNap | SpawnAction | TriggerCatTrap | TriggerRatTrap | null;

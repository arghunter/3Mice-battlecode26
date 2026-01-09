import { schema } from 'battlecode-schema'
import Game, { Team } from './Game'
import assert from 'assert'
import Round from './Round'

const EMPTY_ROBOT_COUNTS: Record<schema.RobotType, number> = {
    [schema.RobotType.NONE]: 0,
    [schema.RobotType.RAT]: 0,
    [schema.RobotType.RAT_KING]: 0,
    [schema.RobotType.CAT]: 0
}

export class TeamRoundStat {
    gameModeCooperation: boolean = true
    cheeseAmount: number = 0
    cheesePercent: number = 0
    catDamageAmount: number = 0
    catDamagePercent: number = 0
    ratKingCount: number = 0
    ratKingPercent: number = 0
    dirtAmount: number = 0
    babyRatCount: number = 0
    ratTrapAmount: number = 0
    catTrapAmount: number = 0

    copy(): TeamRoundStat {
        const newStat: TeamRoundStat = Object.assign(Object.create(Object.getPrototypeOf(this)), this)
        return newStat
    }
}

export default class RoundStat {
    private readonly teams: Map<Team, TeamRoundStat>
    private readonly game: Game
    public completed: boolean = false

    constructor(game: Game, teams?: Map<Team, TeamRoundStat>) {
        this.game = game
        this.teams =
            teams ??
            new Map([
                [game.teams[0], new TeamRoundStat()],
                [game.teams[1], new TeamRoundStat()]
            ])
    }

    copy(): RoundStat {
        const newTeamStats = new Map(this.teams)
        for (const [team, stat] of this.teams) newTeamStats.set(team, stat.copy())
        const copy = new RoundStat(this.game, newTeamStats)
        copy.completed = this.completed
        return copy
    }

    applyRoundDelta(round: Round, delta: schema.Round | null): void {
        assert(
            !delta || round.roundNumber === delta.roundId() + 1,
            `Wrong round ID: is ${delta?.roundId()}, should be ${round.roundNumber + 1}`
        )

        if (this.completed) return

        const time = Date.now()
        
        if (delta) {
            let totalCheese = 0
            let totalCatDamage = 0
            let totalRatKings = 0
            
            for (let i = 0; i < delta.teamIdsLength(); i++) {
                totalCheese = delta.teamCheeseTransferred(i)!
                totalCatDamage = 1
                totalRatKings += 0
            }
            
            // Get the previous round's stat to calculate delta
            const prevRoundNumber = round.roundNumber - 1
            const prevRoundStat = prevRoundNumber >= 0 ? round.match.stats[prevRoundNumber] : null
            
            for (let i = 0; i < delta.teamIdsLength(); i++) {
                const team = this.game.teams[(delta.teamIds(i) ?? assert.fail('teamID not found in round')) - 1]
                assert(team != undefined, `team ${i} not found in game.teams in round`)
                const teamStat = this.teams.get(team) ?? assert.fail(`team ${i} not found in team stats in round`)
                
                const currentCheese = delta.teamCheeseTransferred(i) ?? assert.fail('missing cheese amount')
                
                // Get previous cheese from previous round's stat
                const previousCheese = prevRoundStat ? prevRoundStat.getTeamStat(team).cheeseAmount : currentCheese
                const cheeseDelta = currentCheese - previousCheese
                
                teamStat.cheeseAmount = currentCheese
                teamStat.cheesePercent = cheeseDelta
                teamStat.catDamageAmount = teamStat.catDamageAmount
                teamStat.catDamagePercent = totalCatDamage ? teamStat.catDamageAmount / totalCatDamage : 0
                teamStat.ratKingCount = delta.teamAliveRatKings(i) ?? assert.fail('missing rat king count')
                teamStat.ratKingPercent = totalRatKings ? teamStat.ratKingCount / totalRatKings : 0
                teamStat.dirtAmount = delta.teamDirtAmounts(i) ?? assert.fail('missing dirt amount')
                teamStat.ratTrapAmount = delta.teamRatTrapCount(i) ?? assert.fail('missing rat trap amount')
                teamStat.catTrapAmount = delta.teamCatTrapCount(i) ?? assert.fail('missing cat trap amount')
                teamStat.babyRatCount = delta.teamAliveBabyRats(i) ?? assert.fail('missing baby rat count')

                let isCoop = false
                for (let ti = 0; ti < delta.turnsLength(); ti++) {
                    const t = delta.turns(ti)
                    if (t && t.isCooperation && t.isCooperation()) {
                        isCoop = true
                        break
                    }
                }
                teamStat.gameModeCooperation = isCoop
            }
        }

        // Clear values for recomputing
        for (const stat of this.teams.values()) {
            stat.babyRatCount = 0
        }

        // Compute total robot counts
        for (const body of round.bodies.bodies.values()) {
            if (body.team.id === 0) continue
            const teamStat = round.stat.getTeamStat(body.team)
            if (body.dead) continue
            if (body.robotType == schema.RobotType.RAT) teamStat.babyRatCount++
        }

        const timems = Date.now() - time
        if (timems > 1) {
            console.warn(`took ${timems}ms to calculate stat for round ${round.roundNumber}`)
        }

        this.completed = true
    }

    public getTeamStat(team: Team): TeamRoundStat {
        return this.teams.get(team) ?? assert.fail(`team ${team} not found in team stats in round`)
    }
}
import React, { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { useRound } from '../../../playback/GameRunner'
import Round from '../../../playback/Round'
import { LineChartDataPoint, QuickLineChart } from './quick-line-chart'
import { TeamRoundStat } from '../../../playback/RoundStat'

interface Props {
    active: boolean
    property: keyof TeamRoundStat
    propertyDisplayName: string
}

function getChartData(round: Round, property: keyof TeamRoundStat): LineChartDataPoint[] {
    const teams = round.match.game.teams

    const result: LineChartDataPoint[] = []

    // Sparser graph as datapoints increase
    const interval = Math.ceil(round.roundNumber / 500)

    for (let i = 0; i < round.roundNumber; i += interval) {
        const roundStat = round.match.stats[i]

        const team0Stat = roundStat.getTeamStat(teams[0])
        const team1Stat = roundStat.getTeamStat(teams[1])

        result.push({
            round: i + 1,
            team0: team0Stat[property] as number,
            team1: team1Stat[property] as number
        })
    }

    return result
}

export const ResourceGraph: React.FC<Props> = (props) => {
    const round = useRound()
    const data = props.active && round ? getChartData(round, props.property) : []

    const [fullscreen, setFullscreen] = useState(false)
    const [zoom, setZoom] = useState(1)

    const [hover, setHover] = useState<{
        x: number
        y: number
        point: LineChartDataPoint
    } | null>(null)

    const containerRef = useRef<HTMLDivElement>(null)

    // ESC to close
    useEffect(() => {
        if (!fullscreen) return
        const onKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'Escape') setFullscreen(false)
        }
        window.addEventListener('keydown', onKeyDown)
        return () => window.removeEventListener('keydown', onKeyDown)
    }, [fullscreen])

    // Mouse wheel zoom
    useEffect(() => {
        if (!fullscreen) return
        const el = containerRef.current
        if (!el) return

        const onWheel = (e: WheelEvent) => {
            e.preventDefault()
            setZoom(z =>
                Math.min(2.5, Math.max(0.6, z + (e.deltaY < 0 ? 0.1 : -0.1)))
            )
        }

        el.addEventListener('wheel', onWheel, { passive: false })
        return () => el.removeEventListener('wheel', onWheel)
    }, [fullscreen])

    const onMouseMove = (e: React.MouseEvent) => {
        if (!containerRef.current || data.length === 0) return

        const rect = containerRef.current.getBoundingClientRect()
        const x = e.clientX - rect.left
        const y = e.clientY - rect.top

        const ratio = x / rect.width
        const index = Math.min(
            data.length - 1,
            Math.max(0, Math.round(ratio * (data.length - 1)))
        )

        setHover({
            x,
            y,
            point: data[index]
        })
    }

    const resetZoom = () => setZoom(1)

    return (
        <>
            {/* Normal view */}
            <div
                className="mt-2 w-full cursor-pointer"
                onClick={() => {
                    setZoom(1)
                    setFullscreen(true)
                }}
            >
                <h2 className="mx-auto text-center mb-2">
                    {props.propertyDisplayName}
                </h2>

                <QuickLineChart
                    data={data}
                    width={350}
                    height={170}
                    margin={{ top: 2, right: 20, bottom: 17, left: 40 }}
                />
            </div>

            {/* Fullscreen */}
            {fullscreen &&
                createPortal(
                    <div
                        style={{
                            position: 'fixed',
                            inset: 0,
                            background: 'rgba(0,0,0,0.7)',
                            zIndex: 50,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}
                        onClick={() => setFullscreen(false)}
                    >
                        <div
                            ref={containerRef}
                            onClick={e => e.stopPropagation()}
                            onMouseMove={onMouseMove}
                            onMouseLeave={() => setHover(null)}
                            style={{
                                position: 'relative',
                                background: '#4f345a',
                                padding: 16,
                                borderRadius: 8,
                                boxShadow: '0 10px 30px rgba(0,0,0,0.3)',
                                transform: `scale(${zoom})`,
                                transformOrigin: 'center',
                                transition: 'transform 120ms ease-out'
                            }}
                        >
                            {/* Header */}
                            <div
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    marginBottom: 8
                                }}
                            >
                                <strong>{props.propertyDisplayName}</strong>
                                <div style={{ display: 'flex', gap: 6 }}>
                                    <button onClick={() => setZoom(z => Math.min(2.5, z + 0.1))}>＋</button>
                                    <button onClick={() => setZoom(z => Math.max(0.6, z - 0.1))}>－</button>
                                    <button onClick={resetZoom}>Reset</button>
                                    <button onClick={() => setFullscreen(false)}>✕</button>
                                </div>
                            </div>

                            <QuickLineChart
                                data={data}
                                width={900}
                                height={500}
                                margin={{ top: 20, right: 40, bottom: 40, left: 60 }}
                            />

                            {/* Tooltip */}
                            {hover && (
                                <div
                                    style={{
                                        position: 'absolute',
                                        left: hover.x + 12,
                                        top: hover.y + 12,
                                        background: 'rgba(0,0,0,0.8)',
                                        color: 'white',
                                        padding: '6px 8px',
                                        borderRadius: 4,
                                        fontSize: 12,
                                        pointerEvents: 'none',
                                        whiteSpace: 'nowrap'
                                    }}
                                >
                                    <div><strong>Round {hover.point.round}</strong></div>
                                    <div>Team 0: {hover.point.team0}</div>
                                    <div>Team 1: {hover.point.team1}</div>
                                </div>
                            )}
                        </div>
                    </div>,
                    document.body
                )}
        </>
    )
}
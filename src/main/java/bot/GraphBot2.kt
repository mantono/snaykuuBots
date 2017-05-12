package bot

import gameLogic.*
import java.util.*

class GraphBot2 : Brain
{
	private val MAX_DEPTH = 256
	private var self: Snake? = null
	private var state: GameState? = null
	private var graph: Graph? = null
	private var analyzer: BoardAnalyzer? = null
	private var startTime: Long = 0

	override fun getNextMove(yourSnake: Snake, gameState: GameState): Direction
	{
		this.startTime = System.currentTimeMillis()
		this.self = yourSnake
		this.state = gameState
		this.analyzer = BoardAnalyzer(state, self)
		this.graph = Graph(analyzer)

		val from = self!!.headPosition
		var path: Deque<Direction>? = null
		while (path == null)
		{
			val to = analyzer!!.nextTarget
			path = graph!!.getBfsPath(from, to)
		}

		var firstDirection = path.peek()
		if (isOpositeDirections(self!!.currentDirection, firstDirection))
			firstDirection = firstDirection.turnLeft()

		val direction = getBestDirection(firstDirection, from)
		return direction
	}

	private fun getBestDirection(firstDirection: Direction, from: Position): Direction
	{
		val scores = TreeMap<Int, Direction>()
		val visited = HashSet<Position>(128)
		visited.add(from)


		var next = firstDirection.calculateNextPosition(from)
		var score = getScore(visited, next, 0)
		scores.put(score, firstDirection)

		for (d in Direction.values())
		{
			if (d == firstDirection)
				continue
			next = d.calculateNextPosition(from)
			score = getScore(visited, next, 0)
			scores.put(score, d)
		}

		val highScore = scores.lastKey()
		val bestDirection = scores[highScore]
		println(bestDirection.toString() + "(" + highScore + ")")

		return scores[highScore] ?: Direction.NORTH
	}

	private fun getScore(visited: MutableSet<Position>, current: Position, score: Int): Int
	{
		var score = score
		if (visited.contains(current))
			return score

		if (reachedComputationCapacity(visited.size))
			return score

		if (analyzer!!.isLethal(current))
			return score

		visited.add(current)

		score += analyzer!!.getScore(current)

		var highScore = Integer.MIN_VALUE

		for (d in Direction.values())
		{
			val next = d.calculateNextPosition(current)
			val scoreInDirection = getScore(visited, next, score)
			if (scoreInDirection > highScore)
				highScore = scoreInDirection
		}
		visited.remove(current)

		return highScore
	}

	private fun reachedComputationCapacity(pathSize: Int): Boolean
	{
		if (thinkingTimeLeft() < 10)
			return true

		if (pathSize >= MAX_DEPTH)
			return true

		return false
	}

	private fun thinkingTimeElapsed(): Long
	{
		return System.currentTimeMillis() - startTime
	}

	private fun thinkingTimeLeft(): Long
	{
		val elpasedTime = thinkingTimeElapsed()
		return state!!.metadata.maximumThinkingTime - elpasedTime
	}

	private fun isOpositeDirections(currentDireciton: Direction, dir: Direction): Boolean = dir.turnLeft().turnLeft() == currentDireciton
}
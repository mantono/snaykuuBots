package bot

import gameLogic.*
import java.util.*
import kotlin.collections.HashSet


private const val MAX_DEPTH = 1024

class GraphBotK: Brain
{
	override fun getNextMove(self: Snake, gameState: GameState): Direction
	{
		try
		{
			val time = TimeTracker(gameState.metadata)
			val state: BoardState = BoardState(gameState, self)
			val graph = Graph(state)

			val fruits = rankFruits(state, self.headPosition)
			val firstDirectionToEvaluate = initialDirection(graph, fruits, state, self)
			val from = self.headPosition
			val direction = bestDirection(firstDirectionToEvaluate, self.currentDirection, from, state, time)
			return direction
		}
		catch(e: Exception)
		{
			e.printStackTrace()
		}

		return self.currentDirection.turnLeft()
	}

	/*
	private fun computeRequiredSpace(): Int
	{
		val snakeLength = self!!.segments.size
		val growthFrequency = state!!.metadata.growthFrequency
		val total = snakeLength + snakeLength / growthFrequency + 1
		return total
	}
	*/

	private fun bestDirection(initialDirection: Direction, currentDirection: Direction, current: Position, state: BoardState, time: TimeTracker): Direction
	{
		val directions: Queue<Direction> = listOfDirections(initialDirection, currentDirection)
		val scores = TreeMap<Int, Direction>()
		val visited: MutableSet<Position> = HashSet(MAX_DEPTH/4)

		while(directions.isNotEmpty() && time.remaining() > 20)
		{
			val nextDirection: Direction = directions.poll()
			val nextPosition: Position = current going nextDirection
			val score = getScore(visited, nextPosition, state, time)
			scores.put(score, nextDirection)
		}

		val highScore: Int = scores.lastKey()
		val bestDirection: Direction = scores[highScore] ?: initialDirection
		println(bestDirection.toString() + "(" + highScore + ")")

		return bestDirection
	}

	/**
	 * Create a list of all Direction values, where the initialDirection is first in the list.
	 */
	private fun listOfDirections(initialDirection: Direction, currentDirection: Direction): Queue<Direction>
	{
		val q: Queue<Direction> = LinkedList()
		val complement = Direction.values()
				.filter { it != initialDirection }
				.filter { it != currentDirection }

		q.add(initialDirection)
		q.addAll(complement)

		return q
	}

	/*
	private fun bestDirection(firstDirection: Direction, from: Position): Direction
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

		lastScore = highScore
		return scores[highScore]
	}
	*/

	private fun getScore(visited: MutableSet<Position>, current: Position, state: BoardState, time: TimeTracker, score: Int = 0): Int
	{
		if(current in visited)
			return score

		if(reachedComputationCapacity(time, visited.size))
			return score

		if(state.isLethal(current))
			return score

		visited.add(current)

		val scoreForThis: Int = state.score(current)
		val accumulatedScore: Int = score + scoreForThis

		val highScore: Int = Direction.values()
				.asSequence()
				.map { current going it }
				.map { getScore(visited, it, state, time, accumulatedScore) }
				.max() ?: accumulatedScore

		visited.remove(current)

		return highScore
	}

	/*
	private fun getScore(visited: MutableSet<Position>, current: Position, score: Int = 0): Int
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
	*/

	private fun reachedComputationCapacity(time: TimeTracker, pathSize: Int): Boolean
	{
		if(time.remaining() < 10)
			return true

		if(pathSize >= MAX_DEPTH)
			return true

		return false
	}
}
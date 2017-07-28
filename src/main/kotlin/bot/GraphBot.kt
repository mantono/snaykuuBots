package bot

import gameLogic.*
import java.util.*
import kotlin.collections.HashSet


class GraphBot: Brain
{
	private var maxDepth = 4
	init
	{
		System.out.println("\n\n*** BAM!! ***\n")
		System.out.println("*** Version ${this::class.java.hashCode()} ***\n")
	}

	override fun getNextMove(self: Snake, gameState: GameState): Direction
	{
		try
		{
			maxDepth = maxStackDepth(self.segments.size)
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

	private fun bestDirection(initialDirection: Direction, currentDirection: Direction, current: Position, state: BoardState, time: TimeTracker): Direction
	{
		val directions: Queue<Direction> = listOfDirections(initialDirection, currentDirection)
		val scores = TreeMap<Int, Direction>()
		val visited: MutableSet<Position> = HashSet(maxDepth /4)

		while(directions.isNotEmpty())
		{
			val nextDirection: Direction = directions.poll()
			val nextPosition: Position = current going nextDirection
			val score = getScore(visited, nextPosition, state, time)
			scores.put(score, nextDirection)
		}

		val highScore: Int = scores.lastKey()
		val bestDirection: Direction = scores[highScore] ?: initialDirection
		println("${this::class.java.canonicalName}: $bestDirection ($highScore)")

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

	private fun getScore(visited: MutableSet<Position>, current: Position, state: BoardState, time: TimeTracker, score: Int = 0): Int
	{
		if(current in visited)
			return score

		if(reachedComputationCapacity(time, visited.size))
			return score

		if(state.isLethal(current))
			return score - 10

		visited.add(current)

		val scoreForThis: Byte = state.score(current)
		val accumulatedScore: Int = score + scoreForThis

		val highScore: Int = Direction.values()
				.asSequence()
				.map { current going it }
				.map { getScore(visited, it, state, time, accumulatedScore) }
				.max() ?: accumulatedScore

		visited.remove(current)

		return highScore
	}

	private fun reachedComputationCapacity(time: TimeTracker, pathSize: Int): Boolean
	{
		if(time.remaining() < 15)
			return true

		if(pathSize >= maxDepth)
			return true

		return false
	}
}

fun maxStackDepth(snakeSize: Int): Int = 16 + (snakeSize * 1.5).toInt()
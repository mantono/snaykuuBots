package bot

import gameLogic.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

class Tetrahedron : Brain
{
	override fun getNextMove(self: Snake, state: GameState): Direction
	{
		try
		{
			val myPos = self.headPosition

			val optState = OptimizedState(self, state)
			val matrix = ByteMatrix(state.board)
			populate(matrix, optState)
			val sqr: Position = bestSquare(matrix)

			System.out.println("Found best square: $sqr")

			val analyzer: BoardAnalyzer = BoardAnalyzer(state, self)
			val graph: Graph = Graph(analyzer)

			val path = graph.bfsPath(myPos, sqr)
			val direction: Direction = path?.first ?: Direction.NORTH
			return turn(myPos, direction, analyzer)
		}
		catch(e: Exception)
		{
			System.out.println(e.message)
			e.printStackTrace()
		}
		return Direction.NORTH
	}

	private fun turn(myPos: Position, direction: Direction, analyzer: BoardAnalyzer): Direction
	{
		if(direction != null)
		{
			val newPos = direction.calculateNextPosition(myPos)
			if(!analyzer.isDangerous(newPos))
				return direction
		}

		for(d in Direction.values())
		{
			val newPos = d.calculateNextPosition(myPos)
			if(!analyzer.isDangerous(newPos))
				return d
		}

		return Direction.NORTH
	}

	tailrec fun bestSquare(matrix: ByteMatrix, bounds: Bounds = matrix.bounds): Position
	{
		if(bounds.isSingle)
			return bounds.positions[0]

		val subBounds = bounds.divide()

		val scores: SortedMap<Int, Bounds> = when(bounds.size)
		{
			in 1000..Int.MAX_VALUE -> asyncScore(matrix, subBounds)
			else -> sequentialScore(matrix, subBounds)
		}

		val bestBounds: Bounds = scores[scores.firstKey()]!!
		System.out.println("Best quadrant: $bestBounds, score: ${scores.firstKey()}")

		return bestSquare(matrix, bestBounds)
	}

	private fun sequentialScore(matrix: ByteMatrix, subBounds: List<Bounds>): SortedMap<Int, Bounds>
	{
		val scores: SortedMap<Int, Bounds> = TreeMap(this::compareDescending)

		val score0 = countScore(matrix, subBounds[0])
		val score1 = countScore(matrix, subBounds[1])
		val score2 = countScore(matrix, subBounds[2])
		val score3 = countScore(matrix, subBounds[3])

		scores.apply()
		{
			put(score0, subBounds[0])
			put(score1, subBounds[1])
			put(score2, subBounds[2])
			put(score3, subBounds[3])
		}

		return scores
	}

	private fun compareDescending(a: Int, b: Int): Int = b - a

	private fun asyncScore(matrix: ByteMatrix, subBounds: List<Bounds>): SortedMap<Int, Bounds>
	{
		val scores: SortedMap<Int, Bounds> = TreeMap(this::compareDescending)

		val score0 = async(CommonPool) { countScoreAsync(matrix, subBounds[0]) }
		val score1 = async(CommonPool) { countScoreAsync(matrix, subBounds[1]) }
		val score2 = async(CommonPool) { countScoreAsync(matrix, subBounds[2]) }
		val score3 = async(CommonPool) { countScoreAsync(matrix, subBounds[3]) }

		scores.apply()
		{
			runBlocking()
			{
				put(score0.await(), subBounds[0])
				put(score1.await(), subBounds[1])
				put(score2.await(), subBounds[2])
				put(score3.await(), subBounds[3])
			}
		}

		return scores
	}
}

suspend fun countScoreAsync(data: ByteMatrix, bounds: Bounds): Int
{
	return bounds.asSequence()
			.map {data[it]}
			.sum()
}

fun countScore(data: ByteMatrix, bounds: Bounds): Int
{
	return bounds.asSequence()
			.map {data[it]}
			.sum()
}
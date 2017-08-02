package bot

import gameLogic.*
import java.util.*

class Tetrahedron: Brain
{
	override fun getNextMove(self: Snake, state: GameState): Direction
	{
		try
		{
			val myPos = self.headPosition

			val optState = BoardState(state, self)
			val matrix = Matrix<Byte>(state.board)
			populate(matrix, optState)
			val sqr: Position = bestSquare(matrix)

			System.out.println("Found best square: $sqr")

			val analyzer: BoardState = BoardState(state, self)
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

	private fun turn(myPos: Position, direction: Direction, analyzer: BoardState): Direction
	{
		val newPos = direction.calculateNextPosition(myPos)
		if(!analyzer.isDangerous(newPos))
			return direction

		for(d in Direction.values())
		{
			val pos = d.calculateNextPosition(myPos)
			if(!analyzer.isDangerous(pos))
				return d
		}

		return Direction.NORTH
	}

	tailrec fun bestSquare(matrix: Matrix<Byte>, bounds: Bounds = matrix.bounds): Position
	{
		if(bounds.isSingle)
			return bounds.positions[0]

		val subBounds = bounds.divide()
		val scores: SortedMap<Int, Bounds> = sequentialScore(matrix, subBounds)
		val bestBounds: Bounds = scores[scores.firstKey()]!!
		System.out.println("Best quadrant: $bestBounds, score: ${scores.firstKey()}")

		return bestSquare(matrix, bestBounds)
	}

	private fun sequentialScore(matrix: Matrix<Byte>, subBounds: List<Bounds>): SortedMap<Int, Bounds>
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
}

fun countScore(data: Matrix<Byte>, bounds: Bounds): Int
{
	return bounds.asSequence()
			.map {data[it]}
			.sum()
}
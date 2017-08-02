package bot

import gameLogic.Board
import gameLogic.Position

data class Matrix<T: Number>(val width: Int,
                  val height: Int) : Comparable<Matrix<T>>
{
	private val data: MutableList<T> = ArrayList(width * height)
	val size: Int = data.size
	val bounds: Bounds = Bounds(0, 0, width - 1, height - 1)

	constructor(board: Board): this(board.width, board.height)

	private inline fun indexOf(x: Int, y: Int): Int = x + (y * width)

	operator fun get(p: Position): T = get(p.x, p.y)

	operator fun get(x: Int, y: Int): T
	{
		checkBounds(x, y)
		val i = indexOf(x, y)
		return data[i]
	}

	operator fun set(p: Position, score: T) = set(p.x, p.y, score)

	operator fun set(x: Int, y: Int, score: T)
	{
		checkBounds(x, y)
		val i = indexOf(x, y)
		data[i] = score
	}

	operator fun set(i: Int, value: T)
	{
		data[i] = value
	}

	private inline fun checkBounds(x: Int, y: Int)
	{
		if (x >= width || x < 0)
			throw IndexOutOfBoundsException("$x >= $width")
		if (y >= height || y < 0)
			throw IndexOutOfBoundsException("$y >= $height")
	}

	fun asSequence(): Sequence<T> = data.asSequence()

	override fun compareTo(other: Matrix<T>): Int
	{
		val sumThis = this.asSequence().sumBy { it.toInt() }
		val sumOther = other.asSequence().sumBy { it.toInt() }
		return sumOther - sumThis
	}
}

fun <T: Number>populate(matrix: Matrix<T>, state: BoardState)
{
	state.nonEmptyPositions()
			.forEach { matrix[it.x, it.y] = state.score(Position(it.x, it.y)) as T }
}
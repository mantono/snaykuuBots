package bot

import gameLogic.Board
import gameLogic.Position

data class Matrix<T>(val width: Int, val height: Int)
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
}

fun populate(matrix: Matrix<Byte>, state: BoardState)
{
	state.state.board.asSequnce()
			.forEach { matrix[it] = categorize(state, it).score}
}
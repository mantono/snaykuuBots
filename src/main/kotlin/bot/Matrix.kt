package bot

import gameLogic.Board
import gameLogic.Position

data class Matrix<T: Number>(val width: Int,
                  val height: Int) : Comparable<Matrix<T>>
{
	private val data: Array<Byte> = Array(width * height) {0.toByte()}
	val size: Int = data.size
	val bounds: Bounds = Bounds(0, 0, width - 1, height - 1)

	constructor(board: Board): this(board.width, board.height)

	private inline fun indexOf(x: Int, y: Int): Int = x + (y * width)

	operator fun get(p: Position): Byte = get(p.x, p.y)

	operator fun get(x: Int, y: Int): Byte
	{
		checkBounds(x, y)
		val i = indexOf(x, y)
		return data[i]
	}

	operator fun set(p: Position, score: Byte) = set(p.x, p.y, score)

	operator fun set(x: Int, y: Int, score: Byte)
	{
		checkBounds(x, y)
		val i = indexOf(x, y)
		data[i] = score
	}

	private inline fun checkBounds(x: Int, y: Int)
	{
		if (x >= width || x < 0)
			throw IndexOutOfBoundsException("$x >= $width")
		if (y >= height || y < 0)
			throw IndexOutOfBoundsException("$y >= $height")
	}

	fun asSequence(): Sequence<Byte> = data.asSequence()

	override fun compareTo(other: Matrix<T>): Int
	{
		val sumThis = asSequence().sum()
		val sumOther = asSequence().sum()
		return sumOther - sumThis
	}
}

fun <T: Number>populate(matrix: Matrix<T>, state: BoardState)
{
	state.nonEmptyPositions()
			.forEach { matrix[it.x, it.y] = state.score(Position(it.x, it.y)) }
}
package bot

import gameLogic.Board
import gameLogic.Position

data class Matrix<T: Number>(val width: Int,
                  val height: Int, private val initialValue: T) : Comparable<Matrix<T>>
{
	private val data: MutableList<T> = ArrayList(width * height)
	val size: Int = width * height
	val bounds: Bounds = Bounds(0, 0, width - 1, height - 1)
	val indices: IntRange by lazy { data.indices }

	constructor(board: Board): this(board.width, board.height, 0 as T)

	init
	{
		for(i in 0 until size)
			data.add(i, initialValue)
		assert(data.size == size)
	}

	private inline fun indexOf(x: Int, y: Int): Int = x + (y * width)

	operator fun get(p: Position): T = get(p.x, p.y)

	operator fun get(x: Int, y: Int): T
	{
		checkBounds(x, y)
		val i = indexOf(x, y)
		return data[i]
	}

	operator fun get(i: Int): T = data[i]

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

	override fun toString(): String
	{
		return data.asSequence()
				.mapIndexed{ index, value -> "${value.round(2)}" + if(index % width == width - 1) "\n" else ", " }
				.joinToString(separator = "")
	}
}

fun <T: Number>populate(matrix: Matrix<T>, state: BoardState)
{
	state.nonEmptyPositions()
			.forEach { matrix[it.x, it.y] = state.score(Position(it.x, it.y)) as T }
}

fun Number.round(decimals: Int): Double
{
	val roundingFactor = Math.pow(10.0, decimals.toDouble())
	val multiplied = this.toDouble() * roundingFactor
	val rounded = Math.round(multiplied)
	return rounded / roundingFactor
}
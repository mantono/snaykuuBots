package main.bot

import gameLogic.*
import java.util.*
import java.util.stream.Collectors.toSet

class Tetrahedron: Brain
{
	override fun getNextMove(self: Snake, state: GameState): Direction
	{
		val w = state.board.width
		val h = state.board.height
		val optState = OptimizedState(self, state)
		val matrix = Matrix(w, h)
	}
}

class OptimizedState(self: Snake, state: GameState)
{
	val fruits: Set<Position> = HashSet<Position>(state.fruits)
	val walls: Set<Position> = HashSet<Position>(state.walls)
	val snakes: Set<Position> = allSnakePositions(state.snakes)
	val liveSnakeHeads: Set<Position> = allLiveSnakeHeadPositions(state.snakes, self)

	fun nonEmptyPositions(): Sequence<Position>
	{
		return sequenceOf(fruits.asSequence(), walls.asSequence(), snakes.asSequence(), liveSnakeHeads.asSequence())
				.flatten()
	}
}

fun populate(matrix: Matrix, state: OptimizedState)
{
	state.nonEmptyPositions()
			.forEach { matrix[it.x, it.y] = scoreForSquare(it.x, it.y, state) }
}

data class Matrix(private val _width: Int,
                  private val _height: Int,
                  private val data: Array<Array<Byte>> = Array(_width) { Array(_height) { 0.toByte() } }): Comparable<Matrix>
{
	val width: Int = data.size
	val height: Int = data[0].size
	val size: Int = width * height

	operator fun get(p: Position): Byte = get(p.x, p.y)

	operator fun get(x: Int, y: Int): Byte
	{
		checkBounds(x, y)
		return data[x][y]
	}

	operator fun set(p: Position, score: Byte) = set(p.x, p.y, score)

	operator fun set(x: Int, y: Int, score: Byte)
	{
		checkBounds(x, y)
		data[x][y] = score
	}

	private inline fun checkBounds(x: Int, y: Int)
	{
		if(x >= width || x < 0)
			throw IndexOutOfBoundsException("$x >= $width")
		if(y >= height || y < 0)
			throw IndexOutOfBoundsException("$y >= $height")
	}

	fun asSequnce(): Sequence<Byte>
	{
		generateSequence(0.toByte())
		{
			data.asSequence()
		}
	}

	override fun compareTo(other: Matrix): Int
	{

	}

	fun divide(): List<Matrix>
	{
		if(size == 1)
			return listOf(this)

		val w0 = width/2
		val h0 = height/2

		val a0 = data.sliceArray(0..w0).sliceArray(0..h0)
		val a1 = data.sliceArray(0..w0).sliceArray(h0..height)
		val a2 = data.sliceArray(w0..width).sliceArray(0..h0)
		val a3 = data.sliceArray(w0..width).sliceArray(h0..height)

		val m0 = copy(data = a0)
		val m1 = copy(data = a1)
		val m2 = copy(data = a2)
		val m3 = copy(data = a3)

		return listOf(m0, m1, m2, m3)
	}
}

fun allSnakePositions(snakes: Set<Snake>): Set<Position>
{
	return snakes.parallelStream()
			.map { it.segments }
			.flatMap { it.stream() }
			.collect(toSet())
}

fun allLiveSnakeHeadPositions(snakes: Set<Snake>, self: Snake): Set<Position>
{
	return snakes.stream()
			.filter { it.headPosition != self.headPosition }
			.map { it.headPosition }
			.collect(toSet())
}

enum class SquareObject(val score: Byte)
{
	FRUIT(5),
	EMPTY(0),
	WALL(-1),
	SNAKE(-1),
	ALIVE_ENEMY_SNAKE_HEAD(-10)
}

fun scoreForSquare(x: Int, y: Int, state: OptimizedState): Byte
{
	val p = Position(x, y)
	val obj: SquareObject = objectInPosition(p, state)
	return obj.score
}

fun objectInPosition(p: Position, state: OptimizedState): SquareObject
{
	return when(p)
	{
		in state.liveSnakeHeads -> SquareObject.ALIVE_ENEMY_SNAKE_HEAD
		in state.snakes -> SquareObject.SNAKE
		in state.walls -> SquareObject.WALL
		in state.fruits -> SquareObject.FRUIT
		else -> SquareObject.EMPTY
	}
}


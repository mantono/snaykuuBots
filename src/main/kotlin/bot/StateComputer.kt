package bot

import gameLogic.GameState
import gameLogic.Position
import gameLogic.Snake
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashSet

class OptimizedState(self: Snake, state: GameState)
{
	val state: GameState = state
	val fruits: Set<Position> = HashSet<Position>(state.fruits)
	val walls: Set<Position> = HashSet<Position>(state.walls)
	val aliveEnemySnakes: Set<Position> = allAliveEnemySnakePositions(state.snakes, self.segments)
	val deadEnemySnakes: Set<Position> = allDeadEnemySnakePositions(state.snakes)
	val liveSnakeHeads: Set<Position> = allLiveSnakeHeadPositions(state.snakes, self)
	val ownSnake: Set<Position> = HashSet(self.segments)

	fun boardCongestion(): Float
	{
		val occupied = (aliveEnemySnakes.size.toFloat() + deadEnemySnakes.size + ownSnake.size)
		return occupied / playableSize()
	}

	fun playableSize(): Int = (state.board.width - 2) * (state.board.height - 2)

	fun nonEmptyPositions(): Sequence<Position>
	{
		return sequenceOf(fruits.asSequence(), walls.asSequence(),
				aliveEnemySnakes.asSequence(), deadEnemySnakes.asSequence(),
				liveSnakeHeads.asSequence(), ownSnake.asSequence())
				.flatten()
	}
}

fun populate(matrix: ByteMatrix, state: OptimizedState)
{
	state.nonEmptyPositions()
			.forEach { matrix[it.x, it.y] = scoreForSquare(it.x, it.y, state) }
}

fun allAliveEnemySnakePositions(snakes: Set<Snake>, self: LinkedList<Position>): Set<Position>
{
	return snakes.parallelStream()
			.filter { !it.isDead }
			.map { it.segments }
			.flatMap { it.stream() }
			.collect(Collectors.toSet())
			.apply { removeAll(self) }
}

fun allDeadEnemySnakePositions(snakes: Set<Snake>): Set<Position>
{
	return snakes.parallelStream()
			.filter { it.isDead }
			.map { it.segments }
			.flatMap { it.stream() }
			.collect(Collectors.toSet())
}

fun allLiveSnakeHeadPositions(snakes: Set<Snake>, self: Snake): Set<Position>
{
	return snakes.stream()
			.filter { it.headPosition != self.headPosition }
			.map { it.headPosition }
			.collect(Collectors.toSet())
}

enum class SquareObject(val score: Byte)
{
	FRUIT(50),
	EMPTY(0),
	WALL(-1),
	ALIVE_ENEMY_SNAKE(-3),
	DEAD_ENEMY_SNAKE(-2),
	OWN_SNAKE(-1),
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
		in state.aliveEnemySnakes -> SquareObject.ALIVE_ENEMY_SNAKE
		in state.deadEnemySnakes -> SquareObject.DEAD_ENEMY_SNAKE
		in state.ownSnake -> SquareObject.OWN_SNAKE
		in state.walls -> SquareObject.WALL
		in state.fruits -> SquareObject.FRUIT
		else -> SquareObject.EMPTY
	}
}
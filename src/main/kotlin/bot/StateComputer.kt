package bot

import gameLogic.GameState
import gameLogic.Position
import gameLogic.Snake
import java.util.HashSet
import java.util.stream.Collectors

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

fun populate(matrix: ByteMatrix, state: OptimizedState)
{
	state.nonEmptyPositions()
			.forEach { matrix[it.x, it.y] = scoreForSquare(it.x, it.y, state) }
}

fun allSnakePositions(snakes: Set<Snake>): Set<Position>
{
	return snakes.parallelStream()
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
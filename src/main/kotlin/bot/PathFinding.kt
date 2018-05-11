package bot

import gameLogic.Direction
import gameLogic.Position
import gameLogic.Snake
import java.util.*

fun initialDirection(g: Graph, fruits: Queue<Position>, state: BoardState, self: Snake): Direction
{
	val from: Position = self.headPosition

	val source: Sequence<Position> = fruits.asSequence() +
			closestSquares(self, state).onEach {if(state.isLethal(it)) println("lethal: $it")}.filter { !state.isLethal(it) }

	return source.asSequence()
			.map { g.bfsPath(from, it) }
			.filterNotNull()
			.filter { it.isNotEmpty() }
			.onEach { System.out.println("Path: $it") }
			.map { it.first }
			.filter { !isOppositeDirections(it, self.currentDirection) }
			.first()
}

fun closestSquares(self: Snake, state: BoardState): Sequence<Position>
{
	val current = self.headPosition
	val left = self.currentDirection.turnLeft()
	val forward = self.currentDirection
	val right = self.currentDirection.turnRight()

	val leftPos = current going left
	val forwardPos = current going forward
	val rightPos = current going right

	return when(self.currentDirection)
	{
		Direction.NORTH -> sequenceOf(rightPos, leftPos, forwardPos)
		Direction.EAST -> sequenceOf(rightPos, forwardPos, leftPos)
		Direction.WEST -> sequenceOf(leftPos, forwardPos, rightPos)
		Direction.SOUTH -> sequenceOf(leftPos, rightPos, forwardPos)
	}
}

infix fun Position.going(d: Direction): Position = d.calculateNextPosition(this)
fun isOppositeDirections(dir1: Direction, dir2: Direction): Boolean = dir1.opposite() == dir2
fun Direction.opposite(): Direction = turnLeft().turnLeft()
fun neighboursOf(p: Position): List<Position> = Direction.values().map {p going it}
fun requiredSpace(snakeLength: Int, growthFrequency: Int): Int = snakeLength + (snakeLength / growthFrequency) + 1

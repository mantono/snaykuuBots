package bot

import gameLogic.Direction
import gameLogic.Position
import gameLogic.Snake
import java.util.*

fun initialDirection(g: Graph, fruits: Queue<Position>, state: BoardState, self: Snake): Direction
{
	val from: Position = self.headPosition

	if(fruits.isEmpty())
		fruits.offer(randomSafePosition(state))

	return fruits.asSequence()
			.map { g.bfsPath(from, it) }
			.filter { it.isNotEmpty() }
			.flatMap { it.asSequence() }
			.filter { !isOppositeDirections(it, self.currentDirection) }
			.first()
}

infix fun Position.going(d: Direction): Position = d.calculateNextPosition(this)
fun isOppositeDirections(dir1: Direction, dir2: Direction): Boolean = dir1.opposite() == dir2
fun  Direction.opposite(): Direction = turnLeft().turnLeft()
fun neighboursOf(p: Position): List<Position> = Direction.values().map {p going it}
fun requiredSpace(snakeLength: Int, growthFrequency: Int): Int = snakeLength + (snakeLength / growthFrequency) + 1

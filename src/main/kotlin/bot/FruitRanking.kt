package bot

import gameLogic.Position
import java.util.*

fun rankFruits(state: BoardState, ownPosition: Position): Queue<Position>
{
	val sorted: List<Position> = state.fruits
			.filter { !state.isDangerous(it) }
			.map {
				val score: Int = scoreFruit(it, state, ownPosition)
				score to it
			}
			.sortedByDescending { it.first }
			.map { it.second }
			.toList()

	return ArrayDeque<Position>(sorted)
}

fun scoreFruit(fruit: Position, state: BoardState, ownPosition: Position): Int
{
	val heads = livingEnemiesHeadPos(state.state.snakes, ownPosition)

	return heads.map { scoreFunc(it, ownPosition, fruit) }
			.sortedDescending()
			.first()
}

private fun scoreFunc(otherSnake: Position, self: Position, fruit: Position): Int
{
	val hisDistance: Int = otherSnake.getDistanceTo(fruit)
	val myDistance: Int = self.getDistanceTo(fruit)
	return hisDistance - (myDistance * 2)
}
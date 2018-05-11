package bot

import gameLogic.Position

private const val CERTAIN_DEATH: Byte = Byte.MIN_VALUE

enum class Reward(val score: Byte)
{
	FRUIT(5),
	EMPTY(1),
	TIGHT(-16),
	ADJACENT_TO_ALIVE_SNAKE_HEAD(-64),
	ALIVE_SNAKE_TAIL(-64),
	WALL(CERTAIN_DEATH),
	SNAKE(CERTAIN_DEATH),
	TRAP(CERTAIN_DEATH)
}

fun categorize(board: BoardState, pos: Position): Reward = when(pos)
{
	in board.walls -> Reward.WALL
	in board.snakesPositions -> Reward.SNAKE
	board::isTrap -> Reward.TRAP
	in board.aliveSnakeTails -> Reward.ALIVE_SNAKE_TAIL
	in board.highRiskPositions -> Reward.ADJACENT_TO_ALIVE_SNAKE_HEAD
	board::isTight -> Reward.TIGHT
	in board.fruits -> Reward.FRUIT
	else -> Reward.EMPTY
}
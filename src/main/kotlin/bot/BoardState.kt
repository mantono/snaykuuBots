package bot

import gameLogic.*
import java.util.*
import java.util.stream.Collectors

class BoardState(state: GameState, self: Snake)
{
	val self: Snake = self
	val state: GameState = state
	val walls: Set<Position> = HashSet(state.walls)
	val lethalPositions: Set<Position> = lethalPositions(state)
	val highRiskPositions: Set<Position> = calculateHighRiskPositions(state, self)
	val snakesPositions: Set<Position> = snakePositions(state)
	val fruits: Set<Position> = HashSet(state.fruits)

	fun isLethal(position: Position): Boolean = position in lethalPositions
	fun isHighRisk(position: Position): Boolean = position in highRiskPositions
	fun isDangerous(position: Position): Boolean = isLethal(position) || isHighRisk(position)
	fun hasFruit(position: Position): Boolean = position in fruits

	fun score(position: Position): Int
	{
		if(isTrap(position))
			return -25
		if(isHighRisk(position))
			return -5
		if(hasFruit(position))
			return 5
		return 1
	}

	private fun isTrap(position: Position): Boolean
	{
		val lethalNeighbours: Int = Direction.values()
				.map { position going it }
				.filter(this::isLethal)
				.filter { it != self.headPosition }
				.count()

		return lethalNeighbours >= 3
	}
}

fun lethalPositions(state: GameState): Set<Position>
{
	return HashSet<Position>(totalSize(state.board)).apply {
		addAll(snakePositions(state))
		addAll(state.walls)
	}
}

fun snakePositions(state: GameState): Set<Position>
{
	return state.snakes
			.map { it.segments }
			.flatMap { it }
			.toSet()
}

fun livingEnemiesHeadPos(snakes: Set<Snake>, ownHead: Position): Set<Position>
{
	return snakes.stream()
			.filter { !it.isDead }
			.filter { it.headPosition != ownHead }
			.map { it.headPosition }
			.collect(Collectors.toSet())
}

fun totalSize(b: Board): Int = b.width * b.height
fun playableSize(b: Board): Int = (b.width - 2) * (b.height - 2)
fun congestion(state: BoardState): Float = occupiedSize(state).toFloat() / playableSize(state.state.board)
fun occupiedSize(state: BoardState): Int = state.walls.size + state.snakesPositions.size

fun calculateHighRiskPositions(state: GameState, self: Snake): Set<Position>
{
	val otherSnakes = state.snakes - self
	val positions = HashSet<Position>(otherSnakes.size * 4)
	for (snake in otherSnakes)
	{
		val head = snake.headPosition
		val direction = snake.currentDirection

		val forward = direction.calculateNextPosition(head)
		val left = direction.turnLeft().calculateNextPosition(head)
		val right = direction.turnRight().calculateNextPosition(head)

		positions.add(forward)
		positions.add(left)
		positions.add(right)
	}

	return positions
}

fun randomSafePosition(state: BoardState): Position
{
	return randomPosition(state)
			.filter(state::isLethal)
			.filter(state::isHighRisk)
			.first()
}

fun randomPosition(state: BoardState): Sequence<Position>
{
	return generateSequence()
	{
		val rand = Random()
		val x = rand.nextInt(state.state.board.width)
		val y = rand.nextInt(state.state.board.height)
		Position(x, y)
	}
}
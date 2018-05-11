package bot

import gameLogic.Board
import gameLogic.Direction
import gameLogic.GameState
import gameLogic.Position
import gameLogic.Snake
import java.util.*

class BoardState(val state: GameState, val self: Snake)
{
	val walls: Set<Position> = HashSet(state.walls)
	val lethalPositions: Set<Position> by lazy { lethalPositions(state, self) }
	val highRiskPositions: Set<Position> by lazy { calculateHighRiskPositions(state, self) }
	val snakesPositions: Set<Position> = snakePositions(state)
	val fruits: Set<Position> = state.fruits.toSet()
	val killingPositions: Set<Position> by lazy { killingOpportunities(state, self) }
	val aliveSnakeTails: Set<Position> by lazy { livingEnemiesTailPos(state.snakes, self.tailPosition) }

	fun isLethal(position: Position): Boolean = position in lethalPositions
	fun isHighRisk(position: Position): Boolean = position in highRiskPositions
	fun isDangerous(position: Position): Boolean = isLethal(position) || isHighRisk(position)
	fun hasFruit(position: Position): Boolean = position in fruits
	fun isTrap(position: Position): Boolean = numberOfLethalNeighbours(position) >= 3
	fun isKillingOpportunity(position: Position): Boolean = position in killingPositions
	fun nonEmptyPositions(): Sequence<Position> = walls.asSequence() + snakesPositions.asSequence() + fruits.asSequence()

	fun score(position: Position): Byte = when(position)
	{
		this::isTrap -> -50
		this::isHighRisk -> -5
		this::isTight -> 1
		this::hasFruit -> 10
		this::isKillingOpportunity -> 20
		else -> 2
	}

	fun isTight(position: Position): Boolean
	{
		val up: Position = position going Direction.NORTH
		val down: Position = position going Direction.SOUTH
		if(isLethal(up) && isLethal(down))
			return true

		val left: Position = position going Direction.WEST
		val right: Position = position going Direction.EAST
		return isDangerous(left) && isLethal(right)
	}

	fun numberOfLethalNeighbours(position: Position): Int
	{
		return Direction.values()
				.map { position going it }
				.filter(this::isLethal)
				.filter { it != self.headPosition }
				.count()
	}
}

fun lethalPositions(state: GameState, self: Snake): Set<Position>
{
	return HashSet<Position>(totalSize(state.board)).apply {
		addAll(snakePositions(state))
		addAll(state.walls)

		if(self.segments.size == 1)
		{
			val oppositeDirection = self.currentDirection.opposite()
			val behindMe = self.headPosition going oppositeDirection
			add(behindMe)
		}
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
	return snakes.asSequence()
			.filter { !it.isDead }
			.filter { it.headPosition != ownHead }
			.map { it.headPosition }
			.toSet()
}

fun livingEnemiesTailPos(snakes: Set<Snake>, ownTail: Position): Set<Position>
{
	return snakes.asSequence()
			.filter { !it.isDead }
			.filter { it.tailPosition != ownTail }
			.map { it.tailPosition }
			.toSet()
}

fun killingOpportunities(state: GameState, self: Snake): Set<Position>
{
	val left = self.headPosition going self.currentDirection.turnLeft()
	val right = self.headPosition going self.currentDirection.turnLeft()

	return state.snakes.asSequence()
			.filter { it != self }
			.filter { !it.isDead }
			.filter { it.currentDirection == self.currentDirection }
			.map { it.headPosition going it.currentDirection going it.currentDirection }
			.filter { isValidPosition(state.board, it) }
			.filter { it == left || it == right }
			.toSet()
}

fun isValidPosition(board: Board, position: Position): Boolean = isValidX(board, position) && isValidY(board, position)
fun isValidX(board: Board, position: Position): Boolean = position.x >= 0 && position.x < board.width
fun isValidY(board: Board, position: Position): Boolean = position.y >= 0 && position.y < board.height

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
		if(snake.isDead)
			continue

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

fun randomSafePosition(state: BoardState): Sequence<Position>
{
	return randomPosition(state.state.board.width, state.state.board.height)
			.filter{ !state.isLethal(it) }
			.filter{ !state.isHighRisk(it) }
}

fun randomPosition(width: Int, height: Int): Sequence<Position>
{
	val rand = Random()
	return generateSequence(Position(rand.nextInt(width), rand.nextInt(height))) { Position(rand.nextInt(width), rand.nextInt(height)) }
}
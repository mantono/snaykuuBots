package bot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import gameLogic.Board;
import gameLogic.Brain;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;
import gameLogic.Square;

public class FruitFinder implements Brain
{
	private static final int MAX_DEPTH = 32;
	private SortedMap<Double, Position> fruitRanking;
	private Snake self;
	private GameState state;
	private long maximumThinkingTime;
	private long startTime;
	private Direction fruitDirection;
	private int growthFrequency;
	private int gameTurns = 0;

	@Override
	public Direction getNextMove(Snake yourSnake, GameState gameState)
	{
		gameTurns++;
		this.self = yourSnake;
		this.state = gameState;
		this.growthFrequency = state.getMetadata().getGrowthFrequency();
		this.startTime = System.currentTimeMillis();
		this.maximumThinkingTime = gameState.getMetadata().getMaximumThinkingTime();
		this.fruitRanking = rankFruits(gameState.getFruits());
		this.fruitDirection = directionToHighestRankingFruit();

		return getDesicionTreeDirection();
	}

	private long computationTimeLeft()
	{
		final long elpasedTime = System.currentTimeMillis() - startTime;
		return maximumThinkingTime - elpasedTime;
	}

	private Direction getDesicionTreeDirection()
	{
		final Direction currentDirection = self.getCurrentDirection();
		final Position currentPosition = self.getHeadPosition();

		double scoreForward, scoreLeft, scoreRight;
		scoreForward = scoreRight = scoreLeft = 0;

		LinkedList<Position> snake = self.getSegments();

		if(fruitDirection.equals(currentDirection.turnLeft()))
		{
			scoreLeft = getScore(currentPosition, snake, currentDirection.turnLeft(), 0, 1);
			scoreRight = getScore(currentPosition, snake, currentDirection.turnRight(), 0, 1);
			scoreForward = getScore(currentPosition, snake, currentDirection, 0, 1);
		}
		else if(fruitDirection.equals(currentDirection.turnRight()))
		{
			scoreRight = getScore(currentPosition, snake, currentDirection.turnRight(), 0, 1);
			scoreLeft = getScore(currentPosition, snake, currentDirection.turnLeft(), 0, 1);
			scoreForward = getScore(currentPosition, snake, currentDirection, 0, 1);
		}
		else
		{
			scoreForward = getScore(currentPosition, snake, currentDirection, 0, 1);
			scoreRight = getScore(currentPosition, snake, currentDirection.turnRight(), 0, 1);
			scoreLeft = getScore(currentPosition, snake, currentDirection.turnLeft(), 0, 1);
		}

		if(scoreLeft == scoreRight && scoreRight == scoreForward)
			return fruitDirection;

		if(scoreLeft >= scoreForward && scoreLeft >= scoreRight)
			return currentDirection.turnLeft();
		if(scoreRight >= scoreForward && scoreLeft <= scoreRight)
			return currentDirection.turnRight();
		return currentDirection;
	}

	private Direction directionToHighestRankingFruit()
	{
		if(fruitRanking.isEmpty() || fruitRanking.lastKey() < 0)
			return self.getCurrentDirection();

		final double bestScore = fruitRanking.lastKey();
		final Position bestFruit = fruitRanking.get(bestScore);
		return getDirectionTo(bestFruit);
	}

	private double getScore(Position currentPosition, LinkedList<Position> snake, Direction currentDirection, double score, final int depth)
	{
		if(computationTimeLeft() < 10 || depth == MAX_DEPTH)
			return score;

		final Position nextPosition = currentDirection.calculateNextPosition(currentPosition);

		if(state.getBoard().isLethal(nextPosition))
			return score;
		if(snake.contains(nextPosition))
			return score;
		else
			score++;

		if(state.getBoard().getSquare(nextPosition).hasFruit())
			score += 200 / depth;

		snake.addFirst(nextPosition);
		if(!snakeWillGrow())
			snake.removeLast();

		final double scoreForward = getScore(nextPosition, new LinkedList<Position>(snake), currentDirection, score, depth+1);
		final double scoreLeft = getScore(nextPosition, new LinkedList<Position>(snake), currentDirection.turnLeft(), score, depth+1);
		final double scoreRight = getScore(nextPosition, new LinkedList<Position>(snake), currentDirection.turnRight(), score, depth+1);

		return Math.max(scoreForward, Math.max(scoreLeft, scoreRight));
	}
	
	private boolean snakeWillGrow()
	{
		return gameTurns % growthFrequency == 0;
	}

	private boolean isVisitedPosition(List<Position> visited, Position nextPosition)
	{
		return visited.contains(nextPosition);
	}

	private SortedMap<Double, Position> rankFruits(ArrayList<Position> fruits)
	{
		final int boxRadius = 3;

		SortedMap<Double, Position> ranking = new TreeMap<Double, Position>();
		final Board board = state.getBoard();
		for(Position fruit : fruits)
		{
			double score = 0;
			final int xMin = fruit.getX() - boxRadius;
			final int xMax = fruit.getX() + boxRadius;
			final int yMin = fruit.getY() - boxRadius;
			final int yMax = fruit.getY() + boxRadius;

			for(int x = xMin; x <= xMax; x++)
			{
				if(x < 0 || x >= board.getWidth())
					continue;
				for(int y = yMin; y <= yMax; y++)
				{
					if(y < 0 || y >= board.getHeight())
						continue;

					final Position position = new Position(x, y);
					Square square = board.getSquare(position);

					if(square.hasFruit())
						score += 25;
					else if(square.hasWall())
						score--;
					else if(containsSnakeHeads(position))
						score -= 7;
					else if(square.hasSnake())
						score -= 5;
				}
			}

			final double distanceScore = getDistanceScore(fruit);
			score += distanceScore;
			ranking.put(score, fruit);
		}

		return ranking;
	}

	private boolean containsSnakeHeads(Position position)
	{
		Set<Snake> snakes = state.getSnakes();
		snakes.remove(self);

		for(Snake snake : snakes)
			if(snake.getSegments().contains(position))
				return true;

		return false;
	}

	private double getDistanceScore(Position position)
	{
		final double distance = self.getHeadPosition().getDistanceTo(position);
		return 100 / distance;
	}

	private Direction getDirectionTo(Position position)
	{
		List<Direction> directions = GameState.getRelativeDirections(self.getHeadPosition(), position);
		if(directions.isEmpty())
			return self.getCurrentDirection();
		Direction directionToPosition = directions.get(0);
		if(directionIsOppositeToCurrentDirection(directionToPosition) && directions.size() == 2)
			directionToPosition = directions.get(1);
		else if(directionIsOppositeToCurrentDirection(directionToPosition) && directions.size() == 1)
			directionToPosition = directionToPosition.turnRight();
		return directionToPosition;
	}

	private boolean directionIsOppositeToCurrentDirection(Direction direction)
	{
		final Position currentVector = self.getCurrentDirection().getDirectionVector();
		final Position newVector = direction.getDirectionVector();
		final int x = currentVector.getX() + newVector.getX();
		final int y = currentVector.getY() + newVector.getY();

		return x == 0 && y == 0;
	}

}

package bot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import gameLogic.Board;
import gameLogic.Brain;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;
import gameLogic.Square;

public class FruitFinder implements Brain
{
	private static final int MAX_DEPTH = 64;
	private SortedMap<Double, Position> fruitRanking;
	private Snake self;
	private GameState state;
	private long maximumThinkingTime;
	private long startTime;
	private List<Direction> fruitDirection;
	private int growthFrequency;
	private int gameTurns;
	
	public FruitFinder()
	{
		gameTurns = 0;
		System.out.println("FruitFinder decision tree max depth: " + MAX_DEPTH);
	}

	@Override
	public Direction getNextMove(Snake yourSnake, GameState gameState)
	{
		this.startTime = System.currentTimeMillis();
		gameTurns++;
		this.self = yourSnake;
		this.state = gameState;
		this.growthFrequency = state.getMetadata().getGrowthFrequency();
		this.maximumThinkingTime = gameState.getMetadata().getMaximumThinkingTime();
		this.fruitRanking = rankFruits(gameState.getFruits());
		this.fruitDirection = directionToHighestRankingFruit(self.getHeadPosition(), self.getCurrentDirection());
		
		if(!fruitRanking.isEmpty())
			System.out.println("Fruit score: " + fruitRanking.lastKey() + " --> " + fruitRanking.get(fruitRanking.lastKey()));

		return getDesicionTreeDirection();
	}
	
	private long thinkingTimeElapsed()
	{
		return System.currentTimeMillis() - startTime;
	}

	private long thinkingTimeLeft()
	{
		final long elpasedTime = thinkingTimeElapsed();
		return maximumThinkingTime - elpasedTime;
	}

	private Direction getDesicionTreeDirection()
	{
		final Position currentPosition = self.getHeadPosition();

		LinkedList<Position> snake = self.getSegments();
		Map<Snake, LinkedList<Position>> snakes = createSnakeMap();
		snakes.remove(self);
		SortedMap<Double, Direction> directionRecord = new TreeMap<Double, Direction>();
		
		for(Direction directionToFruit : fruitDirection)
		{
			final double score = getScore(currentPosition, new LinkedList<Position>(snake), snakes, state.getFruits(), directionToFruit, 0, 1);
			directionRecord.put(score, directionToFruit);
		}

		final double bestScore = directionRecord.lastKey();
		System.out.println(bestScore);
		
		return directionRecord.get(bestScore);
	}

	private Map<Snake, LinkedList<Position>> createSnakeMap()
	{
		Map<Snake, LinkedList<Position>> snakes = new HashMap<Snake, LinkedList<Position>>(state.getSnakes().size());
		for(Snake snake : state.getSnakes())
			snakes.put(snake, new LinkedList<Position>(snake.getSegments()));
		
		return snakes;
	}

	private Position getBestFruit()
	{
		final double bestScore = fruitRanking.lastKey();
		return fruitRanking.get(bestScore);
	}

	private double getScore(Position currentPosition, LinkedList<Position> snake, Map<Snake, LinkedList<Position>> snakes, Collection<Position> fruits, Direction currentDirection, double score, final int depth)
	{
		if(thinkingTimeLeft() < 10 || depth == MAX_DEPTH)
			return score;

		currentPosition = currentDirection.calculateNextPosition(currentPosition);

		if(containsSnake(snakes, currentPosition) || containsWall(currentPosition))
			return score;
		if(snake.contains(currentPosition))
			return score;
		else
			score++;

		if(fruits.contains(currentPosition))
		{
			score += 5;
			fruits.remove(currentPosition);
		}

		moveOwnSnake(snake, currentPosition);
		snakes = moveOtherSnakes(snakes, depth);
		
		List<Direction> orderOfDirections = directionToHighestRankingFruit(currentPosition, currentDirection);
		SortedSet<Double> scores = new TreeSet<Double>();
		
		for(Direction direction : orderOfDirections)
		{
			final double scoreOfDirection = getScore(currentPosition, new LinkedList<Position>(snake), snakes, new HashSet<Position>(fruits), direction, score, depth+1);
			scores.add(scoreOfDirection);
		}

		return scores.last();
	}
	
	private boolean containsWall(Position currentPosition)
	{
		return state.getBoard().hasWall(currentPosition);
	}

	private boolean containsSnake(Map<Snake, LinkedList<Position>> snakes, Position currentPosition)
	{
		for(Collection<Position> opponent : snakes.values())
			if(opponent.contains(currentPosition))
				return true;
		
		return false;
	}

	private Map<Snake, LinkedList<Position>> moveOtherSnakes(Map<Snake, LinkedList<Position>> snakes, int depth)
	{
		if(depth > 4)
			return snakes;
		
		Map<Snake, LinkedList<Position>> movedSnakes = new HashMap<Snake, LinkedList<Position>>();
		
		for(Entry<Snake, LinkedList<Position>> snakeEntry : snakes.entrySet())
		{
			final Snake snake = snakeEntry.getKey();
			LinkedList<Position> segments = new LinkedList<Position>(snakeEntry.getValue());
			
			movedSnakes.put(snake, segments);
			
			if(snake.isDead())
				continue;
			
			final Position head = segments.getFirst();
			final Position next = snake.getCurrentDirection().calculateNextPosition(head);
			segments.addFirst(next);
			if(!snakeWillGrow())
				segments.removeLast();
		}
		
		return movedSnakes;
	}

	private void moveOwnSnake(LinkedList<Position> snake, Position currentPosition)
	{
		
		snake.addFirst(currentPosition);
		if(!snakeWillGrow())
			snake.removeLast();
	}

	private List<Direction> directionToHighestRankingFruit(Position from, Direction currentDirection)
	{
		List<Direction> directions = new LinkedList<Direction>();
		if(!fruitRanking.isEmpty())
			directions = GameState.getRelativeDirections(from, getBestFruit());
		
		for(Direction direction : Direction.values())
		{
			if(!directions.contains(direction) && !isOppositeOf(direction, currentDirection))
				directions.add(direction);
		}
		
		return directions;
	}

	private boolean snakeWillGrow()
	{
		return gameTurns % growthFrequency == 0;
	}

	private SortedMap<Double, Position> rankFruits(ArrayList<Position> fruits)
	{
		final int boxRadius = 4;

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
						score += 50;
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
		return 3000 / (distance*2);
	}
	
	private boolean isOppositeOf(final Direction direction1, final Direction direction2)
	{
		final Position vector1 = direction1.getDirectionVector();
		final Position vector2 = direction2.getDirectionVector();
		final int x = vector1.getX() + vector2.getX();
		final int y = vector1.getY() + vector2.getY();
		
		return x == 0 && y == 0;
	}

}

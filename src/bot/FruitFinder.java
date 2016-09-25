package bot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
	private static final int MAX_DEPTH = 8192;
	private static final short VERSION = 8;
	private SortedMap<Double, Position> fruitRanking;
	private Snake self;
	private GameState state;
	private long maximumThinkingTime;
	private long startTime;
	private int growthFrequency;
	private int gameTurns;
	private int boardArea;

	public FruitFinder()
	{
		gameTurns = 0;
		System.out.println("FruitFinder version " + VERSION);
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
		this.boardArea = getBoardArea();

		Deque<Direction> path = getPath(self.getHeadPosition());
		if(path.isEmpty())
			return self.getCurrentDirection();
		System.out.println("Turning to " + path.peekLast());
		return path.pollLast();
	}

	private Position getNextTarget()
	{
		Position target;
		if(fruitRanking.isEmpty())
			target = getSafePosition();
		else
			target = getBestFruit();

		while(isTrap(target))
		{
			System.out.println(target + " is a DANGEROUS position, skipping....");
			fruitRanking.remove(fruitRanking.lastKey());
			if(fruitRanking.isEmpty())
				break;
			else
				target = getBestFruit();
		}

		return target;
	}

	private boolean isTrap(Position to)
	{
		return isVerticalDanger(to) || isHorizontalDanger(to);
	}

	private boolean isHorizontalDanger(Position to)
	{
		final int x = to.getX();
		final int y = to.getY();
		for(int xi = -1; xi < 2; xi++)
		{
			for(int yi = -1; yi < 2; yi += 2)
			{
				final Position pos = new Position(x + xi, y + yi);
				if(!state.getBoard().isLethal(pos))
					return false;
			}
		}

		return true;
	}

	private boolean isVerticalDanger(Position to)
	{
		final int x = to.getX();
		final int y = to.getY();
		for(int xi = -1; xi < 2; xi += 2)
		{
			for(int yi = -1; yi < 2; yi++)
			{
				final Position pos = new Position(x + xi, y + yi);
				if(!state.getBoard().isLethal(pos))
					return false;
			}
		}

		return true;
	}

	private Deque<Direction> getPath(Position from)
	{
		Position to = getNextTarget();
		System.out.println("Trying to reach :" + to);

		final int initialSize = state.getBoard().getWidth() * state.getBoard().getHeight();
		final Deque<Direction> directionStack = new ArrayDeque<Direction>(initialSize);
		final Deque<Position> positionStack = new ArrayDeque<Position>(MAX_DEPTH);
		final Set<Position> visitedPositions = new HashSet<Position>(self.getSegments());
		Position position = from;
		positionStack.push(from);
		Deque<Direction> tallestStack = new ArrayDeque<Direction>();

		int iterations = 0;

		while(!reachedComputationCapacity(directionStack.size()))
		{
			if(position.equals(to))
			{
				to = getNextTarget();
			}
			visitedPositions.add(position);
			Map<Direction, Integer> distances = new HashMap<Direction, Integer>();
			Set<Position> highRiskPositions = calculateHighRiskPositions();
			for(Direction dir : Direction.values())
			{
				final Position next = dir.calculateNextPosition(position);
				final boolean isLethal = isLethal(next);
				final boolean isHighRisk = highRiskPositions.contains(next);
				final boolean visited = visitedPositions.contains(next);
				if(!isLethal && !isHighRisk && !visited)
				{
					final int distance = next.getDistanceTo(to);
					distances.put(dir, distance);
				}
			}

			Direction direction = getDirectionToTarget(distances);
			if(iterations == 0)
				System.out.println("Direction weighting: " + distances);
			if(direction == null && directionStack.isEmpty())
				break;

			if(direction != null)
			{
				directionStack.push(direction);
				final Position positionInDirection = direction.calculateNextPosition(position);
				positionStack.push(positionInDirection);
			}
			else
			{
				if(directionStack.size() > tallestStack.size())
					tallestStack = new ArrayDeque<Direction>(directionStack);
				directionStack.pop();
				positionStack.pop();
			}
			position = positionStack.peek();
			iterations++;
		}

		if(directionStack.size() > tallestStack.size())
			tallestStack = directionStack;

		Iterator<Direction> dirIter = tallestStack.descendingIterator();
		int i = 0;
		while(dirIter.hasNext() && i < 16)
		{
			System.out.print(dirIter.next() + " ");
			i++;
		}
		System.out.println(" (" + tallestStack.size() + "), iterations; " + iterations);

		return tallestStack;
	}

	private boolean isLethal(Position next)
	{
		final Square square = state.getBoard().getSquare(next);
		return square.hasSnake() || square.hasWall();
	}

	private int getBoardArea()
	{
		final int width = state.getBoard().getWidth();
		final int height = state.getBoard().getWidth();
		final int physicalLimit = width * height;

		return physicalLimit;
	}

	private boolean reachedComputationCapacity(final int stackSize)
	{
		if(thinkingTimeLeft() < 10)
			return true;

		if(MAX_DEPTH < stackSize || boardArea < stackSize)
			return true;

		return false;
	}

	private Direction getDirectionToTarget(Map<Direction, Integer> distances)
	{
		if(distances.isEmpty())
			return null;
		int shortest = Integer.MAX_VALUE;
		Direction bestDirection = null;
		
		for(Entry<Direction, Integer> entry : distances.entrySet())
		{
			if(entry.getValue() < shortest)
			{
				shortest = entry.getValue();
				bestDirection = entry.getKey();
			}
		}
		
		return bestDirection;
	}

	private Set<Position> calculateHighRiskPositions()
	{
		Set<Snake> snakes = state.getSnakes();
		snakes.remove(self);
		Set<Position> positions = new HashSet<Position>(snakes.size() * 4);
		for(Snake snake : snakes)
		{
			final Position head = snake.getHeadPosition();
			final Direction direction = snake.getCurrentDirection();

			final Position forward = direction.calculateNextPosition(head);
			final Position left = direction.turnLeft().calculateNextPosition(head);
			final Position right = direction.turnLeft().calculateNextPosition(head);

			positions.add(forward);
			positions.add(left);
			positions.add(right);
		}

		return positions;
	}

	private Position getSafePosition()
	{
		final Board board = state.getBoard();
		final int width = board.getWidth();
		final int height = board.getHeight();
		final int wStep = width / 6;
		final int hStep = height / 6;
		SortedMap<Integer, Position> safeRank = new TreeMap<Integer, Position>();
		for(int x = 0; x < width; x += wStep)
		{
			for(int y = 0; y < height; y += hStep)
			{
				final Position position = new Position(x, y);
				for(int radius = 1; radius < 8; radius += 2)
				{
					if(x + radius >= board.getWidth() || y + radius >= board.getHeight())
						break;
					if(!board.hasLethalObjectWithinRange(position, radius))
						safeRank.put(radius, position);
					else
						break;
				}
			}
		}

		final int max = safeRank.lastKey();
		return safeRank.get(max);
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

	private Position getBestFruit()
	{
		final double bestScore = fruitRanking.lastKey();
		return fruitRanking.get(bestScore);
	}

	private boolean snakeWillGrow()
	{
		return gameTurns % growthFrequency == 0;
	}

	private SortedMap<Double, Position> rankFruits(ArrayList<Position> fruits)
	{
		SortedMap<Double, Position> ranking = new TreeMap<Double, Position>();
		final Set<Snake> snakes = enemySnakes();

		for(Position fruit : fruits)
		{
			int distance = 0;
			final int ownDistance = self.getHeadPosition().getDistanceTo(fruit);
			for(Snake snake : snakes)
			{
				final Position snakeHead = snake.getHeadPosition();
				int snakeToFruiDistance = snakeHead.getDistanceTo(fruit);
				snakeToFruiDistance *= snakeToFruiDistance;
				distance += snakeToFruiDistance;
			}

			final double avgDistance = distance / snakes.size();
			final double score = avgDistance / Math.pow(ownDistance, 2);

			ranking.put(score, fruit);
		}

		return ranking;
	}

	private Set<Snake> enemySnakes()
	{
		final Set<Snake> snakes = state.getSnakes();
		final Set<Snake> liveSnakes = new HashSet<Snake>(snakes.size());

		for(Snake snake : snakes)
			if(snake != self && !snake.isDead())
				liveSnakes.add(snake);

		return liveSnakes;
	}
}

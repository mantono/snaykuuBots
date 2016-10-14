package bot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import gameLogic.Brain;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;

public class FruitFinder implements Brain
{
	private static final int MAX_DEPTH = 8192;
	private static final short VERSION = 8;
	private Snake self;
	private GameState state;
	private long maximumThinkingTime;
	private long startTime;
	private int boardArea;
	private BoardAnalyzer analyzer;

	public FruitFinder()
	{
		System.out.println("\n\nFruitFinder version " + VERSION);
		System.out.println("FruitFinder decision tree max depth: " + MAX_DEPTH);
	}

	@Override
	public Direction getNextMove(Snake yourSnake, GameState gameState)
	{
		this.startTime = System.currentTimeMillis();
		this.self = yourSnake;
		this.state = gameState;
		this.analyzer = new BoardAnalyzer(gameState, yourSnake);
		this.maximumThinkingTime = gameState.getMetadata().getMaximumThinkingTime();
		this.boardArea = getBoardArea();

		Deque<Direction> path = getPath(self.getHeadPosition());
		if(path.isEmpty())
			return self.getCurrentDirection();
		System.out.println("Turning to " + path.peekLast() + " from " + yourSnake.getHeadPosition() + "\n");
		return path.pollLast();
	}

	private Deque<Direction> getPath(Position from)
	{
		Position to = analyzer.getNextTarget();
		System.out.println("Trying to reach " + to);

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
				to = analyzer.getNextTarget();
			}
			visitedPositions.add(position);
			Map<Direction, Integer> distances = new HashMap<Direction, Integer>();
			
			for(Direction dir : Direction.values())
			{
				final Direction currentDireciton = getCurrentDirection(directionStack);
				if(isOpositeDirections(currentDireciton, dir))
					continue;
				final Position next = dir.calculateNextPosition(position);
				final boolean isLethal = analyzer.isLethal(next);
				final boolean isHighRisk = analyzer.isHighRisk(next);
				final boolean visited = visitedPositions.contains(next);
				if(!isLethal && !visited)
				{
					int distance = next.getDistanceTo(to);
					if(isHighRisk)
						distance += 5;
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

		System.out.println("Stack depth: " + tallestStack.size() + ", positions evaluated; " + iterations);

		return tallestStack;
	}

	private boolean isOpositeDirections(Direction currentDireciton, Direction dir)
	{
		dir = dir.turnLeft().turnLeft();
		return currentDireciton.equals(dir);
	}

	private Direction getCurrentDirection(Deque<Direction> directionStack)
	{
		if(directionStack.isEmpty())
			return self.getCurrentDirection();
		return directionStack.peek();
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

		for(int distance : distances.values())
			if(distance < shortest)
				shortest = distance;

		List<Direction> possibleDirections = new ArrayList<Direction>(4);

		for(Entry<Direction, Integer> entry : distances.entrySet())
			if(entry.getValue() == shortest)
				possibleDirections.add(entry.getKey());

		Random rand = new Random();
		final int choiceOfDirection = rand.nextInt(possibleDirections.size());

		return possibleDirections.get(choiceOfDirection);
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
}

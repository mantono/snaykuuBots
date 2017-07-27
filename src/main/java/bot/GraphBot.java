package bot;

import gameLogic.*;

import java.util.*;
/*
public class GraphBot implements Brain
{
	private final static int MAX_DEPTH = 1024;
	private Snake self;
	private GameState state;
	private Graph graph;
	private BoardState analyzer;
	private long startTime;
	private int lastScore = 0;

	public GraphBot()
	{
		System.out.println();
	}

	@Override
	public Direction getNextMove(Snake yourSnake, GameState gameState)
	{
		this.startTime = System.currentTimeMillis();
		this.self = yourSnake;
		this.state = gameState;
		this.analyzer = new BoardAnalyzer(state, self);
		this.graph = new Graph(analyzer);

		final Direction firstDirection = findBestInitialDirection();
		final Position from = self.getHeadPosition();
		final Direction direction = getBestDirection(firstDirection, from);
		return direction;
	}

	private Direction findBestInitialDirection()
	{
		final int requiredSpace = computeRequiredSpace();
		
		if(lastScore <= requiredSpace)
		{
			System.out.println("Last score ("+lastScore+" was less than needed space for snake ("+requiredSpace+"), will not search for fruit.");
			return self.getCurrentDirection();
		}

		final Position from = self.getHeadPosition();
		Deque<Direction> path = null;
		while(path == null)
		{
			final Position to = analyzer.getNextTarget();
			path = graph.bfsPath(from, to);
		}

		Direction direction = path.peek();
		if(isOpositeDirections(self.getCurrentDirection(), direction))
			direction = direction.turnLeft();
		
		return direction;
	}

	private int computeRequiredSpace()
	{
		final int snakeLength = self.getSegments().size();
		final int growthFrequency = state.getMetadata().getGrowthFrequency();
		final int total = snakeLength + snakeLength / growthFrequency + 1;
		return total;
	}

	private Direction getBestDirection(Direction firstDirection, final Position from)
	{
		SortedMap<Integer, Direction> scores = new TreeMap<Integer, Direction>();
		Set<Position> visited = new HashSet<Position>(128);
		visited.add(from);

		Position next = firstDirection.calculateNextPosition(from);
		int score = getScore(visited, next, 0);
		scores.put(score, firstDirection);

		for(Direction d : Direction.values())
		{
			if(d.equals(firstDirection))
				continue;
			next = d.calculateNextPosition(from);
			score = getScore(visited, next, 0);
			scores.put(score, d);
		}

		final int highScore = scores.lastKey();
		final Direction bestDirection = scores.get(highScore);
		System.out.println(bestDirection + "(" + highScore + ")");

		lastScore = highScore;
		return scores.get(highScore);
	}

	private int getScore(Set<Position> visited, Position current, int score)
	{
		if(visited.contains(current))
			return score;

		if(reachedComputationCapacity(visited.size()))
			return score;

		if(analyzer.isLethal(current))
			return score;

		visited.add(current);

		score += analyzer.getScore(current);

		int highScore = Integer.MIN_VALUE;

		for(Direction d : Direction.values())
		{
			final Position next = d.calculateNextPosition(current);
			final int scoreInDirection = getScore(visited, next, score);
			if(scoreInDirection > highScore)
				highScore = scoreInDirection;
		}
		visited.remove(current);

		return highScore;
	}

	private boolean reachedComputationCapacity(final int pathSize)
	{
		if(thinkingTimeLeft() < 10)
			return true;

		if(pathSize >= MAX_DEPTH)
			return true;

		return false;
	}

	private long thinkingTimeElapsed()
	{
		return System.currentTimeMillis() - startTime;
	}

	private long thinkingTimeLeft()
	{
		final long elpasedTime = thinkingTimeElapsed();
		return state.getMetadata().getMaximumThinkingTime() - elpasedTime;
	}

	private boolean isOpositeDirections(Direction currentDireciton, Direction dir)
	{
		dir = dir.turnLeft().turnLeft();
		return currentDireciton.equals(dir);
	}
}
*/
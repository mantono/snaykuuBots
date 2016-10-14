package bot;

import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import gameLogic.Brain;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;

public class GraphBot implements Brain
{
	private final static int MAX_DEPTH = 256;
	private Snake self;
	private GameState state;
	private Graph graph;
	private BoardAnalyzer analyzer;
	private long startTime;
	
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
		
		final Position from = self.getHeadPosition();
		Deque<Direction> path = null;
		while(path == null)
		{	
			final Position to = analyzer.getNextTarget();
			path = graph.getBfsPath(from, to);
		}
		
		Direction firstDirection = path.peek();
		if(isOpositeDirections(self.getCurrentDirection(), firstDirection))
			firstDirection = firstDirection.turnLeft();
		
		final Direction direction = getBestDirection(firstDirection, from);
		return direction;
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
		System.out.println(bestDirection + "("+highScore+")");
		
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

package bot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
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
	private static final short VERSION = 9;
	private Queue<Position> fruitQueue;
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
		System.out.println("\n\nFruitFinder version " + VERSION);
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
		this.fruitQueue = rankFruits(gameState.getFruits());
		this.boardArea = getBoardArea();

		System.out.println(fruitQueue.size());
		Deque<Direction> path = getPath(self.getHeadPosition(), self.getCurrentDirection());
		System.out.println(path.size());
		Iterator<Direction> iter = path.iterator();
		while(iter.hasNext())
			System.out.print(iter.next() + " ");
		if(path.isEmpty())
			return self.getCurrentDirection();
		System.out.println("Turning to " + path.peek() + " from " + yourSnake.getHeadPosition() + "\n");
		return path.pop();
	}

	private Position getNextTarget()
	{
		if(fruitQueue.isEmpty())
			return getSafePosition();
		return fruitQueue.poll();
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

	private Deque<Direction> getPath(Position from, Direction currentDirection)
	{
		Graph g = new Graph(state.getBoard());
		final Direction oppositeDirection = currentDirection.turnLeft().turnLeft();
		final Position unavailablePosition = oppositeDirection.calculateNextPosition(from);
		g.remove(unavailablePosition);
		
		Deque<Direction> path = null;
		while(path == null)
		{
			Position to = getNextTarget();
			System.out.println("Trying to reach " + to);
			path = g.getPath(from, to);
		}
		return path;

	}

	private boolean isOpositeDirections(Direction currentDireciton, Direction dir)
	{
		dir = dir.turnLeft().turnLeft();
		return currentDireciton.equals(dir);
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

	private Set<Position> calculateHighRiskPositions()
	{
		Set<Snake> snakes = state.getSnakes();
		snakes.remove(self);
		Set<Position> positions = new HashSet<Position>(snakes.size() * 4);
		for(Snake snake : snakes)
		{
			if(snake.isDead())
				continue;
			final Position head = snake.getHeadPosition();
			final Direction direction = snake.getCurrentDirection();

			final Position forward = direction.calculateNextPosition(head);
			final Position left = direction.turnLeft().calculateNextPosition(head);
			final Position right = direction.turnRight().calculateNextPosition(head);

			positions.add(forward);
			positions.add(left);
			positions.add(right);
		}

		return positions;
	}

	private Position getSafePosition()
	{
		Random rand = new Random();
		Position p = null;
		while(p == null || isLethal(p) || isTrap(p))
		{
			final int x = rand.nextInt(state.getBoard().getWidth());
			final int y = rand.nextInt(state.getBoard().getHeight());
			p = new Position(x, y);
		}
		
		return p;
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

	private boolean snakeWillGrow()
	{
		return gameTurns % growthFrequency == 0;
	}

	private Queue<Position> rankFruits(ArrayList<Position> fruits)
	{
		SortedMap<Integer, Position> ranking = new TreeMap<Integer, Position>();
		final Set<Snake> snakes = enemySnakes();

		for(Position fruit : fruits)
		{
			if(isLethal(fruit))
				continue;

			int distanceClosestSnake = Integer.MAX_VALUE;
			final int ownDistance = self.getHeadPosition().getDistanceTo(fruit);
			for(Snake snake : snakes)
			{
				final Position snakeHead = snake.getHeadPosition();
				final int snakeToFruiDistance = snakeHead.getDistanceTo(fruit);
				if(snakeToFruiDistance < distanceClosestSnake)
					distanceClosestSnake = snakeToFruiDistance;
			}

			final int score = distanceClosestSnake - ownDistance;

			ranking.put(score, fruit);
		}

		final Queue<Position> fruitQueue = new ArrayDeque<Position>(fruits.size());
		
		while(!ranking.isEmpty())
		{
			final int bestScore = ranking.lastKey();
			final Position fruit = ranking.remove(bestScore);
			fruitQueue.add(fruit);
		}
		
		return fruitQueue;
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

	/**** Graph data structure for path computation *****/

	class Graph
	{
		private final boolean[][] matrix;

		Graph(Board board)
		{
			this.matrix = new boolean[board.getWidth()][board.getHeight()];

			for(int y = 0; y < board.getHeight(); y++)
			{
				for(int x = 0; x < board.getWidth(); x++)
				{
					final Position pos = new Position(x, y);
					if(!isLethal(pos))
						add(pos);
				}
			}
		}

		private void add(Position pos)
		{
			matrix[pos.getX()][pos.getY()] = true;
		}
		
		private void remove(Position pos)
		{
			matrix[pos.getX()][pos.getY()] = false;
		}

		Deque<Direction> getPath(Position from, Position to)
		{
			final Queue<Position> nodesToCheck = new ArrayDeque<Position>(16);
			final Map<Position, Position> path = new HashMap<Position, Position>(32);

			nodesToCheck.add(from);
			path.put(from, null);
			while(!nodesToCheck.isEmpty())
			{
				Position currentNode = nodesToCheck.poll();
				List<Position> possibleNodes = getConnectingNodesTo(currentNode);
				for(Position node : possibleNodes)
				{
					if(!path.containsKey(node))
					{
						nodesToCheck.add(node);
						path.put(node, currentNode);
					}
				}
				if(currentNode.equals(to))
					return buildPath(path, from, to);
			}
			return null;

		}

		private List<Position> getConnectingNodesTo(Position currentNode)
		{
			final List<Position> edges = new ArrayList<Position>(4);
			for(Direction direction : Direction.values())
			{
				final Position next = direction.calculateNextPosition(currentNode);
				if(has(next))
					edges.add(next);
			}
			return edges;
		}

		private boolean has(Position p)
		{
			return matrix[p.getX()][p.getY()];
		}

		private Deque<Direction> buildPath(Map<Position, Position> visitedNodes, Position start, Position end)
		{
			final Deque<Direction> path = new ArrayDeque<Direction>(visitedNodes.size());
			Position node = end;
			while(node != start)
			{
				final Position comingFrom = visitedNodes.get(node);
				final Direction direction = Direction.getDirectionFromPositionToPosition(comingFrom, node);
				path.push(direction);
				node = comingFrom;
			}

			return path;
		}
	}
}

package bot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	private static final int MAX_DEPTH = 512;
	private static final short VERSION = 10;
	private Queue<Position> fruitQueue;
	private Snake self;
	private GameState state;
	private Set<Position> lethalPositions, highRiskPositions;
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
		this.lethalPositions = getLethalPositions(state.getBoard());
		this.highRiskPositions = calculateHighRiskPositions();
		this.growthFrequency = state.getMetadata().getGrowthFrequency();
		this.maximumThinkingTime = gameState.getMetadata().getMaximumThinkingTime();
		this.fruitQueue = rankFruits(gameState.getFruits());
		this.boardArea = getBoardArea();

		try
		{
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
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		return self.getCurrentDirection();
	}

	private Set<Position> getLethalPositions(Board board)
	{
		final Set<Position> danger = new HashSet<Position>(board.getHeight() * board.getWidth());
		danger.addAll(state.getWalls());
		for(Snake snake : state.getSnakes())
			danger.addAll(snake.getSegments());

		return danger;
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

		Deque<Direction> path = new ArrayDeque<>();
		Deque<Direction> subPath = null;
		while(!reachedComputationCapacity(path.size()))
		{

			Position to = null;
			while(subPath == null && !reachedComputationCapacity(path.size()))
			{
				to = getNextTarget();
				System.out.println("Trying to reach " + to);
				if(!from.equals(to))
					subPath = g.getBfsPath(from, to);
				if(subPath == null)
					System.out.println("Found no path between " + from + " and " + to);

			}
			if(subPath != null)
			{
				path.addAll(subPath);
				g.removeAll(from, subPath);
				from = to;
				subPath = null;
			}
		}
		return path;

	}

	private boolean isOpositeDirections(Direction currentDireciton, Direction dir)
	{
		dir = dir.turnLeft().turnLeft();
		return currentDireciton.equals(dir);
	}

	private boolean isLethal(Position position)
	{
		return lethalPositions.contains(position);
	}

	private int getBoardArea()
	{
		final int width = state.getBoard().getWidth();
		final int height = state.getBoard().getWidth();
		final int physicalLimit = width * height;

		return physicalLimit;
	}

	private int remainingBoardSize()
	{
		return boardArea - lethalPositions.size();
	}

	private int getSnakeLength()
	{
		return self.getSegments().size();
	}

	private boolean reachedComputationCapacity(final int stackSize)
	{
		if(thinkingTimeLeft() < 10)
			return true;

		if(MAX_DEPTH < stackSize || remainingBoardSize() < stackSize)
			return true;

		return false;
	}

	private Set<Position> calculateHighRiskPositions()
	{
		Set<Snake> snakes = enemySnakes();
		Set<Position> positions = new HashSet<Position>(snakes.size() * 4);
		for(Snake snake : snakes)
		{
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

			final int score = distanceClosestSnake - ownDistance * 2;

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

		private void removeAll(Position from, Collection<Direction> directions)
		{
			for(Direction d : directions)
			{
				final Position p = d.calculateNextPosition(from);
				remove(p);
				from = p;
			}
		}

		Deque<Direction> getBfsPath(Position from, Position to)
		{
			if(from.equals(to))
				throw new IllegalArgumentException("Start and end node (" + to + ") cannot be the same.");

			final Queue<Position> nodesToCheck = new ArrayDeque<Position>(16);
			final Map<Position, Position> path = new HashMap<Position, Position>(32);

			nodesToCheck.add(from);
			path.put(from, null);
			while(!nodesToCheck.isEmpty())
			{
				Position currentNode = nodesToCheck.poll();
				if(currentNode.equals(to))
					return buildPath(path, from, to);

				List<Position> possibleNodes = getConnectingNodesTo(currentNode);
				for(Position node : possibleNodes)
				{
					if(!path.containsKey(node))
					{
						nodesToCheck.add(node);
						path.put(node, currentNode);
					}
				}
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
			if(start.equals(end))
				throw new IllegalArgumentException("Start and end node (" + end + ") cannot be the same.");

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

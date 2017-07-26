package bot;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import gameLogic.Board;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;

public class BoardAnalyzer
{
	private final GameState state;
	private final Board board;
	private final Set<Position> lethalPositions, highRiskPositions, fruits;
	private Queue<Position> fruitQueue;
	private final Snake self;

	public BoardAnalyzer(GameState state, Snake self)
	{
		this.self = self;
		this.state = state;
		this.board = state.getBoard();
		this.lethalPositions = getLethalPositions();
		this.highRiskPositions = calculateHighRiskPositions();
		this.fruits = new HashSet<Position>(state.getFruits());
		this.fruitQueue = rankFruits();
	}
	
	public Board getBoard()
	{
		return board;
	}
	
	public boolean isLethal(Position position)
	{
		return lethalPositions.contains(position);
	}
	
	public boolean isHighRisk(Position position)
	{
		return highRiskPositions.contains(position);
	}
	
	public boolean isDangerous(Position position)
	{
		return isLethal(position) || isHighRisk(position);
	}
	
	public boolean hasFruit(Position position)
	{
		return fruits.contains(position);
	}
	
	public int getScore(Position position)
	{
		if(isHighRisk(position))
			return -5;
		if(hasFruit(position))
			return 5;
		return 1;
	}

	public int getBoardArea()
	{
		final int width = state.getBoard().getWidth();
		final int height = state.getBoard().getWidth();
		final int physicalLimit = width * height;

		return physicalLimit;
	}

	public int remainingBoardSize()
	{
		return getBoardArea() - lethalPositions.size();
	}

	private Set<Position> getLethalPositions()
	{
		final Set<Position> danger = new HashSet<Position>(board.getHeight() * board.getWidth());
		danger.addAll(state.getWalls());
		for(Snake snake : state.getSnakes())
			danger.addAll(snake.getSegments());

		return danger;
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
	
	public Position getRandomSafePosition()
	{
		Random rand = new Random();
		Position p = null;
		while(p == null || isLethal(p) || isHighRisk(p))
		{
			final int x = rand.nextInt(state.getBoard().getWidth());
			final int y = rand.nextInt(state.getBoard().getHeight());
			p = new Position(x, y);
		}

		return p;
	}

	private Queue<Position> rankFruits()
	{
		SortedMap<Integer, Position> ranking = new TreeMap<Integer, Position>();
		final Set<Snake> snakes = enemySnakes();

		for(Position fruit : fruits)
		{
			if(isDangerous(fruit))
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
	
	public Position getNextTarget()
	{
		if(fruitQueue.isEmpty())
			return getRandomSafePosition();
		return fruitQueue.poll();
	}
	
	public Set<Snake> enemySnakes()
	{
		final Set<Snake> snakes = state.getSnakes();
		final Set<Snake> liveSnakes = new HashSet<Snake>(snakes.size());

		for(Snake snake : snakes)
			if(snake != self && !snake.isDead())
				liveSnakes.add(snake);

		return liveSnakes;
	}
}

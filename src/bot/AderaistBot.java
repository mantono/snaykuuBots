package bot;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gameLogic.Board;
import gameLogic.Brain;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;
import javafx.geometry.Pos;

public class AderaistBot implements Brain
{

	private GameState gamestate;
	private Snake self;
	private Map<Direction, Integer> safestDirection;

	private Direction nextDirection;
	
	public AderaistBot()
	{
		this.safestDirection = new EnumMap<Direction, Integer>(Direction.class);
	}
	
	@Override
	public Direction getNextMove(Snake self, GameState gamestate)
	{
		this.self = self;
		this.gamestate = gamestate;
		this.nextDirection = self.getCurrentDirection();

		List<Position> fruits = gamestate.getFruits();
		if(fruits.size() > 0)
			nextDirection = getDirectionToClosesFruit(fruits);
		
		if(willCollide(nextDirection))
			return nextTurn();

		return nextDirection;
	}

	private Position getClosestFruit(List<Position> fruits)
	{
		Position headPosition = self.getHeadPosition();
		Position closestFruit = fruits.get(0);

		int dist = Integer.MAX_VALUE;
		for(Position fruitPosition : fruits)
		{
			if(fruitPosition.getDistanceTo(headPosition) < dist)
			{
				closestFruit = fruitPosition;
				dist = fruitPosition.getDistanceTo(headPosition);
			}
		}
		return closestFruit;
	}

	private Direction getDirectionToClosesFruit(List<Position> fruits)
	{
		Position closestFruitPosition = getClosestFruit(fruits);
		List<Direction> wayToFruit = GameState.getRelativeDirections(self.getHeadPosition(), closestFruitPosition);
		Direction directionToFruit = wayToFruit.get(0);
		if(directionIsOppositeToCurrentDirection(directionToFruit) && wayToFruit.size() == 2)
			directionToFruit = wayToFruit.get(1);
		return directionToFruit;
	}

	private boolean directionIsOppositeToCurrentDirection(Direction direction)
	{
		if(nextDirection.equals(Direction.WEST) && direction.equals(Direction.EAST))
			return true;
		if(nextDirection.equals(Direction.EAST) && direction.equals(Direction.WEST))
			return true;
		if(nextDirection.equals(Direction.NORTH) && direction.equals(Direction.SOUTH))
			return true;
		if(nextDirection.equals(Direction.SOUTH) && direction.equals(Direction.NORTH))
			return true;
		return false;
	}

	private int turnsUntilCollision(Direction direction, int turns, Set<Position> lethalObstacles)
	{
		if(turns < 1)
			throw new IllegalArgumentException();
		
		if(turns == 16)
			return turns;
		
		if(directionIsOppositeToCurrentDirection(direction))
			return -1;
		
		Set<Snake> snakes = gamestate.getSnakes();
		
		if(lethalObstacles.isEmpty())
		{
			for(Snake snake : snakes)
				lethalObstacles.addAll(snake.getSegments());
		}
		
		for(Snake snake : snakes)
		{
			if(snake.equals(self) || snake.isDead())
				continue;
			Position futurePosition = getFuturePosition(snake, turns);
			lethalObstacles.add(futurePosition);
		}
		
		Position myFuturePosition =	getFuturePosition(direction, self, turns);
		
		if(lethalObstacles.contains(myFuturePosition))
			return turns;
		
		if(outOfBounds(myFuturePosition))
			return turns;
		
		return turnsUntilCollision(direction, ++turns, lethalObstacles);
	}

	private Position getFuturePosition(Snake snake, int turns)
	{
		return getFuturePosition(snake.getCurrentDirection(), snake, turns);
	}
	
	private Position getFuturePosition(Direction direction, Snake snake, int turns)
	{
		Position origin = snake.getHeadPosition();
		Position vector = direction.getDirectionVector();
		final int x = origin.getX()+vector.getX()*turns;
		final int y = origin.getY()+vector.getY()*turns;
		return new Position(x, y);
	}

	private boolean outOfBounds(Position position)
	{
		Board board = gamestate.getBoard();
		if(position.getX() < 1 || position.getY() < 1)
			return true;
		if(position.getX() >= board.getWidth() - 1 || position.getY() >= board.getHeight() -1)
			return true;
		return false;
	}

	private Direction nextTurn()
	{
		for(Direction dir : Direction.values())
		{
			final int turns = turnsUntilCollision(dir, 1, new HashSet<Position>(128));
			safestDirection.put(dir, turns);
		}
		
		int longest = -1;
		Direction safest = Direction.SOUTH;
		
		for(Direction dir : Direction.values())
		{
			int distance = safestDirection.get(dir); 
			if(distance > longest)
			{
				longest = distance;
				safest = dir;
			}
		}
		
		return safest;
	}
	
	private boolean willCollide(Direction direction)
	{
		return turnsUntilCollision(direction, 1, new HashSet<Position>(128)) < 3;
	}
}

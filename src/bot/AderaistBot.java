package bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gameLogic.Brain;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;

public class AderaistBot implements Brain
{

	private GameState gamestate;
	private Snake self;

	private Direction nextDirection;
	
	@Override
	public Direction getNextMove(Snake self, GameState gamestate)
	{
		this.self = self;
		this.gamestate = gamestate;
		this.nextDirection = self.getCurrentDirection();

		List<Position> fruits = gamestate.getFruits();
		if(fruits.size() > 0)
			nextDirection = getDirectionToClosesFruit(fruits);
		
		if(willCollide(nextDirection, 2))
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
	} // getClosestFruit

	private Direction getDirectionToClosesFruit(List<Position> fruits)
	{
		Position closestFruitPosition = getClosestFruit(fruits);
		List<Direction> wayToFruit = GameState.getRelativeDirections(self.getHeadPosition(), closestFruitPosition);
		Direction directionToFruit = wayToFruit.get(0);
		if(directionIsOppositeToCurrentDirection(directionToFruit))
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

	private boolean willCollideInTheFuture(final int steps)
	{
		List<Position> futurePositionsOfOtherSnakesHeads = new ArrayList<Position>();
		Set<Snake> snakes = gamestate.getSnakes();
		for(Snake snake : snakes)
		{
			if(!snake.getHeadPosition().equals(self.getHeadPosition()))
				futurePositionsOfOtherSnakesHeads.add(calculateFuturePosition(snake, steps));
		}
		final Position ourFuturePosition = calculateFuturePosition(self, steps);
		for(Position position : futurePositionsOfOtherSnakesHeads)
			if(ourFuturePosition.equals(position))
				return true;
		return false;
	}

	private Position calculateFuturePosition(Snake snake, final int steps)
	{
		Direction direction = snake.getCurrentDirection();
		Position currentHeadPosition = snake.getHeadPosition();
		switch(direction)
		{
			case WEST:
				return new Position(currentHeadPosition.getX() - steps, currentHeadPosition.getY());
			case EAST:
				return new Position(currentHeadPosition.getX() + steps, currentHeadPosition.getY());
			case NORTH:
				return new Position(currentHeadPosition.getX(), currentHeadPosition.getY() - steps);
			case SOUTH:
				return new Position(currentHeadPosition.getX(), currentHeadPosition.getY() + steps);
		}
		return currentHeadPosition;
	}

	private Direction nextTurn()
	{
		for(Direction direction:Direction.values())
			if(isSafeDirection(direction))
				return direction;
		return Direction.SOUTH;
	}
	
	private boolean isSafeDirection(Direction direction)
	{
		return !willCollide(direction) && !directionIsOppositeToCurrentDirection(direction);
	}
	
	private boolean willCollide(Direction direction)
	{
		return gamestate.willCollide(self, direction);
	}
	
	private boolean willCollide(Direction direction, int turns)
	{
		if(willCollide(direction))
			return true;
		while(turns-- > 1)
			if(willCollideInTheFuture(turns))
				return true;
		return false;
	}
}

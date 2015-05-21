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

	private Direction lastDirection;
	
	@Override
	public Direction getNextMove(Snake self, GameState gamestate)
	{
		this.self = self;
		Direction nextDirection = self.getCurrentDirection();

		List<Position> fruits = gamestate.getFruits();
		if(fruits.size() > 0)
			nextDirection = getDirectionToClosesFruit(fruits);
		
		if(gamestate.willCollide(self, nextDirection))
			return nextTurn(gamestate, self);
		else if(willColideTwoTurnsFromNow(gamestate, self))
			return nextTurn(gamestate, self);

		return nextDirection;
	}

	private boolean goingEast(Position fruitPosition)
	{
		return fruitPosition.getX() > self.getHeadPosition().getX();
	}
	
	private boolean goingWest(Position fruitPosition)
	{
		return fruitPosition.getX() < self.getHeadPosition().getX();
	}

	private boolean goingNorth(Position fruitPosition)
	{
		return fruitPosition.getY() < self.getHeadPosition().getY();
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
		Position fruitPosition = getClosestFruit(fruits);
		if(goingNorth(fruitPosition) && self.getCurrentDirection() != Direction.SOUTH)
			return Direction.NORTH;
		else if(goingEast(fruitPosition) && self.getCurrentDirection() != Direction.WEST)
			return Direction.EAST;
		else if(goingWest(fruitPosition) && self.getCurrentDirection() != Direction.EAST)
			return Direction.WEST;
		return Direction.SOUTH;
	}

	private boolean willColideTwoTurnsFromNow(GameState gamestate, Snake self)
	{
		List<Position> futurePositionsOfOtherSnakesHeads = new ArrayList<Position>();
		Set<Snake> snakes = gamestate.getSnakes();
		for(Snake snake : snakes)
		{
			if(!snake.getHeadPosition().equals(self.getHeadPosition()))
			{
				futurePositionsOfOtherSnakesHeads.add(calculateFuturePosition(snake, 1));
				futurePositionsOfOtherSnakesHeads.add(calculateFuturePosition(snake, 2));
			}
		}
		final Position ourFuturePosition = calculateFuturePosition(self, 1);
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

	private Direction nextTurn(GameState gamestate, Snake self)
	{
		if(!gamestate.willCollide(self, Direction.EAST))
			return Direction.EAST;
		if(!gamestate.willCollide(self, Direction.NORTH))
			return Direction.NORTH;
		if(!gamestate.willCollide(self, Direction.SOUTH))
			return Direction.SOUTH;
		return Direction.WEST;
	}
}

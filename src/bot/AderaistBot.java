package bot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gameLogic.Board;
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
		
		if(willCollide(nextDirection, 3))
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

	private boolean willCollideInTheFuture(final int steps)
	{
		final Position ourFuturePosition = calculateFuturePosition(self, steps);
		if(outOfBounds(ourFuturePosition))
			return true;
		if(isTrap(ourFuturePosition))
			return true;
		Set<Position> lethalObstacles = getSnakeHeads(steps);
		if(lethalObstacles.contains(ourFuturePosition))
			return true;
		return false;
	}

	private boolean outOfBounds(Position position)
	{
		Board board = gamestate.getBoard();
		if(position.getX() < 1 || position.getY() < 1)
			return true;
		if(position.getX() >= board.getWidth() || position.getY() >= board.getHeight())
			return true;
		return false;
	}

	private boolean isTrap(Position ourFuturePosition)
	{
		List<Position> neighbours = ourFuturePosition.getNeighbours();
		int lethalNeighbours = 0;
		Board board = gamestate.getBoard();
		for(Position square:neighbours)
		{
			if(!outOfBounds(square))
				if(board.isLethal(square))
					lethalNeighbours++;
		}
		return lethalNeighbours >= 2;
	}

	private Set<Position> getSnakeHeads(final int steps)
	{
		Set<Snake> snakes = gamestate.getSnakes();
		Set<Position> snakeHeads = new HashSet<Position>(snakes.size()*2);
		for(Snake snake : snakes)
		{
			if(!snake.getHeadPosition().equals(self.getHeadPosition()))
				snakeHeads.add(calculateFuturePosition(snake, steps));
		}
		return snakeHeads;
	}

	private Position calculateFuturePosition(Snake snake, final int steps)
	{
		Direction direction = snake.getCurrentDirection();
		Position headPosition = snake.getHeadPosition();
		switch(direction)
		{
			case WEST:
				headPosition = new Position(snake.getHeadPosition().getX() - steps, headPosition.getY());
			case EAST:
				headPosition = new Position(headPosition.getX() + steps, headPosition.getY());
			case NORTH:
				headPosition = new Position(headPosition.getX(), headPosition.getY() - steps);
			case SOUTH:
				headPosition = new Position(headPosition.getX(), headPosition.getY() + steps);
		}
		if(!outOfBounds(headPosition))
			return headPosition;
		return snake.getHeadPosition();
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

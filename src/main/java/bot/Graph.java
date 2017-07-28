package bot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import gameLogic.Board;
import gameLogic.Direction;
import gameLogic.Position;

public class Graph
{
	private final boolean[][] matrix;

	Graph(BoardState state)
	{
		final Board board = state.getState().getBoard();
		this.matrix = new boolean[board.getWidth()][board.getHeight()];

		for(int y = 0; y < board.getHeight(); y++)
		{
			for(int x = 0; x < board.getWidth(); x++)
			{
				final Position pos = new Position(x, y);
				if(!state.isDangerous(pos))
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

	public Deque<Direction> bfsPath(Position from, Position to)
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
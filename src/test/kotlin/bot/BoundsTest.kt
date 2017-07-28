package bot

import gameLogic.Position
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class BoundsTest
{
	@Test
	fun testEvenBoundsDivisionLeftRight()
	{
		val b = Bounds(0, 0, 7, 7)
		val (left, right) = divideToLeftRight(b)

		assertEquals(Position(0, 0), left.upperLeft)
		assertEquals(Position(3, 0), left.upperRight)
		assertEquals(Position(0, 7), left.lowerLeft)
		assertEquals(Position(3, 7), left.lowerRight)

		assertEquals(Position(4, 0), right.upperLeft)
		assertEquals(Position(7, 0), right.upperRight)
		assertEquals(Position(4, 7), right.lowerLeft)
		assertEquals(Position(7, 7), right.lowerRight)
	}

	@Test
	fun testEvenBoundsDivisionUpperLower()
	{
		val b = Bounds(0, 0, 7, 7)
		val (upper, lower) = divideToUpperLower(b)

		assertEquals(Position(0, 0), upper.upperLeft)
		assertEquals(Position(7, 0), upper.upperRight)
		assertEquals(Position(0, 3), upper.lowerLeft)
		assertEquals(Position(7, 3), upper.lowerRight)

		assertEquals(Position(0, 4), lower.upperLeft)
		assertEquals(Position(7, 4), lower.upperRight)
		assertEquals(Position(0, 7), lower.lowerLeft)
		assertEquals(Position(7, 7), lower.lowerRight)
	}

	@Test
	fun testEvenBoundsDivision()
	{
		val b = Bounds(0, 0, 7, 7)
		val bounds = b.divide()

		val upperLeft = bounds[0]
		val upperRight = bounds[1]
		val lowerLeft = bounds[2]
		val lowerRight = bounds[3]

		assertEquals(Position(0, 0), upperLeft.upperLeft)
		assertEquals(Position(3, 0), upperLeft.upperRight)
		assertEquals(Position(0, 3), upperLeft.lowerLeft)
		assertEquals(Position(3, 3), upperLeft.lowerRight)

		assertEquals(Position(4, 0), upperRight.upperLeft)
		assertEquals(Position(7, 0), upperRight.upperRight)
		assertEquals(Position(4, 3), upperRight.lowerLeft)
		assertEquals(Position(7, 3), upperRight.lowerRight)

		assertEquals(Position(0, 4), lowerLeft.upperLeft)
		assertEquals(Position(3, 4), lowerLeft.upperRight)
		assertEquals(Position(0, 7), lowerLeft.lowerLeft)
		assertEquals(Position(3, 7), lowerLeft.lowerRight)

		assertEquals(Position(4, 4), lowerRight.upperLeft)
		assertEquals(Position(7, 4), lowerRight.upperRight)
		assertEquals(Position(4, 7), lowerRight.lowerLeft)
		assertEquals(Position(7, 7), lowerRight.lowerRight)

	}

	@Test
	fun testErrorBounds()
	{
		val b = Bounds(0, 0, 16, 16)
		val bounds = b.divide()
		bounds[0].divide()
		bounds[1].divide()
		bounds[2].divide()
		bounds[3].divide()
	}
}
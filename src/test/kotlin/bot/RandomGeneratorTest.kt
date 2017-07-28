package bot

import gameLogic.Position
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class RandomGeneratorTest
{
	@Test
	fun testRandomSequence()
	{
		val p: List<Position> = randomPosition(16, 16).take(5).toList()
		assertTrue(p[0].x < 16)
		assertTrue(p[0].y < 16)

		assertTrue(p[0].x >= 0)
		assertTrue(p[0].y >= 0)

		println(p)
	}
}
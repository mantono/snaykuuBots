package bot

import gameLogic.Position

data class Bounds(val xStart: Int, val yStart: Int, val xEnd: Int, val yEnd: Int)
{
	val width: Int = (xEnd - xStart) + 1
	val height: Int = (yEnd - yStart) + 1
	val size = width * height
	val isSingle: Boolean = size == 1
	val upperLeft: Position = Position(xStart, yStart)
	val upperRight: Position = Position(xEnd, yStart)
	val lowerLeft: Position = Position(xStart, yEnd)
	val lowerRight: Position = Position(xEnd, yEnd)
	val positions: List<Position> by lazy {
		val pos: MutableList<Position> = ArrayList(size)

		for(x in xStart..xEnd)
			for(y in yStart..yEnd)
				pos.add(Position(x, y))

		pos
	}

	constructor(start: Position, end: Position): this(start.x, start.y, end.x, end.y)

	init
	{
		if(xEnd < xStart)
			throw IllegalArgumentException("x: $xEnd < $xStart")
		if(yEnd < yStart)
			throw IllegalArgumentException("y: $yEnd < $yStart")
	}

	operator fun contains(p: Position): Boolean = contains(p.x, p.y)

	fun contains(x: Int, y: Int): Boolean = x in xStart..xEnd && y in yStart..yEnd

	fun divide(): List<Bounds>
	{
		if(isSingle)
			throw IllegalStateException("Can not divide bounds! Size is one: $this")
		val (upper, lower) = divideToUpperLower(this)
		val (upperLeft, upperRight) = divideToLeftRight(upper)
		val (lowerLeft, lowerRight) = divideToLeftRight(lower)

		return listOf(upperLeft, upperRight, lowerLeft, lowerRight)
	}

	fun asSequence(): Sequence<Position> = positions.asSequence()

	override fun toString(): String
	{
		return "$upperLeft -> $lowerRight (size: $size)"
		//return "\n\n$xStart, $yStart \t up \t $xEnd, $yStart \n\nleft\t\t\tright\n\n$xStart, $yEnd \t down \t $xEnd, $yEnd\n\n"
	}
}

fun divideToUpperLower(b: Bounds): Array<Bounds>
{
	try
	{
		val newUpperY1 = ((b.height / 2)  - 1) + b.yStart
		val upper = Bounds(b.xStart, b.yStart, b.xEnd, newUpperY1)
		val lower = Bounds(b.xStart, upper.yEnd + 1, b.xEnd, b.yEnd)
		return arrayOf(upper, lower)
	}
	catch(e: Exception)
	{
		System.out.println("Error when trying to process bounds: xStart: ${b.xStart}, yStart: ${b.yStart} - xEnd: ${b.xEnd}, yEnd: ${b.yEnd}")
		throw e
	}
}

fun divideToLeftRight(b: Bounds): Array<Bounds>
{
	val newLeftX1 = ((b.width / 2) - 1) + b.xStart
	val left = Bounds(b.xStart, b.yStart, newLeftX1, b.yEnd)
	val right = Bounds(left.xEnd + 1, b.yStart, b.xEnd, b.yEnd)
	return arrayOf(left, right)
}
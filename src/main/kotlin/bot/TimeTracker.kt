package bot

import gameLogic.Metadata

private const val SETUP_MARGIN: Long = 3

data class TimeTracker(val maximumThinkingTime: Long)
{
	private var startedAt: Long = System.currentTimeMillis() - SETUP_MARGIN

	constructor(metadata: Metadata): this(metadata.maximumThinkingTime.toLong())

	fun resetTime(): Long
	{
		this.startedAt = System.currentTimeMillis() - SETUP_MARGIN
		return this.startedAt
	}

	fun remaining(): Long = maximumThinkingTime - consumedTime()
	fun consumedTime(): Long = System.currentTimeMillis() - startedAt
}
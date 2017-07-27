package bot

import gameLogic.Metadata

data class TimeTracker(val maximumThinkingTime: Long)
{
	private var startedAt: Long = System.currentTimeMillis()

	constructor(metadata: Metadata): this(metadata.maximumThinkingTime.toLong())

	fun resetTime(): Long
	{
		this.startedAt = System.currentTimeMillis()
		return this.startedAt
	}

	fun remaining(): Long = maximumThinkingTime - consumedTime()
	fun consumedTime(): Long = startedAt - System.currentTimeMillis()
}
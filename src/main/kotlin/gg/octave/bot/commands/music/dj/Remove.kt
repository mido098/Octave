package gg.octave.bot.commands.music.dj

import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.embedTitle
import gg.octave.bot.commands.music.embedUri
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.entities.framework.Usages
import gg.octave.bot.utils.extensions.manager
import gg.octave.bot.utils.extensions.removeAll
import gg.octave.bot.utils.extensions.removeAt
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import java.util.regex.Matcher
import java.util.regex.Pattern

class Remove : MusicCog {
    override fun sameChannel() = true

    private val pattern = Pattern.compile("(\\d+)?\\s*?\\.\\.\\s*(\\d+)?")

    @DJ
    @CheckVoiceState
    @Usages("first", "last", "all", "1..5", "1", "1 7 12 3")
    @Command(aliases = ["removesong"], description = "Remove a song from the queue.")
    fun remove(ctx: Context, @Greedy which: String?) {
        val queue = ctx.manager.scheduler.queue

        if (queue.isEmpty()) {
            return ctx.send("The queue is empty.")
        }

        val track = when (which) {
            null -> return ctx.send("You need to specify what to remove. (`first`/`last`/`all`/`1..3`)")
            "first" -> queue.remove() //Remove head
            "last" -> queue.removeAt(queue.size - 1)
            "all" -> {
                queue.clear()
                return ctx.send("Cleared the music queue.")
            }
            else -> {
                val matcher = pattern.matcher(which)

                return when {
                    matcher.find() -> removeRange(ctx, matcher)
                    else -> removeMany(ctx, which.split("\\s+".toRegex()))
                }
            }
        }

        val decodedTrack = Launcher.players.playerManager.decodeAudioTrack(track)
        ctx.send {
            setColor(0x9570D3)
            setTitle("Track Removed")
            setDescription("Removed __[${decodedTrack.info.embedTitle}](${decodedTrack.info.embedUri})__ from the queue.")
        }
    }

    fun removeMany(ctx: Context, ind: List<String>) {
        val queue = ctx.manager.scheduler.queue
        val invalidIndexes = ind.filter {
            val int = it.toIntOrNull()
            return@filter int == null || int < 1 || int > queue.size
        }

        if (invalidIndexes.isNotEmpty()) {
            return ctx.send {
                setColor(0x9570D3)
                setTitle("Invalid Track Indexes")
                setDescription(
                    "The following track numbers are invalid: ${invalidIndexes.joinToString("`, `", prefix = "`", postfix = "`")}\n" +
                        "You must specify the position (or multiple positions) of tracks in the queue you want to remove.\n\n" +
                        "Alternatively, you can try `remove 1`, `remove 1..${queue.size}`, `remove first` or `remove last`."
                )
            }
        }

        val indexes = ind.map { it.toInt() - 1 }.sortedByDescending { it }
        val removed = queue.removeAll(indexes)

        return ctx.send("Removed `$removed` tracks from the queue.")
    }

    fun removeRange(ctx: Context, matcher: Matcher) {
        val queue = ctx.manager.scheduler.queue

        if (matcher.group(1) == null && matcher.group(2) == null) {
            return ctx.send("You must specify start range and/or end range.")
        }

        val start = matcher.group(1).let {
            if (it == null) 1
            else it.toIntOrNull()?.coerceAtLeast(1)
                ?: return ctx.send("Invalid start of range")
        }

        val end = matcher.group(2).let {
            if (it == null) queue.size
            else it.toIntOrNull()?.coerceAtMost(queue.size)
                ?: return ctx.send("Invalid end of range")
        }

        for (i in end downTo start) {
            queue.removeAt(i - 1)
        }

        return ctx.send("Removed tracks `$start-$end` from the queue.")
    }
}

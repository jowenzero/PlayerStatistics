package io.github.kr8gz.playerstatistics.command

import io.github.kr8gz.playerstatistics.command.PageCommand.registerPageAction
import io.github.kr8gz.playerstatistics.command.ShareCommand.storeShareData
import io.github.kr8gz.playerstatistics.database.Leaderboard
import io.github.kr8gz.playerstatistics.extensions.ServerCommandSource.sendFeedback
import io.github.kr8gz.playerstatistics.extensions.Text.newLine
import io.github.kr8gz.playerstatistics.messages.Colors
import io.github.kr8gz.playerstatistics.messages.Components
import io.github.kr8gz.playerstatistics.messages.formatName
import io.github.kr8gz.playerstatistics.messages.formatValue
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.stat.Stat
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.core.text.literalText

object LeaderboardCommand : StatsCommand("leaderboard") {
    override fun LiteralCommandBuilder<ServerCommandSource>.build() {
        statArgument { stat ->
            executes {
                usingDatabase { source.sendLeaderboard(stat()) }
            }
        }
    }

    private suspend fun ServerCommandSource.sendLeaderboard(stat: Stat<*>, page: Int = 1) {
        val leaderboard = Leaderboard.forStat(stat, player?.gameProfile?.name, page)

        val label = Text.translatable("playerstatistics.command.leaderboard", stat.formatName())
        val content = literalText {
            text(label)
            leaderboard.pageEntries.forEach { (rank, player, value) ->
                text("\n» ") { color = Colors.DARK_GRAY }
                text("$rank. $player - "); text(stat.formatValue(value))
            }
        }

        sendFeedback {
            val shareCode = storeShareData(label, content)
            content newLine Components.pageFooter(page, leaderboard.pageCount, shareCode)
        }
        registerPageAction(max = leaderboard.pageCount) { sendLeaderboard(stat, it) }
    }
}

package io.github.kr8gz.playerstatistics.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import io.github.kr8gz.playerstatistics.extensions.ServerCommandSource.uuid
import net.minecraft.server.command.ServerCommandSource
import net.silkmc.silk.commands.LiteralCommandBuilder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private typealias PageAction = suspend ServerCommandSource.(newPage: Int) -> Unit

object PageCommand : StatsCommand("page") {
    override fun LiteralCommandBuilder<ServerCommandSource>.build() {
        argument("page", IntegerArgumentType.integer(1)) { page ->
            executes {
                usingDatabase { source.runPageAction(page()) }
            }
        }
    }

    private val pageActions = ConcurrentHashMap<UUID, PageAction>()

    fun ServerCommandSource.registerPageAction(max: Int, action: PageAction) {
        pageActions[uuid] = { page ->
            if (page <= max) action(page)
            else sendError(CommandExceptions.NO_DATA.getMessage())
        }
    }

    private suspend fun ServerCommandSource.runPageAction(page: Int) {
        pageActions[uuid]?.let { it(page) } ?: sendError(CommandExceptions.NO_DATA.getMessage())
    }
}

package io.pixeloutlaw.minecraft.spigot.questworld.strife

import com.questworld.api.QuestExtension
import org.bukkit.plugin.Plugin

class StrifeQuestExtension : QuestExtension() {
    override fun initialize(parent: Plugin?) {
        setMissionTypes()
    }
}
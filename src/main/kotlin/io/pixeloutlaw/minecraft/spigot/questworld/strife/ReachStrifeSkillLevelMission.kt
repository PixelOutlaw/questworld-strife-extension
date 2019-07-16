package io.pixeloutlaw.minecraft.spigot.questworld.strife

import com.questworld.api.MissionType
import com.questworld.api.SinglePrompt
import com.questworld.api.Ticking
import com.questworld.api.contract.IMission
import com.questworld.api.contract.IMissionState
import com.questworld.api.contract.MissionEntry
import com.questworld.api.menu.MissionButton
import com.questworld.api.menu.QuestBook
import com.questworld.util.ItemBuilder
import com.questworld.util.PlayerTools
import com.questworld.util.Text
import com.questworld.util.version.ObjectMap
import info.faceland.strife.StrifePlugin
import info.faceland.strife.data.champion.ChampionSaveData
import info.faceland.strife.data.champion.LifeSkillType
import info.faceland.strife.events.SkillLevelUpEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.Locale

class ReachStrifeSkillLevelMission :
    MissionType("REACH_STRIFE_SKILL_LEVEL", true, ItemStack(ObjectMap.VDMaterial.EXPERIENCE_BOTTLE)), Listener,
    Ticking {
    override fun userInstanceDescription(instance: IMission?): String {
        return "&7Reach level ${instance?.amount} in ${instance?.customString}"
    }

    override fun userDisplayItem(instance: IMission?): ItemStack {
        return selectorItem.clone()
    }

    override fun onManual(player: Player?, entry: MissionEntry?) {
        if (player == null || entry == null) {
            return
        }
        val lifeSkillType = getSkill(entry.mission)
        val champion = StrifePlugin.getInstance().championManager.getChampion(player)
        val skillLevel = champion.getLifeSkillLevel(lifeSkillType)
        entry.progress = skillLevel
    }

    override fun getLabel(): String {
        return "&r> Click to check levels"
    }

    override fun validate(instance: IMissionState?) {
        if (instance == null) {
            return
        }
        if (!skillExists(instance.customString)) {
            instance.customString = LifeSkillType.MINING.name
            instance.apply()
        }
    }

    override fun layoutMenu(changes: IMissionState?) {
        if (changes == null) {
            return
        }
        val lifeSkillType = getSkill(changes)

        putButton(10, MissionButton.simpleButton(changes, ItemBuilder(Material.NAME_TAG).wrapText("", "", "").get()) {
            val playerWhoClicked = it.whoClicked as Player
            playerWhoClicked.closeInventory()

            PlayerTools.promptInput(
                playerWhoClicked,
                SinglePrompt("&aEnter a skill name (cancel() to abort):") { conversationContext, input ->
                    if (!input.equals("cancel()", ignoreCase = true)) {
                        if (!skillExists(input)) {
                            SinglePrompt.setNextDisplay(conversationContext, "&cInvalid skill: &r${input}")
                            return@SinglePrompt false
                        }

                        changes.customString = input.toUpperCase(Locale.ENGLISH)
                        playerWhoClicked.sendMessage(Text.colorize("&aSuccessfully changed skill."))
                    } else {
                        playerWhoClicked.sendMessage(Text.colorize("&7Aborting..."))
                    }
                    QuestBook.openQuestMissionEditor(playerWhoClicked, changes)
                    return@SinglePrompt true
                })
        })

        putButton(17, MissionButton.amount(changes))
    }

    private fun getSkill(instance: IMission): LifeSkillType {
        return try {
            LifeSkillType.valueOf(instance.customString)
        } catch (ex: Exception) {
            LifeSkillType.MINING
        }
    }

    private fun skillExists(skillName: String): Boolean {
        return try {
            LifeSkillType.valueOf(skillName)
            true
        } catch (ex: Exception) {
            false
        }
    }
}
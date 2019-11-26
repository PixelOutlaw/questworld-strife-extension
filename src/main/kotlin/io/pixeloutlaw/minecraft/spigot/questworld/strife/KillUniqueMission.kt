/*
 * The MIT License
 * Copyright © 2015 Pixel Outlaw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.pixeloutlaw.minecraft.spigot.questworld.strife

import com.questworld.api.MissionType
import com.questworld.api.QuestWorld
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
import land.face.strife.StrifePlugin
import land.face.strife.events.UniqueKillEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class KillUniqueMission :
        MissionType("KILL_STRIFE_UNIQUE", true, ItemStack(ObjectMap.VDMaterial.GOLDEN_SWORD)), Listener,
        Ticking {
    override fun userInstanceDescription(instance: IMission?): String {
        return "&7Kill ${instance?.amount} of unique ${instance?.customString}"
    }

    override fun userDisplayItem(instance: IMission?): ItemStack {
        return selectorItem.clone()
    }

    override fun onManual(player: Player?, entry: MissionEntry?) {
        return
    }

    override fun getLabel(): String {
        return "&r> Click to check kills"
    }

    override fun validate(instance: IMissionState?) {
        if (instance == null) {
            return
        }
        if (!StrifePlugin.getInstance().uniqueEntityManager.isLoadedUnique(instance.customString)) {
            instance.customString = "NONE"
            instance.apply()
        }
    }

    override fun layoutMenu(changes: IMissionState?) {
        if (changes == null) {
            return
        }
        putButton(10, MissionButton.simpleButton(changes, ItemBuilder(Material.NAME_TAG).wrapText("", "", "").get()) {
            val playerWhoClicked = it.whoClicked as Player
            playerWhoClicked.closeInventory()

            PlayerTools.promptInput(
                    playerWhoClicked,
                    SinglePrompt("&aEnter a unique id (cancel() to abort):") { conversationContext, input ->
                        if (!input.equals("cancel()", ignoreCase = true)) {
                            if (!StrifePlugin.getInstance().uniqueEntityManager.loadedUniquesMap.containsKey(input)) {
                                SinglePrompt.setNextDisplay(conversationContext, "&cInvalid id: &r${input}")
                                return@SinglePrompt false
                            }

                            changes.customString = input
                            playerWhoClicked.sendMessage(Text.colorize("&aSuccessfully set unique id"))
                        } else {
                            playerWhoClicked.sendMessage(Text.colorize("&7Aborting..."))
                        }
                        QuestBook.openQuestMissionEditor(playerWhoClicked, changes)
                        return@SinglePrompt true
                    })
        })

        putButton(17, MissionButton.amount(changes))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onStrifeMobKill(event: UniqueKillEvent) {
        for (missionEntry in QuestWorld.getMissionEntries(this, event.killer)) {
            val uniqueId = missionEntry.mission
            if (uniqueId.customString == event.entity.uniqueEntityId) {
                missionEntry.progress += 1
            }
        }
    }
}
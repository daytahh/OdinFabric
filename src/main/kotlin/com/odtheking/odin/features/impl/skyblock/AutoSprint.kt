package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.ChatManager.hideMessage
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.toFixed

object AutoSprint : Module(name = "Auto Sprint", description = "Automatically makes you sprint.") {
    private val filter by BooleanSetting("filter", false, desc = "")
    private val stormtimer by BooleanSetting("storm timer", false, desc = "")
    private val stormhud by HUD("storm hud", "", false) {
        if (!stormtimer || (!it && !shouldrender)) return@HUD 0 to 0
        val color = if (ticks < 620) Colors.MINECRAFT_GOLD else Colors.MINECRAFT_GREEN
        textDim("${(ticks / 20f).toFixed()}s", 0, 0, color)
    }

    private var shouldrender = false
    private var ticks = 0

    private val v = listOf(
        Regex("RIGHT CLICK on (?:a WITHER door|the BLOOD DOOR) to open it. This key can only be used to open 1 door!"),
        Regex("\\[SKULL] Wither Skull: .+"),
//        Regex("\\[BOSS] .+"),
//        Regex("\\[STATUE] Oruo the Omniscient: .+"),
        Regex("You hear the sound of something opening..."),
        Regex("Someone has already activated this lever!"),
        Regex("This lever has already been used."),
        Regex("(?:\\[\\S+] )?\\w+ has obtained (?:Revive Stone|Superboom TNT(?: x2)?|Premium Flesh|Beating Heart|Vitamin Death|Diamond Atom|Blessing of \\w+)!?"),
        Regex("DUNGEON BUFF! .+"),
        Regex("A Blessing of \\w+ was picked up!"),
        Regex(" {5}Granted you .+\\."),
        Regex(" {5}Also granted you \\+[\\d.]+ & \\+[\\d.]+x ☠ Crit Damage."),
//        Regex("You must pick the lock on this chest to open it!"),
//        Regex("Quickly aim at the particles on the chest 5 times in a row to unlock it."),
        Regex("You are not allowed to use Potion Effects while in Dungeon, therefore all active effects have been paused and stored. They will be restored when you leave Dungeon!"),
        Regex("ESSENCE! \\w+ found x\\d+ \\w+ Essence!"),
        Regex("\\w+ formed a tether with \\w+!"),
        Regex("◕ \\w+ picked up your .+ Orb!"),
        Regex("Your tether with \\w+ healed you for [\\d,.]+ health."),
        Regex("◕ You picked up a .+ Orb from \\w+ healing you for .+❤ and granting you .+ for 10 seconds."),
        Regex("(?:Wish|Thunderstorm|Ragnarok|Rapid Fire|Castle of Stone) is ready to use! Press DROP to activate it!"),
        Regex("Your (?:Healer|Mage|Berserk|Archer|Tank) ULTIMATE .+ is now available!"),
        Regex("Used (?:Healing Circle|Guided Sheep|Throwing Axe)!"),
        Regex("(?:Healing Circle|Guided Sheep|Throwing Axe) is now available!"),
        Regex("\\[(?:Healer|Mage|Berserk|Archer|Tank)] .+"),
//        Regex("Your \\w+ stats are doubled because you are the only player using this class!"),
        Regex("Your bone plating reduced the damage you took by [\\d,.]+!"),
        Regex("Party > (?:\\[\\S+] )?\\w+: (?:UwUaddons »|\\[(?:Skyblocker|Dingus ?Client)]:?).*"),
        Regex("\\[NPC] Elle: .+"),
        Regex("Your Auto Recombobulator recombobulated .+!"),
        Regex("\\w+ is no(?:w| longer) ready!"),
        Regex("You cannot use abilities in this room!"),
        Regex("You don't have enough charges to break this block right now!"),
        Regex("A mystical force prevents you from digging that block!"),
        Regex("A mystical force prevents you digging there!"),
        Regex("\\w+ Milestone \\S: You have .+ [\\d,]+ .+! .+s"),
        // blooms list
        Regex("Your .+ hit [\\d,]+ \\w+ for [\\d,.]+ damage\\."),
        Regex("There are blocks in the way!"),
        Regex("\\[NPC] Mort: .+"),
        Regex("^\\+\\d+ Kill Combo.*"),
//        Regex("\\w+(?:'s Wish )?healed you for [\\d,.]+ health.*!"),
        Regex("You earned \\d+ GEXP (?:\\+ [\\d,]+ Event EXP )?from playing .+!"),
        Regex("(?:\\[\\S+] )?\\w+ unlocked \\w+ Essence(?: x\\d+)?!"),
        Regex("This ability is on cooldown for \\d+s."),
        Regex("You do not have the key for this door!"),
        Regex("The Stormy .+ struck you for [\\d,.]+ damage!"),
        Regex("Please wait a few seconds between refreshing!"),
        Regex("This chest has already been searched!"),
        Regex("You cannot (?:move|hit) the silverfish .+!"),
        Regex("Your Kill Combo has expired! You reached a [\\d,]+ Kill Combo!"),
//        Regex("The Flamethrower hit you for [\\d,.]+ damage!"),
        Regex("\\w+ found a Wither Essence! Everyone gains an extra essence!"),
        Regex("This creature is immune to this kind of magic!"),

//        Regex("  ➤ You have reached your Hype limit! Add Hype to Prototype Lobby minigames by right-clicking with the Hype Diamond!"),
        Regex("\\[MVP\\+] \\w+ (?:joined|spooked|slid) (?:into )?the lobby!"),
        Regex(" >>> \\[MVP\\+\\+] \\w+ (?:joined|spooked|slid) (?:into )?the lobby! <<<"),
        Regex("✦ You earned \\d+ (?:Mystery Dust|Pet Consumables)!"),
        Regex("\\[WATCHDOG ANNOUNCEMENT]"),
        Regex("Watchdog has banned [\\d,]+ players in the last 7 days."),
        Regex("Staff have banned an additional [\\d,]+ in the last 7 days."),
        Regex("Blacklisted modifications are a bannable offense!"),
        Regex("Cross-teaming is not allowed! Report cross-teamers using /report."),
        Regex("If you get disconnected use /rejoin to join back in the game."),
        // /-*>newLine<-➲ .+ Gain XP and coins by » CLICKING HERE! «/,
        Regex("Buy Network Boosters at https://store.hypixel.net"),
        Regex("(?:Coins|Tokens) just earned DOUBLED as a Guild Level Reward!"),
    )

    private val bossMessages = setOf(
        "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.",
        // "[BOSS] Storm: ENERGY HEED MY CALL!",
        // "[BOSS] Storm: THUNDER LET ME BE YOUR CATALYST!",
        "[BOSS] Wither King: I no longer wish to fight, but I know that will not stop you.",
        "[BOSS] The Watcher: Let's see how you can handle this."
    )

    init {
        on<WorldEvent.Load> { shouldrender = false }

        on<TickEvent.Server> { if (shouldrender) ticks++ }

        on<ChatPacketEvent> {
            if (stormtimer) {
                if (value == "[BOSS] Storm: Pathetic Maxor, just like expected.") {
                    ticks = 0
                    shouldrender = true
                }
                if (value == "[BOSS] Goldor: Who dares trespass into my domain?") shouldrender = false
            }
            if (!filter) return@on
            if (value.startsWith("[BOSS]") && !bossMessages.contains(value)) hideMessage()
            else if (v.any { it.matches(value) }) hideMessage()
        }
    }
}
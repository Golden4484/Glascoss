package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class LookupTool implements Tool {
    private static final Map<String, String> KNOWLEDGE_BASE = new HashMap<>();

    static {
        KNOWLEDGE_BASE.put("summon_nbt", """
            /summon <entity> <x> <y> <z> {NBT}
            
            Common NBT for summon:
            - Tags: ["tag1","tag2"] — add scoreboard tags
            - CustomName: '{"text":"Name"}' or '"Name"' — display name
            - CustomNameVisible: 1b/0b — always show name
            - PersistenceRequired: 1b — never despawn
            - Silent: 1b — no sounds
            - NoAI: 1b — disable AI (statue mode)
            - NoGravity: 1b — floats in place
            - Invulnerable: 1b — cannot take damage
            - Glowing: 1b — glowing outline
            - Health: 20f — set HP
            - Attributes: [{Name:"generic.max_health",Base:40}] — custom attributes
            
            Equipment (for mobs):
            - ArmorItems: [{Count:1,id:"minecraft:diamond_helmet"},{id:"diamond_chestplate"},{},{id:"diamond_boots"}]
            - HandItems: [{id:"diamond_sword",Count:1},{}]
            
            Example with armor:
            /summon minecraft:zombie ~ ~1 ~ {CustomName:'"Guard"',ArmorItems:[{id:"iron_helmet",Count:1},{id:"iron_chestplate",Count:1},{id:"iron_leggings",Count:1},{id:"iron_boots",Count:1}],HandItems:[{id:"iron_sword",Count:1},{}],Tags:["my_mob"],PersistenceRequired:1b}
            
            Example armor stand:
            /summon minecraft:armor_stand ~ ~ ~ {NoGravity:1b,Invisible:1b,ShowArms:1b,Pose:{RightArm:[0f,0f,0f],LeftArm:[0f,0f,0f]},ArmorItems:[{id:"diamond_helmet",Count:1},{},{},{}],HandItems:[{id:"shield",Count:1},{}]}
            
            Example tamed wolf:
            /summon minecraft:wolf ~ ~ ~ {Owner:[I;0,0,0,0],Sitting:0b,CollarColor:0}
            NOTE: Owner requires the player UUID in int-array format. Use /data merge instead.
            """);

        KNOWLEDGE_BASE.put("item_nbt", """
            Item NBT format: {id:"minecraft:item_name",Count:1,tag:{...}}
            
            Enchantments:
            {Enchantments:[{id:"minecraft:sharpness",lvl:5},{id:"minecraft:unbreaking",lvl:3}]}
            
            Custom potion effects (for potions, arrows, food):
            {CustomPotionEffects:[{Id:1,Amplifier:2,Duration:3600,Ambient:0b,ShowParticles:1b}]}
            
            Effect IDs: 1=speed, 2=slowness, 3=haste, 4=mining_fatigue, 5=strength, 6=instant_health, 7=instant_damage, 8=jump_boost, 9=nausea, 10=regeneration, 11=resistance, 12=fire_resistance, 13=water_breathing, 14=invisibility, 15=blindness, 16=night_vision, 17=hunger, 18=weakness, 19=poison, 20=wither, 21=health_boost, 22=absorption, 23=saturation, 24=glowing, 25=levitation, 26=luck, 27=unluck, 28=slow_falling, 29=conduit_power, 30=dolphins_grace, 31=bad_omen, 32=hero_of_the_village, 33=darkness
            
            Item with enchant:
            /give @p minecraft:diamond_sword{Enchantments:[{id:"minecraft:sharpness",lvl:10},{id:"minecraft:fire_aspect",lvl:2}]} 1
            
            Item with effects (when eaten):
            /give @p minecraft:golden_apple{Effects:[{Id:10,Amplifier:2,Duration:100}]} 1
            
            Potion with custom color:
            /give @p minecraft:potion{CustomPotionColor:16711680,CustomPotionEffects:[{Id:1,Amplifier:5,Duration:3600}]} 1
            
            Rename item:
            /give @p minecraft:diamond_sword{display:{Name:'{"text":"Excalibur","color":"gold"}'}} 1
            
            Lore:
            /give @p minecraft:diamond_sword{display:{Lore:['{"text":"Legendary sword"}','{"text":"+10 damage","color":"red"}']}} 1
            
            Skull with custom player:
            /give @p minecraft:player_head{SkullOwner:"PlayerName"} 1
            
            Bundle with items:
            /give @p minecraft:bundle{Items:[{id:"diamond",Count:1},{id:"emerald",Count:5}]} 1
            """);

        KNOWLEDGE_BASE.put("block_states", """
            Block states use [property=value,property=value] after the block ID.
            Examples:
            - minecraft:oak_door[facing=east,half=lower,hinge=left,open=false]
            - minecraft:oak_fence[east=true,north=false,south=true,west=false,waterlogged=false]
            - minecraft:oak_stairs[facing=north,half=bottom,shape=straight]
            - minecraft:chest[facing=east,type=single,waterlogged=false]
            - minecraft:furnace[facing=north,lit=false]
            - minecraft:redstone_lamp[lit=true]
            - minecraft:campfire[lit=true,waterlogged=false]
            - minecraft:note_block[note=12,instrument=flute]
            - minecraft:repeater[delay=2,facing=east,locked=false,powered=false]
            - minecraft:slab[type=bottom,waterlogged=false] (bottom/top/double for slabs)
            
            Common properties:
            - facing: north, south, east, west (direction the block faces)
            - half: lower, upper (for doors, beds, tall grass)
            - half: bottom, top (for stairs, slabs)
            - hinge: left, right (for doors)
            - open: true, false (for doors, trapdoors, fence gates)
            - powered: true, false (for redstone components)
            - lit: true, false (for furnaces, lamps)
            - waterlogged: true, false
            - axis: x, y, z (for logs, pillars)
            - type: single, left, right (for chests)
            """);

        KNOWLEDGE_BASE.put("potion_effects", """
            Effect IDs for /effect and CustomPotionEffects:
            
            1  = speed           — move faster
            2  = slowness         — move slower
            3  = haste            — mine faster
            4  = mining_fatigue   — mine slower
            5  = strength         — more melee damage
            6  = instant_health   — heal instantly
            7  = instant_damage   — damage instantly
            8  = jump_boost       — jump higher
            9  = nausea           — distorted vision
            10 = regeneration     — heal over time
            11 = resistance       — damage reduction
            12 = fire_resistance  — immune to fire
            13 = water_breathing  — breathe underwater
            14 = invisibility     — become invisible
            15 = blindness        — black fog
            16 = night_vision     — see in dark
            17 = hunger           — get hungry faster
            18 = weakness         — less damage
            19 = poison           — damage over time
            20 = wither           — wither damage
            21 = health_boost     — bonus HP
            22 = absorption       — absorption HP
            23 = saturation       — restore food
            24 = glowing          — glowing outline
            25 = levitation       — float up
            26 = luck             — better loot
            27 = unluck           — worse loot
            28 = slow_falling     — fall slowly
            29 = conduit_power    — underwater buffs
            30 = dolphins_grace   — swim faster
            31 = bad_omen         — triggers raid
            32 = hero_of_village  — discounts from villagers
            
            /effect give @p minecraft:<effect> <duration_seconds> <amplifier>
            Amplifier 0 = level I, 1 = level II, etc.
            Duration in seconds (20 ticks per second).
            Use 999999 for effectively infinite.
            Use amplifier 255 for maximum effect.
            
            Examples:
            - /effect give @p minecraft:speed 60 2     — speed III for 60s
            - /effect give @p minecraft:resistance 9999 255 — nearly invulnerable
            - /effect give @p minecraft:levitation 1 200   — levitate while holding space
            """);

        KNOWLEDGE_BASE.put("enchantments", """
            Enchantment IDs for items:
            sword: sharpness, smite, bane_of_arthropods, fire_aspect, looting, knockback, sweeping_edge
            bow: power, punch, flame, infinity
            crossbow: piercing, multishot, quick_charge
            trident: impaling, loyalty, riptide, channeling
            pickaxe/shovel/axe: efficiency, fortune, silk_touch
            helmet: protection, fire_protection, blast_protection, projectile_protection, respiration, aqua_affinity, thorns, unbreaking, mending
            chestplate: protection, fire_protection, blast_protection, projectile_protection, thorns, unbreaking, mending
            leggings: protection, fire_protection, blast_protection, projectile_protection, thorns, unbreaking, mending, swift_sneak
            boots: protection, fire_protection, blast_protection, projectile_protection, feather_falling, thorns, unbreaking, mending, depth_strider, frost_walker, soul_speed
            fishing_rod: luck_of_the_sea, lure, unbreaking, mending
            helmet/elytra: binding_curse
            any: mending, unbreaking, vanishing_curse
            
            Max levels (vanilla):
            protection=4, fire_protection=4, blast_protection=4, projectile_protection=4
            feather_falling=4, thorns=3, respiration=3, depth_strider=3, swift_sneak=3
            sharpness=5, smite=5, bane_of_arthropods=5
            efficiency=5, fortune=3, silk_touch=1
            unbreaking=3, looting=3, fire_aspect=2, knockback=2
            power=5, punch=2, flame=1, infinity=1
            mending=1, soul_speed=3, frost_walker=2
            binding_curse=1, vanishing_curse=1
            
            You can use /give with any level, even above max:
            /give @p minecraft:diamond_sword{Enchantments:[{id:"sharpness",lvl:255},{id:"fire_aspect",lvl:10}]} 1
            """);

        KNOWLEDGE_BASE.put("entities", """
            Available entity types for /summon:
            
            Passive: minecraft:allay, axolotl, bat, bee, camel, cat, chicken, cod, cow, dolphin, donkey, fox, frog, glow_squid, goat, horse, llama, mooshroom, mule, ocelot, panda, parrot, pig, polar_bear, pufferfish, rabbit, salmon, sheep, skeleton_horse, sniffer, snow_golem, squid, strider, tadpole, tropical_fish, turtle, villager, wandering_trader, zoglin, zombie_horse
            
            Neutral: minecraft:bee (angry variant), cave_spider, enderman, iron_golem, llama (spit), panda (aggressive), piglin, polar_bear (aggressive), spider, wolf (wild), zombie_piglin
            
            Hostile: minecraft:blaze, bogged, creeper, drowned, elder_guardian, ender_dragon, endermite, evoker, ghast, giant, guardan, hoglin, husk, illusioner, magma_cube, phantom, piglin_brute, pillager, ravager, shulker, silverfish, skeleton, slime, stray, vex, vindicator, warder, witch, wither, wither_skeleton, zoglin, zombie, zombie_villager
            
            Utility: minecraft:area_effect_cloud, armor_stand, boat, chest_boat, chest_minecart, command_block_minecart, end_crystal, eye_of_ender, falling_block, firework_rocket, furnace_minecart, glow_item_frame, hopper_minecart, item, item_frame, leash_knot, lightning_bolt, marker, minecart, painting, tnt, tnt_minecart
            
            Useful utility entities:
            - armor_stand: can hold items, wear armor, pose
            - item_frame: display items on walls
            - painting: decoration
            - minecart: rides on rails
            - boat: floats on water
            - end_crystal: decoration (explodes if hit)
            - marker: invisible, no hitbox, used for commands
            - lightning_bolt: weather effect
            - tnt: explodes
            - falling_block: block that falls (can be placed mid-air)
            
            Villager types: desert, jungle, plains, savanna, snow, swamp, taiga
            Villager professions: none, armorer, butcher, cartographer, cleric, farmer, fisherman, fletcher, leatherworker, librarian, mason, nitwit, shepherd, toolsmith, weaponsmith
            """);

        KNOWLEDGE_BASE.put("structures", """
            Structure names for /locate and tp_to_structure:
            
            Overworld: village, desert_pyramid, igloo, jungle_pyramid, swamp_hut, pillager_outpost, mansion, monument, shipwreck, ocean_ruin, buried_treasure, mineshaft, stronghold, ruined_portal, ancient_city, trail_ruins, ruined_portal, amethyst_geode (not locateable)
            
            Nether: fortress, bastion_remnant, ruined_portal, nether_fossil
            
            End: end_city, end_gateway
            
            Use: /locate structure <name> in Minecraft 1.20.1
            Or use the tp_to_structure tool which automatically locates and teleports.
            """);

        KNOWLEDGE_BASE.put("blocks", """
            Common block categories:
            
            Building: stone, stone_bricks, cobblestone, andesite, diorite, granite, deepslate, tuff, calcite, sandstone, red_sandstone, bricks, nether_bricks, end_stone_bricks, blackstone, basalt, quartz_block, purpur_block, prismarine, dark_prismarine
            
            Wood types: oak, spruce, birch, jungle, acacia, dark_oak, cherry, mangrove, bamboo, crimson, warped
            
            Wood variants: _planks, _log, _wood, _stripped_log, _stripped_wood, _stairs, _slab, _fence, _fence_gate, _door, _trapdoor, _pressure_plate, _button, _sign, _hanging_sign
            
            Stone variants: _stairs, _slab, _wall, _button (stone), _pressure_plate (stone)
            
            Glass: glass, glass_pane, tinted_glass, (stained in 16 colors)
            
            Lighting: torch, lantern, soul_lantern, campfire, soul_campfire, glowstone, shroomlight, sea_lantern, jack_o_lantern, redstone_lamp, end_rod
            
            Redstone: redstone_wire, repeater, comparator, piston, sticky_piston, observer, dispenser, dropper, hopper, redstone_torch, lever, button, pressure_plate, note_block, jukebox, target, daylight_detector, sculk_sensor, calibrated_sculk_sensor
            
            Decoration: flower_pot, bookshelf, lectern, grindstone, stonecutter, loom, barrel, smoker, blast_furnace, campfire, bell, brewing_stand, cauldron, enchanting_table, anvil, ender_chest, respawn_anchor
            
            Full ID: minecraft:<block_name>
            Example: minecraft:oak_planks, minecraft:stone_bricks, minecraft:glass
            """);

        KNOWLEDGE_BASE.put("commands", """
            Useful Minecraft commands:
            
            GIVE: /give @p <item> [count]
            /give @p minecraft:diamond 64
            /give @p minecraft:diamond_sword{Enchantments:[{id:sharpness,lvl:10}]} 1
            
            EFFECT: /effect give @p <effect> <duration> <amplifier>
            /effect give @p minecraft:speed 60 2
            /effect clear @p — remove all effects
            
            TELEPORT: /tp @p <x> <y> <z>
            /tp @p ~ ~ ~ — relative teleport
            /tp @p <player> — teleport to player
            
            TIME: /time set day|noon|night|midnight|<tick>
            /time add <tick>
            
            WEATHER: /weather clear|rain|thunder <duration>
            
            GAMEMODE: /gamemode survival|creative|adventure|spectator @p
            
            GAMERULE: /gamerule doDaylightCycle false — stop day cycle
            /gamerule doWeatherCycle false — stop weather
            /gamerule keepInventory true — keep items on death
            /gamerule mobGriefing false — mobs don't destroy blocks
            /gamerule doMobSpawning false — no mobs spawn
            /gamerule falDamage false — no fall damage
            /gamerule doFireTick false — fire doesn't spread
            
            SUMMON: /summon <entity> [pos] [nbt]
            
            SETBLOCK: /setblock <x> <y> <z> <block> [destroy|replace]
            
            FILL: /fill <x1> <y1> <z1> <x2> <y2> <z2> <block> [destroy|replace|outline|hollow]
            Use 'outline' for just the outer layer, 'hollow' for outer with air inside.
            
            CLONE: /clone <x1> <y1> <z1> <x2> <y2> <z2> <x> <y> <z>
            
            KILL: /kill @e[type=minecraft:zombie,distance=..10]
            /kill @e[tag=glascoss_follower]
            
            DATA: /data get entity @p Pos — get player position
            /data merge entity @e[type=zombie,limit=1] {CustomName:'"NewName"'}
            /data merge block <x> <y> <z> {Lock:"password"}
            
            ATTRIBUTE: /attribute @p <attribute> base get|set|add <value>
            Attributes: generic.max_health, generic.follow_range, generic.movement_speed, generic.attack_damage, generic.armor, generic.step_height, generic.block_interaction_range
            
            SCOREBOARD: /scoreboard objectives add <name> <criteria>
            /scoreboard players set @p <objective> <value>
            /scoreboard players get @p <objective>
            
            SCHEDULE: /schedule function <namespace:function> <time>
            /schedule clear <namespace:function>
            """);

        KNOWLEDGE_BASE.put("attributes", """
            Player/Entity attributes that can be modified with /attribute:
            
            generic.max_health — max HP (default 20)
            generic.follow_range — how far mobs follow targets (default 16 for zombies)
            generic.knockback_resistance — resistance to knockback (0 to 1)
            generic.movement_speed — movement speed (default 0.7 for players)
            generic.attack_damage — base melee damage (default 1 for players)
            generic.attack_speed — attack speed (default 4)
            generic.armor — armor points (default 0)
            generic.armor_toughness — armor toughness (default 0)
            generic.luck — luck for loot tables (default 0)
            generic.step_height — how high entity steps (default 0.6)
            generic.block_interaction_range — reach distance (default 4.5)
            generic.entity_interaction_range — entity interaction range (default 3)
            generic.fal_damage_multiplier — fall damage multiplier (default 1)
            player.block_break_speed — mining speed (default 1)
            player.horse.jump_strength — horse jump
            
            Usage:
            /attribute @p minecraft:generic.movement_speed base set 1.0
            /attribute @p minecraft:generic.step_height base set 2.0
            /attribute @p minecraft:generic.block_interaction_range base set 10
            /attribute @p minecraft:generic.max_health base set 100
            """);

        KNOWLEDGE_BASE.put("effects_list", """
            Shorthand reference of useful effect combinations:
            
            FLIGHT: /effect give @p minecraft:slow_falling 9999 1 + /effect give @p minecraft:levitation 1 200
            SPEED: /effect give @p minecraft:speed 9999 5
            HASTE: /effect give @p minecraft:haste 9999 5 (mine faster)
            STRENGTH: /effect give @p minecraft:strength 9999 5
            RESISTANCE: /effect give @p minecraft:resistance 9999 4 (80% damage reduction)
            FIRE_RESIST: /effect give @p minecraft:fire_resistance 9999 1
            WATER_BREATH: /effect give @p minecraft:water_breathing 9999 1
            NIGHT_VISION: /effect give @p minecraft:night_vision 9999 1
            INVISIBILITY: /effect give @p minecraft:invisibility 9999 1
            JUMP_BOOST: /effect give @p minecraft:jump_boost 9999 5
            
            To give ALL useful buffs at once:
            /effect give @p minecraft:speed 9999 2
            /effect give @p minecraft:haste 9999 3
            /effect give @p minecraft:resistance 9999 4
            /effect give @p minecraft:fire_resistance 9999 1
            /effect give @p minecraft:water_breathing 9999 1
            /effect give @p minecraft:night_vision 9999 1
            /effect give @p minecraft:jump_boost 9999 3
            /effect give @p minecraft:regeneration 9999 2
            
            Remove all: /effect clear @p
            """);
    }

    @Override
    public String getName() { return "lookup"; }

    @Override
    public String getDescription() { return "Consult the encyclopedia for technical details. Parameters: topic (string). Topics: summon_nbt, item_nbt, block_states, potion_effects, enchantments, entities, structures, blocks, commands, attributes, effects_list"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String topic = ArgParser.getString(args, "topic", "").toLowerCase().trim();

            if (topic.isBlank()) {
                return "Available topics: summon_nbt, item_nbt, block_states, potion_effects, enchantments, entities, structures, blocks, commands, attributes, effects_list";
            }

            String content = KNOWLEDGE_BASE.get(topic);
            if (content == null) {
                return "Unknown topic: " + topic + ". Available: summon_nbt, item_nbt, block_states, potion_effects, enchantments, entities, structures, blocks, commands, attributes, effects_list";
            }

            return content.trim();
        } catch (Exception e) {
            return "lookup error: " + e.getMessage();
        }
    }
}

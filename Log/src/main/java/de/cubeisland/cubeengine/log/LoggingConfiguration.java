package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.YamlConfiguration;
import de.cubeisland.cubeengine.core.config.annotations.*;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@MapComments(
        {@MapComment(path = "logging.break.block.fade",text = "Ice and snow fading away"),
        @MapComment(path = "logging.block.break.flow",text = "Lava or water destroying blocks"),
        @MapComment(path = "logging.block.enderman",text = "Enderman breaking or placing blocks"),
        @MapComment(path = "logging.block.bucket",text = "Blocks placed or removed by using a bucket"),
        @MapComment(path = "logging.block.explode",text = "Blocks destroyed by various explosions"),
        @MapComment(path = "logging.block.grow",text = "Trees or mushrooms growing"),
        @MapComment(path = "logging.block.form",text = "Ice or Snow forming OR blocks formed by water and lava"),
        @MapComment(path = "logging.block.spread",text = "Fire ,Mushrooms or other Blocks spreading"),
        @MapComment(path = "logging.block.ignite",text = "Fire-Ignition by fireballs, lighter, lava or lightning"),
        @MapComment(path = "logging.block.flow",text = "Unhindered Lava or Water-flow. These can produce a lot of logs!"),
        @MapComment(path = "logging.death.killer",text = "Only log when the killer is"),
        @MapComment(path = "logging.container.type",text = "Container-types to log")
        }
)

public class LoggingConfiguration extends YamlConfiguration
{
    @Comment("Completly enables or disabled logging")
    @Option("logging.enable")
    public boolean enable = true;

    @Comment("Blocks destroyed by a player")
    @Option("logging.block.break.player")
    public boolean BLOCK_BREAK_enable = true;
    @Comment("Blocks destroyed by fire")
    @Option("logging.block.break.fire")
    public boolean BLOCK_BURN_enable = true;
    @Option("logging.block.break.fade.ice")

    public boolean BLOCK_FADE_ice = false;
    @Option("logging.block.break.fade.snow")
    public boolean BLOCK_FADE_snow = false;
    @Option("logging.block.break.fade.other")
    public boolean BLOCK_FADE_other = false;

    @Comment("Leaves decaying after breaking the wood nearby")
    @Option("logging.block.break.decay")
    public boolean LEAF_DECAY_enable = false;

    @Option("logging.block.break.flow.lava")
    public boolean WATER_BREAK_enable = true;
    @Option("logging.block.break.flow.water")
    public boolean LAVA_BREAK_enable = true;

    @Comment("Blocks destroyed by an entity. Usually doors fom zombies.")
    @Option("logging.block.break.by-entity")
    public boolean ENTITY_BREAK_enable = true;
    @Option("logging.block.enderman.pickup")
    public boolean ENDERMAN_PICKUP_enable = true;
    @Option("logging.block.bucket.fill")
    public boolean BUCKET_FILL_enable = true;

    @Comment("Filling a bucket with milk.")
    @Option("logging.use.fill-milk")
    public boolean BUCKET_FILL_milk = false;
    @Comment("Filling a bucket with milk.")
    @Option("logging.use.fill-soup")
    public boolean BOWL_FILL_SOUP = false;

    @Comment("Destroying soil & crops by walking or jumping on it")
    @Option("logging.block.break.trample")
    public boolean CROP_TRAMPLE_enable = false;
    @Comment("Igniting TNT directly with a lighter")
    @Option("logging.block.break.tnt-prime")
    public boolean TNT_PRIME_enable = true;

    @Comment("A List of all materials that will not be logged when destroyed.")
    @Option("logging.block.break.no-logging")
    public Collection<Material> breakNoLogging = new LinkedList<Material>();//TODO

    @Option("logging.block.explode.creeper")
    public boolean CREEPER_EXPLODE_enable = true;
    @Option("logging.block.explode.tnt")
    public boolean TNT_EXPLODE_enable = true;
    @Option("logging.block.explode.ghast-fireball")
    public boolean FIREBALL_EXPLODE_enable = true;
    @Option("logging.block.explode.enderdragon")
    public boolean ENDERDRAGON_EXPLODE_enable = true;
    @Option("logging.block.explode.wither")
    public boolean WITHER_EXPLODE_enable = true;
    @Comment("Explosions that are not caused by any of above reasons")
    @Option("logging.block.explode.other")
    public boolean ENTITY_EXPLODE_enable = false;

    @Option("logging.block.place.player")
    public boolean BLOCK_PLACE_enable = true;
    @Option("logging.block.bucket.lava")
    public boolean LAVA_BUCKET_enable = true;
    @Option("logging.block.bucket.water")
    public boolean WATER_BUCKET_enable = true;
    @Option("logging.block.grow.natural")
    public boolean NATURAL_GROW_enable = false;
    @Option("logging.block.grow.player")
    public boolean PLAYER_GROW_enable = true;
    @Option("logging.block.form.ice")
    public boolean BLOCK_FORM_ice = false;
    @Option("logging.block.form.snow")
    public boolean BLOCK_FORM_snow = false;
    @Option("logging.block.form.lava-water")
    public boolean BLOCK_FORM_lavaWater = true;
    @Option("logging.block.form.lava-water")
    public boolean BLOCK_FORM_other = true;
    @Option("logging.block.enderman.place")
    public boolean ENDERMAN_PLACE_enable = true;
    @Comment("Blocks created by entities (snowgolem)")
    @Option("logging.block.form.by-entity")
    public boolean ENTITY_FORM_enable = false;

    @Comment("A List of all materials that will not be logged when placed.")
    @Option("logging.block.place.no-logging")
    public Collection<Material> placeNoLogging = new LinkedList<Material>();//TODO

    @Option("logging.block.spread.fire")
    public boolean FIRE_SPREAD_enable = true;
    @Option("logging.block.ignite.fireball")
    public boolean FIREBALL_enable = false;
    @Option("logging.block.ignite.lighter")
    public boolean LIGHTER_enable = true;
    @Option("logging.block.ignite.lava")
    public boolean LAVA_IGNITE_enable = false;
    @Option("logging.block.ignite.lightning")
    public boolean LIGHTNING_enable = false;
    @Option("logging.block.ignite.other")
    public boolean OTHER_IGNITE_enable = false;
    @Option("logging.block.spread.other")
    public boolean BLOCK_SPREAD_enable = false;
    @Option("logging.block.flow.lava")
    public boolean LAVA_FLOW_enable = false;
    @Option("logging.block.flow.water")
    public boolean WATER_FLOW_enable = false;

    @Comment("Blocks moved by pistons")
    @Option("logging.block.change.piston")
    public boolean BLOCK_SHIFT_enable = false;
    @Comment("Blocks falling because of gravity (Sand, Gravel, Anvil)")
    @Option("logging.block.change.fall")
    public boolean BLOCK_FALL_enable = false;
    @Comment("Changing the lines of a sign")
    @Option("logging.block.change.sign")
    public boolean SIGN_CHANGE_enable = true;
    @Comment("Sheep converting Grass into Dirt")
    @Option("logging.block.change.sheep-eat")
    public boolean SHEEP_EAT_enable = false;
    @Option("logging.use.bonemeal")
    @Comment("Using bonemeal on a valid target")
    public boolean BONEMEAL_USE_enable = false;
    @Comment("Flipping levers")
    @Option("logging.block.change.lever")
    public boolean LEVER_USE_enable = false;
    @Comment("Changing Repeater settings")
    @Option("logging.block.change.repeater")
    public boolean REPEATER_CHANGE_enable = true;
    @Comment("Changing Comparator state")
    @Option("logging.block.change.comparator")
    public boolean COMPARATPR_CHANGE_enable = true;
    @Comment("Changing Noteblock settings")
    @Option("logging.block.change.note-block")
    public boolean NOTEBLOCK_CHANGE_enable = true;
    @Comment("Opening or closing doors")
    @Option("logging.block.change.door")
    public boolean DOOR_USE_enable = false;
    @Comment("Eating cake")
    @Option("logging.block.change.cake")
    public boolean CAKE_EAT_enable = true;
    @Comment("Log every worldedit change. WARNING! Big Worldedit-actions could crash your server!")
    @Option("logging.block.worldedit")
    public boolean WORLDEDIT_enable = false;

    @Comment("Player looking into a container")
    @Option("logging.container.access")
    public boolean CONTAINER_ACCESS_enable = true;
    @Comment("Pushing a button")
    @Option("logging.use.button")
    public boolean BUTTON_USE_enable = false;
    @Comment("Using firework-rockets")
    @Option("logging.use.firework")
    public boolean FIREWORK_USE_enable = false;

    @Comment("Enter a boat or minecart")
    @Option("logging.vehicle.enter")
    public boolean VEHICLE_ENTER_enable = false;
    @Comment("Exit a boat or minecart")
    @Option("logging.vehicle.exit")
    public boolean VEHICLE_EXIT_enable = false;
    @Comment("Thrown splash-potions")
    @Option("logging.use.splash-potion")
    public boolean POTION_SPLASH_enable = false;
    @Comment("Walking on pressure-plates")
    @Option("logging.use.pressure-plate")
    public boolean PLATE_STEP_enable = false;

    @Comment("Placing a boat or minecart")
    @Option("logging.vehicle.place")
    public boolean VEHICLE_PLACE_enable = false;
    @Comment("Placing a painting or itemframe")
    @Option("logging.hanging.place")
    public boolean HANGING_PLACE_enable = true;
    @Comment("Breaking a boat or minecart")
    @Option("logging.vehicle.break")
    public boolean VEHICLE_BREAK_enable = false;
    @Comment("Breaking a painting or itemframe")
    @Option("logging.hanging.break")
    public boolean HANGING_BREAK_enable = true;

    @Option("logging.death.killer.player")
    public boolean PLAYER_KILL_enable = true;
    @Option("logging.death.killer.entity")
    public boolean ENTITY_KILL_enable = false;
    @Option("logging.death.killer.environment")
    public boolean ENVIRONMENT_KILL_enable = false;
    @Option("logging.death.killer.boss")
    public boolean BOSS_KILL_enable = true;
    //TODO other kill options ? lightning, fall-damage, drowning, suffocation, cacti, starvation , lava

    @Option("logging.death.player")
    public boolean PLAYER_DEATH_enable = true;
    @Option("logging.death.monster")
    public boolean MONSTER_DEATH_enable = false;
    @Comment("Animal-Death: Chicken,Pig,Cow,Sheep,Wolf,Ocelot")
    @Option("logging.death.animal")
    public boolean ANIMAL_DEATH_enable = true;
    @Comment("Pet-Death: Tamed Wolf,Ocelot")
    @Option("logging.death.pet")
    public boolean PET_DEATH_enable = true;
    @Comment("Villager-Death")
    @Option("logging.death.npc")
    public boolean NPC_DEATH_enable = false;
    @Option("logging.death.boss")
    public boolean BOSS_DEATH_enable = true;
    @Comment("Other-Death: Golems,Squids,Bats")
    @Option("logging.death.other")
    public boolean OTHER_DEATH_enable = false;

    @Comment("Entity spawned using a monster-egg")
    @Option("logging.spawn.monster-egg")
    public boolean MONSTER_EGG_USE_enable = true;
    @Comment("Entity naturally spawning. This will cause A LOT of logs!")
    @Option("logging.spawn.natural")
    public boolean NATURAL_SPAWN_enable = false;
    @Comment("Entity spawned by a spawner")
    @Option("logging.spawn.spawner")
    public boolean SPAWNER_SPAWN_enable = false;
    @Comment("Entity spawned indirectly by a player")
    @Option("logging.spawn.other")
    public boolean OTHER_SPAWN_enable = false;

    @Comment("Items dropped by a player OR on death")
    @Option("logging.item.drop")
    public boolean ITEM_DROP_enable = false;
    @Comment("Items picked up by a player")
    //TODO CE-343 Log Zombies picking up items
    @Option("logging.item.pickup")
    public boolean ITEM_PICKUP_enable = false;
    @Comment("Exp gained")
    @Option("logging.exp-pickup")
    public boolean XP_PICKUP_enable = false;
    @Comment("Shearing Sheeps or Mooshrooms")
    @Option("logging.use.shear")
    public boolean ENTITY_SHEAR_enable = false;
    @Comment("Dyeing Sheeps or Wolf-collars")
    @Option("logging.use.dye")
    public boolean ENTITY_DYE_enable = false;

    @Comment("Putting items into a container")
    @Option("logging.container.insert")
    public boolean ITEM_INSERT_enable = true;
    @Comment("Taking items out of a container")
    @Option("logging.container.remove")
    public boolean ITEM_REMOVE_enable = true;
    @Comment("Items moved by a hopper or dropper")
    @Option("logging.container.transfer")
    public boolean ITEM_TRANSFER_enable = true;

    @Option("logging.container.type.chest")
    public boolean containerChest = true;
    @Option("logging.container.type.furnace")
    public boolean containerFurnace = true;
    @Option("logging.container.type.brewingstand")
    public boolean containerBrewingstand = true;
    @Option("logging.container.type.dispenser")
    public boolean containerDispenser = true;
    @Option("logging.container.type.minecart")
    public boolean containerMinecart = false;
    @Option("logging.container.type.hopper")
    public boolean containerHopper = false;
    @Option("logging.container.type.dropper")
    public boolean containerDropper = false;

    @Comment("Commands used by a player")
    @Option("logging.command.player")
    public boolean PLAYER_COMMAND_enable = false;

    @Comment("Commands to ignore when logging")
    @Option("logging.command.ignore-commands")
    public List<String> PLAYER_COMMAND_ignoreRegex = new ArrayList<String>();{
    {
        PLAYER_COMMAND_ignoreRegex.add("(ce|cubeengine) (login|setpassword|setpw) .+");
    }}

    @Comment("Commands used by the console")
    @Option("logging.command.console")
    public boolean CONSOLE_COMMAND_enable = false;
    @Comment("The normal player chat")
    @Option("logging.player.chat")
    public boolean PLAYER_CHAT_enable = false;
    @Comment("Players joining")
    @Option("logging.player.join")
    public boolean PLAYER_JOIN_enable = true;
    @Comment("Log the players ip when joining")
    @Option("logging.player.join-log-ip")
    public boolean PLAYER_JOIN_ip = false;
    @Comment("Players quiting")
    @Option("logging.player.quit")
    public boolean PLAYER_QUIT_enable = true;
    @Comment("Players teleporting")
    @Option("logging.player.teleport")
    public boolean PLAYER_TELEPORT_enable = false;
    @Comment("Players enchanting an item")
    @Option("logging.player.enchant")
    public boolean ENCHANT_ITEM_enable = true;
    @Comment("Players crafting an item")
    @Option("logging.player.craft")
    public boolean CRAFT_ITEM_enable = true;
}

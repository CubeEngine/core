/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.log.action.logaction.container.ContainerType;

public class LoggingConfiguration extends YamlConfiguration
{
    @Comment("Completely enables or disabled logging")
    @Name("logging.enable")
    public boolean enable = true;

    @Name("logging.block")
    public Block block = new Block();

    public class Block implements Section
    {
        @Comment("Blocks destroyed by a player")
        @Name("break.player")
        public boolean BLOCK_BREAK_enable = true;
        @Comment("Blocks destroyed by fire")
        @Name("break.fire")
        public boolean BLOCK_BURN_enable = true;

        @Name("break.fade")
        @Comment("Ice and snow fading away")
        public Fade fade = new Fade();

        public class Fade implements Section
        {
            public boolean enable = false;
            @Comment("The blocks not to log when fading away (ICE, SNOW, GRASS)")
            public Set<Material> ignore = new LinkedHashSet<>();
        }

        @Comment("Leaves decaying after breaking the wood nearby")
        @Name("break.decay")
        public boolean LEAF_DECAY_enable = false;

        @Name("break.flow")
        @Comment("Lava or water destroying blocks")
        public BreakFlow breakFlow = new BreakFlow();

        public class BreakFlow implements Section
        {
            @Name("water")
            public boolean WATER_BREAK_enable = true;
            @Name("lava")
            public boolean LAVA_BREAK_enable = true;
        }

        @Comment("Blocks destroyed by an entity. Usually doors fom zombies.")
        @Name("break.by-entity")
        public boolean ENTITY_BREAK_enable = true;

        @Comment("Enderman breaking or placing blocks")
        public EnderMan enderman = new EnderMan();

        public class EnderMan implements Section
        {
            @Name("pickup")
            public boolean ENDERMAN_PICKUP_enable = true;
            @Name("place")
            public boolean ENDERMAN_PLACE_enable = true;
        }

        @Comment("Blocks placed or removed by using a bucket")
        public Bucket bucket = new Bucket();

        public class Bucket implements Section
        {
            @Name("fill")
            public boolean BUCKET_FILL_enable = true;
            @Name("lava")
            public boolean LAVA_BUCKET_enable = true;
            @Name("water")
            public boolean WATER_BUCKET_enable = true;
        }

        @Comment("Destroying soil & crops by walking or jumping on it")
        @Name("break.trample")
        public boolean CROP_TRAMPLE_enable = false;
        @Comment("Igniting TNT directly with a lighter")
        @Name("break.tnt-prime")
        public boolean TNT_PRIME_enable = true;

        @Comment("A List of all materials that will not be logged when destroyed.")
        @Name("break.no-logging")
        public Set<Material> breakNoLogging = new LinkedHashSet<>();

        @Comment("Blocks destroyed by various explosions")
        public Explode explode = new Explode();

        public class Explode implements Section
        {
            @Name("creeper")
            public boolean CREEPER_EXPLODE_enable = true;
            @Name("tnt")
            public boolean TNT_EXPLODE_enable = true;
            @Name("ghast-fireball")
            public boolean FIREBALL_EXPLODE_enable = true;
            @Name("enderdragon")
            public boolean ENDERDRAGON_EXPLODE_enable = true;
            @Name("wither")
            public boolean WITHER_EXPLODE_enable = true;
            @Comment("Explosions that are not caused by any of above reasons")
            @Name("other")
            public boolean ENTITY_EXPLODE_enable = false;
        }

        @Name("place.player")
        public boolean BLOCK_PLACE_enable = true;

        @Comment("Trees or mushrooms growing")
        public Grow grow = new Grow();
        public class Grow implements Section
        {
            @Name("natural")
            public boolean NATURAL_GROW_enable = false;
            @Name("player")
            public boolean PLAYER_GROW_enable = true;
        }

        @Comment("Ice or Snow forming OR blocks formed by water and lava")
        public Form form = new Form();
        public class Form implements Section
        {
            @Name("enable")
            public boolean BLOCK_FORM_enable = true;
            @Comment("The blocks not to log when forming away (ICE, SNOW, COBBLESTONE, STONE, OBSIDIAN, GRASS?)")
            @Name("ignore")
            public Set<Material> BLOCK_FORM_ignore = new LinkedHashSet<>();
            @Comment("Blocks created by entities (snowgolem)")
            @Name("by-entity")
            public boolean ENTITY_FORM_enable = false;
        }

        @Comment("A List of all materials that will not be logged when placed.")
        @Name("place.no-logging")
        public Collection<Material> placeNoLogging = new LinkedList<>();

        @Comment("Fire ,Mushrooms or other Blocks spreading")
        public Spread spread = new Spread();
        public class Spread implements Section
        {
            @Name("fire")
            public boolean FIRE_SPREAD_enable = true;
            @Name("other")
            public boolean BLOCK_SPREAD_enable = false;
        }

        @Comment("Fire-Ignition by fireballs, lighter, lava or lightning")
        public Ignite ignite = new Ignite();
        public class Ignite implements Section
        {
            @Name("fireball")
            public boolean FIREBALL_IGNITE_enable = false;
            @Name("lighter")
            public boolean LIGHTER_IGNITE_enable = true;
            @Name("lava")
            public boolean LAVA_IGNITE_enable = false;
            @Name("lightning")
            public boolean LIGHTNING_IGNITE_enable = false;
            @Name("other")
            public boolean OTHER_IGNITE_enable = false;
        }

        @Comment("Unhindered Lava or Water-flow. These can produce a lot of logs!")
        public Flow flow = new Flow();
        public class Flow implements Section
        {
            @Name("lava")
            public boolean LAVA_FLOW_enable = false;
            @Name("water")
            public boolean WATER_FLOW_enable = false;
        }

        @Comment("Blocks moved by pistons")
        @Name("change.piston")
        public boolean BLOCK_SHIFT_enable = false;
        @Comment("Blocks falling because of gravity (Sand, Gravel, Anvil)")
        @Name("change.fall")
        public boolean BLOCK_FALL_enable = false;
        @Comment("Changing the lines of a sign")
        @Name("change.sign")
        public boolean SIGN_CHANGE_enable = true;
        @Comment("Sheep converting Grass into Dirt")
        @Name("change.sheep-eat")
        public boolean SHEEP_EAT_enable = false;
        @Name("logging.use.bonemeal")
        @Comment("Using bonemeal on a valid target")
        public boolean BONEMEAL_USE_enable = false;
        @Comment("Flipping levers")
        @Name("change.lever")
        public boolean LEVER_USE_enable = false;
        @Comment("Changing Repeater settings")
        @Name("change.repeater")
        public boolean REPEATER_CHANGE_enable = true;
        @Comment("Changing Comparator state")
        @Name("change.comparator")
        public boolean COMPARATPR_CHANGE_enable = true;
        @Comment("Changing Noteblock settings")
        @Name("change.note-block")
        public boolean NOTEBLOCK_CHANGE_enable = true;
        @Comment("Opening or closing doors")
        @Name("change.door")
        public boolean DOOR_USE_enable = false;
        @Comment("Eating cake")
        @Name("change.cake")
        public boolean CAKE_EAT_enable = true;
        @Comment("Log every worldedit change. WARNING! Big Worldedit-actions could crash your server!")
        @Name("worldedit")
        public boolean WORLDEDIT_enable = false;
    }

    @Comment("Filling a bucket with milk.")
    @Name("logging.use.fill-milk")
    public boolean BUCKET_FILL_milk = false;
    @Comment("Filling a bucket with milk.")
    @Name("logging.use.fill-soup")
    public boolean BOWL_FILL_SOUP = false;

    @Comment("Container-types to log")
    @Name("logging.container")
    public Container container = new Container();
    public class Container implements Section
    {
        @Comment("Player looking into a container")
        @Name("access")
        public boolean CONTAINER_ACCESS_enable = true;
        @Comment("Putting items into a container")
        @Name("insert")
        public boolean ITEM_INSERT_enable = true;
        @Comment("Taking items out of a container")
        @Name("remove")
        public boolean ITEM_REMOVE_enable = true;
        @Comment("Items moved by a hopper or dropper")
        @Name("transfer.enable")
        public boolean ITEM_TRANSFER_enable = true;
        @Comment("Items to ignore when moved by a hopper or dropper")
        @Name("transfer.ignore")
        public Set<Material> ITEM_TRANSFER_ignore = new LinkedHashSet<Material>()
        {
            {
                this.add(Material.EGG);
                this.add(Material.MELON);
                this.add(Material.PUMPKIN);
                this.add(Material.SUGAR_CANE);
                this.add(Material.FEATHER);
                this.add(Material.RAW_CHICKEN);
            }
        };

        @Comment("InventoryTypes to ignore (chest,furnace,dispenser,dropper,hopper,brewing-stand,storage-minecart)")
        @Name("ignored-types")
        public Set<ContainerType> CONTAINER_ignore = new LinkedHashSet<>();
    }

    @Comment("Pushing a button")
    @Name("logging.use.button")
    public boolean BUTTON_USE_enable = false;
    @Comment("Using firework-rockets")
    @Name("logging.use.firework")
    public boolean FIREWORK_USE_enable = false;

    @Comment("Enter a boat or minecart")
    @Name("logging.vehicle.enter")
    public boolean VEHICLE_ENTER_enable = false;
    @Comment("Exit a boat or minecart")
    @Name("logging.vehicle.exit")
    public boolean VEHICLE_EXIT_enable = false;
    @Comment("Thrown splash-potions")
    @Name("logging.use.splash-potion")
    public boolean POTION_SPLASH_enable = false;
    @Comment("Walking on pressure-plates")
    @Name("logging.use.pressure-plate")
    public boolean PLATE_STEP_enable = false;

    @Comment("Placing a boat or minecart")
    @Name("logging.vehicle.place")
    public boolean VEHICLE_PLACE_enable = false;
    @Comment("Placing a painting or itemframe")
    @Name("logging.hanging.place")
    public boolean HANGING_PLACE_enable = true;
    @Name("logging.hanging.remove-item-from-frame")
    public boolean ITEM_REMOVE_FROM_FRAME = true;
    @Comment("Breaking a boat or minecart")
    @Name("logging.vehicle.break")
    public boolean VEHICLE_BREAK_enable = false;
    @Comment("Breaking a painting or itemframe")
    @Name("logging.hanging.break")
    public boolean HANGING_BREAK_enable = true;

    @Name("logging.death")
    public Death death = new Death();
    public class Death implements Section
    {
        @Comment("Only log when the killer is")
        public Killer killer = new Killer();
        public class Killer implements Section
        {
            @Name("player")
            public boolean PLAYER_KILL_enable = true;
            @Name("entity")
            public boolean ENTITY_KILL_enable = false;
            @Name("environment")
            public boolean ENVIRONMENT_KILL_enable = false;
            @Name("boss")
            public boolean BOSS_KILL_enable = true;
            //TODO other kill options ? lightning, fall-damage, drowning, suffocation, cacti, starvation , lava
        }

        @Name("player")
        public boolean PLAYER_DEATH_enable = true;
        @Name("monster")
        public boolean MONSTER_DEATH_enable = false;
        @Comment("Animal-Death: Chicken,Pig,Cow,Sheep,Wolf,Ocelot")
        @Name("animal")
        public boolean ANIMAL_DEATH_enable = true;
        @Comment("Pet-Death: Tamed Wolf,Ocelot")
        @Name("pet")
        public boolean PET_DEATH_enable = true;
        @Comment("Villager-Death")
        @Name("npc")
        public boolean NPC_DEATH_enable = true;
        @Name("boss")
        public boolean BOSS_DEATH_enable = true;
        @Comment("Other-Death: Golems,Squids,Bats")
        @Name("other")
        public boolean OTHER_DEATH_enable = false;
    }

    @Comment("Entity spawned using a monster-egg")
    @Name("logging.spawn.monster-egg")
    public boolean MONSTER_EGG_USE_enable = true;
    @Comment("Entity naturally spawning. This will cause A LOT of logs!")
    @Name("logging.spawn.natural")
    public boolean NATURAL_SPAWN_enable = false;
    @Comment("Entity spawned by a spawner")
    @Name("logging.spawn.spawner")
    public boolean SPAWNER_SPAWN_enable = false;
    @Comment("Entity spawned indirectly by a player")
    @Name("logging.spawn.other")
    public boolean OTHER_SPAWN_enable = false;

    @Comment("Items dropped by a player OR on death")
    @Name("logging.item.drop")
    public boolean ITEM_DROP_enable = false;
    @Comment("Items picked up by a player")
    //TODO CE-343 Log Zombies picking up items (waiting for bukkit)
    @Name("logging.item.pickup")
    public boolean ITEM_PICKUP_enable = false;
    @Comment("Exp gained")
    @Name("logging.exp-pickup")
    public boolean XP_PICKUP_enable = false;
    @Comment("Shearing Sheeps or Mooshrooms")
    @Name("logging.use.shear")
    public boolean ENTITY_SHEAR_enable = false;
    @Comment("Dyeing Sheeps or Wolf-collars")
    @Name("logging.use.dye")
    public boolean ENTITY_DYE_enable = false;

    @Comment("Commands used by a player")
    @Name("logging.command.player")
    public boolean PLAYER_COMMAND_enable = false;

    @Comment("Commands to ignore when logging")
    @Name("logging.command.ignore-commands")
    public List<String> PLAYER_COMMAND_ignoreRegex = new ArrayList<>();{
    {
        PLAYER_COMMAND_ignoreRegex.add("(ce|cubeengine) (login|setpassword|setpw) .+");
    }}

    @Comment("The normal player chat")
    @Name("logging.player.chat")
    public boolean PLAYER_CHAT_enable = false;
    @Comment("Players joining")
    @Name("logging.player.join")
    public boolean PLAYER_JOIN_enable = true;
    @Comment("Log the players ip when joining")
    @Name("logging.player.join-log-ip")
    public boolean PLAYER_JOIN_ip = false;
    @Comment("Players quiting")
    @Name("logging.player.quit")
    public boolean PLAYER_QUIT_enable = true;
    @Comment("Players teleporting")
    @Name("logging.player.teleport")
    public boolean PLAYER_TELEPORT_enable = false;
    @Comment("Players enchanting an item")
    @Name("logging.player.enchant")
    public boolean ENCHANT_ITEM_enable = true;
    @Comment("Players crafting an item")
    @Name("logging.player.craft")
    public boolean CRAFT_ITEM_enable = true;

    @Override
    public String[] head()
    {
        if (this.getDefault() == this)
        {
            return new String[]{"This is the global configuration for logging.","Any settings here can be overwritten for each world in their configuration"};
        }
        return null;
    }
}

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;

import de.cubeisland.engine.log.action.player.item.container.ContainerType;
import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.reflect.Section;
import de.cubeisland.engine.reflect.annotations.Comment;

@SuppressWarnings("all")
public class LoggingConfiguration extends ReflectedYaml
{
    @Comment("Completely enables or disabled logging")
    public boolean enableLogging = true;

    public Block block = new Block();

    public class Block implements Section
    {
        @Comment("Blocks destroyed by a player")
        public boolean destroyByPlayer = true;
        @Comment("Blocks getting destroyed or moved by unknown means")
        public boolean destroyByOther = true;
        @Comment("Blocks destroyed by an entity. Usually doors fom zombies.")
        public boolean destroyByEntity = true;
        public boolean destroyByEnderman = true;
        @Comment("Blocks destroyed by fire")
        public boolean destroyByFire = true;

        @Comment("Leaves decaying after breaking the wood nearby")
        public boolean decay = false;
        @Comment("Ice and snow fading away")
        public LogFadeSection fade = new LogFadeSection();
        public class LogFadeSection implements Section
        {
            public boolean enable = false;
            @Comment("The blocks not to log when fading away (ICE, SNOW, GRASS)")
            public Set<Material> ignore = new LinkedHashSet<>();
        }

        public boolean placeByEnderman = true;

        @Comment("Sheep converting Grass into Dirt")
        public boolean sheepEat = false;
        @Comment("Destroying soil & crops by walking or jumping on it")
        public boolean trample = false;

        @Comment("Blocks destroyed by various explosions")
        public Explode explode = new Explode();

        public class Explode implements Section
        {
            public boolean creeper = true;
            public boolean tnt = true;
            public boolean fireball = true;
            public boolean enderdragon = true;
            public boolean wither = true;
            @Comment("Explosions that are not caused by any of above reasons")
            public boolean other = false;
        }

        @Comment("Blocks placed by players")
        public boolean placeByPlayer = true;

        @Comment("Trees or mushrooms growing naturally")
        public boolean growByNature = false;

        @Comment("Trees or mushrooms growing because of a player")
        public boolean growByPlayer = true;

        @Comment("Blocks forming")
        public boolean formByNature = true;
        @Comment("Blocks formed by an entity")
        public boolean formByEntity;
        @Comment("Blocks formed by water and lava")
        public boolean formByWaterLava;

        @Comment("Blocks spreading e.g. mushrooms")
        public boolean spreadByNature = false;

        public Flow flow = new Flow();
        public class Flow implements Section
        {
            @Comment("Unhindered Water-flow. These can produce a lot of logs!")
            public boolean water = false;
            @Comment("Water breaking blocks")
            public boolean waterBreak = true;
            @Comment("A WaterSource forming")
            public boolean waterForm = true;
            @Comment("Unhindered Lava-flow. These can produce a lot of logs!")
            public boolean lava = false;
            @Comment("Lava breaking blocks")
            public boolean lavaBreak = true;
        }

        @Comment("Blocks moved by pistons")
        public boolean shift = false;
        @Comment("Blocks falling because of gravity (Sand, Gravel, Anvil)")
        public boolean fall = false;
        @Comment("Changing the lines of a sign")
        public boolean signChange = true;

        @Comment("Log every worldedit change. WARNING! Big Worldedit-actions could crash your server!")
        public boolean worldedit = false;
    }

    @Comment("Fire-Ignition by fireballs, lighter, lava or lightning")
    public Ignite ignite = new Ignite();

    public class Ignite implements Section
    {
        public boolean spread = true;
        public boolean fireball = false;
        public boolean lighter = true;
        public boolean lava = false;
        public boolean lightning = false;
        public boolean other = false;
    }


    @Comment("Blocks placed or removed by using a bucket")
    public Bucket bucket = new Bucket();

    public class Bucket implements Section
    {
        public boolean fill = true;
        public boolean lava = true;
        public boolean water = true;
        @Comment("Filling a bucket with milk.")
        public boolean milk = false;
    }


    @Comment("Container-types to log")
    public Container container = new Container();

    public class Container implements Section
    {
        @Comment("Putting items into a container")
        public boolean insert = true;
        @Comment("Taking items out of a container")
        public boolean remove = true;

        @Comment({"Items moved by a hopper or dropper",
                  "WARNING this can potentially create MILLIONS of logs in a very short time"})
        public boolean move = false;

        // TODO
        @Comment("Items to ignore when moved by a hopper or dropper")
        public Set<Material> moveIgnore = new LinkedHashSet<Material>()
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
        // TODO
        public Set<ContainerType> CONTAINER_ignore = new LinkedHashSet<>();
    }


    public LogHangingSection hanging;

    public static class LogHangingSection implements Section
    {
        @Comment("Placing a painting or itemframe")
        public boolean place = true;
        @Comment("Breaking a painting or itemframe")
        public boolean destroy = true;
        @Comment("Removing items from itemframes")
        public boolean item_remove = true;
    }

    public Death death = new Death();

    public class Death implements Section
    {
        @Comment("Only log when the killer is")
        public Killer killer = new Killer();

        public class Killer implements Section
        {
            public boolean enable;

            // TODO kill config
            public boolean PLAYER_KILL_enable = true;
            public boolean ENTITY_KILL_enable = false;
            public boolean ENVIRONMENT_KILL_enable = false;
        }

        public boolean player = true;
        public boolean monster = false;
        @Comment("Animal-Death: Chicken,Pig,Cow,Sheep,Wolf,Ocelot")
        public boolean animal = true;
        @Comment("Pet-Death: Tamed Wolf,Ocelot")
        public boolean pet = true;
        @Comment("Villager-Death")
        public boolean npc = true;
        public boolean boss = true;
        @Comment("Other-Death: Golems,Squids,Bats")
        public boolean other = false;
    }

    public LogSpawnSection spawn;

    public static class LogSpawnSection implements Section
    {
        @Comment("Entity spawned using a monster-egg")
        public boolean monsterEgg = true;
        @Comment("Entity naturally spawning. This will cause A LOT of logs!")
        public boolean natural = false;
        @Comment("Entity spawned by a spawner")
        public boolean spawner = false;
        @Comment("Entity spawned indirectly by a player")
        public boolean other = false;
    }


    public LogItemSection item;

    public static class LogItemSection implements Section
    {
        @Comment("Items dropped by a player")
        public boolean drop_manual = false;
        @Comment("Items dropped by a player on death")
        public boolean drop_onPlayerDeath = true;
        @Comment("Items dropped by an enttiy on death")
        public boolean drop_onEntityDeath = false;
        @Comment("Items picked up by a player")
        public boolean pickup = false;
        //TODO CE-343 Log Zombies picking up items (waiting for bukkit)
        @Comment("Players enchanting an item")
        public boolean enchant = true;
        @Comment("Players crafting an item")
        public boolean craft = true;

    }

    public LogEntitySection entity;

    public static class LogEntitySection implements Section
    {
        @Comment("Filling a bucket with milk.")
        public boolean fillSoup = false;
        @Comment("Shearing Sheeps or Mooshrooms")
        public boolean shear = false;
        @Comment("Dyeing Sheeps or Wolf-collars")
        public boolean dye = false;
    }

    public LogVehicleSection vehicle;

    public static class LogVehicleSection implements Section
    {
        @Comment("Enter a boat or minecart")
        public boolean enter = false;
        @Comment("Exit a boat or minecart")
        public boolean exit = false;
        @Comment("Placing a boat or minecart")
        public boolean place = false;
        @Comment("Breaking a boat or minecart")
        public boolean destroy = false;
    }

    public LogUseSection use;

    public static class LogUseSection implements Section
    {
        @Comment("Fueling a Furnace Minecart")
        public boolean furnaceMinecart = true;
        @Comment("Using firework-rockets")
        public boolean firework = false;
        @Comment("Thrown splash-potions")
        public boolean splashpotion = false;
        @Comment("Igniting TNT directly with a lighter")
        public boolean tnt = true;
        @Comment("Changing Repeater settings")
        public boolean repeater = true;
        @Comment("Pushing a button")
        public boolean button = false;
        @Comment("Walking on pressure-plates")
        public boolean plate = false;
        public boolean bonemeal = false;
        @Comment("Flipping levers")
        public boolean lever = false;
        @Comment("Changing Comparator state")
        public boolean comparator = true;
        @Comment("Changing Noteblock settings")
        public boolean noteblock = true;
        @Comment("Opening or closing doors")
        public boolean door = false;
        @Comment("Eating cake")
        public boolean cake = true;
        @Comment("Player looking into a container")
        public boolean container = true;
    }

    public LogPlayerSection player;

    public static class LogPlayerSection implements Section
    {
        @Comment("Commands used by a player")
        public boolean command_enable = false;

        @Comment("Commands to ignore when logging")
        public List<String> PLAYER_COMMAND_ignoreRegex = new ArrayList<>();

        {
            {
                PLAYER_COMMAND_ignoreRegex.add("(ce|cubeengine) (login|setpassword|setpw) .+");
            }
        }

        @Comment("The normal player chat")
        public boolean chat = false;
        @Comment("Players joining")
        public boolean join_enable = true;
        @Comment("Log the players ip when joining")
        public boolean PLAYER_JOIN_ip = false;
        @Comment("Players quiting")
        public boolean quit = true;
        @Comment("Players teleporting")
        public boolean teleport = false;
        @Comment("Exp gained")
        public boolean xp = false;
    }

    @Override
    public String[] head()
    {
        if (this.getDefault() == this)
        {
            return new String[]{"This is the global configuration for logging.", "Any settings here can be overwritten for each world in their configuration"};
        }
        return null;
    }
}

package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsConfiguration;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.EntityMatcher;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import de.cubeisland.cubeengine.core.util.matcher.ProfessionMatcher;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class ModerationCommands
{
    private UserManager um;
    private BasicsConfiguration config;
    private ModerationListener listener;

    public ModerationCommands(Basics module)
    {
        um = module.getUserManager();
        config = module.getConfiguration();
        listener = new ModerationListener(module);
    }

    @Command(
    desc = "Spawns the specified Mob",
    min = 1,
    max = 3,
    usage = "<mob>[:data][,<ridingmob>[:data]] [amount] [player]")
    public void spawnMob(CommandContext context)
    {//TODO later more ridingmobs riding on the riding mob etc...
        User sender = context.getSenderAsUser();
        if (!context.hasIndexed(2) && sender == null)
        {
            invalidUsage(context, "basics", "&eSuccesfully spawned some &cbugs &einside your server!");
        }
        EntityType entityType;
        EntityType ridingEntityType;

        String entityName;
        String entityData = null;
        String ridingEntityName = null;
        String ridingEntityData = null;
        String mobString = context.getString(0);
        if (mobString.contains(","))
        {
            entityName = mobString.substring(0, mobString.indexOf(","));
            ridingEntityName = mobString.
                substring(mobString.indexOf(",") + 1, mobString.length());
        }
        else
        {
            entityName = mobString;
        }
        if (entityName.contains(":"))
        {
            entityData = entityName.
                substring(entityName.indexOf(":") + 1, entityName.length());
            entityName = entityName.substring(0, entityName.indexOf(":"));
            entityType = EntityMatcher.get().matchMob(entityName);
        }
        else
        {
            entityType = EntityMatcher.get().matchMob(entityName);

        }
        if (entityType == null)
        {
            illegalParameter(context, "basics", "Entitiy-type not found!");
        }
        if (ridingEntityName != null && ridingEntityName.contains(":"))
        {
            ridingEntityData = ridingEntityName.substring(ridingEntityName.
                indexOf(":") + 1, ridingEntityName.length());
            ridingEntityName = ridingEntityName.substring(0, ridingEntityName.
                indexOf(":"));
            ridingEntityType = EntityMatcher.get().matchMob(ridingEntityName);
            if (ridingEntityType == null)
            {
                illegalParameter(context, "basics", "Entity-type not found for riding mob!");
            }
        }
        else
        {
            ridingEntityType = EntityMatcher.get().matchMob(ridingEntityName);
        }
        Location loc;
        if (context.hasIndexed(2))
        {
            User user = context.getUser(2);
            if (user == null)
            {
                illegalParameter(context, "core", "User not found!");
            }
            loc = user.getLocation();
        }
        else
        {
            loc = sender.getTargetBlock(null, 200).getLocation().
                add(new Vector(0, 1, 0)); // TODO do Util method for this in core 
        }
        Integer amount = 1;
        if (context.hasIndexed(1))
        {
            amount = context.getIndexed(1, int.class, null);
            if (amount == null)
            {
                illegalParameter(context, "basics", "&e%s is not a number! Really!", context.
                    getString(1));
            }
            if (amount <= 0)
            {
                illegalParameter(context, "basics", "&eAnd how am i supposed to know which mobs to despawn?");
            }
        }
        if (amount > config.spawnmobLimit)
        {
            illegalParameter(context, "basics", "&cThe serverlimit is set to &e%d &cyou cannot spawn more mobs at once!", config.spawnmobLimit);
        }
        for (int i = 1; i <= amount; ++i)
        {
            Entity entity = loc.getWorld().spawnEntity(loc, entityType.
                getBukkitType());
            this.
                applyDataToMob(context.getSender(), entityType, entity, entityData);
            if (ridingEntityType != null)
            {
                Entity ridingentity = loc.getWorld().
                    spawnEntity(loc, ridingEntityType.getBukkitType());
                this.
                    applyDataToMob(context.getSender(), ridingEntityType, ridingentity, ridingEntityData);
                entity.setPassenger(ridingentity);
            }
        }
        if (ridingEntityType != null)
        {
            context.
                sendMessage("basics", "Spawned %d %s riding %s!", amount, ridingEntityType, entityType);
        }
        else
        {
            context.sendMessage("basics", "Spawned %d %s!", amount, entityType.
                toString());
        }
    }

    private void applyDataToMob(CommandSender sender, EntityType entityType, Entity entity, String data)
    {
        if (data != null)
        {
            String match = StringUtils.matchString(data.
                toLowerCase(Locale.ENGLISH), "baby", "angry", "tamed", "power", "charged");

            if (match.equals("baby"))
            {
                if (entityType.isAnimal())
                {
                    ((Animals)entity).setBaby();
                }
                else
                {
                    illegalParameter(sender, "basics", "&eThis entity can not be a baby! Can you?");
                }
            }
            else
            {
                if (match.equals("angry"))
                {
                    if (entityType.equals(EntityType.WOLF))
                    {
                        ((Wolf)entity).setAngry(true);
                    }
                    else
                    {
                        if (entityType.equals(EntityType.PIG_ZOMBIE))
                        {
                            ((PigZombie)entity).setAngry(true);
                        }
                    }
                }
                else
                {
                    if (match.equals("tamed"))
                    {
                        if (entity instanceof Tameable) // Wolf or Ocelot
                        {
                            ((Tameable)entity).setTamed(true);
                            if (sender instanceof AnimalTamer)
                            {
                                ((Tameable)entity).setOwner((AnimalTamer)sender);
                            }
                            else
                            {
                                invalidUsage(sender, "basics", "&eYou can not own any Animals!");
                            }
                        }
                    }
                    else
                    {
                        if (match.equals("charged") || data.
                            equalsIgnoreCase("power"))
                        {
                            if (entityType.equals(EntityType.CREEPER))
                            {
                                ((Creeper)entity).setPowered(true);
                            }
                        }
                        else
                        {
                            if (entityType.equals(EntityType.SHEEP))
                            {
                                DyeColor color = MaterialMatcher.get().
                                    matchColorData(data);
                                if (color == null)
                                {
                                    illegalParameter(sender, "basics", "Color not found!");
                                }
                                ((Sheep)entity).setColor(color);
                            }
                            else
                            {
                                if (entityType.equals(EntityType.SLIME) || entityType.
                                    equals(EntityType.MAGMA_CUBE))
                                {
                                    int size = 4;
                                    match = StringUtils.
                                        matchString(data, "tiny", "small", "big");
                                    if (match.equals("tiny"))
                                    {
                                        size = 0;
                                    }
                                    else
                                    {
                                        if (match.equals("small"))
                                        {
                                            size = 2;
                                        }
                                        else
                                        {
                                            if (match.equals("big"))
                                            {
                                                size = 4;
                                            }
                                            else
                                            {
                                                try
                                                {
                                                    size = Integer.
                                                        parseInt(data);
                                                }
                                                catch (NumberFormatException e)
                                                {
                                                    illegalParameter(sender, "basics", "The slime-size has to be a number or tiny, small or big!");
                                                }
                                            }
                                        }
                                    }
                                    if (size >= 0 && size <= 250)
                                    {
                                        ((Slime)entity).setSize(size);
                                    }
                                    else
                                    {
                                        illegalParameter(sender, "basics", "The slime-size can not be smaller than 0 or bigger than 250!");
                                    }
                                }
                                else
                                {
                                    if (entityType.equals(EntityType.VILLAGER))
                                    {
                                        Profession profession = ProfessionMatcher.
                                            get().matchProfession(data);
                                        if (profession == null)
                                        {
                                            illegalParameter(sender, "basics", "Unknown villager-profession!");
                                        }
                                        ((Villager)entity).
                                            setProfession(profession);
                                    }
                                    else
                                    {
                                        if (entityType.
                                            equals(EntityType.ENDERMAN))
                                        {
                                            ItemStack item = MaterialMatcher.
                                                get().matchItemStack(data);
                                            if (item == null)
                                            {
                                                illegalParameter(sender, "basics", "Material not found!");
                                            }
                                            ((Enderman)entity).
                                                setCarriedMaterial(item.
                                                getData());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Command(
    desc = "Changes the weather",
    min = 1,
    max = 3,
    usage = "<sun|rain|storm> [world] [duration]")
    public void weather(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        boolean sunny = true;
        boolean noThunder = true;
        int duration = 10000000;
        String weather = StringUtils.
            matchString(context.getString(0), "sun", "rain", "storm");
        if (weather.equalsIgnoreCase("sun"))
        {
            sunny = true;
            noThunder = true;
        }
        else
        {
            if (weather.equalsIgnoreCase("rain"))
            {
                sunny = false;
                noThunder = true;
            }
            else
            {
                if (weather.equalsIgnoreCase("storm"))
                {
                    sunny = false;
                    noThunder = false;
                }
            }
        }
        if (context.hasIndexed(2))
        {
            duration = context.getIndexed(2, int.class, 0);
            if (duration == 0)
            {
                illegalParameter(context, "basics", "The given time is invalid!");
            }
        }
        World world;
        if (context.hasIndexed(1))
        {
            world = context.getSender().getServer().getWorld(context.
                getString(1));
            if (world == null)
            {
                illegalParameter(context, "basics", "World %s not found!", context.
                    getString(1));
            }
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "If not used ingame you have to specify a world!");
            }
            world = sender.getWorld();
        }
        world.setStorm(!sunny);
        world.setThundering(!noThunder);
        world.setWeatherDuration(duration);
    }

    @Command(
    desc = "Changes the global respawnpoint",
    usage = "[world] [<x> <y> <z>]",
    max = 4)
    public void setSpawn(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        Integer x;
        Integer y;
        Integer z;
        World world;
        if (context.hasIndexed(0))
        {
            world = context.getSender().getServer().getWorld(context.
                getString(0));
            if (world == null)
            {
                illegalParameter(context, "basics", "Nu such world: %s", context.
                    getString(0));
            }
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "If not used ingame you have to specify a world and coordinates!");
            }
            world = sender.getWorld();
        }

        if (context.hasIndexed(3))
        {
            x = context.getIndexed(1, Integer.class, null);
            y = context.getIndexed(2, Integer.class, null);
            z = context.getIndexed(3, Integer.class, null);
            if (x == null || y == null || z == null)
            {
                illegalParameter(context, "basics", "Coordinates are invalid!");
            }
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "If not used ingame you have to specify a world and coordinates!");
            }
            x = sender.getLocation().getBlockX();
            y = sender.getLocation().getBlockY();
            z = sender.getLocation().getBlockZ();
        }
        world.setSpawnLocation(x, y, z);
        context.
            sendMessage("bascics", "Spawn was in world %s set to %d %d %d", world.
            getName(), x, y, z);
    }

    @Command(
    desc = "Kills a player",
    usage = "<player>|-a",
    flags =
    {
        @Flag(longName = "all", name = "a")
    })
    public void kill(CommandContext context)
    {//TODO kill a player looking at if possible
        //TODO kill a player with cool effects :) e.g. lightnin
        User user = context.getUser(0);
        if (user == null && !context.hasFlag("a"))
        {
            invalidUsage(context, "core", "User not found!");
        }
        else
        {
            if (!user.isOnline())
            {
                illegalParameter(context, "core", "%s currently not online", user.getName());
            }
            if (BasicsPerm.COMMAND_KILL_EXEMPT.isAuthorized(user))
            {
                context.sendMessage("basics", "You cannot kill that player!");
                return;
            }
        }
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_KILL_ALL.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "You are not allowed to kill everyone!");
            }
            for (Player player : context.getCore().getUserManager().getOnlinePlayers())
            {
                if (BasicsPerm.COMMAND_KILL_EXEMPT.isAuthorized(player))
                {
                    continue;
                }
                if (!player.getName().equals(context.getSender().getName()))
                {
                    user.setHealth(0);
                }
            }
        }
        user.setHealth(0);
        //TODO broadcast alternative Deathmsgs
        context.sendMessage("basics", "You killed %s!", user.getName());
    }

    @Command(
    names =
    {
        "ping", "pong"
    },
    desc = "Pong!",
    max = 0)
    public void ping(CommandContext context)
    {
        if (context.getLabel().equalsIgnoreCase("ping"))
        {
            context.sendMessage("basics", "Pong!");
        }
        else
        {
            if (context.getLabel().equalsIgnoreCase("pong"))
            {
                context.sendMessage("basics", "Ping!");
            }
        }
    }

    @Command(
    desc = "Removes entity",
    usage = "<entityType> [radius] [in <world>] [-a]",
    flags =
    {
        @Flag(longName = "all", name = "a")
    },
    params =
    {
        @Param(names =
        {
            "in"
        }, types = World.class)
    },
    min = 1)
    public void remove(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (context.hasNamed("in"))
        {
            world = context.getNamed("in", World.class);
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "If not used ingame you have to specify a world!");
            }
            world = sender.getWorld();
        }
        int radius = config.removeCmdDefaultRadius;
        if (context.hasFlag("a")) // remove all selected entities in world
        {
            radius = -1;
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "If not used ingame you can only remove all!");
            }
            if (context.hasIndexed(1))
            {
                radius = context.getIndexed(1, int.class, 0);
                if (radius == 0)
                {
                    illegalParameter(context, "basics", "The radius has to be a number!");
                }
            }
        }
        EntityType type = EntityMatcher.get().matchEntity(context.getString(0));
        if (type == null)
        {
            illegalParameter(context, "basics", "Invalid entity-type!\nUse "
                + EntityType.DROPPED_ITEM + ", " + EntityType.ARROW + ", "
                + EntityType.BOAT + ", " + EntityType.MINECART + ", "
                + EntityType.EXPERIENCE_ORB + " or " + EntityType.ARROW);
        }
        if (type.isAlive())
        {
            invalidUsage(context, "basics", "To kill living entities use the butcher command!");
        }
        Location loc = null;
        if (sender != null)
        {
            loc = sender.getLocation();
        }
        int entitiesRemoved = this.removeEntityType(world.getEntities(), loc, radius, type);
        context.sendMessage("basics", "Removed %d entities!", entitiesRemoved);
    }

    private int removeEntityType(List<Entity> list, Location loc, int radius, EntityType type)
    {
        if (loc == null && radius != -1)
        {
            throw new IllegalStateException("Unkown Location with Radius");
        }
        int removed = 0;

        for (Entity entity : list)
        {
            if (!entity.getType().equals(type.getBukkitType()))
            {
                continue;
            }
            if (radius != -1)
            {
                int distance = (int)(entity.getLocation().subtract(loc)).
                    lengthSquared();
                if (radius * radius < distance)
                {
                    continue;
                }
            }
            entity.remove();
            removed++;
        }
        return removed;
    }

    @Command(
    names =
    {
        "clearinventory", "ci"
    },
    desc = "Clears the inventory",
    usage = "[player]",
    flags =
    {
        @Flag(longName = "removeArmor", name = "ra")
    },
    max = 1)
    public void clearinventory(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        User user = sender;
        boolean other = false;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                invalidUsage(context, "core", "User not found!");
            }
            other = true;
        }
        if (other && BasicsPerm.COMMAND_CLEARINVENTORY_OTHER.
            isAuthorized(context.getSender()))
        {
            denyAccess(context, "basics", "&cYou are not allowed to clear the inventory of other User!");
        }
        user.getInventory().clear();
        if (context.hasFlag("ra"))
        {
            user.getInventory().setBoots(null);
            user.getInventory().setLeggings(null);
            user.getInventory().setChestplate(null);
            user.getInventory().setHelmet(null);
        }
        user.updateInventory();
        user.sendMessage("basics", "Cleared Inventory!");
        if (other)
        {
            sender.sendMessage("basics", "Cleared Inventory of %s!", user.
                getName());
        }
    }

    @Command(
    desc = "Stashes or unstashes your inventory to reuse later",
    max = 0)
    public void stash(CommandContext context)
    {
        User sender = context.
            getSenderAsUser("core", "&cThis command can only be used by a player!");
        ItemStack[] stashedInv = sender.getAttribute("stash_Inventory");
        ItemStack[] stashedArmor = sender.getAttribute("stash_Armor");
        ItemStack[] InvToStash = sender.getInventory().getContents().clone();
        ItemStack[] ArmorToStash = sender.getInventory().getArmorContents().
            clone();
        if (stashedInv != null)
        {
            sender.getInventory().setContents(stashedInv);
        }
        else
        {
            sender.getInventory().clear();
        }
        sender.setAttribute("stash_Inventory", InvToStash);
        if (stashedArmor != null)
        {
            sender.getInventory().setBoots(stashedArmor[0]);
            sender.getInventory().setLeggings(stashedArmor[1]);
            sender.getInventory().setChestplate(stashedArmor[2]);
            sender.getInventory().setHelmet(stashedArmor[3]);
        }
        else
        {
            sender.getInventory().setBoots(null);
            sender.getInventory().setLeggings(null);
            sender.getInventory().setChestplate(null);
            sender.getInventory().setHelmet(null);
        }
        sender.setAttribute("stash_Armor", ArmorToStash);
        sender.sendMessage("basics", "Swapped stashed Inventory!");
    }

    @Command(
    desc = "Broadcasts a message",
    usage = "<message>")
    public void broadcast(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        this.um.broadcastMessage("basics", "&2[&cBroadcast&2] &e" + sb.
            toString());
    }

    @Command(
    desc = "Makes a player execute a command",
    usage = "<player> <command>",
    flags =
    {
        @Flag(longName = "chat", name = "c")
    })
    public void sudo(CommandContext context)
    {
        //User sender = context.getSenderAsUser();
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "core", "User not found!");
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        if (context.hasFlag("c"))
        {
            user.chat(sb.toString());
        }
        else
        {
            user.chat("/" + sb.toString()); //TODO later msg to sender if cmd worked??
        }
    }

    @Command(
    desc = "Allows you to see into the inventory of someone else.",
    max = 1)
    public void invsee(CommandContext context)
    {
        User sender = context.
            getSenderAsUser("bascics", "&cThis command can only be used by a player!");
        User user = context.getIndexed(0, User.class, null);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        sender.openInventory(user.getInventory());
        boolean allowModify = false;
        if (BasicsPerm.COMMAND_INVSEE_MODIFY.isAuthorized(sender))
        {
            allowModify = true;
        }
        if (BasicsPerm.COMMAND_INVSEE_PREVENTMODIFY.isAuthorized(user))
        {
            allowModify = false;
        }
        listener.addInventory(sender, allowModify);
    }

    @Command(
    desc = "Kicks a player from the server",
    usage = "<player>|-all [message]",
    flags =
    {
        @Flag(longName = "all", name = "a")
    })
    public void kick(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null && !context.hasFlag("a"))
        {
            illegalParameter(context, "basics", "User not found!");
        }
        int i = 1;
        String message = "Kicked!";
        StringBuilder sb = new StringBuilder();
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++)).append(" ");
        }
        if (sb.length() != 0)
        {
            message = sb.toString();
        }
        if (context.hasFlag("a"))
        {
            if (BasicsPerm.COMMAND_KICK_ALL.isAuthorized(context.getSender()))
            {
                String sendername = context.getSender().getName();
                for (Player player : context.getSender().getServer().
                    getOnlinePlayers())
                {
                    if (!sendername.equalsIgnoreCase(player.getName()))
                    {
                        player.kickPlayer(message);
                    }
                }
            }
            else
            {
                denyAccess(context, "basics", "You are not allowed to kick everyone!");
            }
        }
        else
        {
            user.kickPlayer(message);
        }
    }

    @Command(
    names =
    {
        "ban", "kickban"
    },
    desc = "Bans a player permanently on your server.",
    min = 1,
    usage = "<player> [message] [-ipban]",
    flags =
    {
        @Flag(longName = "ipban", name = "ip")
    })
    public void ban(CommandContext context)
    {
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (player.isBanned())
        {
            invalidUsage(context, "basics", "%s is already banned!");
        }
        if (player.hasPlayedBefore() == false)
        {
            context.sendMessage("basics", "%s has never played on this server before!", player.getName());
        }
        else
        {
            if (player.isOnline())
            {
                User user = context.getCore().getUserManager().getUser(player);
                String message = context.getStrings(1);
                if (message.equals(""))
                {
                    message = _(user, "basics", "&cYou got banned from this server!");
                }
                user.kickPlayer(message);
            }
            if (context.hasFlag("ip"))
            {
                Bukkit.banIP(player.getPlayer().getAddress().getAddress().getHostAddress());
            }
        }
        player.setBanned(true);
        context.sendMessage("basics", "You banned %s.", player.getName());
    }

    @Command(
    desc = "Unbans a previously banned player.",
    min = 1,
    usage = "<player>")
    public void unban(CommandContext context)
    {
        OfflinePlayer user = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (!user.isBanned())
        {
            invalidUsage(context, "basics", "%s is not banned!");
        }
        user.setBanned(false);
        context.sendMessage("basics", "You unbanned %s.", user.getName());
    }

    @Command(
    names =
    {
        "ipban", "banip"
    },
    desc = "Bans the IP from this server.",
    min = 1,
    usage = "<IP address>")
    public void ipban(CommandContext context)
    {
        String ipadress = context.getString(0);
        try
        {
            InetAddress adress = InetAddress.getByName(ipadress);
            Bukkit.banIP(adress.getHostAddress());
            context.sendMessage("basics", "You banned the IP %s from your server!", adress.getHostAddress());
            for (Player player : context.getCore().getUserManager().getOnlinePlayers())
            {
                if (player.getAddress().getAddress().getHostAddress().equals(ipadress))
                {
                    player.kickPlayer(_(player, "basics", "You were banned from this server!"));
                }
            }
        }
        catch (UnknownHostException e)
        {
            invalidUsage(context, "basics", "%s is not a valid IP-address!", ipadress);
        }
    }

    @Command(
    names =
    {
        "ipunban", "unbanip"
    },
    desc = "Bans the IP from this server.",
    min = 1,
    usage = "<IP address>")
    public void ipunban(CommandContext context)
    {
        String ipadress = context.getString(0);
        try
        {
            InetAddress adress = InetAddress.getByName(ipadress);
            Bukkit.unbanIP(adress.getHostAddress());
            context.sendMessage("basics", "You unbanned the IP %s!", adress.getHostAddress());
        }
        catch (UnknownHostException e)
        {
            invalidUsage(context, "basics", "%s is not a valid IP-address!", ipadress);
        }
    }
}
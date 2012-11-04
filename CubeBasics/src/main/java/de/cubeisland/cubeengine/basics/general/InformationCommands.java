package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class InformationCommands
{
    private Basics basics;

    public InformationCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(
        desc = "Displays the direction in which you are looking.")
    public void compass(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "I assume you are looking right at your server-console. Right?");
        final int direction = (int)(sender.getLocation().getYaw() + 180 + 360) % 360;
        //TODO any idea to do this better?
        String dir;
        if (direction < 23)
        {
            dir = "N";
        }
        else if (direction < 68)
        {
            dir = "NE";
        }
        else if (direction < 113)
        {
            dir = "E";
        }
        else if (direction < 158)
        {
            dir = "SE";
        }
        else if (direction < 203)
        {
            dir = "S";
        }
        else if (direction < 248)
        {
            dir = "SW";
        }
        else if (direction < 293)
        {
            dir = "W";
        }
        else if (direction < 338)
        {
            dir = "NW";
        }
        else
        {
            dir = "N";
        }
        sender.sendMessage("basics", "You are looking into %s", _(sender, "basics", dir));
    }

    @Command(
        desc = "Displays your current depth.")
    public void depth(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "You dug too deep!");
        int height = sender.getLocation().getBlockY();
        if (height > 62)
        {
            sender.sendMessage("basics", "You are on heightlevel %d (%d above sealevel)", height, height - 62);
        }
        else
        {
            sender.sendMessage("basics", "You are on heightlevel %d (%d below sealevel)", height, 62 - height);
        }
    }

    @Command(
        desc = "Displays your current location.")
    public void getPos(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "Your position: Right in front of your screen!");
        sender.sendMessage("basics", "Your position is X:%d Y:%d Z:%d", sender.getLocation().getBlockX(), sender.getLocation().getBlockY(), sender.getLocation().getBlockZ());
    }

    @Command(
    desc = "Displays the message of the day!")
    public void motd(CommandContext context)
    {
        context.sendMessage(basics.getConfiguration().motd);//TODO translatable other lang in config else default
        /*
         * default: 'Welcome on our Server! Have fun!'
         * de_DE: 'Willkommen auf unserem Server! Viel Spaß'
         */
    }

    @Command(
    desc = "Displays near players(entities/mobs) to you.",
    max = 2,
    usage = "[radius] [player] [-entity]|[-mob]",
    flags =
    {
        @Flag(longName = "entity", name = "e"),
        @Flag(longName = "mob", name = "m")
    })
    public void near(CommandContext context)
    {
        User user;
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
        }
        else
        {
            user = context.getSenderAsUser("basics", "&eI'am right &cbehind &eyou!");
        }
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        int radius = this.basics.getConfiguration().nearDefaultRadius;
        if (context.hasIndexed(0))
        {
            radius = context.getIndexed(0, int.class, radius);
        }
        List<Entity> list = user.getWorld().getEntities();
        List<String> outputlist = new ArrayList<String>(); //TODO sort list by distance
        //TODO only show the flag is there for
        for (Entity entity : list)
        {
            double distance = entity.getLocation().distance(user.getLocation());
            if (!entity.getLocation().equals(user.getLocation()))
            {
                if (distance < radius)
                {
                    if (context.hasFlag("e"))
                    {
                        this.addNearInformation(outputlist, entity, distance);
                    }
                    else if (context.hasFlag("m"))
                    {
                        if (entity instanceof LivingEntity)
                        {
                            this.addNearInformation(outputlist, entity, distance);
                        }
                    }
                    else
                    {
                        if (entity instanceof Player)
                        {
                            this.addNearInformation(outputlist, entity, distance);
                        }
                    }
                }
            }
        }
        if (outputlist.isEmpty())
        {
            context.sendMessage("Nothing detected nearby!");
        }
        else
        {
            if (context.getSender().getName().equals(user.getName()))
            {
                context.sendMessage("basics", "&eFound those nearby you:\n%s", StringUtils.implode("&f, ", outputlist));
            }
            else
            {
                context.sendMessage("basics", "&eFound those nearby %s:\n%s", user.getName(), StringUtils.implode("&f, ", outputlist));
            }

        }
    }

    private void addNearInformation(List<String> list, Entity entity, double distance)
    {
        if (entity instanceof Player)
        {
            list.add(String.format("&2%s&f (&e%dm&f)", ((Player)entity).getName(), (int)distance));
        }
        else if (entity instanceof LivingEntity)
        {
            list.add(String.format("&3%s&f (&e%dm&f)", EntityType.fromBukkitType(entity.getType()), (int)distance));
        }
        else
        {
            if (entity instanceof Item)
            {
                list.add(String.format("&7%s&f (&e%dm&f)", MaterialMatcher.get().getNameFor(((Item)entity).getItemStack()), (int)distance));
            }
            else
            {
                list.add(String.format("&7%s&f (&e%dm&f)", EntityType.fromBukkitType(entity.getType()), (int)distance));
            }
        }
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
    
    public void lag(CommandContext context)
    {
        
    }
}

package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException.invalidUsage;

public class NukeCommand
{
    private final NukeListener nukeListener;
    private final FunConfiguration config;

    public NukeCommand(Fun module)
    {
        config = module.getConfig();
        nukeListener = new NukeListener();
        module.registerListener(nukeListener);
    }

    @Command(desc = "A tnt carpet is falling at a player or the place the player is looking at.", max = 1, flags = {
        @Flag(longName = "unsafe", name = "u")
    }, usage = "[radius] [height <value>] [player <name>] [concentration <value>] [range <vaule>] [-unsafe]", params = {
            @Param(names = {
                "player", "p"
            }, type = User.class),
            @Param(names = {
                "height", "h"
            }, type = Integer.class),
            @Param(names = {
                "concentration", "c"
            }, type = String.class),
            @Param(names = {
                "range", "r"
            }, type = Integer.class)
    })
    public void nuke(ParameterizedContext context)
    {
        int noBlock = 0;

        int numberOfBlocks = 0;

        int radius = context.getArg(0, int.class, 0);
        int height = context.getParam("height", 5);
        int concentration = 1;
        int concentrationOfBlocksPerCircle = 1;
        int range = context.getParam("range", 4);

        Location centerOfTheCircle;
        User user = null;

        if (context.hasParam("concentration"))
        {
            String concNamed = context.getString("concentration");
            Matcher matcher = Pattern.compile("(\\d*)(\\.(\\d+))?").matcher(concNamed);
            if (concNamed != null && matcher.matches())
            {
                try
                {
                    if (matcher.group(1) != null && matcher.group(1).length() > 0)
                    {
                        concentration = Integer.valueOf(matcher.group(1));
                    }
                    if (matcher.group(3) != null && matcher.group(3).length() > 0)
                    {
                        concentrationOfBlocksPerCircle = Integer.valueOf(matcher.group(3));
                    }
                }
                catch (NumberFormatException e)
                {
                    invalidUsage(context, "fun", "&cThe named Paramter concentration has a wrong usage. \"&a1.1&c\" is the right way. You used %s", concNamed);
                }
            }
            if (concentration > this.config.nukeConcentrationLimit || concentrationOfBlocksPerCircle > this.config.nukeConcentrationLimit)
            {
                illegalParameter(context, "fun", "&cThe concentration should not be greater than %d", this.config.nukeConcentrationLimit);
            }
        }
        if (radius > this.config.nukeRadiusLimit)
        {
            illegalParameter(context, "fun", "&cThe radius should not be greater than %d", this.config.nukeRadiusLimit);
        }
        if (concentration < 1)
        {
            illegalParameter(context, "fun", "&cThe concentration should not be smaller than 1");
        }
        if (concentrationOfBlocksPerCircle < 1)
        {
            illegalParameter(context, "fun", "&cThe concentration of Blocks per Circle should not be smaller than 1");
        }
        if (height < 1)
        {
            illegalParameter(context, "fun", "&cThe height can't be less than 1");
        }
        if (range < 0 || range > this.config.nukeMaxExplosionRange)
        {
            illegalParameter(context, "fun", "&cThe explosion range can't be less than 0 or over %d", this.config.nukeMaxExplosionRange);
        }

        if (context.hasParam("player"))
        {
            user = context.getUser("player");
            if (user == null)
            {
                invalidUsage(context, "fun", "&cUser not found");
            }
            centerOfTheCircle = user.getLocation();
        }
        else
        {
            if (context.getSender() instanceof User)
            {
                user = (User)context.getSender();
            }
            if (user == null)
            {
                context.sendMessage("core", "&cThis command can only be used by a player!");
                return;
            }
            centerOfTheCircle = user.getTargetBlock(null, 40).getLocation();
        }

        while (noBlock != height)
        {
            centerOfTheCircle.add(0, 1, 0);
            if (centerOfTheCircle.getBlock().getType() == Material.AIR)
            {
                noBlock++;
            }
            else
            {
                noBlock = 0;
            }
        }

        for (int i = radius; i > 0; i -= concentration)
        {
            double blocksPerCircle = i * 4 / concentrationOfBlocksPerCircle;
            double angle = 2 * Math.PI / blocksPerCircle;
            for (int j = 0; j < blocksPerCircle; j++)
            {
                TNTPrimed tnt = user.getWorld().spawn(
                    new Location(centerOfTheCircle.getWorld(),
                        Math.cos(j * angle) * i + centerOfTheCircle.getX() + 0.6,
                        centerOfTheCircle.getY(),
                        Math.sin(j * angle) * i + centerOfTheCircle.getZ() + 0.6
                    ), TNTPrimed.class);
                tnt.setVelocity(new Vector(0, 0, 0));
                tnt.setYield(range);

                numberOfBlocks++;

                if (!context.hasFlag("u"))
                {
                    nukeListener.add(tnt);
                }
            }
        }
        if (radius == 0)
        {
            TNTPrimed tnt = user.getWorld().spawn(centerOfTheCircle, TNTPrimed.class);
            tnt.setYield(range);

            if (!context.hasFlag("u"))
            {
                nukeListener.add(tnt);
                numberOfBlocks++;
            }
        }

        context.sendMessage("fun", "&aYou spawnt %d blocks of TNT", numberOfBlocks);
    }

    private class NukeListener implements Listener
    {
        private final Set<TNTPrimed> noBlockDamageSet;

        public NukeListener()
        {
            this.noBlockDamageSet = new HashSet<TNTPrimed>();
        }

        public void add(TNTPrimed tnt)
        {
            noBlockDamageSet.add(tnt);
        }

        public void remove(TNTPrimed tnt)
        {
            noBlockDamageSet.remove(tnt);
        }

        public boolean contains(TNTPrimed tnt)
        {
            return noBlockDamageSet.contains(tnt);
        }

        @EventHandler
        public void onBlockDamage(EntityExplodeEvent event)
        {
            try
            {
                if (event.getEntityType() == EntityType.PRIMED_TNT && this.contains((TNTPrimed)event.getEntity()))
                {
                    event.blockList().clear();
                    remove((TNTPrimed)event.getEntity());
                }
            }
            catch (NullPointerException ignored)
            {}
        }
    }

}

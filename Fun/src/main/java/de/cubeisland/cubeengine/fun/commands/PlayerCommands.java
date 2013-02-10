package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunPerm;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

public class PlayerCommands
{
    private final Fun module;
    private final ExplosionListener explosionListener;

    public PlayerCommands(Fun module)
    {
        this.module = module;
        this.explosionListener = new ExplosionListener();
        this.module.registerListener(explosionListener);
    }

    @Command(desc = "Creates an explosion", params = {
            @Param(names = {
                "player", "p"
            }, type = User.class),
            @Param(names = {
                "damage", "d"
            }, type = Integer.class)
    }, flags = {
            @Flag(longName = "unsafe", name = "u"),
            @Flag(longName = "fire", name = "f"),
            @Flag(longName = "blockDamage", name = "b"),
            @Flag(longName = "playerDamage", name = "p")
    }, max = 0, usage = "[player <name>] [damage <value>] [-blockDamage] [-playerDamage] [-fire] [-unsafe]")
    public void explosion(ParameterizedContext context)
    {
        User user;
        Location location;
        int power = context.getParam("damage", 1);

        if (context.hasParam("player"))
        {
            if (!FunPerm.EXPLOSION_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "rulebook", "&cYou are not allowed to use the player parameter.");
            }
            user = context.getUser("player");
            if (user == null)
            {
                illegalParameter(context, "core", "&cUser not found!");
            }
            location = user.getLocation();
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendMessage("fun", "&cThis command can only be used by a player!");
                return;
            }
            user = (User)context.getSender();
            location = user.getTargetBlock(null, this.module.getConfig().explosionDistance).getLocation();
        }

        if (power > this.module.getConfig().explosionPower)
        {
            illegalParameter(context, "fun", "&cThe power of the explosion shouldn't be greater than %d", this.module.getConfig().explosionPower);
        }

        if (!FunPerm.EXPLOSION_BLOCK_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("b") || context.hasFlag("u")))
        {
            denyAccess(context, "rulebook", "&cYou are not allowed to break blocks");
        }
        if (!FunPerm.EXPLOSION_FIRE.isAuthorized(context.getSender()) && (context.hasFlag("f") || context.hasFlag("u")))
        {
            denyAccess(context, "rulebook", "&cYou are not allowed to set fireticks");
        }
        if (!FunPerm.EXPLOSION_PLAYER_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("p") || context.hasFlag("u")))
        {
            denyAccess(context, "rulebook", "&cYou are not allowed to damage an other player");
        }

        if (!context.hasFlag("u") && !context.hasFlag("p"))
        {
            explosionListener.add(location);
        }

        user.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), power, context.hasFlag("f") || context.hasFlag("u"), context.hasFlag("b") || context.hasFlag("u"));
    }

    @Command(names = {
        "lightning", "strike"
    }, desc = "Strucks a player or the location you are looking at by lightning.", max = 0, params = {
            @Param(names = {
                "player", "p"
            }, type = User.class),
            @Param(names = {
                "damage", "d"
            }, type = Integer.class),
            @Param(names = {
                "fireticks", "f"
            }, type = Integer.class)
    }, flags = {
        @Flag(longName = "unsafe", name = "u")
    }, usage = "[player <name>] [damage <value>] [fireticks <seconds>] [-unsafe]")
    public void lightning(ParameterizedContext context)
    {
        User user;
        Location location;
        int damage = context.getParam("damage", -1);

        if (damage != -1 && !FunPerm.LIGHTNING_PLAYER_DAMAGE.isAuthorized(context.getSender()))
        {
            denyAccess(context, "fun", "You are not allowed the use the damage parameter");
        }
        if (context.hasFlag("u") && !FunPerm.LIGHTNING_UNSAFE.isAuthorized(context.getSender()))
        {
            denyAccess(context, "fun", "You are not allowed to use the unsafe flag");
        }

        if (context.hasParam("player"))
        {
            user = context.getUser("player");
            if (user == null)
            {
                illegalParameter(context, "core", "&cUser not found!");
            }
            location = user.getLocation();
            if ((damage != -1 && damage < 0) || damage > 20)
            {
                illegalParameter(context, "fun", "&cThe damage value has to be a number from 1 to 20");
            }
            user.setFireTicks(20 * context.getParam("fireticks", Integer.valueOf(0)));
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendMessage("fun", "&cThis command can only be used by a player!");
                return;
            }
            user = (User)context.getSender();
            location = user.getTargetBlock(null, this.module.getConfig().lightningDistance).getLocation();
        }

        if (context.hasFlag("u"))
        {
            user.getWorld().strikeLightning(location);
        }
        else
        {
            user.getWorld().strikeLightningEffect(location);
        }
        if (damage != -1)
        {
            user.damage(damage);
        }
    }

    @Command(desc = "Slaps a player", min = 1, max = 2, usage = "<player> [damage]")
    public void slap(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "core", "&cUser not found!");
        }

        int damage = context.getArg(1, int.class, 3);

        if (damage < 1 || damage > 20)
        {
            illegalParameter(context, "fun", "&cOnly damage values from 1 to 20 are allowed!");
            return;
        }

        final Vector userDirection = user.getLocation().getDirection();
        user.damage(damage);
        user.setVelocity(new Vector(userDirection.getX() * damage / 2, 0.05 * damage, userDirection.getZ() * damage / 2));
    }

    @Command(desc = "Burns a player", min = 1, max = 2, flags = {
        @Flag(longName = "unset", name = "u")
    }, usage = "<player> [seconds] [-unset]")
    public void burn(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "core", "&cUser not found!");
        }

        int seconds = context.getArg(1, int.class, 5);

        if (context.hasFlag("u"))
        {
            seconds = 0;
        }
        else if (seconds < 1 || seconds > 26)
        {
            illegalParameter(context, "fun", "&cOnly 1 to 26 seconds are permitted!");
        }

        user.setFireTicks(seconds * 20);
    }

    private class ExplosionListener implements Listener
    {
        private Location location;

        public void add(Location location)
        {

            this.location = location;

            module.getTaskManger().scheduleSyncDelayedTask(module,
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            remove();
                        }
                    }, 1
                );
        }

        private void remove()
        {
            this.location = null;
        }

        @EventHandler
        public void onEntityDamageByBlock(EntityDamageByBlockEvent event)
        {
            if (this.location != null && event.getDamager() == null && event.getEntity() instanceof Player)
            {
                event.setCancelled(true);
            }
        }
    }
}

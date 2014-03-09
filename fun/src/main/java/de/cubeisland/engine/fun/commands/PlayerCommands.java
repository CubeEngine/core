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
package de.cubeisland.engine.fun.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.fun.Fun;

public class PlayerCommands
{
    private final Fun module;
    private final ExplosionListener explosionListener;

    public PlayerCommands(Fun module)
    {
        this.module = module;
        this.explosionListener = new ExplosionListener();
        this.module.getCore().getEventManager().registerListener(module, explosionListener);
    }
    
    @Command(
            desc = "sets the hat of the player",
            max = 1,
            params =
            {
                @Param(names = {"player", "p"}, type = User.class)
            },
            flags =
            {
                @Flag(longName = "quiet", name = "q")
            },
            usage = "[item] [player <player>]"
    )
    public void hat(ParameterizedContext context)
    {
        User user;
        ItemStack head;
        boolean console = false;
        PlayerInventory senderInventory = null;
        PlayerInventory userInventory;
        
        if(!(context.getSender() instanceof User))
        {
            console = true;
        }
        if(context.hasParam( "player" ) )
        {
            if(!module.perms().COMMAND_HAT_OTHER.isAuthorized( context.getSender() ) )
            {
                context.sendTranslated(MessageType.NEGATIVE, "You can't set the hat of an other player.");
                return;
            }
            
            user = context.getUser( "player" );
            
            if(user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "User not found!");
                return;
            }
        }
        else if(!console)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated(MessageType.NEGATIVE, "You has to specify a user!");
            return;
        }
        
        if(context.hasArg( 0 ) )
        {
            if(!module.perms().COMMAND_HAT_ITEM.isAuthorized( context.getSender() ))
            {
                context.sendTranslated(MessageType.NEGATIVE, "You can only use your item in hand!");
                return;
            }
            head = Match.material().itemStack( context.getArg( 0, String.class ) );
            if(head == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Item not found!");
                return;
            }
        }
        else if(console)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Trying to be Notch? No hat for you!");
            context.sendTranslated(MessageType.NEUTRAL, "Please specify an item!");
            return;
        }
        else
        {
            senderInventory = ((User)context.getSender()).getInventory();
            head = senderInventory.getItemInHand().clone();
        }
        if (head.getTypeId() == 0)
        {
            context.sendTranslated(MessageType.NEGATIVE, "You do not have any item in your hand!");
            return;
        }
        userInventory = user.getInventory();
        
        int amount = head.getAmount();
        head.setAmount( 1 );
        
        if( !context.hasArg( 0 ) && senderInventory != null)
        {
            ItemStack item = head.clone();
            item.setAmount( amount - 1);

            senderInventory.setItemInHand( item );
        }
        if(userInventory.getHelmet() != null)
        {
            userInventory.addItem( userInventory.getHelmet() );
        }
        
        userInventory.setHelmet( head );
        
        if( !(context.hasFlag("q") && module.perms().COMMAND_HAT_QUIET.isAuthorized(context.getSender()) ) && module.perms().COMMAND_HAT_NOTIFY.isAuthorized( user ) )
        {
            user.sendTranslated(MessageType.POSITIVE, "Your hat was changed");
        }        
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
            if (!module.perms().COMMAND_EXPLOSION_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to use the player parameter.");
                return;
            }
            user = context.getUser("player");
            if (user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "User not found!");
                return;
            }
            location = user.getLocation();
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated(MessageType.NEGATIVE, "This command can only be used by a player!");
                return;
            }
            user = (User)context.getSender();
            location = user.getTargetBlock(null, this.module.getConfig().command.explosion.distance).getLocation();
        }

        if (power > this.module.getConfig().command.explosion.power)
        {
            context.sendTranslated(MessageType.NEGATIVE, "The power of the explosion shouldn't be greater than {integer}", this.module.getConfig().command.explosion.power);
            return;
        }

        if (!module.perms().COMMAND_EXPLOSION_BLOCK_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("b") || context.hasFlag("u")))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to break blocks");
            return;
        }
        if (!module.perms().COMMAND_EXPLOSION_FIRE.isAuthorized(context.getSender()) && (context.hasFlag("f") || context.hasFlag("u")))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to set fireticks");
            return;
        }
        if (!module.perms().COMMAND_EXPLOSION_PLAYER_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("p") || context.hasFlag("u")))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to damage an other player");
            return;
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

        if (damage != -1 && !module.perms().COMMAND_LIGHTNING_PLAYER_DAMAGE.isAuthorized(context.getSender()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You are not allowed the use the damage parameter");
            return;
        }
        if (context.hasFlag("u") && !module.perms().COMMAND_LIGHTNING_UNSAFE.isAuthorized(context.getSender()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to use the unsafe flag");
            return;
        }

        if (context.hasParam("player"))
        {
            user = context.getUser("player");
            if (user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "User not found!");
                return;
            }
            location = user.getLocation();
            if ((damage != -1 && damage < 0) || damage > 20)
            {
                context.sendTranslated(MessageType.NEGATIVE, "The damage value has to be a number from 1 to 20");
                return;
            }
            user.setFireTicks(20 * context.getParam("fireticks", 0));
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated(MessageType.NEGATIVE, "This command can only be used by a player!");
                return;
            }
            user = (User)context.getSender();
            location = user.getTargetBlock(null, this.module.getConfig().command.lightning.distance).getLocation();
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
            context.sendTranslated(MessageType.NEGATIVE, "User not found!");
            return;
        }

        int damage = context.getArg(1, Integer.class, 3);

        if (damage < 1 || damage > 20)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Only damage values from 1 to 20 are allowed!");
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
            context.sendTranslated(MessageType.NEGATIVE, "User not found!");
            return;
        }

        int seconds = context.getArg(1, Integer.class, 5);

        if (context.hasFlag("u"))
        {
            seconds = 0;
        }
        else if (seconds < 1 || seconds > this.module.getConfig().command.burn.maxTime)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Only 1 to {integer} seconds are permitted!", this.module.getConfig().command.burn.maxTime);
            return;
        }

        user.setFireTicks(seconds * 20);
    }

    private class ExplosionListener implements Listener
    {
        private Location location;

        public void add(Location location)
        {

            this.location = location;

            module.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
            {
                @Override
                public void run()
                {
                    remove();
                }
            }, 1);
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

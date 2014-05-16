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
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.fun.Fun;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

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
    
    @Command(desc = "Gives a player a hat",
            params = @Param(names = {"player", "p"}, type = User.class),
            flags = @Flag(longName = "quiet", name = "q"),
            indexed = @Grouped(req = false, value = @Indexed(label = "item")))
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
                context.sendTranslated(NEGATIVE, "You can't set the hat of an other player.");
                return;
            }
            
            user = context.getUser( "player" );
            
            if(user == null)
            {
                context.sendTranslated(NEGATIVE, "Player not found!");
                return;
            }
        }
        else if(!console)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You have to specify a player!");
            return;
        }
        
        if(context.hasArg( 0 ) )
        {
            if(!module.perms().COMMAND_HAT_ITEM.isAuthorized( context.getSender() ))
            {
                context.sendTranslated(NEGATIVE, "You can only use your item in hand!");
                return;
            }
            head = Match.material().itemStack( context.<String>getArg(0) );
            if(head == null)
            {
                context.sendTranslated(NEGATIVE, "Item not found!");
                return;
            }
        }
        else if(console)
        {
            context.sendTranslated(NEGATIVE, "Trying to be Notch? No hat for you!");
            context.sendTranslated(NEUTRAL, "Please specify an item!");
            return;
        }
        else
        {
            senderInventory = ((User)context.getSender()).getInventory();
            head = senderInventory.getItemInHand().clone();
        }
        if (head.getTypeId() == 0)
        {
            context.sendTranslated(NEGATIVE, "You do not have any item in your hand!");
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
            user.sendTranslated(POSITIVE, "Your hat was changed");
        }        
    }

    @Command(desc = "Creates an explosion",
             params = {@Param(names = {"player", "p"}, type = User.class),
                       @Param(names = {"damage", "d"}, label = "value", type = Integer.class)},
             flags = {@Flag(longName = "unsafe", name = "u"),
                      @Flag(longName = "fire", name = "f"),
                      @Flag(longName = "blockDamage", name = "b"),
                      @Flag(longName = "playerDamage", name = "p")})
    public void explosion(ParameterizedContext context)
    {
        User user;
        Location location;
        int power = context.getParam("damage", 1);

        if (context.hasParam("player"))
        {
            if (!module.perms().COMMAND_EXPLOSION_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to specify a player.");
                return;
            }
            user = context.getUser("player");
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player not found!");
                return;
            }
            location = user.getLocation();
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated(NEGATIVE, "This command can only be used by a player!");
                return;
            }
            user = (User)context.getSender();
            location = user.getTargetBlock(null, this.module.getConfig().command.explosion.distance).getLocation();
        }

        if (power > this.module.getConfig().command.explosion.power)
        {
            context.sendTranslated(NEGATIVE, "The power of the explosion shouldn't be greater than {integer}", this.module.getConfig().command.explosion.power);
            return;
        }

        if (!module.perms().COMMAND_EXPLOSION_BLOCK_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("b") || context.hasFlag("u")))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to break blocks");
            return;
        }
        if (!module.perms().COMMAND_EXPLOSION_FIRE.isAuthorized(context.getSender()) && (context.hasFlag("f") || context.hasFlag("u")))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to set fireticks");
            return;
        }
        if (!module.perms().COMMAND_EXPLOSION_PLAYER_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("p") || context.hasFlag("u")))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to damage another player");
            return;
        }

        if (!context.hasFlag("u") && !context.hasFlag("p"))
        {
            explosionListener.add(location);
        }

        user.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), power, context.hasFlag("f") || context.hasFlag("u"), context.hasFlag("b") || context.hasFlag("u"));
    }

    @Command(names = {"lightning", "strike"}, desc = "Throws a lightning bolt at a player or where you're looking",
             params = {@Param(names = {"player", "p"}, type = User.class),
                       @Param(names = {"damage", "d"}, label = "value", type = Integer.class),
                       @Param(names = {"fireticks", "f"}, label = "seconds", type = Integer.class)},
             flags = @Flag(longName = "unsafe", name = "u"))
    public void lightning(ParameterizedContext context)
    {
        User user;
        Location location;
        int damage = context.getParam("damage", -1);

        if (damage != -1 && !module.perms().COMMAND_LIGHTNING_PLAYER_DAMAGE.isAuthorized(context.getSender()))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to specify the damage!");
            return;
        }
        if (context.hasFlag("u") && !module.perms().COMMAND_LIGHTNING_UNSAFE.isAuthorized(context.getSender()))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to use the unsafe flag");
            return;
        }

        if (context.hasParam("player"))
        {
            user = context.getUser("player");
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player not found!");
                return;
            }
            location = user.getLocation();
            if ((damage != -1 && damage < 0) || damage > 20)
            {
                context.sendTranslated(NEGATIVE, "The damage value has to be a number from 1 to 20");
                return;
            }
            user.setFireTicks(20 * context.getParam("fireticks", 0));
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated(NEGATIVE, "This command can only be used by a player!");
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

    @Command(desc = "Slaps a player",
             indexed = {@Grouped(value = @Indexed(label = "player", type = User.class)),
                        @Grouped(req = false, value = @Indexed(label = "damage"))})
    public void slap(CommandContext context)
    {
        User user = context.getArg(0);
        int damage = context.getArg(1, 3);

        if (damage < 1 || damage > 20)
        {
            context.sendTranslated(NEGATIVE, "Only damage values from 1 to 20 are allowed!");
            return;
        }

        final Vector userDirection = user.getLocation().getDirection();
        user.damage(damage);
        user.setVelocity(new Vector(userDirection.getX() * damage / 2, 0.05 * damage, userDirection.getZ() * damage / 2));
    }

    @Command(desc = "Burns a player",
             indexed = {@Grouped(value = @Indexed(label = "player", type = User.class)),
                        @Grouped(req = false, value = @Indexed(label = "seconds"))},
             flags = @Flag(longName = "unset", name = "u"))
    public void burn(ParameterizedContext context)
    {
        User user = context.getArg(0);
        int seconds = context.getArg(1, 5);

        if (context.hasFlag("u"))
        {
            seconds = 0;
        }
        else if (seconds < 1 || seconds > this.module.getConfig().command.burn.maxTime)
        {
            context.sendTranslated(NEGATIVE, "Only 1 to {integer} seconds are allowed!", this.module.getConfig().command.burn.maxTime);
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

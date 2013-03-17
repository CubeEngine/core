package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunPerm;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

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
        PlayerInventory userInventory = null;
        
        if(!(context.getSender() instanceof User))
        {
            console = true;
        }
        if(context.hasParam( "player" ) )
        {
            if(!FunPerm.COMMAND_HAT_OTHER.isAuthorized( context.getSender() ) )
            {
                context.sendTranslated("&cYou can't set the had of an other player.");
                return;
            }
            
            user = context.getUser( "player" );
            
            if(user == null)
            {
                context.sendTranslated("&cUser not found!");
                return;
            }
        }
        else if(!console)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated("&cYou has to specify a user!");
            return;
        }
        
        if(context.hasArg( 0 ) )
        {
            if(!FunPerm.COMMAND_HAT_ITEM.isAuthorized( context.getSender() ))
            {
                context.sendTranslated("&cYou can only use your item in hand!");
                return;
            }
            head = Match.material().itemStack( context.getArg( 0, String.class ) );
            if(head == null)
            {
                context.sendTranslated("&cItem not found!");
                return;
            }
        }
        else if(console)
        {
            context.sendTranslated("&cYou has to specify an item!");
            return;
        }
        else
        {
            senderInventory = ((User)context.getSender()).getInventory();
            head = senderInventory.getItemInHand().clone();
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
        
        if( !(context.hasFlag("q") && FunPerm.COMMAND_HAT_QUIET.isAuthorized(context.getSender()) ) && FunPerm.COMMAND_HAT_NOTIFY.isAuthorized( user ) )
        {
            user.sendTranslated("&aYour hat was changed");
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
            if (!FunPerm.COMMAND_EXPLOSION_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to use the player parameter.");
                return;
            }
            user = context.getUser("player");
            if (user == null)
            {
                context.sendTranslated("&cUser not found!");
                return;
            }
            location = user.getLocation();
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated("&cThis command can only be used by a player!");
                return;
            }
            user = (User)context.getSender();
            location = user.getTargetBlock(null, this.module.getConfig().explosionDistance).getLocation();
        }

        if (power > this.module.getConfig().explosionPower)
        {
            context.sendTranslated("&cThe power of the explosion shouldn't be greater than %d", this.module.getConfig().explosionPower);
            return;
        }

        if (!FunPerm.COMMAND_EXPLOSION_BLOCK_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("b") || context.hasFlag("u")))
        {
            context.sendTranslated("&cYou are not allowed to break blocks");
            return;
        }
        if (!FunPerm.COMMAND_EXPLOSION_FIRE.isAuthorized(context.getSender()) && (context.hasFlag("f") || context.hasFlag("u")))
        {
            context.sendTranslated("&cYou are not allowed to set fireticks");
            return;
        }
        if (!FunPerm.COMMAND_EXPLOSION_PLAYER_DAMAGE.isAuthorized(context.getSender()) && (context.hasFlag("p") || context.hasFlag("u")))
        {
            context.sendTranslated("&cYou are not allowed to damage an other player");
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

        if (damage != -1 && !FunPerm.COMMAND_LIGHTNING_PLAYER_DAMAGE.isAuthorized(context.getSender()))
        {
            context.sendTranslated("&cYou are not allowed the use the damage parameter");
            return;
        }
        if (context.hasFlag("u") && !FunPerm.COMMAND_LIGHTNING_UNSAFE.isAuthorized(context.getSender()))
        {
            context.sendTranslated("&cYou are not allowed to use the unsafe flag");
            return;
        }

        if (context.hasParam("player"))
        {
            user = context.getUser("player");
            if (user == null)
            {
                context.sendTranslated("&cUser not found!");
                return;
            }
            location = user.getLocation();
            if ((damage != -1 && damage < 0) || damage > 20)
            {
                context.sendTranslated("&cThe damage value has to be a number from 1 to 20");
                return;
            }
            user.setFireTicks(20 * context.getParam("fireticks", Integer.valueOf(0)));
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated("&cThis command can only be used by a player!");
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
            context.sendTranslated("&cUser not found!");
            return;
        }

        int damage = context.getArg(1, Integer.class, 3);

        if (damage < 1 || damage > 20)
        {
            context.sendTranslated("&cOnly damage values from 1 to 20 are allowed!");
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
            context.sendTranslated("&cUser not found!");
            return;
        }

        int seconds = context.getArg(1, Integer.class, 5);

        if (context.hasFlag("u"))
        {
            seconds = 0;
        }
        else if (seconds < 1 || seconds > 26)
        {
            context.sendTranslated("&cOnly 1 to 26 seconds are permitted!");
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

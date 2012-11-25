package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

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
    
    @Command
    (
        desc = "Creates an explosion",
        params = 
        {
            @Param(names = {"player", "p"}, type = User.class),
            @Param(names = {"damage", "d"}, type = Integer.class)
        },
        flags = 
        { 
            @Flag(longName = "unsafe", name = "u"),
            @Flag(longName = "fire", name = "f")
        },
        max = 0,
        usage = "[player <name>] [damage <value>] [-unsafe] [-fire]"
    )
    public void explosion(CommandContext context)
    {
        User user;
        Location location;
        int power = context.getNamed("damage", Integer.class, 1);
        boolean fire = context.hasFlag("f") ? true : false;
        
        if(context.hasNamed("player"))
        {
            user = context.getNamed("player", User.class);
            if (user == null)
            {
                illegalParameter(context, "core", "&cUser not found!");
            }
            location = user.getLocation();
        }
        else
        {
            user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");
            location = user.getTargetBlock(null, this.module.getConfig().explosionDistance).getLocation();
        }
        
        if(power > this.module.getConfig().explosionPower)
        {
            illegalParameter(context, "fun", "&cThe power of the explosion shouldn't be greater than %d", this.module.getConfig().explosionPower);
        }
        
        if( !context.hasFlag("u") )
        {
            explosionListener.add(location);
        }
        
        user.getWorld().createExplosion(location, power, fire);
    }
    
    @Command
    (
        names = {"lightning", "strike"},
        desc = "Strucks a player or the location you are looking at by lightning.",
        max = 0,
        params = 
        {
            @Param(names = {"player", "p"}, type = User.class),
            @Param(names = {"damage", "d"}, type = Integer.class),
            @Param(names = {"fireticks", "f"}, type = Integer.class)
        },
        flags = {@Flag(longName = "unsafe", name = "u")},
        usage = "[player <name>] [damage <value>] [fireticks <seconds>] [-unsafe]"
    )
    public void lightning(CommandContext context)
    {
        User user;
        Location location;
        int damage = context.getNamed("damage", Integer.class, Integer.valueOf(-1));

        if(context.hasNamed("player"))
        {
            user = context.getNamed("player", User.class);
            if (user == null)
            {
                illegalParameter(context, "core", "&cUser not found!");
            }
            location = user.getLocation();
            if( (damage != -1 && damage < 0) || damage > 20 )
            {
                illegalParameter(context, "fun", "&cThe damage value has to be a number from 1 to 20");
            }
            user.setFireTicks(20 * context.getNamed("fireticks", Integer.class, Integer.valueOf(0)));
        }
        else
        {
            user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");
            location = user.getTargetBlock(null, this.module.getConfig().lightningDistance).getLocation();
        }

        if(context.hasFlag("u"))
        {
            user.getWorld().strikeLightning(location);
        }
        else 
        {
            user.getWorld().strikeLightningEffect(location);
        }
        if(damage != -1)
        {
            user.damage(damage);
        }
    }
    
    @Command(
        desc = "Slaps a player",
        min = 1,
        max = 2,
        usage = "<player> [damage]"
    )
    public void slap(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
              illegalParameter(context, "core", "&cUser not found!");
        }
        
        int damage = context.getIndexed(1, Integer.class, 3);

        if (damage < 1 || damage > 20)
        {
            illegalParameter(context, "fun", "&cOnly damage values from 1 to 20 are allowed!");
            return;
        }
        
        user.damage(damage);
        user.setVelocity(new Vector(user.getLocation().getDirection().getX() * damage / 2, 0.05 * damage, user.getLocation().getDirection().getZ() * damage / 2));
    }
    
    @Command(
        desc = "Burns a player",
        min = 1,
        max = 2,
        flags = {@Flag(longName = "unset", name = "u")},
        usage = "<player> [seconds] [-unset]"
    )
    public void burn(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "core", "&cUser not found!");
        }
        
        int seconds = context.getIndexed(1, Integer.class, 5);

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
        private Set<Location> locations;
        
        public ExplosionListener()
        {
            this.locations = new HashSet<Location>();
        }
        
        public void add(Location location)
        {
            if(!this.contains(location))
            {
                locations.add(location);
            }
        }
        
        public boolean contains(Location location)
        {
            return locations.contains(location);
        }
        
        public void remove(Location location)
        {
            locations.remove(location);
        }
        
        @EventHandler
        public void onEntityExplode(EntityExplodeEvent event)
        {
            if( event.getEntity() == null && this.contains( event.getLocation() ) )
            {
                this.remove(event.getLocation());
                event.blockList().clear();
            }
        }
    }
}

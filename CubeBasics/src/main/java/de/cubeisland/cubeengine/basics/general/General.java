package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.core.user.User;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 *
 * @author Anselm Brehme
 */
public class General  //TODO remove or at least reduce this class
{
    public List<Player> near(Location loc, int radius)
    {
        List<Player> nearPlayers = new ArrayList<Player>();
        List<LivingEntity> nearEntities = loc.getWorld().getLivingEntities();
        for (LivingEntity entity : nearEntities)
        {
            if (entity instanceof Player)
            {
                if (entity.getLocation().distanceSquared(loc) < radius * radius)
                {
                    nearPlayers.add((Player) entity);
                }
            }
        }
        return nearPlayers;
    }

    public String randomDeathMessage(User user, EntityDamageByEntityEvent lastDmg)
    {
        return null;
        /*
         int random;//TODO more messages
         String s = _(user, "basics", "&c%s died!", user.getName());
         DamageCause cause = lastDmg.getCause();
        
         if (cause == DamageCause.ENTITY_ATTACK)
         {
         if (lastDmg.getDamager() instanceof Player)
         {
         s = _(user, "basics", "&c%s was slain by %s", user.getName(),((Player)lastDmg.getDamager()).getName());
         }
         else
         {
         s = _(user, "basics", "&c%s was slain by %s", user.getName(),lastDmg.getDamager().toString());
         }
         }
         if (cause == DamageCause.PROJECTILE)
         {
         Projectile projectile = (Projectile)lastDmg.getDamager();
         if (projectile.getShooter() instanceof Player)
         {
         s = _(user, "basics", "&c%s was shot by %s", user.getName(),((Player)lastDmg.getDamager()).getName());
         }
         else
         {
         if (projectile.getShooter() instanceof Ghast ||projectile.getShooter() instanceof Blaze)
         s = _(user, "basics", "&c%s was fireballed by %s", user.getName(),lastDmg.getDamager().toString());
         else if (projectile.getShooter() == null)
         s = _(user, "basics", "&c%s was shot by an arrow", user.getName());
         else
         s = _(user, "basics", "&c%s was shot by %s", user.getName(),lastDmg.getDamager().toString());
         }
         }
         return s;*/
    }

    public String randomDeathMessage(User user, EntityDamageEvent lastDmg)
    {
        return null;
        /*
         int random;//TODO more messages
         String s = _(user, "basics", "&c%s died", user.getName());
         DamageCause cause = lastDmg.getCause();
         if (cause == DamageCause.DROWNING)
         s = _(user, "basics", "&c%s drowned", user.getName());
         if (cause == DamageCause.DROWNING)
         s = _(user, "basics", "&c%s hit the ground to hard", user.getName());
         if (cause == DamageCause.VOID)
         s = _(user, "basics", "&c%s fell out of the world", user.getName());
         if (cause == DamageCause.LAVA)
         s = _(user, "basics", "&c%s tried to swim in lava", user.getName());
         if (cause == DamageCause.FIRE)
         s = _(user, "basics", "&c%s went up in flames", user.getName());
         if (cause == DamageCause.FIRE_TICK)
         s = _(user, "basics", "&c%s burned to death", user.getName());
         if (cause == DamageCause.ENTITY_EXPLOSION)
         s = _(user, "basics", "&c%s blew up", user.getName());
         if (cause == DamageCause.SUFFOCATION)
         s = _(user, "basics", "&c%s suffocated in a wall", user.getName());
         if (cause == DamageCause.CONTACT)
         s = _(user, "basics", "&c%s was pricked to death", user.getName());
         if (cause == DamageCause.STARVATION)
         s = _(user, "basics", "&c%s starved to death", user.getName());
         return s;*/
    }
}

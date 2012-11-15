package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredPrevention;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * Prevents damage.
 */
public class DamagePrevention extends FilteredPrevention<DamageCause>
{
    private String  damagerMessage;
    private boolean preventPotions;
    private String  potionMessage;

    public DamagePrevention(Guests guests)
    {
        super("damage", guests, false);
        setThrottleDelay(3);
        setFilterMode(FilterMode.WHITELIST);
        setFilterItems(EnumSet.of(DamageCause.VOID));
        this.damagerMessage = null;
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader() + "\n" +
                "Configuration info:\n" +
                "    damagerMessage: this message will be send to the player who attacked a guest\n" +
                "    preventPostions: if this is enabled potion effects also get prevented\n" +
                "    postionMessage: this will be send to players protected from postions\n";
    }

    @Override
    public Configuration getDefaultConfig()
    {
        Configuration config = super.getDefaultConfig();

        config.set("damagerMessage", _("guests", "damagerMessage"));
        config.set("preventPotions", true);
        config.set("potionMessage", _("guests", "potionMessage"));

        return config;
    }

    @Override
    public List<String> encodeSet(Set<DamageCause> set)
    {
        List<String> damageCauses = super.encodeSet(set);

        for (int i = 0; i < damageCauses.size(); ++i)
        {
            damageCauses.set(i, damageCauses.get(i).toLowerCase().replace('_', ' '));
        }

        return damageCauses;
    }

    @Override
    public Set<DamageCause> decodeList(List<String> list)
    {
        Set<DamageCause> damageCauses = EnumSet.noneOf(DamageCause.class);
        for (String entry : list)
        {
            try
            {
                damageCauses.add(DamageCause.valueOf(entry.trim().replace(" ", "_").toUpperCase()));
            }
            catch (IllegalArgumentException e)
            {}
        }
        return damageCauses;
    }

    @Override
    public void enable()
    {
        super.enable();
        final Configuration config = getConfig();

        this.damagerMessage = parseMessage(config.getString("damagerMessage"));
        this.preventPotions = config.getBoolean("preventPotions");
        this.potionMessage = parseMessage(config.getString("potionMessage"));

    }

    @Override
    public void disable()
    {
        super.disable();
        this.damagerMessage = null;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void damage(EntityDamageEvent event)
    {
        final Entity entity = event.getEntity();
        if (entity instanceof Player)
        {
            final Player player = (Player)entity;
            if (prevent(event, player, event.getCause()) && this.damagerMessage != null)
            {
                player.setFireTicks(0);
                if (event instanceof EntityDamageByEntityEvent)
                {
                    final Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
                    if (damager instanceof Player)
                    {
                        ((Player)damager).sendMessage(this.damagerMessage);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void splash(PotionSplashEvent event)
    {
        if (!this.preventPotions)
        {
            return;
        }
        Collection<LivingEntity> affectedEntities = event.getAffectedEntities();
        Player affectedPlayer;
        Entity shooter = event.getPotion().getShooter();
        int affectedCount = 0;
        for (LivingEntity entity : affectedEntities)
        {
            if (entity instanceof Player)
            {
                affectedPlayer = (Player)entity;
                if (!can(affectedPlayer))
                {
                    ++affectedCount;
                    affectedEntities.remove(entity);
                    if (this.potionMessage != null)
                    {
                        affectedPlayer.sendMessage(this.potionMessage);
                    }
                }
            }
        }
        if (affectedCount > 0 && this.damagerMessage != null && shooter != null && shooter instanceof Player)
        {
            ((Player)shooter).sendMessage(this.damagerMessage);
        }
    }
}

package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.logger.config.KillConfig;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillLogger extends Logger<KillConfig>
{
    public KillLogger(Log module)
    {
        super(module, KillConfig.class);
    }

    public void logKill(DamageCause cause, Entity damager, Entity damagee, Location loc)
    {
        //TODO save additional data for mobs e.g. colored sheeps ??
        KillCause killCause = KillCause.getKillCause(cause, damager);
        int causeID;
        if (this.checkLog(killCause, damagee))
        {
            if (killCause.equals(KillCause.PLAYER))
            {
                causeID = CubeEngine.getUserManager().getExactUser((Player)damager).getKey().intValue();
            }
            else if (damager == null)
            {
                causeID = killCause.getId();
            }
            else
            {
                causeID = -damager.getType().getTypeId();
            }
            int killedId;
            if (damagee instanceof Player)
            {
                killedId = CubeEngine.getUserManager().getExactUser((Player)damagee).getKey().intValue();
            }
            else
            {
                killedId = -damagee.getType().getTypeId();
            }
            this.module.getLogManager().logKillLog(causeID, loc, killedId);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        EntityDamageEvent dmgEvent = entity.getLastDamageCause();
        if (dmgEvent == null)
        {
            return; // squids dying in air, lazy bukkit :S
        }
        if (dmgEvent instanceof EntityDamageByEntityEvent)
        {
            Entity damager = ((EntityDamageByEntityEvent)dmgEvent).getDamager();
            if (dmgEvent.getCause().equals(DamageCause.PROJECTILE) && damager instanceof Projectile)
            {
                if (((Projectile)damager).getShooter() != null)
                {
                    this.logKill(dmgEvent.getCause(), ((Projectile)damager).getShooter(), event.getEntity(), event.getEntity().getLocation());
                }
                // else Projectile shot by Dispenser ?
            }
            else
            {
                this.logKill(dmgEvent.getCause(), damager, event.getEntity(), event.getEntity().getLocation());
            }
        }
        else
        {
            this.logKill(dmgEvent.getCause(), null, event.getEntity(), event.getEntity().getLocation());
        }
    }

    private boolean checkLog(KillCause killer, Entity killed)
    {
        KillConfig config = this.configs.get(killed.getWorld());
        if (config.enabled)
        {
            switch (killer)
            {
                case PLAYER:
                    if (killed instanceof Player)
                    {
                        if (!config.logPvp)
                        {
                            return false;
                        }
                        return true;
                    }
                    if (!config.logKillsByPlayer)
                    {
                        return false;
                    }
                    break;

                case MONSTER:
                    if (!config.logKillsByMonster)
                    {
                        return false;
                    }
                    break;
                case BOSSMONSTER:
                    if (!config.logKillsByBoss)
                    {
                        return false;
                    }
                    break;
                case ENVIRONEMENT:
                case PLAYERSTATUS:
                    if (!config.logKillsByEnvironement)
                    {
                        return false;
                    }
                    break;
                case LAVA:
                    if (!config.logKillsByLava)
                    {
                        return false;
                    }
                    break;
                case MAGIC:
                    if (!config.logKillsByMagic)
                    {
                        return false;
                    }
                    break;
                case OTHER:
                    if (!config.logKillsByOther)
                    {
                        return false;
                    }
            }
            if (killed instanceof Player)
            {
                if (!config.logPlayerKilled)
                {
                    return false;
                }
            }
            else if (killed instanceof Wither || killed instanceof Giant || killed instanceof EnderDragon)
            {
                if (!config.logBossKilled)
                {
                    return false;
                }
            }
            else if (killed instanceof Monster || killed instanceof Ghast || killed instanceof Slime)
            {

                if (!config.logMonsterKilled)
                {
                    return false;
                }
            }
            else if (killed instanceof Animals)
            {
                if (killed instanceof Tameable)
                {
                    if (!config.logPetKilled)
                    {
                        return false;
                    }
                }
                else if (!config.logAnimalKilled)
                {
                    return false;
                }
            }
            else if (killed instanceof NPC)
            {
                if (!config.logNpcKilled)
                {
                    return false;
                }
            }
            else if (!config.logOtherKilled)
            {
                return false;
            }
            return true;
        }
        return false;
    }

    public static enum KillCause
    {
        PLAYER(
            0),
        MONSTER(
            0),
        BOSSMONSTER(
            0),
        NEUTRALMONSTER(
            0),
        ENVIRONEMENT(
            -1),
        LAVA(
            -2),
        PLAYERSTATUS(
            -3),
        MAGIC(
            -4),
        OTHER(
            -5);

        private KillCause(int causeID)
        {
            this.causeID = causeID;
        }

        final private int causeID;

        public int getId()
        {
            return causeID;
        }

        public static KillCause getKillCause(DamageCause cause, Entity damager)
        {
            switch (cause)
            {
                case ENTITY_ATTACK:
                    switch (damager.getType())
                    {
                        case PLAYER:
                            return PLAYER;
                        case BLAZE:
                        case CREEPER:
                        case ENDERMAN:
                        case GHAST:
                        case MAGMA_CUBE:
                        case SILVERFISH:
                        case SKELETON:
                        case SLIME:
                        case SPIDER:
                        case ZOMBIE:
                        case PIG_ZOMBIE:
                        case WITCH:
                            return MONSTER;

                        case GIANT:
                        case ENDER_DRAGON:
                        case WITHER:
                        case WITHER_SKULL:
                            return BOSSMONSTER;
                        case IRON_GOLEM:
                        case WOLF:
                            return NEUTRALMONSTER;

                    }
                case LIGHTNING:
                case FALL:
                case CONTACT:
                    return ENVIRONEMENT;
                case DROWNING:
                case SUFFOCATION:
                case STARVATION:
                    return PLAYERSTATUS;
                case LAVA:
                    return LAVA;
                case POISON:
                case MAGIC:
                    return MAGIC;
                default:
                    return OTHER;
            }
        }
    }
}

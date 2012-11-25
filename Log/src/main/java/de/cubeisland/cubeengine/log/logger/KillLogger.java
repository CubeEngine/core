package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class KillLogger extends Logger<KillLogger.KillConfig>
{
    public KillLogger()
    {
        super(LogAction.KILL);
        this.config = new KillConfig();
    }

    public void logKill(DamageCause cause, Entity damager, Entity damagee, Location loc)
    {
        //TODO save additional data for mobs e.g. colored sheeps
        KillCause killCause = KillCause.getKillCause(cause, damager);
        int causeID;
        if (this.checkLog(killCause, damagee))
        {
            if (killCause.equals(KillCause.PLAYER))
            {
                causeID = CubeEngine.getUserManager().getExactUser((Player)damager).getKey();
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
                killedId = CubeEngine.getUserManager().getExactUser((Player)damagee).getKey();
            }
            else
            {
                killedId = -damagee.getType().getTypeId();
            }
            this.module.getLogManager().logKillLog(causeID, loc, killedId);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof LivingEntity)
        {
            LivingEntity entity = (LivingEntity)event.getEntity();
            if (entity.getHealth() - event.getDamage() <= 0)
            {
                if (event instanceof EntityDamageByEntityEvent)
                {
                    Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
                    if (event.getCause().equals(DamageCause.PROJECTILE) && damager instanceof Projectile)
                    {
                        if (((Projectile)damager).getShooter() != null)
                        {
                            this.logKill(event.getCause(), ((Projectile)damager).getShooter(), event.getEntity(), event.getEntity().getLocation());
                        }
                        // else Projectile shot by Dispenser ?
                    }
                    else
                    {
                        this.logKill(event.getCause(), damager, event.getEntity(), event.getEntity().getLocation());
                    }
                }
                else
                {
                    this.logKill(event.getCause(), null, event.getEntity(), event.getEntity().getLocation());
                }
            }
        }
    }

    private boolean checkLog(KillCause killer, Entity killed)
    {
        switch (killer)
        {
            case PLAYER:
                if (killed instanceof Player)
                {
                    if (!this.config.logPvp)
                    {
                        return false;
                    }
                    return true;
                }
                if (!this.config.logKillsByPlayer)
                {
                    return false;
                }
                break;

            case MONSTER:
                if (!this.config.logKillsByMonster)
                {
                    return false;
                }
                break;
            case BOSSMONSTER:
                if (!this.config.logKillsByBoss)
                {
                    return false;
                }
                break;
            case ENVIRONEMENT:
            case PLAYERSTATUS:
                if (!this.config.logKillsByEnvironement)
                {
                    return false;
                }
                break;
            case LAVA:
                if (!this.config.logKillsByLava)
                {
                    return false;
                }
                break;
            case MAGIC:
                if (!this.config.logKillsByMagic)
                {
                    return false;
                }
                break;
            case OTHER:
                if (!this.config.logKillsByOther)
                {
                    return false;
                }
        }
        if (killed instanceof Player)
        {
            if (!this.config.logPlayerKilled)
            {
                return false;
            }
        }
        else if (killed instanceof Wither || killed instanceof Giant || killed instanceof EnderDragon)
        {
            if (!this.config.logBossKilled)
            {
                return false;
            }
        }
        else if (killed instanceof Monster || killed instanceof Ghast || killed instanceof Slime)
        {

            if (!this.config.logMonsterKilled)
            {
                return false;
            }
        }
        else if (killed instanceof Animals)
        {
            if (killed instanceof Tameable)
            {
                if (!this.config.logPetKilled)
                {
                    return false;
                }
            }
            else if (!this.config.logAnimalKilled)
            {
                return false;
            }
        }
        else if (killed instanceof NPC)
        {
            if (!this.config.logNpcKilled)
            {
                return false;
            }
        }
        else if (!this.config.logOtherKilled)
        {
            return false;
        }
        return true;
    }

    public static class KillConfig extends SubLogConfig
    {
        @Option("log-when-killer-is.player")
        public boolean logKillsByPlayer = true;
        @Option("log-when-killer-is.monster")
        public boolean logKillsByMonster = false;
        @Option("log-when-killer-is.boss")
        public boolean logKillsByBoss = false;
        @Comment("Environemental damage such as: lightning, fall-damage, drowning, suffocation, cacti, starvation BUT NOT lava")
        @Option("log-when-killer-is.environement")
        public boolean logKillsByEnvironement = false;
        @Option("log-when-killer-is.lava")
        public boolean logKillsByLava = false;
        @Option("log-when-killer-is.magic")
        public boolean logKillsByMagic = true;
        @Option("log-when-killer-is.unkown")
        public boolean logKillsByOther = true;
        @Comment("Log player-deaths BUT NOT pvp")
        @Option("log-when-killed-is.player")
        public boolean logPlayerKilled = true;
        @Comment("Will log pvp even if killer and/or killed is player is disabled")
        @Option("log-pvp")
        public boolean logPvp = true;
        @Option("log-when-killed-is.monster")
        public boolean logMonsterKilled = false;
        @Option("log-when-killed-is.boss")
        public boolean logBossKilled = true;
        @Comment("Animals are here: chickens, cows, pigs, sheeps")
        @Option("log-when-killed-is.animal")
        public boolean logAnimalKilled = true;
        @Comment("Pets are here: Tamed wolfes and ocelots")
        @Option("log-when-killed-is.pet")
        public boolean logPetKilled = true;
        @Option("log-when-killed-is.npc")
        public boolean logNpcKilled = true;
        @Comment("Other are here: bats, squid, golems and more")
        @Option("log-when-killed-is.other")
        public boolean logOtherKilled = false;

        public KillConfig()
        {
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "kills";
        }
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

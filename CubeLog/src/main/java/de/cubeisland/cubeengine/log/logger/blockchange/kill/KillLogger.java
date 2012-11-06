package de.cubeisland.cubeengine.log.logger.blockchange.kill;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.logger.LogAction;
import de.cubeisland.cubeengine.log.logger.Logger;
import de.cubeisland.cubeengine.log.logger.SubLogConfig;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class KillLogger extends Logger<KillLogger.KillConfig>
{
    private KillLogManager killLogManager;

    public KillLogger()
    {
        super(LogAction.KILL);
        this.config = new KillConfig();
        this.killLogManager = new KillLogManager(module.getDatabase());

    }

    public void logKill(DamageCause cause, Entity damager, Entity damagee, Location loc)
    {
        KillCause killCause = KillCause.getKillCause(cause, damager);
        int causeID;
        if (killCause.equals(KillCause.PLAYER))
        {
            causeID = CubeEngine.getUserManager().getExactUser((Player)damager).getKey();
        }
        else
        {
            causeID = killCause.getId();
        }
        int killedId;
        if (damagee instanceof Player)
        {
            killedId = CubeEngine.getUserManager().getExactUser((Player)damagee).getKey();
        }
        else
        {
            killedId = damagee.getType().getTypeId();
        }
        this.killLogManager.store(new KillLog(causeID, killedId, loc));
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
                    this.logKill(event.getCause(), ((EntityDamageByEntityEvent)event).getDamager(),
                        event.getEntity(), event.getEntity().getLocation());
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
        boolean log;
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

            case BLAZE:
            case CREEPER:

            case ENDERMAN:
            case GHAST:
            case MAGMA_CUBE:
            case SILVER_FISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case WOLF:
            case IRON_GOLEM:
            case ZOMBIE:

            case ZOMBIE_PIGMAN:
                if (!this.config.logKillsByMonster)
                {
                    return false;
                }
                break;
            case WITHER:
            case ENDER_DRAGON:
                if (!this.config.logKillsByBoss)
                {
                    return false;
                }
                break;
            case LIGHTNING:
            case FALL_DAMAGE:
            case DROWNING:
            case SUFFOCATION:
            case CACTI:
            case STARVATION:
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
            case POISON: //cant really kill someone in normal minecraft
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
        @Option(value = "log-when-killer-is.player")
        public boolean logKillsByPlayer = true;
        @Option(value = "log-when-killer-is.monster")
        public boolean logKillsByMonster = false;
        @Option(value = "log-when-killer-is.boss")
        public boolean logKillsByBoss = false;
        @Comment("Environemental damage such as: lightning, fall-damage, drowning, suffocation, cacti, starvation BUT NOT lava")
        @Option(value = "log-when-killer-is.environement")
        public boolean logKillsByEnvironement = false;
        @Option(value = "log-when-killer-is.lava")
        public boolean logKillsByLava = false;
        @Option(value = "log-when-killer-is.magic")
        public boolean logKillsByMagic = true;
        @Option(value = "log-when-killer-is.unkown")
        public boolean logKillsByOther = true;
        @Comment("Log player-deaths BUT NOT pvp")
        @Option(value = "log-when-killed-is.player")
        public boolean logPlayerKilled = true;
        @Comment("Will log pvp even if killer and/or killed is player is disabled")
        @Option(value = "log-pvp")
        public boolean logPvp = true;
        @Option(value = "log-when-killed-is.monster")
        public boolean logMonsterKilled = false;
        @Option(value = "log-when-killed-is.boss")
        public boolean logBossKilled = true;
        @Comment("Animals are here: chickens, cows, pigs, sheeps")
        @Option(value = "log-when-killed-is.animal")
        public boolean logAnimalKilled = true;
        @Comment("Pets are here: Tamed wolfes and ocelots")
        @Option(value = "log-when-killed-is.pet")
        public boolean logPetKilled = true;
        @Option(value = "log-when-killed-is.npc")
        public boolean logNpcKilled = true;
        @Comment("Other are here: bats, squid, golems and more")
        @Option(value = "log-when-killed-is.other")
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
        PLAYER(-1),
        BLAZE(-2),
        CREEPER(-3),
        ENDER_DRAGON(-4),
        ENDERMAN(-4),
        GHAST(-5),
        IRON_GOLEM(-6),
        MAGMA_CUBE(-7),
        SILVER_FISH(-8),
        SKELETON(-9),
        SLIME(-10),
        SPIDER(-11),
        WOLF(-12),
        ZOMBIE(-13),
        ZOMBIE_PIGMAN(-14),
        LIGHTNING(-15),
        FALL_DAMAGE(-16),
        DROWNING(-17),
        SUFFOCATION(-18),
        STARVATION(-19),
        CACTI(-20),
        LAVA(-21),
        POISON(-22), //cant really kill someone in normal minecraft
        WITHER(-23),
        MAGIC(-24),
        OTHER(-25);
        //TODO projectile -> find owner

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
                            return BLAZE;
                        case CREEPER:
                            return CREEPER;
                        case ENDER_DRAGON:
                            return ENDER_DRAGON;
                        case ENDERMAN:
                            return ENDERMAN;
                        case GHAST:
                            return GHAST;
                        case IRON_GOLEM:
                            return IRON_GOLEM;
                        case MAGMA_CUBE:
                            return MAGMA_CUBE;
                        case SILVERFISH:
                            return SILVER_FISH;
                        case SKELETON:
                            return SKELETON;
                        case SLIME:
                            return SLIME;
                        case SPIDER:
                            return SPIDER;
                        case WOLF:
                            return WOLF;
                        case ZOMBIE:
                            return ZOMBIE;
                        case PIG_ZOMBIE:
                            return ZOMBIE_PIGMAN;
                    }
                case LIGHTNING:
                    return LIGHTNING;
                case FALL:
                    return FALL_DAMAGE;
                case DROWNING:
                    return DROWNING;
                case SUFFOCATION:
                    return SUFFOCATION;
                case STARVATION:
                    return STARVATION;
                case CONTACT:
                    return CACTI; // or other blockcontact
                case LAVA:
                    return LAVA;
                case POISON:
                    return POISON;
                case WITHER:
                    return WITHER;
                case MAGIC:
                    return MAGIC;
                default:
                    return OTHER;
            }
        }
    }
}

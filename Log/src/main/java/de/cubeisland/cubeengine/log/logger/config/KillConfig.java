package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.LoggerConfig;

public class KillConfig extends LoggerConfig
{
    public KillConfig()
    {
        super(true);
    }

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

    @Override
    public String getName()
    {
        return "kills";
    }
}

package Area;

import de.cubeisland.CubeWar.Util;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author Faithcaio
 */
public class Area_Arena extends Area{

    public Area_Arena(Configuration config) 
    {
        //ARENA_Default NICHT in DB!
        super(  -5, AreaType.ARENA,"DEFAULT_ARENA",
                config.getBoolean("cubewar.area.arena_default.pvp.PvP"),
                config.getBoolean("cubewar.area.arena_default.pvp.damage"),
                config.getBoolean("cubewar.area.arena_default.pvp.friendlyfire"),
                config.getInt    ("cubewar.area.arena_default.pvp.spawnprotectseconds"),
                config.getBoolean("cubewar.area.arena_default.monster.spawn"),
                config.getBoolean("cubewar.area.arena_default.monster.damage"),
                config.getBoolean("cubewar.area.arena_default.build.place"),
                config.getBoolean("cubewar.area.arena_default.build.destroy"),
                config.getBoolean("cubewar.area.arena_default.use.fire"),
                config.getBoolean("cubewar.area.arena_default.use.lava"),
                config.getBoolean("cubewar.area.arena_default.use.water"),
                config.getBoolean("cubewar.area.arena_default.power.powerloss"),
                config.getBoolean("cubewar.area.arena_default.power.powergain"),
                config.getStringList("cubewar.area.arena_default.denycommands"),
                Util.convertListStringToMaterial(config.getStringList("cubewar.area.arena_default.protect")),
                "ARENA-NO", "ARENA-NO-DESC.",
                config.getBoolean("cubewar.area.arena_default.economy.bank"),
                config.getInt    ("cubewar.area.arena_default.power.permanentpower"),
                config.getInt    ("cubewar.area.arena_default.power.powerboost")
            );
    }
}

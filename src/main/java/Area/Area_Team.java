package Area;

import de.cubeisland.CubeWar.Util;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author Faithcaio
 */
public class Area_Team extends Area{

    public Area_Team(Configuration config) 
    {
        //TEAM_Default NICHT in DB!
        
        super(  -10, AreaType.TEAMZONE,"DEFAULT_TEAM",
                config.getBoolean("cubewar.area.team_default.pvp.PvP"),
                config.getBoolean("cubewar.area.team_default.pvp.damage"),
                config.getBoolean("cubewar.area.team_default.pvp.friendlyfire"),
                config.getInt    ("cubewar.area.team_default.pvp.spawnprotectseconds"),
                config.getBoolean("cubewar.area.team_default.monster.spawn"),
                config.getBoolean("cubewar.area.team_default.monster.damage"),
                config.getBoolean("cubewar.area.team_default.build.place"),
                config.getBoolean("cubewar.area.team_default.build.destroy"),
                config.getBoolean("cubewar.area.team_default.use.fire"),
                config.getBoolean("cubewar.area.team_default.use.lava"),
                config.getBoolean("cubewar.area.team_default.use.water"),
                config.getBoolean("cubewar.area.team_default.power.powerloss"),
                config.getBoolean("cubewar.area.team_default.power.powergain"),
                config.getStringList("cubewar.area.team_default.denycommands"),
                Util.convertListStringToMaterial(config.getStringList("cubewar.area.team_default.protect")),
                "TEAM-NO", "TEAM-NO-DESC.",
                config.getBoolean("cubewar.area.team_default.economy.bank"),
                config.getInt    ("cubewar.area.team_default.power.permanentpower"),
                config.getInt    ("cubewar.area.team_default.power.powerboost")
            );
    }
    
    public int registerDB()
    {
        //TODO DB...
        //TODO get ID-Key in DB
        return 1;
    }
}

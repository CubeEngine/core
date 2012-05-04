package Area;

import de.cubeisland.CubeWar.Util;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author Faithcaio
 */
public class Area_Safezone extends Area {

    public Area_Safezone(Configuration config) 
    {
        //SAFEZONE ID = -2
        super(  -2,  AreaType.SAFEZONE,"SafeZone",
                config.getBoolean("cubewar.area.safezone.pvp.PvP"),
                config.getBoolean("cubewar.area.safezone.pvp.damage"),
                config.getBoolean("cubewar.area.safezone.pvp.friendlyfire"),
                config.getInt    ("cubewar.area.safezone.pvp.spawnprotectseconds"),
                config.getBoolean("cubewar.area.safezone.monster.spawn"),
                config.getBoolean("cubewar.area.safezone.monster.damage"),
                config.getBoolean("cubewar.area.safezone.build.place"),
                config.getBoolean("cubewar.area.safezone.build.destroy"),
                config.getBoolean("cubewar.area.safezone.use.fire"),
                config.getBoolean("cubewar.area.safezone.use.lava"),
                config.getBoolean("cubewar.area.safezone.use.water"),
                config.getBoolean("cubewar.area.safezone.power.powerloss"),
                config.getBoolean("cubewar.area.safezone.power.powergain"),
                config.getStringList("cubewar.area.safezone.denycommands"),
                Util.convertListStringToMaterial(config.getStringList("cubewar.area.safezone.protect")),
                "SAFE", "SafeZone No PvP", false, null, 0
            );
    }
}

package Area;

import de.cubeisland.CubeWar.Util;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author Faithcaio
 */
public class Area_Wildland extends Area 
{

    public Area_Wildland(Configuration config) 
    {
        //WILDLAND ID = 0
        super(  0, AreaType.WILDLAND,"WildLand",
                config.getBoolean("cubewar.area.wildland.pvp.PvP"),
                config.getBoolean("cubewar.area.wildland.pvp.damage"),
                config.getBoolean("cubewar.area.wildland.pvp.friendlyfire"),
                config.getInt    ("cubewar.area.wildland.pvp.spawnprotectseconds"),
                config.getBoolean("cubewar.area.wildland.monster.spawn"),
                config.getBoolean("cubewar.area.wildland.monster.damage"),
                config.getBoolean("cubewar.area.wildland.build.place"),
                config.getBoolean("cubewar.area.wildland.build.destroy"),
                config.getBoolean("cubewar.area.wildland.use.fire"),
                config.getBoolean("cubewar.area.wildland.use.lava"),
                config.getBoolean("cubewar.area.wildland.use.water"),
                config.getBoolean("cubewar.area.wildland.power.powerloss"),
                config.getBoolean("cubewar.area.wildland.power.powergain"),
                config.getStringList("cubewar.area.wildland.denycommands"),
                Util.convertListStringToMaterial(config.getStringList("cubewar.area.wildland.protect")),
                "WILD", "WildLands", false, null, 0
            );
    }
}

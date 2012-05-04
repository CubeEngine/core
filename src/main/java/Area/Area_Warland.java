package Area;

import de.cubeisland.CubeWar.Util;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author Faithcaio
 */
public class Area_Warland extends Area {

    public Area_Warland(Configuration config) 
    {
        //WARLAND ID = -1
        super(  -1, AreaType.WARLAND,"WarLand",
                config.getBoolean("cubewar.area.warland.pvp.PvP"),
                config.getBoolean("cubewar.area.warland.pvp.damage"),
                config.getBoolean("cubewar.area.warland.pvp.friendlyfire"),
                config.getInt    ("cubewar.area.warland.pvp.spawnprotectseconds"),
                config.getBoolean("cubewar.area.warland.monster.spawn"),
                config.getBoolean("cubewar.area.warland.monster.damage"),
                config.getBoolean("cubewar.area.warland.build.place"),
                config.getBoolean("cubewar.area.warland.build.destroy"),
                config.getBoolean("cubewar.area.warland.use.fire"),
                config.getBoolean("cubewar.area.warland.use.lava"),
                config.getBoolean("cubewar.area.warland.use.water"),
                config.getBoolean("cubewar.area.warland.power.powerloss"),
                config.getBoolean("cubewar.area.warland.power.powergain"),
                config.getStringList("cubewar.area.warland.denycommands"),
                Util.convertListStringToMaterial(config.getStringList("cubewar.area.warland.protect")),
                "WAR", "WarZone PvP ON", false, null, 0
            );
    }
}

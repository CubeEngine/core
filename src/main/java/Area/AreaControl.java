package Area;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author Faithcaio
 */
public class AreaControl {

    public final Area_Safezone safe;
    public final Area_Warland war;
    public final Area_Wildland wild;
    public final Area_Team team;
    public final Area_Arena arena;
    List<Area> areas = new ArrayList<Area>();
    private static AreaControl instance = null;
    
    public AreaControl(Configuration config) 
    {
        this.wild = new Area_Wildland(config);
        this.arena = new Area_Arena(config);
        this.safe = new Area_Safezone(config);
        this.war = new Area_Warland(config);
        this.team = new Area_Team(config);
    }
    
    public static void createIntstance(Configuration config)
    {
        instance = new AreaControl(config);
    }
    
    public static AreaControl getInstance()
    {
        return instance;
    }
    
    public void addTeam(String tag, String name)
    {
        Area_Team newTeam = (Area_Team)this.team.clone();
        newTeam
    }
}

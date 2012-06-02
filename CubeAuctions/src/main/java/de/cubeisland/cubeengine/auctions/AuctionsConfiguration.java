package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.core.persistence.filesystem.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.Option;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Faithcaio
 */
public class AuctionsConfiguration extends Configuration
{
    @Option("debug")
    public boolean debugMode = false;
    @Option("auction.default.length")
    public String default_length = "30m";
    @Option("auction.comission")
    public int comission = 3;//in percent
    @Option("auction.punish")
    public double punish = 0.3;
    @Option("auction.undo-time")
    public int undotime = 30; //in sec
    @Option("auction.item-box-length")
    public String itemBoxLength = "2d";
    //not used yet...
    @Option("auction.max.overall")
    public Integer maxAuctions_overall = 100;
    @Option("auction.max.player")
    public Integer maxAuctions_player = 5;
    @Option("auction.max.length")
    public String maxLength = "3d";
    public List<String> blacklist_string = new ArrayList<String>()
    {
        
        {
            add("7");
            add("8");
            add("9");
            add("10");
            add("11");
            add("26");
            add("34");
            add("36");
            add("51");
            add("52");
            add("55");
            add("59");
            add("60");
            add("63");
            add("64");
            add("68");
            add("71");
            add("74");
            add("75");
            add("83");
            add("90");
            add("92");
            add("93");
            add("94");
            add("95");
            add("104");
            add("105");
            add("115");
            add("117");
            add("118");
            add("119");
            add("120");
        }
    };
    @Option("auction.remove-time")
    public int removetime = 30; //in sec
    //convert later
    public List<Material> blacklist;

    public void convert()
    {
        blacklist = new ArrayList<Material>();
        for (String s : blacklist_string)
        {
            blacklist.add(Material.matchMaterial(s));
        }

    }
}

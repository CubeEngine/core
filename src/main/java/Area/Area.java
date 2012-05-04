package Area;

import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Faithcaio
 */
public class Area {

    private int id; //FOR DATABASE
    //For all Areas
    private AreaType type;
    private String name;
    private boolean pvp_on;
    private boolean pvp_damage;
    private boolean pvp_friendlyfire;
    private int pvp_spawnprotect;//in seconds
    private boolean monster_spawn;
    private boolean monster_damage;
    private boolean build_place;
    private boolean build_destroy;
    private boolean use_fire;
    private boolean use_lava;
    private boolean use_water;
    private boolean power_loss;
    private boolean power_gain;
    private List<String> denycommands;
    private List<Material> protect;
    //only Team / Arena
    private String tag;
    private String description;
    private boolean economy_bank;
    private Integer power_perm = null;
    private int power_boost;
    
//TODO power etc.
    
    
    
        
    
    public Area(int id, AreaType type, String name,
                boolean pvp_on, boolean pvp_damage, boolean pvp_friendlyfire, int pvp_spawnprotect,
                boolean monster_spawn, boolean monster_damage,
                boolean build_place, boolean build_destroy,
                boolean use_fire, boolean use_lava, boolean use_water,
                boolean power_loss, boolean power_gain,
                List<String> denycommands, List<Material> protect,
                String tag, String description,
                boolean economy_bank,
                Integer power_perm, int power_boost
                ) 
    {
        this.type = type;
        this.name = name;
        this.pvp_on = pvp_on;
        this.pvp_damage = pvp_damage;
        this.pvp_friendlyfire = pvp_friendlyfire;
        this.pvp_spawnprotect = pvp_spawnprotect;
        this.monster_spawn = monster_spawn;
        this.monster_damage = monster_damage;
        this.build_place = build_place;
        this.build_destroy = build_destroy;
        this.use_fire = use_fire;
        this.use_lava = use_lava;
        this.use_water = use_water;
        this.power_loss = power_loss;
        this.power_gain = power_gain;
        this.denycommands = denycommands;
        this.protect = protect;
        this.tag = tag;
        this.description = description;
        this.economy_bank = economy_bank;
        this.power_perm = power_perm;
        this.power_boost = power_boost;
        
        this.id = id;
    }
}

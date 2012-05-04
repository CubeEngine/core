package Area;

import de.cubeisland.libMinecraft.bitmask.BitMask;
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

    private BitMask bits;
    public static final int PVP_ON = 1;
    public static final int PVP_DAMAGE = 2;
    public static final int PVP_FRIENDLYFIRE = 4;
    public static final int MONSTER_SPAWN = 8;
    public static final int MONSTER_DAMAGE = 16;
    public static final int BUILD_PLACE = 32;
    public static final int BUILD_DESTROY = 64;
    public static final int USE_FIRE = 128;
    public static final int USE_LAVA = 256;
    public static final int USE_WATER = 512;
    public static final int POWER_LOSS = 1024;
    public static final int POWER_GAIN = 2048;
    public static final int ECONOMY_BANK = 4096;
    
    private int pvp_spawnprotect;//in seconds
    private List<String> denycommands;
    private List<Material> protect;
    //only Team / Arena
    private String tag;
    private String description;
    private Integer power_perm = null;
    private int power_boost;
    
    //Variablen
    private Integer power_max;
    private Integer power_max_used;
    private Integer power_used;

    public Area() 
    {
        this.bits = new BitMask();
        
    }
    
    public void setBit(int Bit)
    {
        this.getBits().set(Bit);
    }
    
    public void unsetBit(int Bit)
    {
        this.getBits().unset(Bit);
    }

    public void toggleBit(int Bit)
    {
        this.getBits().toggle(Bit);
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * @return the type
     */
    public AreaType getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AreaType type)
    {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the bits
     */
    public BitMask getBits()
    {
        return bits;
    }

    /**
     * @param bits the bits to set
     */
    public void setBits(BitMask bits)
    {
        this.bits = bits;
    }

    /**
     * @return the pvp_spawnprotect
     */
    public int getPvp_spawnprotect()
    {
        return pvp_spawnprotect;
    }

    /**
     * @param pvp_spawnprotect the pvp_spawnprotect to set
     */
    public void setPvp_spawnprotect(int pvp_spawnprotect)
    {
        this.pvp_spawnprotect = pvp_spawnprotect;
    }

    /**
     * @return the denycommands
     */
    public List<String> getDenycommands()
    {
        return denycommands;
    }

    /**
     * @param denycommands the denycommands to set
     */
    public void setDenycommands(List<String> denycommands)
    {
        this.denycommands = denycommands;
    }

    /**
     * @return the protect
     */
    public List<Material> getProtect()
    {
        return protect;
    }

    /**
     * @param protect the protect to set
     */
    public void setProtect(List<Material> protect)
    {
        this.protect = protect;
    }

    /**
     * @return the tag
     */
    public String getTag()
    {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the power_perm
     */
    public Integer getPower_perm()
    {
        return power_perm;
    }

    /**
     * @param power_perm the power_perm to set
     */
    public void setPower_perm(Integer power_perm)
    {
        this.power_perm = power_perm;
    }

    /**
     * @return the power_boost
     */
    public int getPower_boost()
    {
        return power_boost;
    }

    /**
     * @param power_boost the power_boost to set
     */
    public void setPower_boost(int power_boost)
    {
        this.power_boost = power_boost;
    }

    /**
     * @return the power_max
     */
    public Integer getPower_max()
    {
        return power_max;
    }

    /**
     * @param power_max the power_max to set
     */
    public void setPower_max(Integer power_max)
    {
        this.power_max = power_max;
    }

    /**
     * @return the power_max_used
     */
    public Integer getPower_max_used()
    {
        return power_max_used;
    }

    /**
     * @param power_max_used the power_max_used to set
     */
    public void setPower_max_used(Integer power_max_used)
    {
        this.power_max_used = power_max_used;
    }

    /**
     * @return the power_used
     */
    public Integer getPower_used()
    {
        return power_used;
    }

    /**
     * @param power_used the power_used to set
     */
    public void setPower_used(Integer power_used)
    {
        this.power_used = power_used;
    }
    
    
}
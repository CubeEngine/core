package Area;

import de.cubeisland.libMinecraft.bitmask.BitMask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

/**
 *
 * @author Faithcaio
 */
public class Area implements Cloneable{

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
    
    private BitMask bits;
    private AreaType type;
    
    private int pvp_spawnprotect;//in seconds
    
    private Map<String,Object> areavalues = new HashMap<String,Object>();

    public Area() 
    {
        this.bits = new BitMask();
        areavalues.put("bits", this.bits);
    }
    
    public void setBit(int Bit)
    {
        this.bits.set(Bit);
    }
    
    public void unsetBit(int Bit)
    {
        this.bits.unset(Bit);
    }

    public void toggleBit(int Bit)
    {
        this.bits.toggle(Bit);
    }
    
    public boolean setValue(String key, String value)
    {
        if (areavalues.get(key) instanceof Integer)
        {
            try
            {
                Integer intval = Integer.valueOf(value);
                return this.setIntegerValue(key, intval);
            }
            catch (NumberFormatException ex) {return false;}
        }
            
        if (areavalues.get(key) instanceof String)
            return this.setStringValue(key, value);
        if (areavalues.get(key) instanceof List)
            return this.setListValue(key, value);
    
       return this.setOtherValue(key, value);
          
    }
    
    public boolean setStringValue(String key, String value)
    {
        areavalues.put(key, value);
        return true;
    }
    
    public boolean setIntegerValue(String key, Integer value)
    {
        areavalues.put(key, value);
        return true;
    }
    
    public boolean setListValue(String key, String value)
    {//TODO geht nicht :(
        if (key.equalsIgnoreCase("denycommands"))
            areavalues.put(key, value);
        if (key.equalsIgnoreCase("protect"))
        {
            if (Material.matchMaterial(value)!=null)
                areavalues.put(key, Material.matchMaterial(value));
            else
                return false;
        }
        return true;
    }
    
    public boolean setListValue(String key, List value)
    {
        areavalues.put(key, value);
        return true;
    }
    
    public boolean setOtherValue(String key, String value)
    {
        int bitkey = -1;
        if (key.equalsIgnoreCase("PVP_ON")) bitkey = Area.PVP_ON ;
        if (key.equalsIgnoreCase("PVP_DAMAGE")) bitkey = Area.PVP_DAMAGE ;
        if (key.equalsIgnoreCase("PVP_FRIENDLYFIRE")) bitkey = Area.PVP_FRIENDLYFIRE ;
        if (key.equalsIgnoreCase("MONSTER_SPAWN")) bitkey = Area.MONSTER_SPAWN ;
        if (key.equalsIgnoreCase("MONSTER_DAMAGE")) bitkey = Area.MONSTER_DAMAGE ;
        if (key.equalsIgnoreCase("BUILD_PLACE")) bitkey = Area.BUILD_PLACE ;
        if (key.equalsIgnoreCase("BUILD_DESTROY")) bitkey = Area.BUILD_DESTROY ;
        if (key.equalsIgnoreCase("USE_FIRE")) bitkey = Area.USE_FIRE ;
        if (key.equalsIgnoreCase("USE_LAVA")) bitkey = Area.USE_LAVA ;
        if (key.equalsIgnoreCase("USE_WATER")) bitkey = Area.USE_WATER ;
        if (key.equalsIgnoreCase("POWER_LOSS")) bitkey = Area.POWER_LOSS ;
        if (key.equalsIgnoreCase("POWER_GAIN")) bitkey = Area.POWER_GAIN ;
        if (key.equalsIgnoreCase("ECONOMY_BANK")) bitkey = Area.ECONOMY_BANK ;
        if (bitkey < 0)            
            return false;
        else
            return this.setBoolValue(bitkey, value);
    }
     
    public boolean setBoolValue(int bit, String value)
    {
        if (value.equalsIgnoreCase("toggle")||value.equalsIgnoreCase("t"))
            this.bits.toggle(bit);
        else
            if (value.equalsIgnoreCase("true")||value.equalsIgnoreCase("on"))
                this.bits.set(bit);
            else
                if (value.equalsIgnoreCase("false")||value.equalsIgnoreCase("off"))
                    this.bits.unset(bit);
                else
                    return false;
        return true;
        
    }
        
    
    public Object getValue(String key)
    {
        return areavalues.get(key);
    }
    
    @Override
    public Area clone()
    {
        try
        {
            return (Area)super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            return null;
        }
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return (Integer)this.getValue("id");
    }

    /**
     * @return the type
     */
    public AreaType getType()
    {
        return type;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return (String)this.getValue("name");
    }

    /**
     * @return the pvp_spawnprotect
     */
    public int getPvp_spawnprotect()
    {
        return pvp_spawnprotect;
    }

    /**
     * @return the denycommands
     */
    public List<String> getDenycommands()
    {
        return (List<String>)this.getValue("denycommands");
    }

    /**
     * @return the protect
     */
    public List<Material> getProtect()
    {
        return (List<Material>)this.getValue("protect");
    }

    /**
     * @return the tag
     */
    public String getTag()
    {
        return (String)this.getValue("tag");
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return (String)this.getValue("description");
    }

    /**
     * @return the power_perm
     */
    public Integer getPower_perm()
    {
        return (Integer)this.getValue("power_perm");
    }

    /**
     * @return the power_boost
     */
    public int getPower_boost()
    {
        return (Integer)this.getValue("power_boost");
    }

    /**
     * @return the power_max
     */
    public Integer getPower_max()
    {
        return (Integer)this.getValue("power_max");
    }

    /**
     * @return the power_max_used
     */
    public Integer getPower_max_used()
    {
        return (Integer)this.getValue("power_max_used");
    }

    /**
     * @return the power_used
     */
    public Integer getPower_used()
    {
        return (Integer)this.getValue("power_used");
    }

    /**
     * @param type the type to set
     */
    public void setType(AreaType type)
    {
        this.type = type;
    }
}
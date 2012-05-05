package Groups;

import de.cubeisland.CubeWar.Hero;
import de.cubeisland.libMinecraft.bitmask.BitMask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

/**
 *
 * @author Faithcaio
 */
public class Group implements Cloneable{

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
    private Map<String,Integer> intval = new HashMap<String,Integer>();
    private Map<String,String> stringval = new HashMap<String,String>();
    private Map<String,List> listval = new HashMap<String,List>();
    //private Map<String,Object> areavalues = new HashMap<String,Object>();
    
    private int power_used;
    private int power_max;
    private int power_max_used;
    
    private List<Hero> admin = new ArrayList<Hero>();
    private List<Hero> mod = new ArrayList<Hero>();
    private List<Hero> user = new ArrayList<Hero>();
    
    private List<Group> enemy = new ArrayList<Group>();
    private List<Group> ally = new ArrayList<Group>();

    public Group() 
    {
        this.bits = new BitMask();
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
        if (intval.containsKey(key.toLowerCase()))
        {
            try
            {
                Integer intval = Integer.valueOf(value);
                return this.setIntegerValue(key, intval);
            }
            catch (NumberFormatException ex) {return false;}
        }    
        if (stringval.containsKey(key.toLowerCase()))
            return this.setStringValue(key, value);
        if (listval.containsKey(key.toLowerCase()))
            return this.setListValue(key, value);
    
       return this.setOtherValue(key, value);
          
    }
    
    public boolean setStringValue(String key, String value)
    {
        stringval.put(key, value);
        return true;
    }
    
    public boolean setIntegerValue(String key, Integer value)
    {
        intval.put(key, value);
        return true;
    }
    
    public boolean setListValue(String key, String value)
    {
        if (key.equalsIgnoreCase("denycommands"))
        {
            List<String> list = listval.get(key);
            if (value.charAt(0)=='-')
                list.remove(value.substring(1));
            else
                list.add(value);
            listval.put(key, list);
            return true;
        }
        if (key.equalsIgnoreCase("protect"))
        {
            List<Material> list = listval.get(key);
            if (value.charAt(0)=='-')
                list.remove(Material.matchMaterial(value.substring(1)));
            else
                list.add(Material.matchMaterial(value));
            listval.put(key, list);
            return true;
        }
        return false;
    }
    
    public boolean setListValue(String key, List value)
    {
        listval.put(key, value);
        return true;
    }
    
    public boolean setOtherValue(String key, String value)
    {
        int bitkey = -1;
        if (key.equalsIgnoreCase("PVP_ON")) bitkey = Group.PVP_ON ;
        if (key.equalsIgnoreCase("PVP_DAMAGE")) bitkey = Group.PVP_DAMAGE ;
        if (key.equalsIgnoreCase("PVP_FRIENDLYFIRE")) bitkey = Group.PVP_FRIENDLYFIRE ;
        if (key.equalsIgnoreCase("MONSTER_SPAWN")) bitkey = Group.MONSTER_SPAWN ;
        if (key.equalsIgnoreCase("MONSTER_DAMAGE")) bitkey = Group.MONSTER_DAMAGE ;
        if (key.equalsIgnoreCase("BUILD_PLACE")) bitkey = Group.BUILD_PLACE ;
        if (key.equalsIgnoreCase("BUILD_DESTROY")) bitkey = Group.BUILD_DESTROY ;
        if (key.equalsIgnoreCase("USE_FIRE")) bitkey = Group.USE_FIRE ;
        if (key.equalsIgnoreCase("USE_LAVA")) bitkey = Group.USE_LAVA ;
        if (key.equalsIgnoreCase("USE_WATER")) bitkey = Group.USE_WATER ;
        if (key.equalsIgnoreCase("POWER_LOSS")) bitkey = Group.POWER_LOSS ;
        if (key.equalsIgnoreCase("POWER_GAIN")) bitkey = Group.POWER_GAIN ;
        if (key.equalsIgnoreCase("ECONOMY_BANK")) bitkey = Group.ECONOMY_BANK ;
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
        
        if (intval.containsKey(key))
            return intval.get(key);
        if (stringval.containsKey(key))
            return stringval.get(key);
        if (listval.containsKey(key))
            return listval.get(key);
        return null;
    }
    
    @Override
    public Group clone()
    {
        try
        {
            return (Group)super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            return null;
        }
    }
    
    public void addAdmin(Hero hero)
    {
        if (hero == null) return;
        this.admin.add(hero);
        this.mod.remove(hero);
        this.user.remove(hero);
        hero.setTeam(this);
    }
    
    public void delAdmin(Hero hero)
    {
        this.admin.remove(hero);
        hero.setTeam(null);
    }
        
    public boolean isAdmin(Hero hero)
    {
        return this.admin.contains(hero);
    }
    
    public void addMod(Hero hero)
    {
        if (hero == null) return;
        this.mod.add(hero);
        this.admin.remove(hero);
        this.user.remove(hero);
        hero.setTeam(this);
    }
    
    public void delMod(Hero hero)
    {
        this.mod.remove(hero);
        this.admin.remove(hero);
        hero.setTeam(null);
    }
        
    public boolean isMod(Hero hero)
    {
        if (this.admin.contains(hero)) return true;
        return this.mod.contains(hero);
    }
    
    public void addUser(Hero hero)
    {
        if (hero == null) return;
        this.user.add(hero);
        this.mod.remove(hero);
        this.admin.remove(hero);
        hero.setTeam(this);
    }
    
    public void delUser(Hero hero)
    {
        this.user.remove(hero);
        this.mod.remove(hero);
        this.admin.remove(hero);
        hero.setTeam(null);
    }
        
    public boolean isUser(Hero hero)
    {
        if (this.admin.contains(hero)) return true;
        if (this.mod.contains(hero)) return true;
        return this.user.contains(hero);
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
        return (Integer)this.getValue("pvp_spawnprotect");
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
        return this.power_max;
    }

    /**
     * @return the power_max_used
     */
    public Integer getPower_max_used()
    {
        return this.power_max_used;
    }

    /**
     * @return the power_used
     */
    public Integer getPower_used()
    {
        return this.power_used;
    }

    /**
     * @param type the type to set
     */
    public void setType(AreaType type)
    {
        this.type = type;
    }
    
    public void setneutral(Group g)
    {
        this.ally.remove(g);
        this.enemy.remove(g);
    }
    
    public void setally(Group g)
    {
        this.ally.add(g);
        this.enemy.remove(g);
    }
    
    public void setenemy(Group g)
    {
        this.ally.remove(g);
        this.enemy.add(g);
    }
}
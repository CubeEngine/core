package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.war.groups.AreaType;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.user.User;
import de.cubeisland.libMinecraft.bitmask.BitMask;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Faithcaio
 */
public class GroupModel implements Model
{
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
    public static final int IS_CLOSED = 8192;
    public static final int AUTO_CLOSE = 16384;
    public static final int IS_PEACEFUL = 32768;
    private int id;
    private BitMask bits;
    private AreaType type;
    private String tag;
    private String name;
    private String description;
    private Integer influence_perm;
    private int influence_boost;
    private int respawnProtection;
    private Integer dmg_mod_percent;
    private Integer dmg_mod_set;
    private Integer dmg_mod_add;
    private List<Material> protect = new ArrayList<Material>();
    private List<String> denyCmd = new ArrayList<String>();
    private List<String> invited = new ArrayList<String>();
    private List<Group> enemy = new ArrayList<Group>();
    private List<Group> ally = new ArrayList<Group>();
    //No Save in DB / load in later from other source
    private int influence_used;
    private int influence_max;
    private List<User> adminlist = new ArrayList<User>();
    private List<User> modlist = new ArrayList<User>();
    private List<User> userlist = new ArrayList<User>();

    public int getId()
    {
        return this.id;
    }

    public GroupModel(int id)
    {
        this.id = id;
    }
    
    public GroupModel(){}//for deepCopy

    public void setRelations(List<Group> enemy, List<Group> ally)
    {
        this.setEnemy(enemy);
        this.setAlly(ally);
    }

    public void setListVal(List<Material> protect, List<String> denyCmd, List<String> invited)
    {
        this.protect = protect;
        this.denyCmd = denyCmd;
        this.invited = invited;
    }

    public void setStringVal(String tag, String name, String description)
    {
        this.setTag(tag);
        this.setName(name);
        this.setDescription(description);
    }

    public void setIntVal(Integer influence_perm, int influence_boost, int respawnProtection,
            Integer dmg_mod_percent, Integer dmg_mod_set, Integer dmg_mod_add)
    {
        
        this.setInfluence_perm(influence_perm);
        this.setInfluence_boost(influence_boost);
        this.setRespawnProtection(respawnProtection);
        this.setDmg_mod_percent(dmg_mod_percent);
        this.setDmg_mod_set(dmg_mod_set);
        this.setDmg_mod_add(dmg_mod_add);
    }
    
    public void resetBitMask(int bitmask)
    {
        this.bits.reset(bitmask);
    }

    /**
     * @return the type
     */
    public AreaType getType()
    {
        return type;
    }

    /**
     * @return the tag
     */
    public String getTag()
    {
        return tag;
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
     * @return the influence_perm
     */
    public Integer getInfluence_perm()
    {
        return influence_perm;
    }

    /**
     * @param influence_perm the influence_perm to set
     */
    public void setInfluence_perm(Integer influence_perm)
    {
        this.influence_perm = influence_perm;
    }

    /**
     * @return the influence_boost
     */
    public int getInfluence_boost()
    {
        return influence_boost;
    }

    /**
     * @param influence_boost the influence_boost to set
     */
    public void setInfluence_boost(int influence_boost)
    {
        this.influence_boost = influence_boost;
    }

    /**
     * @return the respawnProtection
     */
    public int getRespawnProtection()
    {
        return respawnProtection;
    }

    /**
     * @param respawnProtection the respawnProtection to set
     */
    public void setRespawnProtection(int respawnProtection)
    {
        this.respawnProtection = respawnProtection;
    }

    /**
     * @return the dmg_mod_percent
     */
    public Integer getDmg_mod_percent()
    {
        return dmg_mod_percent;
    }

    /**
     * @param dmg_mod_percent the dmg_mod_percent to set
     */
    public void setDmg_mod_percent(Integer dmg_mod_percent)
    {
        this.dmg_mod_percent = dmg_mod_percent;
    }

    /**
     * @return the dmg_mod_set
     */
    public Integer getDmg_mod_set()
    {
        return dmg_mod_set;
    }

    /**
     * @param dmg_mod_set the dmg_mod_set to set
     */
    public void setDmg_mod_set(Integer dmg_mod_set)
    {
        this.dmg_mod_set = dmg_mod_set;
    }

    /**
     * @return the dmg_mod_add
     */
    public Integer getDmg_mod_add()
    {
        return dmg_mod_add;
    }

    /**
     * @param dmg_mod_add the dmg_mod_add to set
     */
    public void setDmg_mod_add(Integer dmg_mod_add)
    {
        this.dmg_mod_add = dmg_mod_add;
    }

    /**
     * @return the protect
     */
    public List<Material> getProtect()
    {
        return protect;
    }

    /**
     * @return the denyCmd
     */
    public List<String> getDenyCmd()
    {
        return denyCmd;
    }

    /**
     * @return the invited
     */
    public List<String> getInvited()
    {
        return invited;
    }

    /**
     * @return the enemy
     */
    public List<Group> getEnemy()
    {
        return enemy;
    }

    /**
     * @param enemy the enemy to set
     */
    public void setEnemy(List<Group> enemy)
    {
        this.enemy = enemy;
    }

    /**
     * @return the ally
     */
    public List<Group> getAlly()
    {
        return ally;
    }

    /**
     * @param ally the ally to set
     */
    public void setAlly(List<Group> ally)
    {
        this.ally = ally;
    }

    /**
     * @return the influence
     */
    public int getInfluence_used()
    {
        return influence_used;
    }

    /**
     * @param influence the influence to set
     */
    public void setInfluence_used(int influence)
    {
        this.influence_used = influence;
    }

    /**
     * @return the influence_max
     */
    public int getInfluence_max()
    {
        return influence_max;
    }

    /**
     * @param influence_max the influence_max to set
     */
    public void setInfluence_max(int influence_max)
    {
        this.influence_max = influence_max;
    }

    /**
     * @return the adminlist
     */
    public List<User> getAdminlist()
    {
        return adminlist;
    }

    /**
     * @param adminlist the adminlist to set
     */
    public void setAdminlist(List<User> adminlist)
    {
        this.adminlist = adminlist;
    }

    /**
     * @return the modlist
     */
    public List<User> getModlist()
    {
        return modlist;
    }

    /**
     * @param modlist the modlist to set
     */
    public void setModlist(List<User> modlist)
    {
        this.modlist = modlist;
    }

    /**
     * @return the userlist
     */
    public List<User> getUserlist()
    {
        return userlist;
    }

    /**
     * @param userlist the userlist to set
     */
    public void setUserlist(List<User> userlist)
    {
        this.userlist = userlist;
    }

    public void setType(AreaType type)
    {
        this.type = type;
    }
    
    public int getBitMaskValue()
    {
        return this.bits.get();
    }
    
    /**
     * Use only for Default Groups
     * 
     * @param id 
     */
    public void setId(int id)
    {
        this.id = id;
    }

    public void setBit(int bit)
    {
        this.bits.set(bit);
    }
    
    public void unsetBit(int bit)
    {
        this.bits.unset(bit);
    }
    
    public void toggleBit(int bit)
    {
        this.bits.toggle(bit);
    }
    
    public boolean hasBit(int bit)
    {
        return this.bits.isset(bit);
    }
    
    public GroupModel deepCopy()
    {
        GroupModel newModel = new GroupModel();//ID is NOT set
        newModel.setStringVal(tag, name, description);
        newModel.setIntVal(influence_perm, influence_boost, respawnProtection, dmg_mod_percent, dmg_mod_set, dmg_mod_add);
        newModel.resetBitMask(this.getBitMaskValue());
        newModel.setListVal(protect, denyCmd, invited);
        newModel.setType(type);
        return newModel;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    
    public void addInfluence_used(int amount)
    {
        this.influence_used += amount;
    }
    
    
}

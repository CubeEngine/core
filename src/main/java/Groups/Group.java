package Groups;

import Hero.Hero;
import Hero.Heroes;
import de.cubeisland.CubeWar.CubeWar;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.libMinecraft.bitmask.BitMask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                Integer intvalue = Integer.valueOf(value);
                return this.setIntegerValue(key, intvalue);
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
        Group group = new Group();
        group.bits.reset();
        group.setType(AreaType.TEAMZONE);
        group.setIntegerValue("id", this.getId());
        group.setStringValue("name", this.getName());
        group.setStringValue("tag", this.getTag());
        group.setStringValue("description", this.getDescription());
        if (this.bits.isset(Group.ECONOMY_BANK)) group.setBit(Group.ECONOMY_BANK);
        group.setIntegerValue("power_perm", this.getPower_perm());
        group.setIntegerValue("power_boost", this.getPower_boost());
        if (this.bits.isset(Group.POWER_LOSS)) group.setBit(Group.POWER_LOSS);
        if (this.bits.isset(Group.POWER_GAIN)) group.setBit(Group.POWER_GAIN);
        if (this.bits.isset(Group.PVP_ON)) group.setBit(Group.PVP_ON);
        if (this.bits.isset(Group.PVP_DAMAGE)) group.setBit(Group.PVP_DAMAGE);
        if (this.bits.isset(Group.PVP_FRIENDLYFIRE)) group.setBit(Group.PVP_FRIENDLYFIRE);
        group.setIntegerValue("pvp_spawnprotect", this.getPvp_spawnprotect());
        if (this.bits.isset(Group.MONSTER_SPAWN)) group.setBit(Group.MONSTER_SPAWN);
        if (this.bits.isset(Group.MONSTER_DAMAGE)) group.setBit(Group.MONSTER_DAMAGE);
        if (this.bits.isset(Group.BUILD_DESTROY)) group.setBit(Group.BUILD_DESTROY);
        if (this.bits.isset(Group.BUILD_PLACE)) group.setBit(Group.BUILD_PLACE);
        group.setListValue("protect", this.getProtect());
        if (this.bits.isset(Group.USE_FIRE)) group.setBit(Group.USE_FIRE);
        if (this.bits.isset(Group.USE_LAVA)) group.setBit(Group.USE_LAVA);
        if (this.bits.isset(Group.USE_WATER)) group.setBit(Group.USE_WATER);
        group.setListValue("denycommands", this.getDenycommands());
        
        return group;
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
    
    public boolean isAlly(Group g)
    {
        return ally.contains(g);
    }
    
    public boolean isenemy(Group g)
    {
        return enemy.contains(g);
    }
    
    public boolean isneutral(Group g)
    {
        return (!(enemy.contains(g)||ally.contains(g)));
    }
    
    public void sendToTeam(String msg)
    {
        Player[] players = CubeWar.getInstance().getServer().getOnlinePlayers();
        for (Player player : players)
        {
            if (this.isUser(Heroes.getOfflineHero(player)))
                player.sendMessage(msg);
        }
    }
    
    public void sendToAlly(String msg)
    {
        this.sendToTeam(msg);
        for (Group theally : this.ally)
        {
            if (theally.isAlly(this))
                theally.sendToTeam(msg);
        }
    }
    
    public void sendInfo(CommandSender sender)
    {
        sender.sendMessage(t("g_01",this.getTag()));
        sender.sendMessage(t("g_02",this.getName()));
        sender.sendMessage(t("g_03",this.getDescription()));
        sender.sendMessage(t("g_04",GroupControl.get().getRank(this),
                           t("g_05",this.power_used,this.power_max_used,this.power_max)));
        Group team = Heroes.getHero(sender).getTeam();
        if ((team.equals(this))||(team.isAlly(this) && this.isAlly(team)))
            sender.sendMessage(t("g_06"));
        else
            sender.sendMessage(t("g_07"));
        String pvp;
        if (this.bits.isset(PVP_ON))
        {
            if (this.bits.isset(PVP_FRIENDLYFIRE))
                pvp = t("g_081");
            else
                pvp = t("g_083");
        }
        else
        {
            pvp = t("g_082");
        }
        sender.sendMessage(pvp);
        String allies = "";
        for (Group group : this.ally)
        {
            allies += ", "+group.getTag();
        }
        if (!allies.isEmpty())
            sender.sendMessage(t("g_09",allies.substring(2)));
        String enemies = "";
        for (Group group : this.enemy)
        {
            enemies += ", "+group.getTag();
        }
        if (!enemies.isEmpty())
            sender.sendMessage(t("g_10",enemies.substring(2)));
        if (this.bits.isset(ECONOMY_BANK))
            sender.sendMessage(t("g_11","NO MONEY HAHA"));//TODO Vault dependency blah blubb
        List<Hero> list = new ArrayList<Hero>();
        list.addAll(0,this.admin);
        list.addAll(0,this.mod);
        list.addAll(0,this.user);
        String onplayer = "";
        List<Hero> onlist = new ArrayList<Hero>();
        for (Hero player : list)
        {
            if (player.getPlayer()!=null)
            {
                onplayer += ", "+player.getName();
                onlist.add(player);
            }
        }
        if (!onplayer.isEmpty())
            sender.sendMessage(t("g_12",onplayer.substring(2)));
        list.removeAll(onlist);
        String offplayer = "";
        for (Hero player : list)
        {
            if (player.getPlayer()!=null)
            {
                offplayer += ", "+player.getName();
            }
        }
        if (!offplayer.isEmpty())
            sender.sendMessage(t("g_13",offplayer.substring(2)));
        //TODO
        sender.sendMessage(t("g_-14"));
            
        
        
    }
}
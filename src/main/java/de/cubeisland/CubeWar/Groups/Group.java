package de.cubeisland.CubeWar.Groups;

import de.cubeisland.CubeWar.CubeWar;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.User.User;
import de.cubeisland.CubeWar.User.Users;
import de.cubeisland.libMinecraft.bitmask.BitMask;
import java.util.ArrayList;
import java.util.EnumMap;
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
    //TODO Bank
    private BitMask bits;
    private AreaType type;
    private Map<String,Integer> intval = new HashMap<String,Integer>();
    private Map<String,String> stringval = new HashMap<String,String>();
    private Map<String,List> listval = new HashMap<String,List>();
    
    private boolean closed;//TODO in Bitmask einfügen
    
    private int power_used;
    private int power_max;
    private int power_max_used;
    
    private List<User> admin = new ArrayList<User>();
    private List<User> mod = new ArrayList<User>();
    private List<User> user = new ArrayList<User>();
    private List<User> invited = new ArrayList<User>();
    
    private List<Group> enemy = new ArrayList<Group>();
    private List<Group> ally = new ArrayList<Group>();

    public Group() 
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
    
    private String convertKey(String key)
    {
        if (key.equalsIgnoreCase("dmgmod")) return "damagemodifier";
        else return key;
    }
    
    
    public boolean setValue(String key, String value)
    {
        
        key = this.convertKey(key);
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
        if (bitkey > 0)            
            return this.setBoolValue(bitkey, value);
        else
        {
            if ((key.equalsIgnoreCase("damagemodifier")))
            {
                this.setIntegerValue("damagemodifier_percent", null);
                this.setIntegerValue("damagemodifier_set", null);
                this.setIntegerValue("damagemodifier_add", null);
                if (value.charAt(0)=='%')
                {
                    this.setIntegerValue("damagemodifier_percent", Integer.valueOf(value.substring(1)));
                }else
                if (value.charAt(0)=='#')
                {
                    this.setIntegerValue("damagemodifier_set", Integer.valueOf(value.substring(1)));
                }
                else
                {
                   this.setIntegerValue("damagemodifier_add", Integer.valueOf(value.substring(1))); 
                }
                return true;
            }
            return false;
        }
    }
     
    public boolean setBoolValue(int bit, String value)
    {
        if (value.equalsIgnoreCase("toggle")||value.equalsIgnoreCase("t"))
            this.getBits().toggle(bit);
        else
            if (value.equalsIgnoreCase("true")||value.equalsIgnoreCase("on"))
                this.getBits().set(bit);
            else
                if (value.equalsIgnoreCase("false")||value.equalsIgnoreCase("off"))
                    this.getBits().unset(bit);
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
        group.getBits().reset();
        group.setType(AreaType.TEAMZONE);
        group.setIntegerValue("id", this.getId());
        group.setStringValue("name", this.getName());
        group.setStringValue("tag", this.getTag());
        group.setStringValue("description", this.getDescription());
        if (this.getBits().isset(Group.ECONOMY_BANK)) group.setBit(Group.ECONOMY_BANK);
        group.setIntegerValue("power_perm", this.getPower_perm());
        group.setIntegerValue("power_boost", this.getPower_boost());
        if (this.getBits().isset(Group.POWER_LOSS)) group.setBit(Group.POWER_LOSS);
        if (this.getBits().isset(Group.POWER_GAIN)) group.setBit(Group.POWER_GAIN);
        if (this.getBits().isset(Group.PVP_ON)) group.setBit(Group.PVP_ON);
        if (this.getBits().isset(Group.PVP_DAMAGE)) group.setBit(Group.PVP_DAMAGE);
        if (this.getBits().isset(Group.PVP_FRIENDLYFIRE)) group.setBit(Group.PVP_FRIENDLYFIRE);
        group.setIntegerValue("pvp_spawnprotect", this.getPvp_spawnprotect());
        group.setIntegerValue("damagemodifier_percent", this.getDamagemodifier_percent());
        group.setIntegerValue("damagemodifier_set", this.getDamagemodifier_set());
        group.setIntegerValue("damagemodifier_add", this.getDamagemodifier_add());
        if (this.getBits().isset(Group.MONSTER_SPAWN)) group.setBit(Group.MONSTER_SPAWN);
        if (this.getBits().isset(Group.MONSTER_DAMAGE)) group.setBit(Group.MONSTER_DAMAGE);
        if (this.getBits().isset(Group.BUILD_DESTROY)) group.setBit(Group.BUILD_DESTROY);
        if (this.getBits().isset(Group.BUILD_PLACE)) group.setBit(Group.BUILD_PLACE);
        group.setListValue("protect", this.getProtect());
        if (this.getBits().isset(Group.USE_FIRE)) group.setBit(Group.USE_FIRE);
        if (this.getBits().isset(Group.USE_LAVA)) group.setBit(Group.USE_LAVA);
        if (this.getBits().isset(Group.USE_WATER)) group.setBit(Group.USE_WATER);
        group.setListValue("denycommands", this.getDenycommands());
        
        group.setClosed(this.isClosed()); //TODO in Bitmask einfügen
        
        return group;
    }
    
    public void addAdmin(User hero)
    {
        if (hero == null) return;
        this.admin.add(hero);
        this.mod.remove(hero);
        this.user.remove(hero);
        hero.setTeam(this);
    }
    
    public void delAdmin(User hero)
    {
        this.admin.remove(hero);
        hero.setTeam(null);
    }
        
    public boolean isAdmin(User hero)
    {
        return this.admin.contains(hero);
    }
    
    public void addMod(User hero)
    {
        if (hero == null) return;
        this.mod.add(hero);
        this.admin.remove(hero);
        this.user.remove(hero);
        hero.setTeam(this);
    }
    
    public void delMod(User hero)
    {
        this.mod.remove(hero);
        this.admin.remove(hero);
        hero.setTeam(null);
    }
        
    public boolean isMod(User hero)
    {
        if (this.admin.contains(hero)) return true;
        return this.mod.contains(hero);
    }
    
    public void addUser(User hero)
    {
        if (hero == null) return;
        this.user.add(hero);
        this.mod.remove(hero);
        this.admin.remove(hero);
        hero.setTeam(this);
    }
    
    public void delUser(User hero)
    {
        this.user.remove(hero);
        this.mod.remove(hero);
        this.admin.remove(hero);
        hero.setTeam(null);
    }
        
    public boolean isUser(User hero)
    {
        if (this.admin.contains(hero)) return true;
        if (this.mod.contains(hero)) return true;
        return this.user.contains(hero);
    }
    
    public boolean invite(User user)
    {
        if (this.invited.contains(user))
            return false;
        this.invited.add(user);
        return true;
    }
    
    public boolean isInvited(User user)
    {
        if (this.closed)
            if (this.invited.contains(user))
                return true;
            else
                return false;
        return true;
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
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed the closed to set
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public static enum DmgModType 
    {
        PERCENT(null),
        SET(null),
        ADD(null);

      
        public Integer val;
        
        private DmgModType(Integer val)
        {
            this.val = val;
        }
    }
        
    private Integer getDamagemodifier_percent()
    {
        return (Integer)this.getValue("damagemodifier_percent");
    }

    private Integer getDamagemodifier_set()
    {
        return (Integer)this.getValue("damagemodifier_set");
    }

    private Integer getDamagemodifier_add()
    {
       return (Integer)this.getValue("damagemodifier_add");
    }

    public Map<DmgModType,Integer> getDamagemodifier()
    {
        Map<DmgModType,Integer> tmp = new EnumMap<DmgModType,Integer>(DmgModType.class);
        tmp.put(DmgModType.SET, (Integer)this.getValue("damagemodifier_set"));
        tmp.put(DmgModType.PERCENT, (Integer)this.getValue("damagemodifier_percent"));
        tmp.put(DmgModType.ADD, (Integer)this.getValue("damagemodifier_add"));
        return tmp;
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
    
    public boolean isTrueAlly(Group g)
    {
        if (this.ally.contains(g))
            return g.isAlly(this);
        else 
            return false;
    }
    
    public boolean isEnemy(Group g)
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
            if (this.isUser(Users.getOfflineUser(player)))
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
        Group team = Users.getUser(sender).getTeam();
        if ((team.equals(this))||(team.isAlly(this) && this.isAlly(team)))
            sender.sendMessage(t("g_06"));
        else
            sender.sendMessage(t("g_07"));
        String pvp;
        if (this.getBits().isset(PVP_ON))
        {
            if (this.getBits().isset(PVP_FRIENDLYFIRE))
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
        String enemies = "";
        for (Group group : GroupControl.getAreas())
        {
            if (this.isTrueAlly(group))
                allies += ", "+group.getTag();
            else
            if ((group.isEnemy(this))||this.isEnemy(group))
                enemies += ", "+group.getTag();
        }
        if (!allies.isEmpty())
            sender.sendMessage(t("g_09",allies.substring(2)));
        if (!enemies.isEmpty())
            sender.sendMessage(t("g_10",enemies.substring(2)));
        if (this.getBits().isset(ECONOMY_BANK))
            sender.sendMessage(t("g_11","NO MONEY HAHA"));//TODO Vault dependency blah blubb
        List<User> list = this.getUserList();
        sender.sendMessage(t("g_15",list.size(),this.getKPSum()));
        String onplayer = "";
        List<User> onlist = new ArrayList<User>();
        for (User player : list)
        {
            if (player.getPlayer()!=null)
            {
                onplayer += ", "+player.getName();
                onlist.add(player);
            }
        }
        if (!onplayer.isEmpty())
            sender.sendMessage(t("g_12",onlist.size(),onplayer.substring(2)));
        list.removeAll(onlist);
        String offplayer = "";
        for (User player : list)
        {
            if (player.getPlayer()!=null)
            {
                offplayer += ", "+player.getName();
            }
        }
        if (!offplayer.isEmpty())
            sender.sendMessage(t("g_13",list.size(),offplayer.substring(2)));
        //TODO closed Teams
        sender.sendMessage(t("g_-14"));
    }
    
    public List<User> getUserList()
    {
        List<User> list = new ArrayList<User>();
        list.addAll(0,this.admin);
        list.addAll(0,this.mod);
        list.addAll(0,this.user);
        return list;
    }
    
    public int getKPSum()
    {
        int kp = 0;
        for (User tmp : this.getUserList())
        {
            kp += tmp.getKp();
        }
        return kp;
    }
    
    
    /**
     * @return the bits
     */
    public BitMask getBits()
    {
        return bits;
    }
}
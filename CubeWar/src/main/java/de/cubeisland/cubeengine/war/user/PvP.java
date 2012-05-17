package de.cubeisland.cubeengine.war.user;

import de.cubeisland.cubeengine.core.user.CubeUser;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.CubeWarConfiguration;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class PvP{

    private CubeUserManager cuManager = CubeUserManager.getInstance();
    private CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    private GroupControl groups = GroupControl.get();
    
    public PvP() 
    {
    
    }
    
    public boolean isFriendlyFireOn(Player damager, Player damagee)
    {
        if (isAlly(damager, damagee))
        {
            if (isAreaDenyingFF(damager, damagee)) return false;
            CubeWar.debug("FF-ON!");
            return true;
        }
        else
        {
            CubeWar.debug("isNoAlly");
            return true;
        }
    }
    
    public boolean isDamageOn(Player damager, Player damagee)
    {
        if (isAreaDenyingDamage(damager, damagee)) return false;
        if (isPlayerRespawning(damager, damagee)) return false;
        CubeWar.debug("Damage-ON!");
        return true;
    }
    
    public boolean isPvPallowed(Player damager, Player damagee)
    {
        if (isAreaPvPOff(damager, damagee)) return false;
        if (isUserPeaceFull(damager)||isUserPeaceFull(damagee)) return false;
        CubeWar.debug("PVP-ON!");
        return true;
    }
    
    private boolean isUserPeaceFull(Player player)
    {
        return Users.getUser(player).getMode().equals(PlayerMode.PEACE);
    }
    
    private boolean isAreaPvPOff(Player damager, Player damagee)
    {
        if (groups.getGroup(damager).getBits().isset(Group.PVP_ON)) return false;
        if (groups.getGroup(damagee).getBits().isset(Group.PVP_ON)) return false;
        return true;
    }
    
    private boolean isPlayerRespawning(Player damager, Player damagee)
    {
        if (Users.getUser(damager).isRespawning()) return true;
        if (Users.getUser(damagee).isRespawning()) return true;
        return false;
    }
    
    private boolean isAreaDenyingFF(Player damager, Player damagee)
    {
        if (groups.getGroup(damager).getBits().isset(Group.PVP_FRIENDLYFIRE)) return false;
        if (groups.getGroup(damagee).getBits().isset(Group.PVP_FRIENDLYFIRE)) return false;
        return true;
    }
    
    private boolean isAreaDenyingDamage(Player damager, Player damagee)
    {
        if (groups.getGroup(damager).getBits().isset(Group.PVP_DAMAGE)) return false;
        if (groups.getGroup(damagee).getBits().isset(Group.PVP_DAMAGE)) return false;
        return true;
    }
    
    public int modifyDamage(Player damager, Player damagee, int damage)
    {
        int dmg = damage;
        Map<Group.DmgModType,Integer> modifiers = groups.getGroup(damagee).getDamagemodifier();
        
        Integer tmp;
        for (Group.DmgModType type : Group.DmgModType.values())
        {
            tmp = modifiers.get(type);
            if (tmp != null)
            {
                switch (type)
                {
                    case ADD:
                    {
                        dmg += tmp;
                        break;
                    }
                    case PERCENT:
                    {
                        dmg *= 1 + (tmp / 100);
                        break;
                    } 
                    case SET:
                    {
                        dmg = tmp;
                        break;
                    }
                }
                break;
            }
        }
        CubeWar.debug("Damage:" + damage + " --> "+ dmg);
        return dmg;
    }
    
    private boolean isAlly(Player damager, Player damagee)
    {
        return Users.isAllied(damager, damagee);
    }
    
    
    public void stopFlyArrow(Player player)
    {
        
        player.sendMessage(t("event_arrow"));
        stopFlyAndFall(player);
    }
    
    public void stopFlyAndFall(Player player)
    {
        CubeWar.debug("Fly Stop + Fall");
        stopFly(player);
        player.setAllowFlight(false);
    }
    
    public void stopFly(final Player player)
    {
        CubeWar.debug("Fall");
        player.setFlying(false);  
        if (config.fly_block > 0)
        {
            cuManager.getCubeUser((OfflinePlayer)player).setFlag(CubeUser.BLOCK_FLY);
            CubeWar plugin = CubeWar.getInstance(); 
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
                    new Runnable() {
                        public void run()
                        {
                            cuManager.getCubeUser((OfflinePlayer)player).unsetFlag(CubeUser.BLOCK_FLY);
                        }} , config.fly_block*20);
        }
    }
    
    public void loot(final Player killer,final Player killed, List<ItemStack> drops, final Location deathloc)
    {
        final Inventory loot = Bukkit.createInventory(killed, 6*9, killed.getName());
        for (ItemStack item : drops)
            loot.addItem(item);
        killer.openInventory(loot);
        CubeWar plugin = CubeWar.getInstance();           
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
                new Runnable() {
                    public void run()
                    {
                        killer.closeInventory();
                        if ((killed.isOnline()) && (!killed.isDead()))
                        {
                            killed.getInventory().addItem(loot.getContents());
                            killed.sendMessage(t("loot_back"));
                        }
                        else
                        {
                            for (ItemStack item : loot.getContents())
                                if (item != null)
                                    killer.getWorld().dropItemNaturally(deathloc, item);
                        }
                    }} , 7*20);//TODO LootTime (7sec) Veränderbar je nach KP
        
    }
}

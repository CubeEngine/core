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

    private static CubeUserManager cuManager = CubeUserManager.getInstance();
    private static CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    
    public PvP() 
    {
    
    }
    
    public static boolean isFriendlyFireOn(Player damager, Player damagee)
    {
        if (PvP.isAlly(damager, damagee))
        {
            if (PvP.isAreaDenyingFF(damager, damagee)) return false;
            CubeWar.debug("FF-ON!");
            return true;
        }
        else
        {
            CubeWar.debug("isNoAlly");
            return true;
        }
    }
    
    public static boolean isDamageOn(Player damager, Player damagee)
    {
        if (PvP.isAreaDenyingDamage(damager, damagee)) return false;
        if (PvP.isPlayerRespawning(damager, damagee)) return false;
        CubeWar.debug("Damage-ON!");
        return true;
    }
    
    public static boolean isPvPallowed(Player damager, Player damagee)
    {
        if (PvP.isAreaPvPOff(damager, damagee)) return false;
        if (PvP.isUserPeaceFull(damager)||PvP.isUserPeaceFull(damagee)) return false;
        CubeWar.debug("PVP-ON!");
        return true;
    }
    
    private static boolean isUserPeaceFull(Player player)
    {
        return Users.getUser(player).getMode().equals(PlayerMode.PEACE);
    }
    
    private static boolean isAreaPvPOff(Player damager, Player damagee)
    {
        if (GroupControl.getArea(damager).getBits().isset(Group.PVP_ON)) return false;
        if (GroupControl.getArea(damagee).getBits().isset(Group.PVP_ON)) return false;
        return true;
    }
    
    private static boolean isPlayerRespawning(Player damager, Player damagee)
    {
        if (Users.getUser(damager).isRespawning()) return true;
        if (Users.getUser(damagee).isRespawning()) return true;
        return false;
    }
    
    private static boolean isAreaDenyingFF(Player damager, Player damagee)
    {
        if (GroupControl.getArea(damager).getBits().isset(Group.PVP_FRIENDLYFIRE)) return false;
        if (GroupControl.getArea(damagee).getBits().isset(Group.PVP_FRIENDLYFIRE)) return false;
        return true;
    }
    
    private static boolean isAreaDenyingDamage(Player damager, Player damagee)
    {
        if (GroupControl.getArea(damager).getBits().isset(Group.PVP_DAMAGE)) return false;
        if (GroupControl.getArea(damagee).getBits().isset(Group.PVP_DAMAGE)) return false;
        return true;
    }
    
    public static int modifyDamage(Player damager, Player damagee, int damage)
    {
        int dmg = damage;
        Map<Group.DmgModType,Integer> modifiers = GroupControl.getArea(damagee).getDamagemodifier();
        
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
    
    private static boolean isAlly(Player damager, Player damagee)
    {
        return Users.isAllied(damager, damagee);
    }
    
    
    public static void stopFlyArrow(Player player)
    {
        
        player.sendMessage(t("event_arrow"));
        PvP.stopFlyAndFall(player);
    }
    
    public static void stopFlyAndFall(Player player)
    {
        CubeWar.debug("Fly Stop + Fall");
        PvP.stopFly(player);
        player.setAllowFlight(false);
    }
    
    public static void stopFly(final Player player)
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
    
    public static void loot(final Player killer,final Player killed, List<ItemStack> drops, final Location deathloc)
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

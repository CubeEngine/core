package de.cubeisland.cubeengine.war.user;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.CubeWarConfiguration;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.storage.GroupModel;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class PvP
{
    private UserManager cuManager = CubeCore.getInstance().getUserManager();
    private CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    private GroupControl groups = GroupControl.get();
    private UserControl users = UserControl.get();

    public PvP()
    {
    }

    public boolean isFriendlyFireOn(Player damager, Player damagee)
    {
        if (isAlly(damager, damagee))
        {
            if (isAreaDenyingFF(damager, damagee))
            {
                return false;
            }
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
        if (isAreaDenyingDamage(damager, damagee))
        {
            return false;
        }
        if (isPlayerRespawning(damager, damagee))
        {
            return false;
        }
        CubeWar.debug("Damage-ON!");
        return true;
    }

    public boolean isPvPallowed(Player damager, Player damagee)
    {
        if (isAreaPvPOff(damager, damagee))
        {
            return false;
        }
        if (isUserPeaceFull(damager) || isUserPeaceFull(damagee))
        {
            return false;
        }
        CubeWar.debug("PVP-ON!");
        return true;
    }

    private boolean isUserPeaceFull(Player player)
    {
        return users.getUser(player).getMode().equals(PlayerMode.PEACE);
    }

    private boolean isAreaPvPOff(Player damager, Player damagee)
    {
        if (groups.getGroupAtLocation(damager).hasBit(GroupModel.PVP_ON))
        {
            return false;
        }
        if (groups.getGroupAtLocation(damagee).hasBit(GroupModel.PVP_ON))
        {
            return false;
        }
        return true;
    }

    private boolean isPlayerRespawning(Player damager, Player damagee)
    {
        if (users.getUser(damager).isRespawning())
        {
            return true;
        }
        if (users.getUser(damagee).isRespawning())
        {
            return true;
        }
        return false;
    }

    private boolean isAreaDenyingFF(Player damager, Player damagee)
    {
        if (groups.getGroupAtLocation(damager).hasBit(GroupModel.PVP_FRIENDLYFIRE))
        {
            return false;
        }
        if (groups.getGroupAtLocation(damagee).hasBit(GroupModel.PVP_FRIENDLYFIRE))
        {
            return false;
        }
        return true;
    }

    private boolean isAreaDenyingDamage(Player damager, Player damagee)
    {
        if (groups.getGroupAtLocation(damager).hasBit(GroupModel.PVP_DAMAGE))
        {
            return false;
        }
        if (groups.getGroupAtLocation(damagee).hasBit(GroupModel.PVP_DAMAGE))
        {
            return false;
        }
        return true;
    }

    public int modifyDamage(Player damager, Player damagee, int damage)
    {
        int dmg = damage;
        Integer tmp;
        Group group = groups.getGroupAtLocation(damagee);
        tmp = group.getDmgMod_P();
        if (tmp != null)
        {
            dmg *= 1 + (tmp / 100);
        }
        else
        {
            tmp = group.getDmgMod_S();
            if (tmp != null)
            {
                dmg = tmp;
            }
            else
            {
                tmp = group.getDmgMod_A();
                if (tmp != null)
                {
                    dmg += tmp;
                }
            }
        }
        CubeWar.debug("Damage:" + damage + " --> " + dmg);
        return dmg;
    }

    private boolean isAlly(Player damager, Player damagee)
    {
        return users.isAllied(damager, damagee);
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
    private static HashSet<User> blockFly = new HashSet<User>();

    public static boolean isFlyBlocked(User user)
    {
        return blockFly.contains(user);
    }

    public void stopFly(final Player player)
    {
        CubeWar.debug("Fall");
        player.setFlying(false);
        if (config.fly_block > 0)
        {
            blockFly.add(cuManager.getUser(player));
            CubeWar plugin = CubeWar.getInstance();
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
                    new Runnable()
                    {
                        public void run()
                        {
                            blockFly.remove(cuManager.getUser(player));
                        }
                    }, config.fly_block * 20);
        }
    }

    public void loot(final Player killer, final Player killed, List<ItemStack> drops, final Location deathloc)
    {
        final Inventory loot = Bukkit.createInventory(killed, 6 * 9, killed.getName());
        for (ItemStack item : drops)
        {
            loot.addItem(item);
        }
        killer.openInventory(loot);
        CubeWar plugin = CubeWar.getInstance();
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
                new Runnable()
                {
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
                            {
                                if (item != null)
                                {
                                    killer.getWorld().dropItemNaturally(deathloc, item);
                                }
                            }
                        }
                    }
                }, 7 * 20);//TODO LootTime (7sec) Veränderbar je nach KP

    }
}

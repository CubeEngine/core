package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import java.util.ArrayList;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PowerToolListener implements Listener
{
    @EventHandler
    public void onLeftClick(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            if (!event.getPlayer().getItemInHand().getType().equals(Material.AIR))
            {
                if (BasicsPerm.POWERTOOL_USE.isAuthorized(event.getPlayer()))
                {
                    CraftItemStack item = (CraftItemStack)event.getPlayer().getItemInHand();
                    NBTTagCompound tag = item.getHandle().getTag();
                    if (tag == null)
                    {
                        return;
                    }
                    NBTTagList ptVals = (NBTTagList)tag.get("UniquePowerToolID");
                    if (ptVals == null)
                    {
                        return;
                    }
                    ArrayList<String> list = new ArrayList<String>();
                    for (int i = 0; i < ptVals.size();i++)
                    {
                        list.add(((NBTTagString)ptVals.get(i)).data);
                    }
                    for (String command : list)
                    {
                        event.getPlayer().chat("/"+command);
                    }
                    if (!list.isEmpty())
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}

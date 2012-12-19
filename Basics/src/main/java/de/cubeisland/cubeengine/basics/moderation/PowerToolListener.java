package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import java.util.ArrayList;
import net.minecraft.server.v1_4_5.v1_4_5.ItemStack;
import net.minecraft.server.v1_4_5.v1_4_5.NBTTagCompound;
import net.minecraft.server.v1_4_5.v1_4_5.NBTTagList;
import net.minecraft.server.v1_4_5.v1_4_5.NBTTagString;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
            Player player = event.getPlayer();
            if (!player.getItemInHand().getType().equals(Material.AIR))
            {
                if (BasicsPerm.POWERTOOL_USE.isAuthorized(event.getPlayer()))
                {
                    ItemStack item = BukkitUtils.getNmsItemStack(player.getItemInHand());
                    NBTTagCompound tag = item.getTag();
                    if (tag == null)
                    {
                        return;
                    }
                    NBTTagList ptVals = (NBTTagList)tag.get("powerToolCommands");
                    if (ptVals == null)
                    {
                        return;
                    }
                    ArrayList<String> list = new ArrayList<String>();
                    for (int i = 0; i < ptVals.size(); i++)
                    {
                        list.add(((NBTTagString)ptVals.get(i)).data);
                    }
                    for (String command : list)
                    {
                        if (command.startsWith("chat:"))
                        {
                            command = command.substring(5);
                            command = ChatFormat.parseFormats(command);
                        }
                        else
                        {
                            command = "/" + command;
                        }
                        player.chat(command);
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

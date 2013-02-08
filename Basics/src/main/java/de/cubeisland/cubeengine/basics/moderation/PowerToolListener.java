package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
                    ItemMeta meta = player.getItemInHand().getItemMeta();
                    if (meta != null)
                    {
                        List<String> lore = meta.getLore();
                        List<String> powerTool = new ArrayList<String>();
                        boolean ptStart = false;
                        if (lore != null)
                        {
                            for (String l : lore)
                            {
                                if (!ptStart)
                                {
                                    if (l.equals("§2PowerTool"))
                                    {
                                        ptStart = true;
                                    }
                                }
                                else
                                {
                                    powerTool.add(l);
                                }
                            }
                        }
                        for (String command : powerTool)
                        {
                            player.chat(command);
                        }
                        if (!powerTool.isEmpty())
                        {
                            event.setUseItemInHand(Event.Result.DENY);
                            event.setUseInteractedBlock(Event.Result.DENY);
                        }
                    }
                }
            }
        }
    }
}

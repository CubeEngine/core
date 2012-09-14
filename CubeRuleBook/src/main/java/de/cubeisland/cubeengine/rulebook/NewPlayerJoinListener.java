/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Wolfi
 */
class NewPlayerJoinListener implements Listener 
{

    Rulebook module;
    
    public NewPlayerJoinListener(Rulebook module) 
    {
        this.module = module;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if(player.hasPlayedBefore())
        {
            BookItem ruleBook = new BookItem(new ItemStack(Material.WRITTEN_BOOK));
            ruleBook.setAuthor(this.module.getCore().getServer().getServerName());
            ruleBook.setTitle("Regelbuch");
            
            player.sendMessage("hallo");
            player.setItemInHand(ruleBook.getItemStack());
        }
    }
}

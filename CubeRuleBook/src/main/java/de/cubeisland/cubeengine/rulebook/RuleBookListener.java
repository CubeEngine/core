/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
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
class RuleBookListener implements Listener 
{

    Rulebook module;
    
    public RuleBookListener(Rulebook module) 
    {
        this.module = module;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if(!player.hasPlayedBefore())
        {
            User user = this.module.getCore().getUserManager().getUser(player);
            BookItem ruleBook = new BookItem(new ItemStack(Material.WRITTEN_BOOK));
            
            ruleBook.setAuthor(this.module.getCore().getServer().getServerName());
            ruleBook.setTitle(CubeEngine._(user.getLanguage(), "rulebook", "Rulebook"));
            ruleBook.setPages(this.module.getConfig().getPages(user.getLanguage()));
            
            player.sendMessage("hallo");
            player.setItemInHand(ruleBook.getItemStack());
        }
    }
}

package de.cubeisland.cubeengine.rulebook;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookManager;

class RulebookListener implements Listener
{

    private final Rulebook module;
    private final RulebookManager rulebookManager;

    public RulebookListener(Rulebook module)
    {
        this.module = module;
        this.rulebookManager = module.getRuleBookManager();
    }

    @EventHandler
    public void onPlayerLanguageReceived(PlayerLanguageReceivedEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer());
        if (!user.hasPlayedBefore() && !this.rulebookManager.getLanguages().isEmpty())
        {
            ItemStack hand = user.getItemInHand();
            user.setItemInHand(this.rulebookManager.getBook( event.getLanguage() ) );
            
            if(hand != null && hand.getType() != Material.AIR)
            {
                for(ItemStack item : user.getInventory().addItem( hand ).values())
                {
                    user.getWorld().dropItemNaturally( user.getLocation(), item);
                }
            }
        }
    }
}

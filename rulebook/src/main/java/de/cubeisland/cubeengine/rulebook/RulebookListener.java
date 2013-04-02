package de.cubeisland.cubeengine.rulebook;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookManager;
import java.util.Locale;

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
        if(!user.hasPlayedBefore() && !this.rulebookManager.getLocales().isEmpty())
        {
            Language language = this.rulebookManager.getLanguage(event.getLanguage());
            Locale locale;
            
            if(language == null || !this.rulebookManager.contains(language.getLocale()))
            {
                locale = this.module.getCore().getI18n().getDefaultLanguage().getLocale();
                if(!this.rulebookManager.contains(locale))
                {
                    locale = this.rulebookManager.getLocales().iterator().next();
                }
            }
            else
            {
                locale = language.getLocale();
            }
            
            ItemStack hand = user.getItemInHand();
            user.setItemInHand(this.rulebookManager.getBook(locale));

            if(hand != null && hand.getType() != Material.AIR)
            {
                for(ItemStack item : user.getInventory().addItem(hand).values())
                {
                    user.getWorld().dropItemNaturally(user.getLocation(), item);
                }
            }
        }
    }
}

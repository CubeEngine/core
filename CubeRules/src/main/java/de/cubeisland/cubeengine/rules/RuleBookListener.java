package de.cubeisland.cubeengine.rules;

import de.cubeisland.cubeengine.core.bukkit.BookItem;
import de.cubeisland.cubeengine.core.bukkit.event.PlayerLanguageReceivedEvent;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

class RuleBookListener implements Listener
{
    Rules module;

    public RuleBookListener(Rules module)
    {
        this.module = module;
    }

    @EventHandler
    public void onPlayerLanguageReceived(PlayerLanguageReceivedEvent event)
    {
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (!user.hasPlayedBefore())
        {
            String language = user.getLanguage();
            
//            if (!this.module.getConfig().getLanguages().contains(language))
//            {
//                language = this.module.getCore().getI18n().getDefaultLanguage();
//            }
//            
//            BookItem ruleBook = new BookItem(new ItemStack(Material.WRITTEN_BOOK));
//
//            ruleBook.setAuthor(Bukkit.getServerName());
//            ruleBook.setTitle(_(language, "rulebook", "Rulebook"));
//            ruleBook.setPages(this.module.getConfig().getPages(language));
//            
//            user.setItemInHand(ruleBook.getItemStack());
        }
    }
}

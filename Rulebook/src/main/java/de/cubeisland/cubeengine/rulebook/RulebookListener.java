package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import org.bukkit.inventory.meta.BookMeta;

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
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (!user.hasPlayedBefore() && !this.rulebookManager.getLanguages().isEmpty())
        {
            String language = user.getLanguage();

            if (!this.module.getRuleBookManager().contains(language))
            {
                language = this.module.getCore().getI18n().getDefaultLanguage();
                if (!this.module.getRuleBookManager().contains(language))
                {
                    language = this.rulebookManager.getLanguages().iterator().next();
                }
            }

            ItemStack ruleBook = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = ((BookMeta)ruleBook.getItemMeta());
            meta.setAuthor(Bukkit.getServerName());
            meta.setTitle(_(language, "rulebook", "Rulebook"));
            meta.setPages(this.rulebookManager.getPages(language));
            ruleBook.setItemMeta(meta);
            user.setItemInHand(ruleBook);
        }
    }
}

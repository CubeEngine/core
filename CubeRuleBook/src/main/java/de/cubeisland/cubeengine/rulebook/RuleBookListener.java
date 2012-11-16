package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

class RuleBookListener implements Listener, Runnable
{
    private static String playerName = null;
    Rulebook module;

    public RuleBookListener(Rulebook module)
    {
        this.module = module;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore())
        {
            playerName = player.getName();
            player.getServer().getScheduler().
                scheduleSyncDelayedTask((Plugin)this.module.getCore(), this, 15);
        }
    }

    public void run()
    {
        if (playerName != null)
        {
            User user = this.module.getCore().getUserManager().getUser(playerName, true);
            String language = user.getLanguage();

            if (!this.module.getConfig().getLanguages().contains(language))
            {
                language = this.module.getCore().getI18n().getDefaultLanguage();
            }

            BookItem ruleBook = new BookItem(new ItemStack(Material.WRITTEN_BOOK));

            ruleBook.
                setAuthor(this.module.getCore().getServer().getServerName());
            ruleBook.setTitle(_(language, "rulebook", "Rulebook"));
            ruleBook.setPages(this.module.getConfig().getPages(language));

            user.setItemInHand(ruleBook.getItemStack());
            playerName = null;
        }
    }
}

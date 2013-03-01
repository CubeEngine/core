package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
            user.setItemInHand(this.rulebookManager.getBook( event.getLanguage() ) );
        }
    }
}

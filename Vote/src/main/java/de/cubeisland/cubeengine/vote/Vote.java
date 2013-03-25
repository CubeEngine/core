package de.cubeisland.cubeengine.vote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.Account;

import com.vexsoftware.votifier.model.VotifierEvent;

public class Vote extends Module implements Listener
{
    private Conomy conomy;

    @Override
    public void onEnable()
    {
        this.registerListener(this);
    }

    @EventHandler
    private void onVote(VotifierEvent event)
    {
        final com.vexsoftware.votifier.model.Vote vote = event.getVote();
        final User user = this.getCore().getUserManager().getUser(vote.getUsername());
        final Account account = this.conomy.getAccountsManager().getAccount(user);
    }
}

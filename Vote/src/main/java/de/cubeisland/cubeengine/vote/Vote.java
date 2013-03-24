package de.cubeisland.cubeengine.vote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.module.Module;

import com.vexsoftware.votifier.model.VotifierEvent;

public class Vote extends Module implements Listener
{
    @Override
    public void onEnable()
    {
        this.registerListener(this);
    }

    @EventHandler
    private void onVote(VotifierEvent event)
    {
        com.vexsoftware.votifier.model.Vote vote = event.getVote();

    }
}

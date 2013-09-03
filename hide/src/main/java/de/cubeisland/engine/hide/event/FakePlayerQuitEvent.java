package de.cubeisland.engine.hide.event;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author CodeInfection
 */
public class FakePlayerQuitEvent extends PlayerQuitEvent
{
    public FakePlayerQuitEvent(Player who, String quitMessage)
    {
        super(who, quitMessage);
    }
}

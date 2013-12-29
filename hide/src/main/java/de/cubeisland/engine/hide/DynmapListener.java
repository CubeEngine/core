package de.cubeisland.engine.hide;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.engine.hide.event.UserHideEvent;
import de.cubeisland.engine.hide.event.UserShowEvent;
import org.dynmap.DynmapAPI;

public class DynmapListener implements Listener
{
    private final DynmapAPI dynmapAPI;

    public DynmapListener(DynmapAPI dynmapAPI)
    {
        this.dynmapAPI = dynmapAPI;
    }

    @EventHandler
    public void onHide(UserHideEvent event)
    {
        this.dynmapAPI.setPlayerVisiblity(event.getUser(), false);
    }

    @EventHandler
    public void onShow(UserShowEvent event)
    {
        this.dynmapAPI.setPlayerVisiblity(event.getUser(), true);
    }
}

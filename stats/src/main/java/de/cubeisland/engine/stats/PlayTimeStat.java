package de.cubeisland.engine.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayTimeStat extends Stat
{
    private final String name = "playtime";

    private Map<String, Long> joined;

    public void onActivate()
    {
        this.joined = new HashMap<>();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @EventHandler()
    public void playerJoin(PlayerJoinEvent event)
    {
        joined.put(event.getPlayer().getName(), System.currentTimeMillis());
    }

    public void playerLeave(PlayerQuitEvent event)
    {
        Long playtime = System.currentTimeMillis() - joined.get(event.getPlayer().getName());

    }
}

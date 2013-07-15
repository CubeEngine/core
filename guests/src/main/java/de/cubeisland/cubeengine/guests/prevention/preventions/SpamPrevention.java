/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.guests.prevention.preventions;

import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;

import gnu.trove.map.hash.TObjectLongHashMap;

/**
 * Prevents spamming.
 */
public class SpamPrevention extends Prevention
{
    private long spamLockDuration;
    private TObjectLongHashMap<Player> chatTimestamps;

    public SpamPrevention(Guests guests)
    {
        super("spam", guests);
        setEnableByDefault(true);
        setEnablePunishing(true);
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader()
            + "Configuration info:\n"
            + "    lockDuration: the time in seconds a player has to wait between messages";
    }

    @Override
    public Configuration getDefaultConfig()
    {
        Configuration config = super.getDefaultConfig();

        config.set("lockDuration", 2);

        return config;
    }

    @Override
    public void enable()
    {
        super.enable();
        this.spamLockDuration = TimeUnit.SECONDS.toMillis(getConfig().getLong("lockDuration"));
        this.chatTimestamps = new TObjectLongHashMap<Player>();
    }

    @Override
    public void disable()
    {
        super.disable();
        this.chatTimestamps = null;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
        if (!can(player))
        {
            if (isChatLocked(player))
            {
                sendMessage(player);
                punish(player);
                event.setCancelled(true);
            }
            else
            {
                setChatLock(player);
            }
        }
    }

    private synchronized void setChatLock(final Player player)
    {
        this.chatTimestamps.put(player, System.currentTimeMillis() + this.spamLockDuration);
    }

    private synchronized boolean isChatLocked(final Player player)
    {
        final long nextPossible = this.chatTimestamps.get(player);
        if (nextPossible == 0)
        {
            return false;
        }

        final long currentTime = System.currentTimeMillis();
        if (nextPossible < currentTime)
        {
            return false;
        }
        return true;
    }
}

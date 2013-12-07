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
package de.cubeisland.engine.stats.stat;

import org.bukkit.event.Listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.stats.StatsManager;

/**
 * A statistic, for example play time
 */
public abstract class Stat implements Listener
{
    private final StatsManager manager;
    private final Module owner;
    private final Core core;

    /**
     * Initialize this statistic.
     * This should only be used by the StatsManager
     *
     * @param manager The StatsManager loading this statistic
     */
    public Stat(StatsManager manager, Module owner)
    {
        this.manager = manager;
        this.owner = owner;
        this.core = owner.getCore();
        core.getEventManager().registerListener(owner, this);
    }

    /**
     * Per stat initialization, called after init
     */
    public void onActivate()
    {}

    /**
     * Called when this stat is deactivated.
     * Cleanup should go here.
     */
    public void onDeactivate()
    {}

    /**
     * Write a database entry
     *
     * This just calls StatsManager.save(Stat, Object)
     *
     * @param object The object to write in the entry
     */
    public void save(Object object)
    {
        manager.save(this, object);
    }
}

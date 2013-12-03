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
    private StatsManager manager;
    private Module owner;
    private Core core;

    /**
     * Initialize this statistic.
     * This should only be used by the StatsManager
     *
     * @param manager The StatsManager associated with this statistic
     */
    public void init(StatsManager manager)
    {
        this.manager = manager;
        this.owner = manager.getModule();
        this.core = owner.getCore();
        core.getEventManager().registerListener(owner, this);
    }

    /**
     * Per stat initialization, called after init
     */
    public void onActivate()
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

    /**
     * Get the unique name of this stat.
     * It is the statistic's responsibility that this is unique.
     * NB: Max 20 characters
     *
     * @return the unique name of this stat
     */
    public abstract String getName();
}

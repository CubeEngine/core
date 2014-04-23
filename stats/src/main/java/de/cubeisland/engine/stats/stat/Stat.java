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

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.stats.StatsManager;

/**
 * A statistic, for example play time
 */
public abstract class Stat implements Listener
{
    private StatsManager manager;
    private Module owner;
    private Core core;

    private boolean started = false;

    /**
     * Initialize this statistic.
     * This should only be used by the StatsManager
     *
     * @param manager The StatsManager loading this statistic
     */
    public final void init(StatsManager manager, Module owner)
    {
        this.manager = manager;
        this.owner = owner;
        this.core = owner.getCore();
        core.getEventManager().registerListener(owner, this);
        this.started = true;
        // We make a onInit if we need it.
    }

    /**
     * Activate this statistic.
     */
    public final void activate()
    {
        if (!this.started)
        {
            throw new IllegalStateException("Stat was not started!");
        }
        this.onActivate();
    }

    /**
     * Deactivate this statistic.
     *
     * Once deactivated, no methods can be called on it again. It can be reactivated by running init again.
     */
    public final void deactivate()
    {
        if (!this.started)
        {
            throw new IllegalStateException("Stat was not started!");
        }
        this.onDeactivate();
        this.started = false;
    }

    /**
     * Called when the stat is activated
     */
    protected void onActivate()
    {}

    /**
     * Called when the stat is disabled
     */
    protected void onDeactivate()
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
        if (!this.started)
        {
            throw new IllegalStateException("Stat was not started!");
        }
        manager.save(this, object);
    }

    protected StatsManager getStatsManager()
    {
        return this.manager;
    }

    protected Core getCore()
    {
        return this.core;
    }
}

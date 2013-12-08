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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.stats.StatsManager;
import de.cubeisland.engine.stats.annotations.Scheduled;

public class LagStat extends Stat
{
    private LagTimer lagTimer;

    @Override
    public void onActivate()
    {
        this.lagTimer = new LagTimer();
        lagTimer.run();
    }


    @Scheduled(name = "tps-calculator", interval = 20, periodFinal = true)
    public void tick()
    {
        this.lagTimer.run();
    }

    /**
     * Fetch the current TPS and free memory and push it to the database
     */
    @Scheduled(name = "fetcher", interval = 200, comment = "The task that fetches the memory and tps and pushes it to the database", async = true)
    public void fetch()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("tps", this.lagTimer.getAverageTPS());

        Runtime rt = Runtime.getRuntime();
        long freemem = rt.freeMemory();
        long usedmem = rt.totalMemory() - rt.freeMemory();

        data.put("freemem", freemem);
        data.put("usedmem", usedmem);

        this.save(data);
    }

    /**
     * A modified version of de.cubeisland.engine.basics.command.general.LagTimer
     */
    private class LagTimer implements Runnable
    {
        private long lastTick = 0;
        private final LinkedList<Float> tpsHistory = new LinkedList<>();

        @Override
        public void run()
        {
            synchronized (this)
            {
                if (lastTick == 0)
                {
                    lastTick = System.currentTimeMillis();
                    return;
                }
                final long currentTick = System.currentTimeMillis();
                long timeSpent = (currentTick - lastTick) / 1000;
                if (timeSpent == 0)
                {
                    timeSpent = 1;
                }
                if (tpsHistory.size() > 10)
                {
                    tpsHistory.remove();
                }
                final float tps = 20f / timeSpent;
                if (tps <= 20)
                {
                    tpsHistory.add(tps);
                }
                lastTick = currentTick;
            }
        }

        public float getAverageTPS()
        {
            synchronized (this)
            {
                float ticks = 0;
                for (Float tps : tpsHistory)
                {
                    if (tps != null)
                    {
                        ticks += tps;
                    }
                }
                return ticks / tpsHistory.size();
            }
        }
    }
}

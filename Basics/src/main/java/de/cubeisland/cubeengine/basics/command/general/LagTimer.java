package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.CubeEngine;
import java.util.LinkedList;

public class LagTimer implements Runnable
{
    private static LagTimer instance = null;

    private long lastTick = System.currentTimeMillis();
    private final LinkedList<Float> tpsHistory = new LinkedList<Float>();

    private LagTimer()
    {}

    public static LagTimer getTimer()
    {
        if (instance == null)
        {
            instance = new LagTimer();
            CubeEngine.getTaskManager().scheduleSyncRepeatingTask(Basics.getInstance(), instance, 0, 20); //start timer
        }
        return instance;
    }

    @Override
    public void run()
    {
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

    public float getAverageTPS()
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

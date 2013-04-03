package de.cubeisland.cubeengine.core.command.result;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;

public abstract class LongRunningResult implements CommandResult
{
    private boolean isDone = false;
    private int taskId = -1;

    @Override
    public void show(final CommandContext context)
    {
        this.taskId = context.getCore().getTaskManager().scheduleSyncRepeatingTask(context.getCommand().getModule(), new Runnable()
        {
            @Override
            public void run()
            {
                LongRunningResult.this.run(context);
                if (LongRunningResult.this.isDone)
                {
                    context.getCore().getTaskManager().cancelTask(context.getCommand().getModule(), LongRunningResult.this.taskId);
                }
            }
        }, 0, 1);
        if (this.taskId == -1)
        {
            throw new RuntimeException("Failed to schedule the task for the long running command result!");
        }
    }

    protected void setDone()
    {
        this.isDone = true;
    }

    public abstract void run(CommandContext context);
}

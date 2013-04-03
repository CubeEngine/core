package de.cubeisland.cubeengine.core.command.result;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;

public abstract class DelayedResult implements CommandResult
{
    private final long delay;

    protected DelayedResult(long delay)
    {
        this.delay = delay;
    }

    protected DelayedResult()
    {
        this(1);
    }

    @Override
    public void show(final CommandContext context)
    {
        final int taskId = context.getCore().getTaskManager().scheduleSyncDelayedTask(context.getCommand().getModule(), new Runnable() {
            @Override
            public void run()
            {
                DelayedResult.this.run(context);
            }
        }, this.delay);

        if (taskId == -1)
        {
            throw new RuntimeException("Failed to schedule the task for the delayed command result!");
        }
    }

    public abstract void run(CommandContext context);
}

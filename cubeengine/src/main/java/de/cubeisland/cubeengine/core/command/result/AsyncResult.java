package de.cubeisland.cubeengine.core.command.result;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;

public abstract class AsyncResult implements CommandResult
{
    public abstract void asyncMain(CommandContext context);
    public void onFinish(CommandContext context)
    {}

    @Override
    public final void show(final CommandContext context)
    {
        context.getCore().getTaskManager().getThreadFactory().newThread(new Runnable() {
            @Override
            public void run()
            {
                AsyncResult.this.asyncMain(context);
                context.getCore().getTaskManager().scheduleSyncDelayedTask(context.getCommand().getModule(), new Runnable() {
                    @Override
                    public void run()
                    {
                        AsyncResult.this.onFinish(context);
                    }
                });
            }
        }).start();
    }
}

package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.matcher.EntityMatcher;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunConfiguration;
import de.cubeisland.cubeengine.fun.listeners.NukeListener;
import de.cubeisland.cubeengine.fun.listeners.RocketListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import org.bukkit.Bukkit;

public class FunCommands
{
    private final FunConfiguration config;
    private final Fun module;
    private final UserManager userManager;
    private final TaskManager taskManager;

    public FunCommands(Fun module)
    {
        this.module = module;
        this.userManager = module.getUserManager();
        this.taskManager = module.getCore().getTaskManager();
        this.config = module.getConfig();
    }

    @Command(
        desc = "rockets a player",
        max = 1,
        usage = "[height]",
        params = {@Param(names = {"player", "p"}, type = User.class)}
    )
    public void rocket(CommandContext context)
    {
        RocketListener rocketListener = this.module.getRocketListener();
        int ticks = 20;

        int height = context.getIndexed(0, Integer.class, 10);
        User user = (context.hasNamed("player"))
            ? context.getNamed("player", User.class, null)
            : context.getSenderAsUser("fun", "&cThis command can only be used by a player!");

        if (user == null)
        {
            invalidUsage(context, "core", "User not found!");
        }

        if (height > 100)
        {
            invalidUsage(context, "fun", "Do you never wanna see %s again?", user.getName());
        }
        else
        {
            if (height < 0)
            {
                invalidUsage(context, "fun", "The height has to be greater than 0");
            }
        }

        user.setVelocity(new Vector(0.0, (double)height / 10, 0.0));
        rocketListener.addInstance(user, ticks);

        for (int i = 1; i <= ticks; i++)
        {
            this.taskManager.scheduleSyncDelayedTask(module, rocketListener, 5 * i);
        }
    }
}

package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.bukkit.CubeCommandMap;
import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.libs.jline.console.completer.Completer;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.util.StringUtils.explode;

public class ConsoleCommandCompleter implements Completer
{
    private final BukkitCore core;
    private final CubeCommandMap commandMap;

    public ConsoleCommandCompleter(BukkitCore core, CubeCommandMap commandMap)
    {
        this.core = core;
        this.commandMap = commandMap;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates)
    {
        try
        {
            if (buffer.isEmpty())
            {
                return cursor;
            }

            String[] args = explode(" ", buffer);
            if (args.length == 1)
            {
                String token = args[0].toLowerCase(Locale.ENGLISH);
                Command command = this.commandMap.getCommand(token);
                if (command != null)
                {
                    return cursor;
                }

                for (String label : this.commandMap.getKnownCommands().keySet())
                {
                    if (label.toLowerCase(Locale.ENGLISH).startsWith(token))
                    {
                        candidates.add(label);
                    }
                }
                return cursor - token.length();
            }
            else
            {
                String label = args[0].toLowerCase(Locale.ENGLISH);
                Command command = this.commandMap.getCommand(label);
                if (!(command instanceof CubeCommand))
                {
                    args = buffer.split(" ");
                }
                if (command != null)
                {
                    final ConsoleCommandSender sender = this.core.getServer().getConsoleSender();
                    List<String> result = command.tabComplete(sender, label, Arrays.copyOfRange(args, 1, args.length));

                    if (!result.isEmpty())
                    {
                        candidates.addAll(result);
                    }
                    else
                    {
                        final String token = args[args.length - 1].toLowerCase(Locale.ENGLISH);
                        for (Player player : sender.getServer().getOnlinePlayers())
                        {
                            String name = player.getName();
                            if (StringUtil.startsWithIgnoreCase(name, token))
                            {
                                candidates.add(name);
                            }
                        }
                    }

                    if (candidates.size() > 0)
                    {
                        return cursor - args[args.length - 1].length();
                    }
                }
            }
        }
        catch (Exception e)
        {
            this.core.getCoreLogger().log(ERROR, "An error occurred while completing your command line!", e);
        }
        return cursor;
    }
}

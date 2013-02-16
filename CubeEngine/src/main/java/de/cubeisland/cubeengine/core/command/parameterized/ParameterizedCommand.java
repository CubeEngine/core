package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.cubeisland.cubeengine.core.util.StringUtils.startsWithIgnoreCase;

public abstract class ParameterizedCommand extends CubeCommand
{
    protected ParameterizedCommand(Module module, String name, String description, ParameterizedContextFactory parser)
    {
        super(module, name, description, parser);
    }

    protected ParameterizedCommand(Module module, String name, String description, String usageMessage, List<String> aliases, ParameterizedContextFactory parser)
    {
        super(module, name, description, usageMessage, aliases, parser);
    }

    @Override
    public ParameterizedContextFactory getContextFactory()
    {
        return (ParameterizedContextFactory)super.getContextFactory();
    }

    public void addParameter(CommandParameter param)
    {
        this.getContextFactory().addParameter(param);
    }

    public void addFlag(CommandFlag flag)
    {
        this.getContextFactory().addFlag(flag);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        if (args.length > 0)
        {
            String token = args[args.length - 1];
            ParameterizedContextFactory contextFactory = this.getContextFactory();
            if (args.length == 1)
            {
                List<String> flagNames = new ArrayList<String>();
                if (!token.isEmpty() && token.charAt(0) == '-')
                {
                    token = token.substring(1).toLowerCase(Locale.ENGLISH);
                    for (CommandFlag flag : contextFactory.getFlags())
                    {
                        final String name = flag.getLongName().toLowerCase(Locale.ENGLISH);
                        if (startsWithIgnoreCase(name, token))
                        {
                            flagNames.add("-" + name);
                        }
                    }
                    Collections.sort(flagNames, String.CASE_INSENSITIVE_ORDER);
                    return flagNames;
                }
            }
            else
            {
                CommandParameter param = contextFactory.getParameter(args[args.length - 2]);
                if (param != null)
                {
                    ParamCompleter completer = param.getCompleter();
                    if (completer != null)
                    {
                        final List<String> result = completer.complete(sender, token);
                        if (result != null)
                        {
                            Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
                        }
                        return result;
                    }
                }
            }
        }
        return null;
    }
}

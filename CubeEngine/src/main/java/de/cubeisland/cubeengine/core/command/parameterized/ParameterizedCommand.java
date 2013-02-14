package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;


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
        if (!(sender instanceof User))
        {
            return null;
        }
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
                    for (String flagName : contextFactory.getFlagMap().keySet())
                    {
                        // TODO filter out aliases?
                        if (flagName.toLowerCase(Locale.ENGLISH).startsWith(token))
                        {
                            flagNames.add("-" + flagName);
                        }
                    }
                    Collections.sort(flagNames);
                    return flagNames;
                }
            }
            else
            {
                Map<String, CommandParameter> params = contextFactory.getParamMap();
                CommandParameter param = params.get(args[args.length - 2].toLowerCase(Locale.ENGLISH));
                if (param != null)
                {
                    ParamCompleter completer = param.getCompleter();
                    if (completer != null)
                    {
                        return completer.complete((User)sender, token);
                    }
                }
            }
        }
        return null;
    }
}

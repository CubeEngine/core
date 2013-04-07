package de.cubeisland.cubeengine.log.commands;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.log.Log;

public class LookupCommands extends ContainerCommand
{
    private final Log module;

    public LookupCommands(Log module)
    {
        super(module, "lookup", "Searches in the database for needed informations.");
        this.module = module;
    }

    @Command(desc = "Displays all possible parameters.")
    public void params(CommandContext context)
    {
        context.sendTranslated("&6Lookup&f/&6Rollback&f/&6Restore&f-&6Parameters:");
        context.sendMessage("");
        context.sendTranslated(" &f-&6 action &7<actionType> &flike &3block-break &f(See full list below)");
        context.sendTranslated(" &f-&6 radius &7<radius>&f or &3sel&f, &3global&f, &3player:<radius>");
        context.sendTranslated(" &f-&6 player &7<users>&f like &3p Faithcaio ");
        context.sendTranslated(" &f-&6 entity &7<entities>&f like &3e sheep");
        context.sendTranslated(" &f-&6 block &7<blocks>&f like &3b stone &for &3b 1");
        context.sendTranslated(" &f-&6 since &7<time>&f default is 3 days");
        context.sendTranslated(" &f-&6 before &7<time>");
        context.sendTranslated(" &f-&6 world &7<world>&f default is your current world");
        context.sendMessage("");
        context.sendTranslated("Use &6!&f to exclude the parameters instead of including them.");
        context.sendMessage("");
        context.sendTranslated("&6Registered ActionTypes:");
        context.sendMessage(this.module.getActionTypeManager().getActionTypesAsString());
    }

    /**
     * Returns the Selection or null if nothing is selected
     *
     * @param context
     * @return
     */
    //TODO change return to a selection See WE how they did it
    private Location getSelection(ParameterizedContext context)
    {
        if (!context.hasFlag("sel"))
        {
            throw new IllegalStateException("Did not choose selection!");
        }
        return null;
    }

    @Command(names = {
        "block", "blocklog"
    }, desc = "Changes regarding blocks", usage = "", flags = {
        @Flag(longName = "coordinates", name = "coords"),
        @Flag(longName = "detailed", name = "det"),
        @Flag(longName = "descending", name = "desc") //sort in descending order (default ascending)
    },
    params = {
        @Param(names = {"action","a"}),// !!must have tabcompleter for all register actionTypes
        @Param(names = {"radius","r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
        @Param(names = {"user","player","p"}),
        @Param(names = {"block","b"}),
        @Param(names = {"entity","e"}),
        @Param(names = {"since","time","t"},type = Date.class), // if not given default since 3d
        @Param(names = {"before"},type = Date.class),
        @Param(names = {"world","w","in"}, type = World.class),
        @Param(names = {"limit"},type = Integer.class),
    })
    public void lookup(ParameterizedContext context)
    {
        context.sendMessage("LOOKUP IS NOT IMPLEMENTED YET");
    }
}

package de.cubeisland.cubeengine.log.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Date;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author Anselm Brehme
 */
public class LogCommands extends ContainerCommand
{
    public LogCommands(Module module)
    {
        super(module, "log", "Searches in the database for needed informations.");
        module.registerCommand(this);
    }

    @Command(
    names = {"lookup"},
    desc = "Lookups",
    usage = "<params>",
    flags =
    {
        @Flag(longName = "selection", name = "sel"), // only search in Selection
        @Flag(longName = "created", name = "c"), // only search for placed blocks (on by default)
        @Flag(longName = "destroyed", name = "d"), // only search for breaked blocks (on by default)
        @Flag(longName = "chat", name = "d"), //only search for chatlogs (off by default)
        @Flag(longName = "chestaccess", name = "chest"), //only search for chestaccess (off by default)
        @Flag(longName = "coordinates", name = "coords"),//display position (off by default)
        @Flag(longName = "descending", name = "desc"), //sort in descending order (default ascending)
    },
    params =
    {
        @Param(names = {"player", "p"}, types = {User[].class}),
        @Param(names = {"area"}, types = {int.class}),
        @Param(names = {"block"}, types = {Material[].class}),
        @Param(names = {"since", "time"}, types = {Date.class}),
        @Param(names = {"before"}, types = {Date.class}),
        @Param(names = {"limit"}, types = {Date.class}),
        @Param(names = {"world"}, types = {World.class}),
    })
    public void lookup(CommandContext context)
    {
    }
}

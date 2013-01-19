package de.cubeisland.cubeengine.log.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Collection;
import java.util.Date;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class LogCommands extends ContainerCommand
{
    public LogCommands(Module module)
    {
        super(module, "lookup", "Searches in the database for needed informations.");
    }

    @Command(desc = "Displays all possible parameters.")
    public void params(CommandContext context)
    {
        context.sendMessage("You used the /log params command!");
    }

    /**
     * Returns the Selection or null if nothing is selected
     *
     * @param context
     * @return
     */
    //TODO change return to a selection
    private Location getSelection(CommandContext context)
    {
        if (!context.hasFlag("sel"))
        {
            throw new IllegalStateException("Did not choose selection!");
        }
        return null;
        //TODO
    }

    @Command(names =
    {
        "block", "blocklog"
    }, desc = "Changes regarding blocks",
             usage = "",
             flags =
    {
        @Flag(longName = "selection", name = "sel"),
        @Flag(longName = "coordinates", name = "coords"),
        @Flag(longName = "detailed", name = "det")
    })
    // /lookup block [types][radius <r>][block <blocks...>][player <player...>][in world][-selection][-coords][-detailed]
    //FLAGS:
    //Selection
    //show coords
    //detailed or simple/brief (summing up results or not?)
    //TYPEs:    
    //break/place/interact/signs (default: break,place,interact)
    //WORLD:
    //in|world world (def current world)
    //PLAYER
    //player player1,player2,!notplayer3
    //including commaseparated exclude with ! infront
    //BLOCKTYPE
    //block 35,!1,!wood
    //same as player matching ,-separated ! infront excludes
    //RADIUS
    //radius|area 6
    //time (since before (timeframe))//<-- TODO very difficult
    public void block(CommandContext context)
    {
        //blocklog_
        //[<world_>[loc_|range_]]
        //
        
        
        
    }
    
  
    //TODO remove that
    @Command(names =
    {
        "lookup"
    }, desc = "Lookups", usage = "<params>", flags =
    {
        @Flag(longName = "selection", name = "sel"), // only search in Selection
        @Flag(longName = "created", name = "c"), // only search for placed blocks (on by default)
        @Flag(longName = "destroyed", name = "d"), // only search for breaked blocks (on by default)
        @Flag(longName = "chat", name = "ch"), //only search for chatlogs (off by default)
        @Flag(longName = "chestaccess", name = "chest"), //only search for chestaccess (off by default)
        @Flag(longName = "coordinates", name = "coords"),//display position (off by default)
        @Flag(longName = "descending", name = "desc"), //sort in descending order (default ascending)
    }, params =
    {
        @Param(names =
        {
            "player", "p"
        }, type = User[].class),
        @Param(names = "area", type = Integer.class),
        @Param(names = "block", type = ItemStack[].class),
        @Param(names =
        {
            "since", "time"
        }, type = Date.class),
        @Param(names = "before", type = Date.class),
        @Param(names = "limit", type = Date.class),
        @Param(names = "world", type = World.class),
    })
    public void lookup(CommandContext context)
    {
        ItemStack[] blocktypes;
        if (context.hasNamed("block"))
        {
            blocktypes = context.getNamed("block", ItemStack[].class);
        }
    }
}

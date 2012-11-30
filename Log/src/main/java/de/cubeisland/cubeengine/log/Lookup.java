package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.LogManager;
import de.cubeisland.cubeengine.log.storage.LogModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * This class represents an instance of a lookup. (The lookup gets bound to the
 * user executing the lookup)
 */
public class Lookup
{
    boolean showCoords;
    
    private List<LogModel> blocklogs;

    //private List<ChatLog> chatlogs;
    //private List<ChestLog> chestlogs;
    //TODO possibility to "give" the lookup to an other User
    public void printLookup(User user)
    {
        //TODO sort by timestamp (or other)
    }

    public Lookup filterSelection()//TODO the selection as param / & / how to get the selection?
    {
        List<LogModel> newBlockLogs = new ArrayList<LogModel>();
        for (LogModel blocklog : blocklogs)
        {
            //TODO if in selection
            if (1 == 0)
            {
                newBlockLogs.add(blocklog);
            }
        }
        //TODO same with chatlogs
        blocklogs = newBlockLogs;
        return this;
    }

    public Lookup filterWorld(World world)
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup filterItemType(ItemStack[] blocks)//or item
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup filterUsers(User[] names)
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup filterBlockBreak()
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup filterBlockPlace()
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup filterTimeSince(Date date)
    {
        return this.filterTimeFrame(date, new Date(System.currentTimeMillis()));
    }

    public Lookup filterTimeOlder(Date date)
    {
        return this.filterTimeFrame(new Date(0), date);
    }

    public Lookup filterTimeFrame(Date date1, Date date2)
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
    
    public static Lookup getBlocklogs()
    {
        int blocklog = LogModel.BLOCKLOG;
        Location loc1;
        Location loc2;
        List<User> userList;
        List<BlockData> blockList;
        
        //LazyLoading of prepared statements:
        //Building statement name
        //log_<type>_<world?>_<checkLocation?>_<Anz_Users>_<Anz_BlockType>
        QueryBuilder builder = Log.getInstance().getDatabase().getQueryBuilder();
        builder.select().wildcard().
                from("logs").
                where().
                //SELECT BLOCKLOG TYPE
                field("type").isEqual().value();
        builder.select().wildcard().
                from("logs").
                where().
                //SELECT WORLD
                field("worldUUID").isEqual().value();
        builder.select().wildcard().
                from("logs").
                where().
                //SELECT BETWEEN LOCATIONS
                //make sure both Loc are in the same world
                field("x").between().and().
                field("y").between().and().
                field("z").between();
        builder.select().wildcard().
                from("logs").
                where().
                //SELECT a USER
                //make sure both Loc are in the same world
                field("causeID").isEqual().value(); //CONNECT LIST WITH OR & put () around
       builder.select().wildcard().
                from("logs").
                where().
                //SELECT BlockTypes
                beginSub().
                        field("newBlockOrLines").isEqual().value().or().
                        field("oldBlockOrLines").isEqual().value().
                endSub(); //CONNECT LIST WITH OR & put () around
        return null;      
        
    }
    
}

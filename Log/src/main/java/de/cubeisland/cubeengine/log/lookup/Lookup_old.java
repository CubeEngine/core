package de.cubeisland.cubeengine.log.lookup;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.BlockData;
import java.util.Date;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * This class represents an instance of a lookup. (The lookup gets bound to the
 * user executing the lookup)
 */
public class Lookup_old
{
    boolean showCoords;

    //private List<ChatLog> chatlogs;
    //private List<ChestLog> chestlogs;
    //TODO possibility to "give" the lookup to an other User
    public void printLookup(User user)
    {
    //TODO sort by timestamp (or other)
    }

    public Lookup_old filterSelection()//TODO the selection as param / & / how to get the selection?
    {
        return this;
    }

    public Lookup_old filterWorld(World world)
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup_old filterItemType(ItemStack[] blocks)//or item
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup_old filterUsers(User[] names)
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup_old filterBlockBreak()
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup_old filterBlockPlace()
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Lookup_old filterTimeSince(Date date)
    {
        return this.filterTimeFrame(date, new Date(System.currentTimeMillis()));
    }

    public Lookup_old filterTimeOlder(Date date)
    {
        return this.filterTimeFrame(new Date(0), date);
    }

    public Lookup_old filterTimeFrame(Date date1, Date date2)
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public static Lookup_old getBlocklogs()
    {
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

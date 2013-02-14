package de.cubeisland.cubeengine.basics.storage;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TwoKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IgnoreListManager extends TwoKeyStorage<Long, Long, IgnoreList>
{

    private static final int REVISION = 1;
    private TLongObjectHashMap<List<Long>> ignoreList = new TLongObjectHashMap<List<Long>>();

    public IgnoreListManager(Database database)
    {
        super(database, IgnoreList.class, REVISION);
        this.initialize();
    }

    @Override
    public void initialize()
    {
        try
        {
            super.initialize();
            this.database.storeStatement(modelClass, "getAllByUser", this.database.getQueryBuilder()
                    .select().wildcard().from(this.tableName)
                    .where().field("key").isEqual().value().end().end());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while initializing queries for IngoreList", ex);
        }
    }

    public boolean addIgnore(User user, User toIgnore)
    {
        if (this.checkIgnore(user, toIgnore))
        {
            return false;
        }
        this.store(new IgnoreList(user, toIgnore));
        this.ignoreList.get(user.key).add(toIgnore.key);
        return true;
    }

    public boolean removeIgnore(User user, User toUnIgnore)
    {
        if (!this.checkIgnore(user, toUnIgnore))
        {
            return false;
        }
        this.delete(new IgnoreList(user, toUnIgnore));
        this.ignoreList.get(user.key).remove(toUnIgnore.key);
        return true;
    }

    public boolean checkIgnore(User user, User doesIgnore)
    {
        if (this.ignoreList.containsKey(user.key))
        {
            List<Long> ignored = this.ignoreList.get(user.key);
            return ignored.contains(doesIgnore.key);
        }
        try
        {
            List<Long> ignored = this.ignoreList.get(user.key);
            if (ignored == null)
            {
                ignored = new ArrayList<Long>();
                this.ignoreList.put(user.key, ignored);
            }
            ignored.clear();
            ResultSet result = this.database.preparedQuery(modelClass, "getAllByUser", user.key);
            while (result.next())
            {
                ignored.add(result.getLong("ignore"));
            }
            return ignored.contains(doesIgnore.key);
        }
        catch (SQLException e)
        {
            throw new StorageException("Error while getting models form database", e);
        }
    }
}

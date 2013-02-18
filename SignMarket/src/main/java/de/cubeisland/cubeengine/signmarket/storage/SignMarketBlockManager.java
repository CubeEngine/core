package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.signmarket.Signmarket;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SignMarketBlockManager extends SingleKeyStorage<Long, SignMarketBlockModel>
{
    private static final int REVISION = 1;
    private final Signmarket module;

    public SignMarketBlockManager(Signmarket module)
    {
        super(module.getDatabase(), SignMarketBlockModel.class, REVISION);
        this.module = module;
        this.initialize();
    }

    @Override
    public void initialize()
    {
        super.initialize();
        try
        {
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.storeStatement(modelClass, "getByLocation",
                    builder.select().cols("key").from(this.tableName).
                            where().field("world").isEqual().value().
                            and().field("x").isEqual().value().
                            and().field("y").isEqual().value().
                            and().field("z").isEqual().value().
                            end().end());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not initialize SignMarketBlockManager", ex);
        }
    }

    public Long getMarketSignID(Location location)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getByLocation",
                    this.module.getCore().getWorldManager().getWorldId(location.getWorld()),
                    location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (resulsSet.next())
            {
                return resulsSet.getLong("key");
            }
            return null;
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while reading from Database", ex, this.database.getStoredStatement(modelClass,"getByLocation"));
        }
    }
}

package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KitsGivenManager extends BasicStorage<KitsGiven>
{
    private static final int REVISION = 1;

    public KitsGivenManager(Database database)
    {
        super(database, KitsGiven.class, REVISION);

        this.initialize();
    }

    @Override
    protected void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.storeStatement(modelClass, "getLimitForUser",
                    builder.select().cols("amount").
                    from(this.table).
                    where().field("userId").isEqual().value()
                    .and().field("kitName").isEqual().value().end().end());
            
            this.database.storeStatement(modelClass, "mergeLimitForUser", //TODO this cannot work i have to allow multiple keys in models
                    builder.merge().into(this.table).
                    cols(this.dbAttributes.toArray(new String[this.dbAttributes.size()])).
                    updateCols("amount").end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the mail-manager!", e);
        }
    }

    public boolean reachedUsageLimit(User user, String name, int limitUsagePerPlayer)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", user.key);
            if (resulsSet.next())
            {
                Integer amount = resulsSet.getInt("amount");
                if (amount >= limitUsagePerPlayer)
                {
                    return true;
                }
            }
            return false;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
    }
}

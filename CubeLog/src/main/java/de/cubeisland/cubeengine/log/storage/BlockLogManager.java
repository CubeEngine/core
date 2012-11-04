package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import java.sql.SQLException;

public class BlockLogManager extends BasicStorage<BlockLog>
{
    private static final int REVISION = 1;

    public BlockLogManager(Database database)
    {
        super(database, BlockLog.class, REVISION);
        this.initialize();
        this.keyIsAI = false;
        
        String[] allFields = new String[this.attributes.size() + 1];
        allFields[0] = this.key;
        System.arraycopy(this.attributes.toArray(), 0, allFields, 1, this.attributes.size());
        QueryBuilder builder = this.database.getQueryBuilder();
        builder.insert()
            .into(this.table)
            .cols(allFields)
            .end();
        try
        {
            this.database.prepareAndStoreStatement(modelClass, "store", builder.end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing and storing custom SQL code.", ex);
        }
    }
}
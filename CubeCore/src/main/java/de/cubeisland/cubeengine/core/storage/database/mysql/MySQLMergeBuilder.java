package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.MergeBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Phillip Schichtel
 */
public class MySQLMergeBuilder extends MySQLComponentBuilder<MySQLMergeBuilder,MySQLQueryBuilder> implements MergeBuilder<MySQLMergeBuilder,MySQLQueryBuilder>
{
    private boolean updateColsSpecified;
    private String[] insertCols;
    private MySQLQueryBuilder parent;
    
    protected MySQLMergeBuilder(MySQLQueryBuilder parent, Database database)
    {
        super(database);
        this.parent = parent;
    }

    public MySQLMergeBuilder into(String table)
    {
        this.query = new StringBuilder("INSERT INTO ").append(this.database.prepareName(table)).append(" ");
        this.updateColsSpecified = false;
        this.insertCols = null;
        return this;
    }

    public MySQLMergeBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "You have to specify at least one col to insert");
        
        this.query.append('(').append(this.database.prepareFieldName(cols[0]));
        int i;
        for (i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        this.query.append(") VALUES (?");
        for (i = 0; i < cols.length; ++i)
        {
            this.query.append(",?");
        }
        this.query.append(')');
        
        this.insertCols = cols;
        return this;
    }

    public MySQLMergeBuilder updateCols(String... updateCols)
    {
        if (this.insertCols == null)
        {
            throw new IllegalStateException("No insert cols specified!");
        }
        Validate.notEmpty(updateCols, "You have to specify at least one col to update!");
        Validate.isTrue(this.insertCols.length >= updateCols.length, "More update cols than insert cols specified!");
        
        String col = this.database.prepareFieldName(updateCols[0]);
        this.query.append(" ON DUPLICATE KEY UPDATE ").append(col).append("=VALUES(").append(col).append(')');
        for (int i = 1; i < updateCols.length; ++i)
        {
            col = this.database.prepareFieldName(updateCols[i]);
            this.query.append(',').append(col).append(col).append("=VALUES(").append(col).append(')');
        }
        
        this.updateColsSpecified = true;
        return this;
    }

    public MySQLQueryBuilder end()
    {
        Validate.isTrue(this.updateColsSpecified, "You have to specify which cols to update!");
        this.insertCols = null;
        
        this.parent.query.append(this.query.toString());
        this.query = null;
        return this.parent;
    }
}

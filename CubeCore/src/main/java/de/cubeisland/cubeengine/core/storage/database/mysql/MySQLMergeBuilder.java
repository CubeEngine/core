package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.MergeBuilder;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Phillip Schichtel
 */
public class MySQLMergeBuilder extends MySQLBuilderBase implements MergeBuilder
{
    private boolean updateColsSpecified;
    private String[] insertCols;
    
    protected MySQLMergeBuilder(MySQLQueryBuilder buider)
    {
        super(buider);
    }

    public MergeBuilder into(String table)
    {
        this.query = new StringBuilder("INSERT INTO ").append(this.prepareName(table)).append(" ");
        this.updateColsSpecified = false;
        this.insertCols = null;
        return this;
    }

    public MergeBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "You have to specify at least one col to insert");
        
        this.query.append('(').append(this.prepareColName(cols[0]));
        int i;
        for (i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.prepareColName(cols[i]));
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

    public MergeBuilder updateCols(String... updateCols)
    {
        if (this.insertCols == null)
        {
            throw new IllegalStateException("No insert cols specified!");
        }
        Validate.notEmpty(updateCols, "You have to specify at least one col to update!");
        Validate.isTrue(this.insertCols.length >= updateCols.length, "More update cols than insert cols specified!");
        
        String col = this.prepareColName(updateCols[0]);
        this.query.append(" ON DUPLICATE KEY UPDATE ").append(col).append("=VALUES(").append(col).append(')');
        for (int i = 1; i < updateCols.length; ++i)
        {
            col = this.prepareColName(updateCols[i]);
            this.query.append(',').append(col).append(col).append("=VALUES(").append(col).append(')');
        }
        
        this.updateColsSpecified = true;
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        Validate.isTrue(this.updateColsSpecified, "You have tp specify which cols to update!");
        this.insertCols = null;
        
        return super.end();
    }
}

package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.UpdateBuilder;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLUpdateBuilder extends MySQLOrderedBuilder<UpdateBuilder> implements UpdateBuilder
{
    private int setCounter;
    
    public MySQLUpdateBuilder(MySQLQueryBuilder querybuilder)
    {
        this.queryBuilder = querybuilder;
        this.database = querybuilder.database;
        this.query = new StringBuilder();
    }
    

    public UpdateBuilder tables(String... tables)
    {
        this.setCounter = 0;
        this.query.append("UPDATE ");
        this.query.append(StringUtils.implode(",", this.database.quote(tables))).append(" ");
        return this;
    }

    public UpdateBuilder beginSets()
    {
        this.query.append("SET ");
        return this;
    }

    public UpdateBuilder set(String col)
    {
        if (this.setCounter > 0)
        {
            this.query.append(",");
        }
        ++this.setCounter;
        
        this.query.append(this.database.quote(col)).append("=?");
        return this;
    }

    public UpdateBuilder endSets()
    {
        return this;
    }

    public UpdateBuilder values(String... cols)
    {
        cols = this.database.quote(cols);
        for (int i = 0 ; i< cols.length ; ++i)
        {
            cols[i]=cols[i]+"=VALUES("+cols[i]+")";
        }
        this.query.append(StringUtils.implode(",", cols));
        return this;
    }
}
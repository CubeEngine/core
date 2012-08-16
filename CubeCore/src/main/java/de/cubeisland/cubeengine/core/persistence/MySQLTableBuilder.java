package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLTableBuilder implements TableBuilder
{
    private StringBuilder table;
    private Database database;
        
    
    public MySQLTableBuilder(Database database, String tablename)
    {
        table = new StringBuilder();
        this.database = database;
        table.append(this.quote(tablename)).append("(");
    }
    
    private String quote(String s)
    {
        return this.database.quote(s);
    }
    
    public TableBuilder attribute(String name, AttrType type)
    {
        table.append(this.quote(name)).append(" ").append(type.name());
        return this;
    }

    public TableBuilder attribute(String name, AttrType type, int length)
    {
        table.append(this.quote(name)).append(" ").append(type.name()).append("(").append(length).append(")");
        return this;
    }

    public TableBuilder next()
    {
        table.append(",");
        return this;
    }

    public TableBuilder primaryKey(String key)
    {
        table.append("PRIMARY KEY (").append(this.quote(key)).append(")");
        return this;
    }

    public TableBuilder foreignKey(String key)
    {
        table.append("FOREIGN KEY (").append(this.quote(key)).append(")");
        return this;
    }

    public TableBuilder references(String otherTable, String key)
    {
        table.append("REFERENCES ").append(this.quote(otherTable)).append("(").append(this.quote(key)).append(")");
        return this;
    }

    public TableBuilder unsigned()
    {
        table.append(" unsigned");
        return this;
    }
    
    public TableBuilder notNull()
    {
        table.append(" NOT NULL");
        return this;
    }

    public TableBuilder nulL()
    {
        table.append(" NULL");
        return this;
    }

    public TableBuilder autoincrement()
    {
        table.append(" AUTO_INCREMENT");
        return this;
    }
    
    public String toString()
    {
        this.table.append(")");
        return this.table.toString();
    }
}

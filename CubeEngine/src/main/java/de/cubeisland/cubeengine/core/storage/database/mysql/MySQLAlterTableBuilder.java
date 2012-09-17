package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.AlterTableBuilder;

/**
 *
 * @author Anselm Brehme
 */
class MySQLAlterTableBuilder extends MySQLComponentBuilder<AlterTableBuilder> implements AlterTableBuilder
{

    public MySQLAlterTableBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }
    
    @Override
    public AlterTableBuilder alterTable(String table)
    {
        this.query = new StringBuilder("ALTER TABLE ").append(this.database.prepareName(table)).append(' ');
        return this;
    }
    
    @Override
    public AlterTableBuilder add(String field, AttrType type)
    {
        this.query.append("ADD ").append(this.database.prepareFieldName(field)).append(" ").append(type.getType());
        return this;       
    }

    @Override
    public AlterTableBuilder drop(String field)
    {
        this.query.append("DROP COLUMN ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder modify(String field, AttrType type)
    {
        this.query.append("ALTER COLUMN ").append(this.database.prepareFieldName(field)).append(" ").append(type.getType());
        return this;
    }

    
    
}

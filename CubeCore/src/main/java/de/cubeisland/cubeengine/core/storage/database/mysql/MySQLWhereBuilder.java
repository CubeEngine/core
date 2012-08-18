package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.WhereBuilder;

/**
 *
 * @author Phillip Schichtel
 */
public class MySQLWhereBuilder<T extends MySQLConditionalBuilder> extends MySQLCompareBuilder implements WhereBuilder
{
    public MySQLWhereBuilder(T parent)
    {
        super(parent);
        this.query.append(" WHERE ");
    }

    public T endWhere()
    {
        this.parent.query.append(this.query);
        return (T)this.parent;
    }
}
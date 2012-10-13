package de.cubeisland.cubeengine.core.storage.database.querybuilder;

import de.cubeisland.cubeengine.core.storage.database.AttrType;

/**
 *
 * @author Anselm Brehme
 */
public interface AlterTableBuilder extends ComponentBuilder<AlterTableBuilder>
{
    public AlterTableBuilder alterTable(String table);

    public AlterTableBuilder add(String field, AttrType type);

    public AlterTableBuilder drop(String field);

    public AlterTableBuilder modify(String field, AttrType type);
}

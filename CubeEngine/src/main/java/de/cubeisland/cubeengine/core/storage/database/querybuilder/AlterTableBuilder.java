package de.cubeisland.cubeengine.core.storage.database.querybuilder;

import de.cubeisland.cubeengine.core.storage.database.AttrType;

public interface AlterTableBuilder extends ComponentBuilder<AlterTableBuilder>
{
    /**
     * Alter the table.
     * 
     * @param table
     * @return fluent interface
     */
    public AlterTableBuilder alterTable(String table);

    /**
     * Add a field to the table
     * 
     * @param field
     * @param type
     * @return fluent interface
     */
    public AlterTableBuilder add(String field, AttrType type);
    
    public AlterTableBuilder addUnique(String field);

    /**
     * Drop a field from the table
     * 
     * @param field
     * @return fluent interface
     */
    public AlterTableBuilder drop(String field);

    /**
     * Modify a field from the table
     * 
     * @param field
     * @param type
     * @return fluent interface
     */
    public AlterTableBuilder modify(String field, AttrType type);
}

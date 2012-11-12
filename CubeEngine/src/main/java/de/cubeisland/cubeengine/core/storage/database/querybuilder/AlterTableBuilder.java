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
    
    public AlterTableBuilder addUniques(String... fields);
    public AlterTableBuilder addCheck();
    public AlterTableBuilder setDefault(String field);
    public AlterTableBuilder addForeignKey(String field, String foreignTable, String foreignField);
    public AlterTableBuilder setPrimary(String field);
    public AlterTableBuilder dropUnique(String field);
    public AlterTableBuilder dropPrimary();
    public AlterTableBuilder dropCheck(String field);
    public AlterTableBuilder dropDefault(String field);
    public AlterTableBuilder dropIndex(String field);
    public AlterTableBuilder dropForeignKey(String field);
    
    
    public AlterTableBuilder defaultValue(String value);
    public AlterTableBuilder defaultValue();
    
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

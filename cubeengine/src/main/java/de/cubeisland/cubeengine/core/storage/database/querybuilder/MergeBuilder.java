package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface MergeBuilder extends ComponentBuilder<MergeBuilder>
{
    /**
     * Adds the tables to merge into.
     *
     * @param table
     * @return fluent interface
     */
    public MergeBuilder into(String table);

    /**
     * Adds the cols to merge into
     *
     * @param cols
     * @return fluent interface
     */
    public MergeBuilder cols(String... cols);

    /**
     * Adds the cols to update when merging.
     *
     * @param cols
     * @return fluent interface
     */
    public MergeBuilder updateCols(String... cols);
}

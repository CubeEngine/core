package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface MergeBuilder extends ComponentBuilder<MergeBuilder>
{
    public MergeBuilder into(String table);

    public MergeBuilder cols(String... cols);

    public MergeBuilder updateCols(String... cols);
}
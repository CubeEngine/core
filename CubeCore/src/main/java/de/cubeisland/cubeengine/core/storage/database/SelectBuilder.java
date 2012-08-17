package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface SelectBuilder extends ConditionalBuilder<SelectBuilder>
{
    public SelectBuilder cols(String... cols);
    public SelectBuilder from(String... tables);
}

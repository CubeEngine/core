package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface DeleteBuilder extends ConditionalBuilder<DeleteBuilder>
{
    public DeleteBuilder from(String... tables);
}
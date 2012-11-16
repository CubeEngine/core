package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface DeleteBuilder extends ConditionalBuilder<DeleteBuilder>
{
    /**
     * Adds deletion from tables
     *
     * @param tables
     * @return fluent interface
     */
    public DeleteBuilder from(String... tables);
}

package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface DeleteBuilder<This extends DeleteBuilder,QueryBuilder> extends ComponentBuilder<This,QueryBuilder>
{
    public This from(String... tables);
}

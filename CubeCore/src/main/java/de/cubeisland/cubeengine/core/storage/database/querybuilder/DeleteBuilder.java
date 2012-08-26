package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface DeleteBuilder<This extends DeleteBuilder,Parent extends QueryBuilder> extends ComponentBuilder<This,Parent>
{
    public This from(String... tables);
}

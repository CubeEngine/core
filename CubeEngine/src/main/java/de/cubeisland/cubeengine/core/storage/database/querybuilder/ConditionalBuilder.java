package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface ConditionalBuilder<This extends ConditionalBuilder> extends ComponentBuilder<This>
{
    public This orderBy(String... cols);

    public This limit(int n);

    public This offset(int n);

    public This where();
}
package de.cubeisland.cubeengine.core.filesystem;

/**
 * Holds a resource.
 */
public interface Resource
{
    /**
     * Returns the path where the resource is located in e.g. a jar
     *
     * @return the path toth e resource
     */
    public String getSource();

    /**
     * Returns the target path in te file system
     *
     * @return a file path
     */
    public String getTarget();
}

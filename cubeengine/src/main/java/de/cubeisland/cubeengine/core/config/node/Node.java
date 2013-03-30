package de.cubeisland.cubeengine.core.config.node;

/**
 * A config Node
 */
public abstract class Node
{

    private ParentNode parentNode;

    public ParentNode getParentNode()
    {
        return parentNode;
    }

    public void setParentNode(ParentNode parentNode)
    {
        this.parentNode = parentNode;
    }

    public String getPath(String pathSeparator)
    {
        if (this.getParentNode() == null)
        {
            return null;
        }
        return this.getParentNode().getPath(this, "", pathSeparator);
    }

    public abstract String unwrap();

    /**
     * Returns the last subKey of this path
     *
     * <p>Example: first.second.third -> third
     *
     * @param path the path
     * @param pathSeparator the pathSeparator
     * @return the last subKey
     */
    public static String getSubKey(String path, String pathSeparator)
    {
        if (path.contains(pathSeparator))
            return path.substring(path.lastIndexOf(pathSeparator) + 1);
        else
            return path;
    }

    /**
     * Returns the subPath of this path
     *
     * <p>Example: first.second.third -> second.third
     *
     * @param path the path
     * @param pathSeparator the pathSeparator
     * @return the subPath
     */
    public static String getSubPath(String path, String pathSeparator)
    {
        if (path.contains(pathSeparator))
            return path.substring(path.indexOf(pathSeparator) + 1);
        else
            return path;
    }

    /**
     * Returns the base path of this path
     * <p>Example: first.second.third -> first
     *
     * @param path the path
     * @param pathSeparator the pathSeparator
     * @return the basePath
     */
    public static String getBasePath(String path, String pathSeparator)
    {
        if (path.contains(pathSeparator))
            return path.substring(0, path.indexOf(pathSeparator));
        else
            return path;
    }
}

/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.config.node;

/**
 * A config Node
 */
public abstract class Node
{
    // TODO override toString()
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

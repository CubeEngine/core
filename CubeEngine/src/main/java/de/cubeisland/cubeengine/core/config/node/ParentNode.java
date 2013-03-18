package de.cubeisland.cubeengine.core.config.node;

import de.cubeisland.cubeengine.core.CubeEngine;

import java.util.logging.Level;

/**
 * A Node that can be a parent of another Node
 */
public abstract class ParentNode extends Node
{

    /**
     * Sets this node for given path
     *
     * @param path the path
     * @param pathSeparator the pathSeparator
     * @param node the node to set
     * @return the previously mapped Node or null if not set
     */
    public Node setNodeAt(String path, String pathSeparator, Node node)
    {
        Node parentNode = this.getNodeAt(path, pathSeparator).getParentNode();
        if (parentNode instanceof ParentNode)
        {
            return ((ParentNode)parentNode).setExactNode(this.getSubKey(path, pathSeparator), node);
        }
        throw new UnsupportedOperationException("Not supported!");
    }

    /**
     * Sets this node for given direct key (without pathseparators).
     * <p>The key will be lowercased!
     *
     * @param key the key
     * @param node the node to set
     * @return the previously mapped Node or null if not set
     */
    protected abstract Node setExactNode(String key, Node node);

    /**
     * Gets the Node under the specified path,
     * using pathSeparator to separate
     * <p>Will create new Nodes if not found!
     *
     * @param path the path
     * @param pathSeparator the path-separator
     * @return the Node at given path
     */
    public Node getNodeAt(String path, String pathSeparator)
    {
        if (path.contains(pathSeparator))
        {
            String basePath = this.getBasePath(path, pathSeparator);
            String subPath = this.getSubPath(path, pathSeparator);
            Node baseNode = this.getNodeAt(basePath, pathSeparator);
            if (baseNode instanceof NullNode) // node not found -> create new node
            {
                if (subPath.startsWith("[")) //  baseNode is a List!
                {
                    baseNode = ListNode.emptyList();
                }
                else
                {
                    baseNode = MapNode.emptyMap();
                }
                baseNode.setParentNode(this);
                this.setExactNode(basePath, baseNode);
            }
            else if (!(baseNode instanceof ParentNode))
            {
                throw new IllegalArgumentException("Could not resolve path (" + path + ") for " + baseNode);
            }
            return ((ParentNode)baseNode).getNodeAt(subPath, pathSeparator);
        }
        else
        {
            Node node = this.getExactNode(path);
            if (node == null)
            {
                node = NullNode.emptyNode();
                node.setParentNode(this);
                this.setExactNode(path, node);
            }
            else if (node instanceof NullNode)
            {
                node.setParentNode(this);
                this.setExactNode(path, node);
            }
            return node;
        }
    }

    protected abstract String getPath(Node node, String path, String pathSeparator);

    /**
     * Returns the Node for given direct key (without pathseparators).
     * <p>The key will be lowercased!
     *
     * @param key the key
     * @return the matched Node or null
     */
    public abstract Node getExactNode(String key);

    /**
     * Removes the Node for given path
     *
     * @param path the path
     * @param pathSeparator the path-separator
     * @return the previously mapped Node or null if not set
     */
    public Node removeNode(String path, String pathSeparator)
    {
        Node nodeToRemove = this.getNodeAt(path, pathSeparator);
        return nodeToRemove.getParentNode().removeExactNode(this.getSubKey(path, pathSeparator));
    }

    /**
     * Removes the node for given direct key (without pathseparators).
     * <p>The key will be lowercased!
     *
     * @param key the key
     * @return the previously mapped Node or null if not set
     */
    protected abstract Node removeExactNode(String key);

    public abstract boolean isEmpty();

    @Override
    public String unwrap()
    {
        CubeEngine.getLogger().log(Level.WARNING,"Unexpected parent-node data! Is the config up to date?");
        return null;
    }
}

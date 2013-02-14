package de.cubeisland.cubeengine.core.config.node;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import gnu.trove.map.hash.THashMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapNode extends ParentNode
{

    private LinkedHashMap<String, Node> mappedNodes = new LinkedHashMap<String, Node>();
    private THashMap<String, String> keys = new THashMap<String, String>(); // LowerCase trimmed -> Original
    private LinkedHashMap<Node, String> reverseMappedNodes = new LinkedHashMap<Node, String>();

    /**
     * Creates a MapNode with given map as values.
     * MapKeys of the MapNode will always be a lowercased and trimmed String.
     *
     * @param map the map to convert into Nodes
     */
    public MapNode(Map<Object, Object> map)
    {
        if (map != null)
        {
            for (Map.Entry<Object, Object> entry : map.entrySet())
            {

                Node node = Convert.wrapIntoNode(entry.getValue());
                node.setParentNode(this);
                this.setExactNode(entry.getKey().toString(), node);
            }
        }
    }

    private MapNode()
    {}

    /**
     * Returns an empty MapNode
     * <p>This is equivalueent to {@link #MapNode(Map)} with null parameter
     *
     * @return an empty MapNode
     */
    public static MapNode emptyMap()
    {
        return new MapNode();
    }

    @Override
    public Node getExactNode(String key)
    {
        return this.mappedNodes.get(key.trim().toLowerCase());
    }

    @Override
    protected Node setExactNode(String key, Node node)
    {
        String loweredKey = key.trim().toLowerCase();
        if (loweredKey.isEmpty())
        {
            CubeEngine.getLogger().warning("Empty key-mapping!");
        }
        this.keys.put(loweredKey, key);
        node.setParentNode(this);
        this.reverseMappedNodes.put(node, loweredKey);
        return this.mappedNodes.put(loweredKey, node);
    }

    @Override
    protected Node removeExactNode(String key)
    {
        Node node = this.mappedNodes.get(key);
        if (node instanceof NullNode)
        {
            this.reverseMappedNodes.remove(node);
            this.mappedNodes.remove(key);
            return null;
        }
        return node;
    }

    public Node setNode(StringNode keyNode, Node node)
    {
        return this.setExactNode(keyNode.getValue(), node);
    }

    public String getOriginalKey(String lowerCasedKey)
    {
        return this.keys.get(lowerCasedKey);
    }

    public LinkedHashMap<String, Node> getMappedNodes()
    {
        return mappedNodes;
    }

    @Override
    public boolean isEmpty()
    {
        return this.mappedNodes.isEmpty();
    }

    @Override
    protected String getPath(Node node, String path, String pathSeparator)
    {
        String key = this.reverseMappedNodes.get(node);
        if (key == null)
        {
            throw new IllegalArgumentException("Parented node not in map!");
        }
        if (path.isEmpty())
        {
            path = key;
        }
        else
        {
            path = key + pathSeparator + path;
        }
        if (this.getParentNode() != null)
        {
            return this.getParentNode().getPath(this, path, pathSeparator);
        }
        return path;
    }
}

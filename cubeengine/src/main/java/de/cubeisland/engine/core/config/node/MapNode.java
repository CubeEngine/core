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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.util.convert.Convert;

import gnu.trove.map.hash.THashMap;

public class MapNode extends ParentNode
{

    private LinkedHashMap<String, Node> mappedNodes = new LinkedHashMap<>();
    private THashMap<String, String> keys = new THashMap<>(); // LowerCase trimmed -> Original
    private LinkedHashMap<Node, String> reverseMappedNodes = new LinkedHashMap<>();

    /**
     * Creates a MapNode with given map as values.
     * MapKeys of the MapNode will always be a lowercased and trimmed String.
     *
     * @param map the map to convert into Nodes
     */
    public MapNode(Map<?, ?> map)
    {
        if (map != null)
        {
            for (Map.Entry<?, ?> entry : map.entrySet())
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
        Node node = this.mappedNodes.get(key.trim().toLowerCase());
        if (node == null)
        {
            node = NullNode.emptyNode();
        }
        return node;
    }

    @Override
    protected Node setExactNode(String key, Node node)
    {
        String loweredKey = key.trim().toLowerCase();
        if (loweredKey.isEmpty())
        {
            CubeEngine.getLog().warn("Empty key-mapping!");
        }
        this.keys.put(loweredKey, key);
        node.setParentNode(this);
        this.reverseMappedNodes.put(node, loweredKey);
        return this.mappedNodes.put(loweredKey, node);
    }

    @Override
    protected Node removeExactNode(String key)
    {
        Node node = this.mappedNodes.remove(key);
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


    @Override
    public void cleanUpEmptyNodes()
    {
        Set<String> nodesToRemove = new HashSet<>();
        for (String key : this.mappedNodes.keySet())
        {
            if (this.mappedNodes.get(key) instanceof ParentNode)
            {
                ((ParentNode) this.mappedNodes.get(key)).cleanUpEmptyNodes();
                if (((ParentNode) this.mappedNodes.get(key)).isEmpty())
                {
                    nodesToRemove.add(key);
                }
            }
        }
        for (String key : nodesToRemove)
        {
            this.mappedNodes.remove(key);
        }
    }
}

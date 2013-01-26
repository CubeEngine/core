package de.cubeisland.cubeengine.core.config.node;


import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import gnu.trove.map.hash.THashMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapNode extends Node {

    private LinkedHashMap<String,Node> mappedNodes = new LinkedHashMap<String, Node>();
    private THashMap<String, String> keys = new THashMap<String, String>(); // LowerCase trimmed -> Original

    public MapNode(Map<Object,Object> map) {
        if (map != null)
        {
            for (Map.Entry<Object,Object> entry : map.entrySet())
            {
                String key = entry.getKey().toString().trim().toLowerCase();
                this.keys.put(key,entry.getKey().toString());
                if (this.mappedNodes.containsKey(key))
                {
                    CubeEngine.getLogger().warning("Duplicate keymapping for: "+key);
                }
                if (key.isEmpty())
                {
                    CubeEngine.getLogger().warning("Emtpy keymapping!");
                }
                Node node = Convert.toNode(entry.getValue());
                node.setParentNode(this);
                this.mappedNodes.put(key, node);
            }
        }
    }

    private MapNode() {
    }

    public static Node emptyMap() {
        return new MapNode();
    }
}

package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.config.node.ListNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.roles.Roles;

import java.util.LinkedHashMap;
import java.util.Map;

public class PermissionTreeConverter implements Converter<PermissionTree>
{
    private final Roles module;

    public PermissionTreeConverter(Roles module)
    {
        this.module = module;
    }

    @Override
    public Node toNode(PermissionTree permTree) throws ConversionException
    {
        Map<String, Object> easyMap = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Boolean> entry : permTree.getPermissions().entrySet())
        {
            this.easyMapValue(easyMap, entry.getKey(), entry.getValue());
        }
        return this.organizeTree(easyMap);
    }

    private ListNode organizeTree(Map<String, Object> baseMap)
    {
        ListNode result = ListNode.emptyList();
        for (Map.Entry<String, Object> entry : baseMap.entrySet())
        {
            if (entry.getKey().startsWith(" "))
            {
                result.addNode(StringNode.of((((Boolean)entry.getValue() ? "" : "-") + entry.getKey().trim())));
            }
            else
            {
                Map<String, Object> baseValueMap = (Map<String, Object>)entry.getValue();
                ListNode values = this.organizeTree(baseValueMap);
                MapNode subMap = MapNode.emptyMap();
                int size = baseValueMap.size();
                if (size == 1)
                {
                    if (values.getListedNodes().size() == 1)
                    {
                        for (Node subValue : values.getListedNodes())
                        {
                            if (subValue instanceof StringNode)
                            {
                                if (subValue.unwrap().startsWith("-"))
                                {
                                    result.addNode(StringNode.of("-" + entry.getKey() + "." + subValue.unwrap().substring(1)));
                                }
                                else
                                {
                                    result.addNode(StringNode.of(entry.getKey() + "." + subValue.unwrap()));
                                }
                            }
                            else
                            {
                                String subKey = ((MapNode)subValue).getMappedNodes().keySet().iterator().next();
                                subMap.setNode(StringNode.of(entry.getKey() + "." + subKey), ((MapNode)subValue).getExactNode(subKey));
                                result.addNode(subMap);
                            }
                        }
                        continue;
                    }
                }
                subMap.setNode(StringNode.of(entry.getKey()), values);
                result.addNode(subMap);
            }
        }
        return result;
    }

    private void easyMapValue(Map<String, Object> map, String path, boolean value)
    {
        String base = this.getBasePath(path);
        if (base.isEmpty())
        {
            map.put(" " + path, value);
            return;
        }
        Map<String, Object> subMap = (Map<String, Object>)map.get(base); // this should never give an exception if it does something went wrong!
        if (subMap == null) // sub map not yet existant?
        {
            subMap = new LinkedHashMap<String, Object>();
            map.put(base, subMap);
        }
        this.easyMapValue(subMap, this.getSubPath(path, base), value); // create map for subPath
    }

    private String getBasePath(String path)
    {
        if (path.contains("."))
        {
            return path.substring(0, path.indexOf("."));
        }
        return "";
    }

    private String getSubPath(String path, String basePath)
    {
        if (path.contains(basePath))
        {
            return path.substring(path.indexOf(basePath) + basePath.length() + 1);
        }
        return null;
    }

    @Override
    public PermissionTree fromNode(Node node) throws ConversionException
    {
        PermissionTree permTree = new PermissionTree();
        if (node instanceof ListNode)
        {
            this.loadFromList(permTree, (ListNode)node, "");
        }
        else
        {
            this.module.getLogger().warning("Deleted Invalid PermissionTree!");
        }
        return permTree;
    }

    private void loadFromList(PermissionTree permTree, ListNode list, String path)
    {
        for (Node value : list.getListedNodes())
        {
            if (value instanceof StringNode)
            {
                String permissionString = value.unwrap();
                boolean isSet = true;
                if (permissionString.startsWith("!") || permissionString.startsWith("^") || permissionString.startsWith("-"))
                {
                    permissionString = permissionString.substring(1);
                    isSet = false;
                }
                if (!path.isEmpty())
                {
                    permissionString = path + "." + permissionString;
                }
                permTree.addPermission(permissionString, isSet);
            }
            else if (value instanceof MapNode)
            {
                this.loadFromMap(permTree, (MapNode)value, path);
            }
            else
            {
                throw new IllegalArgumentException("Invalid PermissionTree!");
            }
        }
    }

    private void loadFromMap(PermissionTree permTree, MapNode map, String path)
    {
        for (Map.Entry<String, Node> entry : map.getMappedNodes().entrySet())
        {
            if (entry.getValue() instanceof ListNode)
            {
                this.loadFromList(permTree, (ListNode)entry.getValue(), path.isEmpty() ? entry.getKey() : (path + "." + entry.getKey()));
            }
            else
            {
                throw new IllegalArgumentException("Invalid PermissionTree!");
            }
        }
    }
}

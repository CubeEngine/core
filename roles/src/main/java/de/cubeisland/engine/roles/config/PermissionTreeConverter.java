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
package de.cubeisland.engine.roles.config;

import java.util.LinkedHashMap;
import java.util.Map;

import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.ListNode;
import de.cubeisland.engine.reflect.node.MapNode;
import de.cubeisland.engine.reflect.node.Node;
import de.cubeisland.engine.reflect.node.StringNode;
import de.cubeisland.engine.roles.Roles;

public class PermissionTreeConverter implements Converter<PermissionTree>
{
    private final Roles module;

    public PermissionTreeConverter(Roles module)
    {
        this.module = module;
    }

    @Override
    public Node toNode(PermissionTree permTree, ConverterManager manager) throws ConversionException
    {
        Map<String, Object> easyMap = new LinkedHashMap<>();
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
                @SuppressWarnings("unchecked")
                Map<String, Object> baseValueMap = (Map<String, Object>)entry.getValue();
                ListNode values = this.organizeTree(baseValueMap);
                MapNode subMap = MapNode.emptyMap();
                int size = baseValueMap.size();
                if (size == 1)
                {
                    if (values.getValue().size() == 1)
                    {
                        for (Node subValue : values.getValue())
                        {
                            if (subValue instanceof StringNode)
                            {
                                if (subValue.asText().startsWith("-"))
                                {
                                    result.addNode(StringNode.of("-" + entry.getKey() + "." + subValue.asText().substring(1)));
                                }
                                else
                                {
                                    result.addNode(StringNode.of(entry.getKey() + "." + subValue.asText()));
                                }
                            }
                            else
                            {
                                String subKey = ((MapNode)subValue).getValue().keySet().iterator().next();
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

    private void easyMapValue(Map<String, Object> map, String path, Boolean value)
    {
        String base = this.getBasePath(path);
        if (base.isEmpty())
        {
            map.put(" " + path, value);
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> subMap = (Map<String, Object>)map.get(base); // this should never give an exception if it does something went wrong!
        if (subMap == null) // sub map not yet existant?
        {
            subMap = new LinkedHashMap<>();
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
    public PermissionTree fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        PermissionTree permTree = new PermissionTree();
        if (node instanceof ListNode)
        {
            this.loadFromList(permTree, (ListNode)node, "");
        }
        else
        {
            this.module.getLog().warn("Deleted Invalid PermissionTree!");
        }
        return permTree;
    }

    private void loadFromList(PermissionTree permTree, ListNode list, String path)
    {
        for (Node value : list.getValue())
        {
            if (value instanceof StringNode)
            {
                String permissionString = value.asText();
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
        for (Map.Entry<String, Node> entry : map.getValue().entrySet())
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

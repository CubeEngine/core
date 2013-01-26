package de.cubeisland.cubeengine.core.config.node;


import de.cubeisland.cubeengine.core.util.convert.Convert;

import java.util.LinkedList;

public class ListNode  extends Node{

    private LinkedList<Node> listedNodes = new LinkedList<Node>();
    public ListNode(Iterable list) {
        if (list == null)
        {
            for (Object object : list)
            {
                Node node = Convert.toNode(object);
                node.setParentNode(this);
                listedNodes.add(node);
            }
        }
    }

    public ListNode(Object[] array) {
        if (array == null)
        {
            for (Object object : array)
            {
                Node node = Convert.toNode(object);
                node.setParentNode(this);
                listedNodes.add(node);
            }
        }
    }
}

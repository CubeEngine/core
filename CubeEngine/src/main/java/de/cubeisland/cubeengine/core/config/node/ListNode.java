package de.cubeisland.cubeengine.core.config.node;

import de.cubeisland.cubeengine.core.util.convert.Convert;

import java.util.ArrayList;

public class ListNode extends ParentNode{

    private ArrayList<Node> listedNodes = new ArrayList<Node>();
    public ListNode(Iterable list) {
        if (list == null)
        {
            for (Object object : list)
            {
                Node node = Convert.wrapIntoNode(object);
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
                Node node = Convert.wrapIntoNode(object);
                node.setParentNode(this);
                listedNodes.add(node);
            }
        }
    }

    private ListNode() {
    }

    public ArrayList<Node> getListedNodes() {
        return listedNodes;
    }

    public static ListNode emptyList() {
        return new ListNode();
    }

    public void addNode(Node node) {
        this.listedNodes.add(node);
        node.setParentNode(this);
    }

    @Override
    protected Node setExactNode(String key, Node node) {
        if (key.startsWith("["))
        {
            try
            {
                int pos = Integer.valueOf(key.substring(1));
                node.setParentNode(this);
                return this.listedNodes.set(pos,node);
            }
            catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException("Cannot set Node! Could not parse ListPath", ex);
            }
            catch (IndexOutOfBoundsException ex)
            {
                throw new IllegalArgumentException("Cannot set Node! Out of Range!", ex);
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot set Node! ListPath has to start with [!");
        }
    }

    @Override
    protected Node getExactNode(String key) {
        if (key.startsWith("["))
        {
            try
            {
                int pos = Integer.valueOf(key.substring(1));
                return this.listedNodes.get(pos);
            }
            catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException("Cannot get Node! Could not parse ListPath", ex);
            }
            catch (IndexOutOfBoundsException ex)
            {
                throw new IllegalArgumentException("Cannot get Node! Out of Range!", ex);
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot get Node! ListPath has to start with [!");
        }
    }

    @Override
    protected Node removeExactNode(String key) {
        if (key.startsWith("["))
        {
            try
            {
                int pos = Integer.valueOf(key.substring(1));
                return this.listedNodes.remove(pos);
            }
            catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException("Cannot remove Node! Could not parse ListPath!", ex);
            }catch (IndexOutOfBoundsException ex)
            {
                throw new IllegalArgumentException("Cannot remove Node! Out of Range!", ex);
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot remove Node! ListPath has to start with [!");
        }
    }

    public Node setNode(IntNode keyNode, Node node) {
        return this.setExactNode("["+keyNode.getValue(),node);
    }

    @Override
    public boolean isEmpty() {
        return this.listedNodes.isEmpty();
    }

    @Override
    protected String getPath(Node node, String path, String pathSeparator) {
        int pos = this.listedNodes.indexOf(node);
        if (pos == -1)
        {
            throw new IllegalArgumentException("Parented node not in list!");
        }
        if (path.isEmpty())
        {
            path = "["+pos;
        }
        else
        {
            path = "["+pos+pathSeparator+path;
        }
        if (this.getParentNode() != null)
        {
           return this.getParentNode().getPath(this, path, pathSeparator);
        }
        return path;
    }
}

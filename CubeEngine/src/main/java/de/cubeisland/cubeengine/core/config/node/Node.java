package de.cubeisland.cubeengine.core.config.node;

/**
 * A config Node
 */
public abstract class Node {

    private Node parentNode;

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }
}

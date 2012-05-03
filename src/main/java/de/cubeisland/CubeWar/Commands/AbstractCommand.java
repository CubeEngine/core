package de.cubeisland.CubeWar.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * This class represents a command that can be attached to a BaseCommand
 *
 * @author Phillip Schichtel
 */
public abstract class AbstractCommand
{
    private final String[] labels;
    private final BaseCommand base;
    private final Permission permission;

    /**
     * Initializes this command with its name and base command
     *
     * @param label the name/label
     * @param base the base command
     */
    public AbstractCommand(BaseCommand base, String... labels)
    {
        if (labels.length < 1)
        {
            throw new IllegalStateException("A command needs at least one label!");
        }
        this.labels = labels;
        this.base = base;
        this.permission = new Permission(base.permissinBase + labels[0], PermissionDefault.OP);
    }

    /**
     * Returns the base command
     *
     * @return the base command
     */
    public final BaseCommand getBase()
    {
        return this.base;
    }

    /**
     * Returns the permission needed to run this command
     *
     * @return the permission
     */
    public final Permission getPermission()
    {
        return this.permission;
    }

    /**
     * Returns the label of this command
     *
     * @return the label
     */
    public final String getLabel()
    {
        return this.labels[0];
    }

    /**
     * Returns all labels
     *
     * @return the labels
     */
    public final String[] getLabels()
    {
        return this.labels;
    }

    /**
     * Returns the usage string of this command
     *
     * @return the usage string
     */
    public String getUsage()
    {
        return "/" + base.getLabel() + " " + getLabel();
    }

    /**
     * Returns a short description of this command
     *
     * @return the description
     */
    public abstract String getDescription();

    /**
     * This method executes this command
     *
     * @param sender a command sender
     * @param args the arguments
     * @return true if the command succeeded
     */
    public abstract boolean execute(CommandSender sender, CommandArgs args);
}

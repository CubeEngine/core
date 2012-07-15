package de.cubeisland.cubeengine.core.abstraction.implementations;

import de.cubeisland.cubeengine.core.abstraction.PluginDescription;
import java.util.List;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 *
 * @author CodeInfection
 */
public class BukkitPluginDescription implements PluginDescription
{
    private final PluginDescriptionFile pdf;

    public BukkitPluginDescription(PluginDescriptionFile pdf)
    {
        this.pdf = pdf;
    }

    public PluginDescriptionFile getHandle()
    {
        return this.pdf;
    }

    public String getName()
    {
        return this.pdf.getName();
    }

    public String getFullName()
    {
        return this.pdf.getFullName();
    }

    public String getVersion()
    {
        return this.pdf.getVersion();
    }

    public String getMain()
    {
        return this.pdf.getMain();
    }

    public List<String> getAuthors()
    {
        return this.pdf.getAuthors();
    }

    public String getDescription()
    {
        return this.pdf.getDescription();
    }

    public String getAuthor()
    {
        return this.getAuthor();
    }

    public String getWebsite()
    {
        return this.getWebsite();
    }

    public List<String> getDepends()
    {
        return this.getDepends();
    }

    public List<String> getSoftDepends()
    {
        return this.getSoftDepends();
    }
}

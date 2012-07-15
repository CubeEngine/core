package de.cubeisland.cubeengine.core.abstraction;

import java.util.List;

/**
 *
 * @author CodeInfection
 */
public interface PluginDescription
{
    public String getName();

    public String getFullName();

    public String getVersion();

    public String getMain();

    public String getAuthor();

    public List<String> getAuthors();

    public String getDescription();

    public String getWebsite();

    public List<String> getDepends();

    public List<String> getSoftDepends();
}

package de.cubeisland.cubeengine.core.persistence.filesystem;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

public class YamlRepresenter extends Representer
{
    public YamlRepresenter()
    {
        this.multiRepresenters.put(ConfigurationSection.class, new YamlRepresenter.RepresentConfigurationSection());
    }

    private class RepresentConfigurationSection extends RepresentMap
    {
        @Override
        public Node representData(Object data)
        {
            ConfigurationSection section = (ConfigurationSection)data;
            return super.representData(section.getValues());
        }
    }
}

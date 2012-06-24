package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

public class YamlConstructor extends SafeConstructor
{
    public YamlConstructor()
    {
        this.yamlConstructors.put(Tag.MAP, new YamlConstructor.ConstructCustomObject());
    }

    private class ConstructCustomObject extends SafeConstructor.ConstructYamlMap
    {
        @Override
        public Object construct(Node node)
        {
            Map<?, ?> raw = (Map<?, ?>) super.construct(node);

            Map<String, Object> typed = new LinkedHashMap<String, Object>(raw.size());
            for (Map.Entry<?, ?> entry : raw.entrySet())
            {
                typed.put(entry.getKey().toString(), entry.getValue());
            }
            return raw;
        }
    }
}

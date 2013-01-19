package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.module.Module;

public class SimpleRegistry implements ModuleRegistry
{
    private final Module module;
    private final Registry registry;

    public SimpleRegistry(Module module, Registry registry)
    {
        this.module = module;
        this.registry = registry;
    }

    @Override
    public String get(String key)
    {
        return this.registry.getValue(key, module);
    }

    @Override
    public void set(String key, String value)
    {
        this.registry.merge(module, key, value);
    }

    @Override
    public String remove(String key)
    {
        return this.registry.delete(module, key);
    }

    @Override
    public void clear()
    {
        this.registry.clear(module);
    }
}

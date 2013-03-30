package de.cubeisland.cubeengine.core.storage;

public interface ModuleRegistry
{
    public String get(String key);

    public void set(String key, String value);

    public String remove(String key);

    public void clear();
}

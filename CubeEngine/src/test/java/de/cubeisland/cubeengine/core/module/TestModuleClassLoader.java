package de.cubeisland.cubeengine.core.module;

import java.net.MalformedURLException;
import java.net.URL;

public class TestModuleClassLoader extends ModuleClassLoader
{
    public TestModuleClassLoader(ModuleLoader moduleLoader, URL jarURL, ModuleInfo info, ClassLoader parent) throws MalformedURLException
    {
        super(moduleLoader, jarURL, info, parent);
    }
}

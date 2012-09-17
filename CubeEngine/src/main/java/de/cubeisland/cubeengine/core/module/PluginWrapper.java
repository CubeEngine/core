package de.cubeisland.cubeengine.core.module;

import com.avaje.ebean.EbeanServer;
import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.Core;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

/**
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("This wraps the module in a Bukkit plugin")
public class PluginWrapper implements Plugin
{
    private final Module module;
    private final Plugin bukkitPlugin;

    public PluginWrapper(Core core, Module module)
    {
        this.module = module;
        this.bukkitPlugin = (Plugin)core;
    }

    @Override
    public File getDataFolder()
    {
        return this.module.getFolder();
    }

    @Override
    public PluginDescriptionFile getDescription()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    @Override
    public FileConfiguration getConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    @Override
    public InputStream getResource(String string)
    {
        return this.module.getResource(string);
    }

    @Override
    public void saveConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    @Override
    public void saveDefaultConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    @Override
    public void saveResource(String string, boolean bln)
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    @Override
    public void reloadConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    @Override
    public PluginLoader getPluginLoader()
    {
        return this.bukkitPlugin.getPluginLoader();
    }

    @Override
    public Server getServer()
    {
        return this.bukkitPlugin.getServer();
    }

    @Override
    public boolean isEnabled()
    {
        return this.module.isEnabled();
    }

    @Override
    public void onDisable()
    {
        this.module.onDisable();
    }

    @Override
    public void onLoad()
    {
        this.module.onLoad();
    }

    @Override
    public void onEnable()
    {
        this.module.onEnable();
    }

    @Override
    public boolean isNaggable()
    {
        return this.bukkitPlugin.isNaggable();
    }

    @Override
    public void setNaggable(boolean bln)
    {
        this.bukkitPlugin.setNaggable(bln);
    }

    @Override
    public EbeanServer getDatabase()
    {
        return this.bukkitPlugin.getDatabase();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String string, String string1)
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    @Override
    public Logger getLogger()
    {
        return this.module.getLogger();
    }

    @Override
    public String getName()
    {
        return this.module.getName();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings)
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }
}
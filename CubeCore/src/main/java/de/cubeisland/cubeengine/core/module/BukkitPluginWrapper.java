package de.cubeisland.cubeengine.core.module;

import com.avaje.ebean.EbeanServer;
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
public class BukkitPluginWrapper implements PluginWrapper, Plugin
{
    private final Module module;

    public BukkitPluginWrapper(Module module)
    {
        this.module = module;
    }

    public File getDataFolder()
    {
        return this.module.getFolder();
    }

    public PluginDescriptionFile getDescription()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    public FileConfiguration getConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    public InputStream getResource(String string)
    {
        return this.module.getResource(string);
    }

    public void saveConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    public void saveDefaultConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    public void saveResource(String string, boolean bln)
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    public void reloadConfig()
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    public PluginLoader getPluginLoader()
    {
        return this.module.getCore().getPluginLoader();
    }

    public Server getServer()
    {
        return this.module.getServer();
    }

    public boolean isEnabled()
    {
        return this.module.isEnabled();
    }

    public void onDisable()
    {
        this.module.onDisable();
    }

    public void onLoad()
    {
        this.module.onLoad();
    }

    public void onEnable()
    {
        this.module.onEnable();
    }

    public boolean isNaggable()
    {
        return this.module.getCore().isNaggable();
    }

    public void setNaggable(boolean bln)
    {
        this.module.getCore().setNaggable(bln);
    }

    public EbeanServer getDatabase()
    {
        return this.module.getCore().getDatabase();
    }

    public ChunkGenerator getDefaultWorldGenerator(String string, String string1)
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }

    public Logger getLogger()
    {
        return this.module.getLogger();
    }

    public String getName()
    {
        return ModuleLoader.CLASS_PREFIX + this.module.getName();
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings)
    {
        throw new UnsupportedOperationException("Unsupported operation!");
    }
}

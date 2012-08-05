package de.cubeisland.cubeengine.core.module;

import com.avaje.ebean.EbeanServer;
import de.cubeisland.cubeengine.BukkitDependend;
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

    public PluginWrapper(Module module)
    {
        this.module = module;
        this.bukkitPlugin = (Plugin)module.getCore();
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
        return this.bukkitPlugin.getPluginLoader();
    }

    public Server getServer()
    {
        return this.bukkitPlugin.getServer();
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
        return this.bukkitPlugin.isNaggable();
    }

    public void setNaggable(boolean bln)
    {
        this.bukkitPlugin.setNaggable(bln);
    }

    public EbeanServer getDatabase()
    {
        return this.bukkitPlugin.getDatabase();
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

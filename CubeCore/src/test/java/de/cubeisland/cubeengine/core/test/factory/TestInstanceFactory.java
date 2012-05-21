package de.cubeisland.cubeengine.core.test.factory;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.filesystem.CubeConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.test.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Test;
import org.mockito.Matchers;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockGateway;



/**
 *
 * @author Faithcaio
 */
public class TestInstanceFactory
{
    private CubeCore core;
    private Server mockServer;
    private CommandSender commandSender;
    
    public static final File pluginDirectory = new File("bin/test/server/plugins/coretest");
    public static final File configDirectory = new File("bin/test/server/plugins/coretest/CubeCore");
    public static final File worldsDirectory = new File("bin/test/server");
    
    @Test
    public void setUp()
    {
        //TODO Das hier zum laufen bringen...
        //CubeCore initialiesieren
        //FakeServer erstellen
        //OP-CommandSender erstellen
        
        core = PowerMockito.spy(new CubeCore());
        when(core.getDataFolder()).thenReturn(pluginDirectory);
        
        FileManager fileManager = PowerMockito.spy(new FileManager(core));
        
        CubeConfiguration config = mock(CubeConfiguration.class);
        FileConfiguration fileconfig = mock(FileConfiguration.class);

        doReturn(fileconfig).when(core).getConfig(); 
        doReturn(config).when(fileManager).getCoreConfig();
        doReturn(config).when(fileManager).getDatabaseConfig();
        //Config Files are now simulated ...
        
        //TODO PluginManager faken
        
        mockServer = mock(Server.class);
        PluginManager manager = mock(PluginManager.class);
        when(mockServer.getPluginManager()).thenReturn(manager);
        doReturn(mockServer).when(core.getServer());
        
        
        //when(core.getServer()).thenReturn(mockServer);
        
        // when(core.getDataFolder().getParentFile()).thenReturn(pluginDirectory);
        
        /* 
        pluginDirectory.mkdirs();
        Assert.assertTrue(pluginDirectory.exists());

        MockGateway.MOCK_STANDARD_METHODS = false;

        core = PowerMockito.spy(new CubeCore());
        
        FileManager fileManager = mock(FileManager.class);
        CubeConfiguration config = mock(CubeConfiguration.class);
        
        when(fileManager.getCoreConfig()).thenReturn(config);
        when(fileManager.getDatabaseConfig()).thenReturn(config);
        when(core.getFileManager()).thenReturn(fileManager);
        mockServer = mock(Server.class);
        when(core.getServer()).thenReturn(mockServer);
        
        // Let's let all files go to bin/test
        doReturn(pluginDirectory).when(core).getDataFolder();

* 
*/

        final Logger commandSenderLogger = Logger.getLogger("CommandSender");
        commandSenderLogger.setParent(Util.logger);
        commandSender = mock(CommandSender.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                commandSenderLogger.info(ChatColor.stripColor((String) invocation.getArguments()[0]));
                return null;
            }}).when(commandSender).sendMessage(anyString());
        when(commandSender.getServer()).thenReturn(mockServer);
        when(commandSender.getName()).thenReturn("MockCommandSender");
        when(commandSender.isPermissionSet(anyString())).thenReturn(true);
        when(commandSender.isPermissionSet(Matchers.isA(Permission.class))).thenReturn(true);
        when(commandSender.hasPermission(anyString())).thenReturn(true);
        when(commandSender.hasPermission(Matchers.isA(Permission.class))).thenReturn(true);
        when(commandSender.addAttachment(core)).thenReturn(null);
        when(commandSender.isOp()).thenReturn(true);

        
        core.onLoad();
        core.onEnable();
    }
    
    
    public CubeCore getCore() {
        return this.core;
    }

    public Server getServer() {
        return this.mockServer;
    }

    public CommandSender getCommandSender() {
        return commandSender;
    }
}

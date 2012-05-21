package de.cubeisland.cubeengine.core.test.factory;

import de.cubeisland.cubeengine.core.*;
import de.cubeisland.cubeengine.core.permission.Perm;
import de.cubeisland.cubeengine.core.persistence.filesystem.CubeConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.test.util.Util;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Matchers;
import static org.mockito.Matchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

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
        doReturn(manager).when(core).getPluginManager();
        doNothing().when(core).registerPermissions(Perm.values());

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

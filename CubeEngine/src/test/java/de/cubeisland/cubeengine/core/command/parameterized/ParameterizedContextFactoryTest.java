package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.TestCore;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.TestCommand;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.command.sender.TestConsoleSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Stack;

import static de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContextFactory.readString;
import static de.cubeisland.cubeengine.core.util.StringUtils.explode;

public class ParameterizedContextFactoryTest extends TestCase
{
    private Core core;
    private ModuleManager mm;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        this.core = new TestCore();
        this.mm = this.core.getModuleManager();
    }

    public void testReadString()
    {
        StringBuilder sb;
        int argsRead = -1;

        argsRead = readString(sb = new StringBuilder(), explode(" ", "'  '"), 0);
        assertEquals(3, argsRead);
        assertEquals("  ", sb.toString());

        argsRead = readString(sb = new StringBuilder(), explode(" ", "'I am text  '"), 0);
        assertEquals(5, argsRead);
        assertEquals("I am text  ", sb.toString());

        argsRead = readString(sb = new StringBuilder(), explode(" ", "'   I am text'"), 0);
        assertEquals(6, argsRead);
        assertEquals("   I am text", sb.toString());

        argsRead = readString(sb = new StringBuilder(), explode(" ", "    "), 3);
        assertEquals(1, argsRead);
        assertEquals("", sb.toString());

        argsRead = readString(sb = new StringBuilder(), explode(" ", "  ''  "), 2);
        assertEquals(1, argsRead);
        assertEquals("", sb.toString());
    }

    public void testContextFactory()
    {
        final ParameterizedContextFactory factory = new ParameterizedContextFactory(
            Arrays.asList(new CommandFlag("a", "all")),
            Arrays.asList(new CommandParameter("test", new String[]{}, String.class, false))
        );

        Stack<String> labels = new Stack<String>();
        labels.add("testCommand");
        CommandSender sender = new TestConsoleSender();
        Module module = this.mm.getModule("test");
        CubeCommand testCommand = new TestCommand(module, labels.get(0), "desscription", factory);
        ParameterizedContext ctx = factory.parse(testCommand, sender, labels, new String[] {"-a", "test", "\"value\""});

        assertEquals(ctx.hasFlag("a"), true);
        assertEquals(ctx.getParam("test"), "value");
    }
}

/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.command.parameterized;

import java.util.Stack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.TestCore;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.CubeContextFactory;
import de.cubeisland.engine.core.command.TestCommand;
import de.cubeisland.engine.core.command.sender.TestConsoleSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleManager;
import junit.framework.TestCase;

import static de.cubeisland.engine.core.command.ContextParser.readString;
import static de.cubeisland.engine.core.util.StringUtils.explode;

public class CubeContextFactoryTest extends TestCase
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
        int argsRead;

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
        CubeContextFactory factory = new CubeContextFactory();
        factory.addFlag(new CommandFlag("a", "all", null));
        factory.addNamed(new CommandParameter("test", "label", String.class, null));

        Stack<String> labels = new Stack<>();
        labels.add("testCommand");
        CommandSender sender = new TestConsoleSender(this.core);
        Module module = this.mm.getModule("test");
        CubeCommand testCommand = new TestCommand(module, labels.get(0), "description", factory);
        CubeContext ctx = factory.parse(testCommand, sender, labels, new String[] {
        "-a", "test", "\"value\""
        });
        ctx.getCommand().getContextFactory().readContext(ctx, sender.getLocale());

        assertEquals(ctx.hasFlag("a"), true);
        assertEquals(ctx.getArg("test"), "value");
    }
}

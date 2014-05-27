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
package de.cubeisland.engine.core.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class MacroProcessorTest extends TestCase
{
    public void testProcess() throws Exception
    {
        MacroProcessor processor = new MacroProcessor();

        Map<String, String> args = new HashMap<>();
        args.put("parameter1", "value1");
        args.put("PARAMETER2", "value2");

        assertEquals("test {normal textvalue1 or }value2", processor.process("test \\{normal text{parameter1} or }{PARAMETER2}", args));
    }
}

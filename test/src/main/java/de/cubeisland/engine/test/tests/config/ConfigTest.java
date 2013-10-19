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
package de.cubeisland.engine.test.tests.config;

import java.nio.file.Path;

import de.cubeisland.engine.test.tests.Test;

public class ConfigTest extends Test
{
    private final de.cubeisland.engine.test.Test module;
    protected TestConfig config;

    public ConfigTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable()
    {
        this.config = this.module.loadConfig(TestConfig.class);
        Path childConfig = module.getFolder().resolve("childConfig.yml");
        try
        {
            this.config.loadChild(childConfig);
        }
        catch (IllegalStateException ex)
        {
            module.getLog().info("{} does not exist.", childConfig);
        }
        setSuccess(true);
    }
}

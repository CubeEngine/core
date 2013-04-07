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
package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 * This TestConfig is used for testing the updating process of configurations.
 */
@Codec("yml")
//@Revision(2)
//@Updater(TestConfig2Updater.class)
public class TestConfig2 extends Configuration
{
    @Option("string2")
    @Comment("string1 on revision 1 will update to string2")
    public String string = "This will update!";
    /*
     public static class TestConfig2Updater implements ConfigurationUpdater
     {
     @Override
     public Map<String, Object> update(Map<String, Object> loadedConfig, int fromRevision)
     {
     if (fromRevision == 1)
     {
     loadedConfig.put("string2", loadedConfig.get("string1"));
     }
     return loadedConfig;
     }
     }*/
    //TODO rewrite test for updater
}

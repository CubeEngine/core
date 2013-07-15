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
package de.cubeisland.cubeengine.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashSet;

import de.cubeisland.cubeengine.core.config.codec.MultiConfigurationCodec;

/**
 * This Configuration can have child-configs.
 * A child config will ignore default-set data and instead use the data saved in the parent config
 * Any data set in the configuration file will stay in the file. Even if the data is equal to the parents data.
 */
public class MultiConfiguration<ConfigCodec extends MultiConfigurationCodec> extends Configuration<ConfigCodec>
{
    protected MultiConfiguration parent = null;

    public final void saveChild()
    {
        if (this.codec == null)
        {
            throw new IllegalStateException("A configuration cannot be saved without a valid codec!");
        }
        if (this.file == null)
        {
            throw new IllegalStateException("A configuration cannot be saved without a valid file!");
        }
        this.codec.saveChildConfig(this.parent, this, this.file);
        this.onSaved(this.file);
    }

    @SuppressWarnings("unchecked")
    public <T extends Configuration> T loadChild(File sourceFile) //and save
    {
        MultiConfiguration<ConfigCodec> childConfig;
        try
        {
            childConfig = (MultiConfiguration) this.configurationClass.newInstance();
            childConfig.inheritedFields = new HashSet<Field>();
            childConfig.setFile(sourceFile);
            childConfig.parent = this;
            try
            {
                FileInputStream is = new FileInputStream(sourceFile);
                childConfig.getCodec().loadChildConfig(childConfig, is);
            }
            catch (FileNotFoundException ignored) // not found load from parent / save child
            {
                childConfig.getCodec().loadChildConfig(childConfig, null);
            }
            childConfig.onLoaded(file);
            childConfig.saveChild();
            return (T)childConfig;
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Could not load ChildConfig!", ex);
        }
    }

    public MultiConfiguration getParent() {
        return parent;
    }

    private HashSet<Field> inheritedFields;
    public void addinheritedField(Field field)
    {
        this.inheritedFields.add(field);
    }

    public boolean isInheritedField(Field field) {
        return inheritedFields.contains(field);
    }
}

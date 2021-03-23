/*
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
package org.cubeengine.libcube.service.filesystem;

import org.cubeengine.libcube.ModuleManager;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * Manages all the configurations of the CubeEngine.
 */
public class FileManager
{
    private final ModuleManager mm;
    private final Path dataFolder;
    private final Path modulesFolder;

    private final Path languagePath;

    private final Path translationPath;

    public FileManager(ModuleManager moduleManager, Path dataFolder)
    {
        this.mm = moduleManager;
        this.dataFolder = dataFolder;
        this.modulesFolder = dataFolder.resolve("modules");

        try
        {
            FileAttribute<?>[] folderCreateAttributes;
            if (Files.getFileAttributeView(this.modulesFolder, PosixFileAttributeView.class) != null)
            {
                folderCreateAttributes = new FileAttribute[] {PosixFilePermissions.asFileAttribute(FileUtil.DEFAULT_FOLDER_PERMS)};
                Files.createDirectories(dataFolder);
                Files.setPosixFilePermissions(dataFolder, FileUtil.DEFAULT_FOLDER_PERMS);
            }
            else
            {
                folderCreateAttributes = new FileAttribute[0];
            }

            this.languagePath = Files.createDirectories(dataFolder.resolve("language"), folderCreateAttributes);
            this.translationPath = Files.createDirectories(dataFolder.resolve("translations"), folderCreateAttributes);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the data folder of the CubeEngine
     *
     * @return a file
     */
    public Path getDataPath()
    {
        return this.dataFolder;
    }

    /**
     * Returns the language directory
     *
     * @return the directory
     */
    public Path getLanguagePath()
    {
        return this.languagePath;
    }

    /**
     * Returns the translation override directory
     *
     * @return the directory
     */
    public Path getTranslationPath()
    {
        return translationPath;
    }

    public Path getModulePath(Class<?> clazz) {
        return this.mm.getPlugin(clazz)
                      .map(this::getModulePath)
                      .orElseThrow(() -> new IllegalStateException("Failed to find the underlying plugin!"));
    }

    public Path getModulePath(PluginContainer plugin) {
        Path modulePath = this.modulesFolder.resolve(mm.getModuleId(plugin));
        try
        {
            Files.createDirectories(modulePath);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        return modulePath;
    }
}

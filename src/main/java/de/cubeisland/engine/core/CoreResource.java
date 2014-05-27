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
package de.cubeisland.engine.core;

import de.cubeisland.engine.core.filesystem.Resource;

/**
 * Holds all the resource of the core
 */
public enum CoreResource implements Resource
{
//    GERMAN_META(
//        "resources/language/de_DE.yml",
//        "language/de_DE.yml"),
//    FRENCH_META(
//        "resources/language/fr_FR.yml",
//        "language/fr_FR.yml"),
//    GERMAN_MESSAGES(
//        "resources/language/messages/de_DE.json",
//        "language/de_DE/core.json"),
    ENCHANTMENTS(
        "resources/enchantments.txt",
        "data/enchantments.txt"),
    ITEMS(
        "resources/items.txt",
        "data/items.txt"),
    DATAVALUES(
        "resources/datavalues.txt",
        "data/datavalues.txt"),
    ENTITIES(
        "resources/entities.txt",
        "data/entities.txt"),
    TIMES(
        "resources/times.txt",
        "data/times.txt");
    private final String target;
    private final String source;

    private CoreResource(String source, String target)
    {
        this.source = source;
        this.target = target;
    }

    @Override
    public String getSource()
    {
        return this.source;
    }

    @Override
    public String getTarget()
    {
        return this.target;
    }
}

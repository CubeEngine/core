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
package de.cubeisland.cubeengine.core.util;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("de.cubeisland.cubeengine.core.util.RequiredDefaultConstructor")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class RequiredDefaultConstructorProcessor extends AbstractProcessor
{
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        for (TypeElement type : annotations)
        {
            for (Element elem : roundEnv.getElementsAnnotatedWith(type))
            {
                if (!this.hasDefaultConstructor(elem))
                {
                    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Class " + elem + " needs a default constructor!");
                }
            }
        }
        return true;
    }

    private boolean hasDefaultConstructor(Element main)
    {
        for (Element sub : main.getEnclosedElements())
        {
            if (sub.getKind() == ElementKind.CONSTRUCTOR && sub.getModifiers().contains(Modifier.PUBLIC) && sub.asType().accept(DEFAULT_CONSTRUCTOR_VISITOR, null))
            {
                return true;
            }
        }
        return false;
    }

    private static final TypeVisitor<Boolean, Void> DEFAULT_CONSTRUCTOR_VISITOR = new SimpleTypeVisitor6<Boolean, Void>()
    {
        @Override
        public Boolean visitExecutable(ExecutableType t, Void p)
        {
            return t.getParameterTypes().isEmpty();
        }
    };
}

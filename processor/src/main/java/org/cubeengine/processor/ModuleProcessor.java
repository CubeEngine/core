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
package org.cubeengine.processor;

import static javax.tools.Diagnostic.Kind.NOTE;
import static org.cubeengine.processor.ModuleProcessor.DEP_ANNOTATION;
import static org.cubeengine.processor.ModuleProcessor.PLUGIN_ANNOTATION;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

@SupportedAnnotationTypes({ PLUGIN_ANNOTATION, DEP_ANNOTATION })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ModuleProcessor extends AbstractProcessor {

    private static final String PACKAGE = "org.cubeengine.processor.";
    static final String PLUGIN_ANNOTATION = PACKAGE + "Module";
    static final String DEP_ANNOTATION = PACKAGE + "Dependency";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element el : roundEnv.getElementsAnnotatedWith(Module.class)) {
            final TypeElement element = (TypeElement) el;

            Module annotation = element.getAnnotation(Module.class);
            Dependency[] deps = annotation.dependencies();
            Name packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName();
            String pluginName = "Plugin" + element.getSimpleName();
            this.processingEnv.getMessager().printMessage(NOTE, "Generating Plugin for CubeEngine Module " + annotation.name() + "(" + annotation.id() + ")" + "...");
            String moduleClass = packageName + "." + element.getSimpleName();

            List<Dependency> allDeps = new ArrayList<>(Arrays.asList(deps));
            // TODO always add cubeengine-core

            String authors = Arrays.stream(annotation.authors()).map(a -> "\"" + a + "\"").collect(Collectors.joining(", "));
            String dependencies = allDeps.stream().map(d -> String.format("@Dependency(id = \"%s\", version = \"%s\", optional = %s)", d.value(), d.version(), d.optional())).collect(Collectors.joining(",\n"));
            try (BufferedWriter writer = newWriter(packageName, pluginName))
            {
                writer.write("package " + packageName + ";\n");
                writer.write("import javax.inject.Inject;\n");
                writer.write("import com.google.inject.Injector;\n");
                writer.write("import org.spongepowered.api.plugin.Plugin;\n");
                writer.write("import org.spongepowered.api.plugin.Dependency;\n");
                writer.write("import org.cubeengine.libcube.CubeEnginePlugin;\n");
                writer.write("import org.cubeengine.libcube.LibCube;\n");
                writer.write("import org.spongepowered.api.Sponge;\n");
                writer.write(String.format("import %s;\n", moduleClass));
                writer.write("\n");
                writer.write(String.format("@Plugin(id = \"%s\",\n"
                                + "        name = \"%s\",\n"
                                + "        version = \"%s\",\n"
                                + "        description = \"%s\",\n"
                                + "        url = \"%s\",\n"
                                + "        authors = {%s},\n"
                                + "        dependencies = {%s})\n",
                                annotation.id(),
                                annotation.name(),
                                annotation.version(),
                                annotation.description(),
                                annotation.url(),
                                authors,
                                dependencies));
                writer.write(String.format(
                          "public class %s extends CubeEnginePlugin\n"
                        + "{\n"
                        + "    @Inject\n"
                        + "    public %s(Injector injector)\n"
                        + "    {\n"
                        + "         super(injector, Sponge.getPluginManager().getPlugin(\"cubeengine-core\").get(), %s.class);\n"
                        + "    }\n"
                        + "}\n",
                        pluginName, pluginName, element.getSimpleName()));
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        }

        return false;
    }

    private BufferedWriter newWriter(Name packageName, String pluginName) throws IOException
    {
        FileObject obj = this.processingEnv.getFiler().createSourceFile(packageName + "." + pluginName);
        return new BufferedWriter(obj.openWriter());
    }
}

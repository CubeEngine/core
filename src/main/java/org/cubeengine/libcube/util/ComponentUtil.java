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
package org.cubeengine.libcube.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.spongepowered.api.adventure.SpongeComponents;

import static java.util.Collections.singletonList;

public class ComponentUtil
{
    private static final Pattern TEMPLATE_TOKENS = Pattern.compile("(\\{[^}]+}|[^{]+)");
    private static final Pattern URL_IN_STRING = Pattern.compile("https?://\\S+(?=\\s|$)", Pattern.CASE_INSENSITIVE);

    public static Component clickableLink(String label, String url) {
        return clickableLink(label, url, url);
    }

    public static Component clickableLink(String label, String url, String hover) {
        return clickableLink(Component.text(label), url, Component.text(hover));
    }

    public static Component clickableLink(Component label, String url, Component hover) {
        return label.clickEvent(ClickEvent.openUrl(url))
                    .hoverEvent(hover.asHoverEvent());
    }

    public static Component legacyToComponent(String message) {
        return SpongeComponents.legacyAmpersandSerializer().deserialize(message);
    }

    public static Component deepAppend(Component target, Component component) {
        final List<Component> children = target.children();
        if (children.isEmpty()) {
            return target.children(singletonList(component));
        }

        List<Component> newChildren = new ArrayList<>(children);
        Component newLastChild = deepAppend(newChildren.get(children.size() - 1), component);
        newChildren.set(children.size() - 1, newLastChild);
        return target.children(newChildren);
    }

    public static Component legacyMessageTemplateToComponent(String template, Map<String, Component> replacements) {
        final Matcher matcher = TEMPLATE_TOKENS.matcher(template);
        List<Pair<Component, Boolean>> out = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String token = matcher.group(0);
            if (token.startsWith("{")) {
                String varName = token.substring(1, token.length() - 1);
                Component replacement = replacements.get(varName);
                if (replacement != null) {
                    out.add(new Pair<>(legacyToComponent(buffer.toString()), true));
                    buffer.setLength(0);
                    out.add(new Pair<>(replacement, false));
                    continue;
                }
            }
            buffer.append(token);
        }

        if (buffer.length() != 0) {
            out.add(new Pair<>(legacyToComponent(buffer.toString()), true));
        }

        Collections.reverse(out);
        return out.stream().reduce((next, previous) -> {
            if (previous.getRight()) {
                return new Pair<>(deepAppend(previous.getLeft(), next.getLeft()), false);
            } else {
                return new Pair<>(Component.text().append(previous.getLeft()).append(next.getLeft()).build(), false);
            }
        }).map(Pair::getLeft).orElse(Component.empty());
    }

    public static Component autoLink(String input, String hover) {
        Matcher matcher = URL_IN_STRING.matcher(input);
        List<Component> parts = new ArrayList<>();
        int offset = 0;
        while (matcher.find()) {
            String url = matcher.group();
            int start = matcher.start();
            if (matcher.start() != offset) {
                parts.add(Component.text(input.substring(offset, start)));
            }

            parts.add(clickableLink(url, url, hover));
            offset = matcher.end();
        }

        if (offset < input.length()) {
            parts.add(Component.text(input.substring(offset)));
        }

        if (parts.isEmpty()) {
            return Component.empty();
        } else if (parts.size() == 1) {
            return parts.get(0);
        } else {
            return Component.join(Component.empty(), parts);
        }
    }
}

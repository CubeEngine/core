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

    public static Component clickableLink(String label, String url) {
        return Component.text()
                        .content(label)
                        .clickEvent(ClickEvent.openUrl(url))
                        .hoverEvent(Component.text(url).asHoverEvent())
                        .build();
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

    private static Component legacyMessageTemplateToComponent(String template, Map<String, Component> replacements) {
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
}

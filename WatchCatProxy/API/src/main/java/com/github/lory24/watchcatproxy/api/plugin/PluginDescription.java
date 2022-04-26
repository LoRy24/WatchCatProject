package com.github.lory24.watchcatproxy.api.plugin;

import lombok.Getter;

/**
 * @param name    The name of the plugin.
 * @param version The version string of the plugin.
 * @param author  The author of the plugin
 */
public record PluginDescription(@Getter String name, @Getter String version, @Getter String author) {
}

package com.github.lory24.watchcatproxy.api.chatcomponent;

/**
 * Chat click action. Used by the ChatComponent as "action" of the ClickEvent field.
 */
public enum ChatClickAction {
    OPEN_URL,
    RUN_COMMAND,
    SUGGEST_COMMAND,
    CHANGE_PAGE,
    COPY_TO_CLIPBOARD
}

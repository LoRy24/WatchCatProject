package com.github.lory24.watchcatproxy.api;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatComponent {

    /**
     * Chat hover action. Used by the ChatComponent as "action" of the HoverEvent field.
     */
    public enum ChatHoverAction {
        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY
    }

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

    /**
     * A special ChatComponent designed to write only a text field in the constructor.
     */
    public static class TextChatComponent extends ChatComponent {

        /**
         * The constructor for the TextChatComponent class.
         * @param text The only parameter of the TextChatComponent. The ChatComponent will start only with this
         */
        public TextChatComponent(final String text) {
            this.setText(text);
        }

        /**
         * Build the text chat component into a new json string
         * @return The json string
         */
        public String buildTextChatComponent() {
            this.getExtras().clear();
            return this.toString();
        }
    }

    /**
     * The HoverEvent record. This will get the two params, and it will parse them into a new JSON string
     * @param hoverAction The action
     * @param value The value of the action.
     */
    public record HoverEvent(ChatHoverAction hoverAction, String value) {
        @NotNull
        @Override
        public String toString() {
            return "\"hoverEvent\": {" +
                    "\"action\":\"" + this.hoverAction.name().toLowerCase(Locale.ROOT) + "\"," +
                    "\"value\":\"" + this.value + "\"" +
                    "}";
        }
    }

    /**
     * The ClickEvent record. This will get the two params, and it will parse them into a new JSON string
     * @param clickAction The action
     * @param value The value of the action.
     */
    public record ClickEvent(ChatClickAction clickAction, String value) {
        @NotNull
        @Override
        public String toString() {
            return "\"clickEvent\": {" +
                    "\"action\":\"" + this.clickAction.name().toLowerCase(Locale.ROOT) + "\"," +
                    "\"value\":\"" + this.value + "\"" +
                    "}";
        }
    }

    @Setter @Getter private String                  text = "";
    @Setter @Getter private boolean                 bold = false;
    @Setter @Getter private boolean                 italic = false;
    @Setter @Getter private boolean                 underlined = false;
    @Setter @Getter private boolean                 strikethrough = false;
    @Setter @Getter private boolean                 obfuscated = false;
    @Setter @Getter private String                  font = "minecraft:default";
    @Setter @Getter private String                  color = "";
    @Setter @Getter private ClickEvent              clickEvent;
    @Setter @Getter private HoverEvent              hoverEvent;
    @Getter private final   List<TextChatComponent> extras = new ArrayList<>();

    /**
     * Build the extras array list into a json string
     * @return The json
     */
    private @NotNull String buildExtrasToJSONString() {
        if (extras.isEmpty()) return "";
        StringBuilder jsonResult = new StringBuilder("\"extra\":[");

        for (int i = 0; i < extras.size(); i++) {
            if (i != 0) jsonResult.append(",");
            jsonResult.append(extras.get(i).buildTextChatComponent());
        }

        jsonResult.append("]");
        return jsonResult.toString();
    }

    /**
     * Put all the stuff into a JSON string
     * @return The json
     */
    @Override
    public String toString() {
        return "{" + "\"text\":\"" + getText() + "\"" +
                // Add the booleans values
                (isBold() ? ",\"bold\":\"true\"" : "") +
                (isItalic() ? ",\"italic\":\"true\"" : "") +
                (isUnderlined() ? ",\"underlined\":\"true\"" : "") +
                (isStrikethrough() ? ",\"strikethrough\":\"true\"" : "") +
                (isObfuscated() ? ",\"obfuscated\":\"true\"" : "") +
                // Add the font value
                (getFont().equals("minecraft:default") ? "" : ",\"font\":\"" + getFont() + "\"") +
                // Add the color value
                (getColor().isEmpty() ? "" : ",\"color\":\"" + getColor() + "\"") +
                // Add the two events json values
                (getClickEvent() == null ? "" : "," + getClickEvent().toString()) +
                (getHoverEvent() == null ? "" :
                        "," + getHoverEvent().toString()) +
                // Add extras
                (buildExtrasToJSONString().isEmpty() ? "" : ","
                        + buildExtrasToJSONString()) +
                // End the json string
                "}";
    }
}

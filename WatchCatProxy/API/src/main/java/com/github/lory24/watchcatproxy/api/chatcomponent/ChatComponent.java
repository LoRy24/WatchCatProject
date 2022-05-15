package com.github.lory24.watchcatproxy.api.chatcomponent;

import com.google.gson.Gson;
import org.jetbrains.annotations.Contract;

public abstract class ChatComponent {
    public String text = "";
    public boolean bold;
    public boolean italic;
    public boolean underlined;
    public boolean strikethrough;
    public boolean obfuscated;
    public String font = "minecraft:default";
    public String color;
    public String insertion;
    public ClickEvent clickEvent;
    public HoverEvent hoverEvent;
    public TextChatComponent[] extras;

    /**
     * Put all the stuff into a JSON string
     * @return The json
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Create a new ChatComponent from a json string
     * @param json The json string
     * @return The Gson object
     */
    @Contract(pure = true)
    public static ChatComponent fromJson(String json) {
        return new Gson().fromJson(json, ChatComponent.class);
    }
}

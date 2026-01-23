package com.mndk.bteterrarenderer.mcconnector.client.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameInputManagerImpl implements GameInputManager {

    private static final Map<Identifier, KeyBinding.Category> CATEGORIES = new HashMap<>();
    private static List<KeyBinding.Category> VANILLA_CATEGORIES;

    @Override
    public boolean isKeyDown(InputKey key) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), key.glfwKeyCode);
    }

    @Override
    public IKeyBinding registerInternal(String description, InputKey key, String category) {
        // category must be a KeyBinding.Category in 1.21.11
        Identifier catId = category.contains(":")
                ? Identifier.of(category)
                : Identifier.of("bteterrarenderer", category);

        KeyBinding.Category cat = CATEGORIES.computeIfAbsent(catId, GameInputManagerImpl::getOrCreateCategory);

        KeyBinding keyBinding = new KeyBinding(description, key.glfwKeyCode, cat);
        KeyBindingHelper.registerKeyBinding(keyBinding);
        return keyBinding::wasPressed;
    }

    @Override
    public String getClipboardContent() {
        return MinecraftClient.getInstance().keyboard.getClipboard();
    }

    @Override
    public void setClipboardContent(String content) {
        MinecraftClient.getInstance().keyboard.setClipboard(content);
    }

    private static KeyBinding.Category getOrCreateCategory(Identifier id) {
        KeyBinding.Category existing = findExistingCategory(id);
        return existing != null ? existing : KeyBinding.Category.create(id);
    }

    private static KeyBinding.Category findExistingCategory(Identifier id) {
        List<KeyBinding.Category> categories = VANILLA_CATEGORIES;
        if (categories == null) {
            try {
                Field field = KeyBinding.Category.class.getDeclaredField("CATEGORIES");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<KeyBinding.Category> value = (List<KeyBinding.Category>) field.get(null);
                VANILLA_CATEGORIES = value;
                categories = value;
            } catch (Throwable ignored) {
                return null;
            }
        }
        for (KeyBinding.Category cat : categories) {
            if (id.equals(cat.id())) return cat;
        }
        return null;
    }
}

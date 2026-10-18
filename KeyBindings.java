/*
 * This file is part of Cobblemon Autobattle.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Cobblemon Autobattle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cobblemon Autobattle is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Cobblemon Autobattle. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.cobblemon_autobattle.client;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.util.Lazy;
import org.lwjgl.glfw.GLFW;

import com.gitlab.srcmc.cobblemon_autobattle.ModCommon;
import com.gitlab.srcmc.cobblemon_autobattle.client.party.PartyQuickAction;
import com.gitlab.srcmc.cobblemon_autobattle.client.screen.ConfigScreen;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Manages all keybindings for the AutoBattle suite.
 *
 * <ul>
 *   <li><b>K</b> — Toggle AutoBattle enabled (original behaviour)</li>
 *   <li><b>O</b> — Open the Config Screen</li>
 *   <li><b>P</b> — Panic: disable AutoBattle, cancel pending actions, clear alerts</li>
 * </ul>
 */
public class KeyBindings {

    private static final int INPUT_DELAY_TICKS = 20;

    public enum InputType {
        TOGGLE_AUTOBATTLE_ENABLED,
        OPEN_CONFIG_SCREEN,
        PANIC,
        TOGGLE_ANTI_AFK,
        SEND_OUT_ALL,
        RECALL_ALL,
    }

    class MappingEntry {
        public final Lazy<KeyMapping> mapping;
        public final Runnable handler;
        public boolean canHold;
        public int delay;

        public MappingEntry(Lazy<KeyMapping> mapping, Runnable handler) {
            this(mapping, handler, false);
        }

        public MappingEntry(Lazy<KeyMapping> mapping, Runnable handler, boolean canHold) {
            this.mapping = mapping;
            this.handler = handler;
            this.canHold = canHold;
        }
    }

    private Map<InputType, MappingEntry> mappings = Map.ofEntries(
        Map.entry(InputType.TOGGLE_AUTOBATTLE_ENABLED, new MappingEntry(
            Lazy.lazy(() -> new KeyMapping(
                String.format("key.%s.autobattle_enabled", ModCommon.MOD_ID),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                String.format("key.categories.%s", ModCommon.MOD_ID)
            )),
            () -> {
                ClientConfig.INSTANCE.autobattle_enabled(!ClientConfig.INSTANCE.autobattle_enabled());
                Messages.minor(Component
                    .translatable("chat.cobblemon_autobattle.autobattle")
                    .append(Component.literal(" "))
                    .append(ClientConfig.INSTANCE.autobattle_enabled()
                        ? Component.translatable("chat.cobblemon_autobattle.enabled")
                            .withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD)
                        : Component.translatable("chat.cobblemon_autobattle.disabled")
                            .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD)
                    )
                );
            }
        )),

        Map.entry(InputType.OPEN_CONFIG_SCREEN, new MappingEntry(
            Lazy.lazy(() -> new KeyMapping(
                String.format("key.%s.open_config", ModCommon.MOD_ID),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                String.format("key.categories.%s", ModCommon.MOD_ID)
            )),
            () -> {
                var mc = Minecraft.getInstance();
                if (mc.screen == null) {
                    mc.setScreen(new ConfigScreen(null));
                }
            }
        )),

        Map.entry(InputType.PANIC, new MappingEntry(
            Lazy.lazy(() -> new KeyMapping(
                String.format("key.%s.panic", ModCommon.MOD_ID),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                String.format("key.categories.%s", ModCommon.MOD_ID)
            )),
            () -> {
                ModClient.panic();
                Messages.minor(Component
                    .literal("⏸ AutoBattle PAUSED")
                    .withStyle(ChatFormatting.YELLOW)
                    .withStyle(ChatFormatting.BOLD)
                );
            }
        )),

        Map.entry(InputType.TOGGLE_ANTI_AFK, new MappingEntry(
            Lazy.lazy(() -> new KeyMapping(
                String.format("key.%s.toggle_anti_afk", ModCommon.MOD_ID),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                String.format("key.categories.%s", ModCommon.MOD_ID)
            )),
            () -> {
                AntiAfkMode current = ClientConfig.INSTANCE.anti_afk_mode();
                AntiAfkMode next;
                if (current == AntiAfkMode.OFF) {
                    next = AntiAfkMode.SPRINT_RUNNER;
                } else if (current == AntiAfkMode.SPRINT_RUNNER) {
                    next = AntiAfkMode.MICRO_STEP;
                } else {
                    next = AntiAfkMode.OFF;
                }
                ClientConfig.INSTANCE.anti_afk_mode(next);
                Messages.minor(Component
                    .literal("🏃 Anti-AFK: ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(next.displayName)
                        .withStyle(next == AntiAfkMode.OFF ? ChatFormatting.RED : ChatFormatting.GREEN)
                        .withStyle(ChatFormatting.BOLD)
                    )
                );
            }
        )),

        Map.entry(InputType.SEND_OUT_ALL, new MappingEntry(
            Lazy.lazy(() -> new KeyMapping(
                String.format("key.%s.send_out_all", ModCommon.MOD_ID),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F5,
                String.format("key.categories.%s", ModCommon.MOD_ID)
            )),
            PartyQuickAction::sendOutAll
        )),

        Map.entry(InputType.RECALL_ALL, new MappingEntry(
            Lazy.lazy(() -> new KeyMapping(
                String.format("key.%s.recall_all", ModCommon.MOD_ID),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                String.format("key.categories.%s", ModCommon.MOD_ID)
            )),
            PartyQuickAction::recallAll
        ))
    );

    public Lazy<KeyMapping> get(InputType type) {
        return this.mappings.get(type).mapping;
    }

    public Collection<Lazy<KeyMapping>> getAll() {
        return this.mappings.values().stream().map(e -> e.mapping).toList();
    }

    public boolean matchesPanic(int keyCode, int scanCode) {
        try {
            var m = get(InputType.PANIC).get();
            return m.matches(keyCode, scanCode);
        } catch (Exception e) {
            return keyCode == GLFW.GLFW_KEY_P;
        }
    }

    public boolean matchesConfig(int keyCode, int scanCode) {
        try {
            var m = get(InputType.OPEN_CONFIG_SCREEN).get();
            return m.matches(keyCode, scanCode);
        } catch (Exception e) {
            return keyCode == GLFW.GLFW_KEY_O;
        }
    }

    public void tick(Minecraft mc) {
        this.mappings.values().forEach(e -> {
            if ((e.delay == 0 || (e.delay > 0 && (--e.delay == 0)))) {
                var mapping = e.mapping.get();
                boolean consumed;

                while ((consumed = mapping.consumeClick())) {
                    e.handler.run();
                }

                if (consumed) {
                    e.delay = INPUT_DELAY_TICKS;
                }
            }
        });

        // If a screen is open (e.g. Cobblemon's BattleGUI), vanilla consumeClick() won't fire.
        // Query GLFW key state directly so Panic (P) works seamlessly inside combat menus!
        if (mc.screen != null && mc.getWindow() != null) {
            try {
                long window = mc.getWindow().getWindow();
                var panicEntry = this.mappings.get(InputType.PANIC);
                if (panicEntry != null && panicEntry.delay <= 0) {
                    int keyCode = panicEntry.mapping.get().getDefaultKey().getValue();
                    if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_P) || (keyCode > 0 && InputConstants.isKeyDown(window, keyCode))) {
                        panicEntry.handler.run();
                        panicEntry.delay = INPUT_DELAY_TICKS;
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}

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
package com.gitlab.srcmc.cobblemon_autobattle.fabric.client;

import com.gitlab.srcmc.cobblemon_autobattle.client.ModClient;
import com.gitlab.srcmc.cobblemon_autobattle.client.hud.BattleHud;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Fabric client entry point.
 *
 * <p>Registers:
 * <ul>
 *   <li>All AutoBattle keybindings</li>
 *   <li>The HUD overlay via {@link HudRenderCallback}</li>
 * </ul>
 */
public final class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModClient.init();

        // Register keybindings
        ModClient.KEY_BINDINGS.getAll().forEach(m -> KeyBindingHelper.registerKeyBinding(m.get()));

        // Register HUD renderer
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            if (ClientConfig.INSTANCE.hud_enabled()) {
                BattleHud.render(graphics);
            }
        });
    }
}

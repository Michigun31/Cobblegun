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
package com.gitlab.srcmc.cobblemon_autobattle.neoforge.client;

import com.gitlab.srcmc.cobblemon_autobattle.ModCommon;
import com.gitlab.srcmc.cobblemon_autobattle.client.ModClient;
import com.gitlab.srcmc.cobblemon_autobattle.client.hud.BattleHud;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * NeoForge client entry point.
 *
 * <p>Registers:
 * <ul>
 *   <li>All AutoBattle keybindings</li>
 *   <li>The HUD overlay via {@link RenderGuiEvent.Pre}</li>
 * </ul>
 */
@Mod(value = ModCommon.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ModCommon.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClient {

    public NeoForgeClient() {
        ModClient.init();
    }

    @SubscribeEvent
    static void registerBindings(RegisterKeyMappingsEvent event) {
        ModClient.KEY_BINDINGS.getAll().forEach(mapping -> event.register(mapping.get()));
    }

    @SubscribeEvent
    static void onRenderGui(RenderGuiEvent.Pre event) {
        if (ClientConfig.INSTANCE.hud_enabled()) {
            BattleHud.render(event.getGuiGraphics());
        }
    }
}

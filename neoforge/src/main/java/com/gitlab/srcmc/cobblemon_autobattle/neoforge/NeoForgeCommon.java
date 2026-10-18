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
package com.gitlab.srcmc.cobblemon_autobattle.neoforge;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import com.gitlab.srcmc.cobblemon_autobattle.ModCommon;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

@Mod(ModCommon.MOD_ID)
public final class NeoForgeCommon {
    public NeoForgeCommon(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.INSTANCE.getSpec());
        container.getEventBus().addListener(this::onCommonSetup);
    }

    void onCommonSetup(FMLCommonSetupEvent event) {
        ModCommon.init();
    }
}

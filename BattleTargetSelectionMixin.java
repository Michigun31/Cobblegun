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
package com.gitlab.srcmc.cobblemon_autobattle.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection;
import com.gitlab.srcmc.cobblemon_autobattle.client.ModClient;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

import net.minecraft.client.Minecraft;

/**
 * Intercepts {@link BattleTargetSelection} initialisation to automatically
 * pick a target in doubles / multi battles.
 *
 * <p>Always targets the first selectable tile (the opponent). Random
 * target selection could be added for doubles but a single opponent target
 * is the correct behaviour in most cases.
 */
@Mixin(BattleTargetSelection.class)
public abstract class BattleTargetSelectionMixin {

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void injectInit(CallbackInfo ci) {
        final ClientConfig cfg = ClientConfig.INSTANCE;
        if (!cfg.autobattle_enabled()) return;

        final BattleTargetSelection self = (BattleTargetSelection)(Object)this;
        if (self.getRequest() == null || self.getRequest().getResponse() != null) return;

        ModClient.runLater(() -> {
            if (self.getBattleGUI().getCurrentActionSelection() != self) return;
            if (self.getRequest().getResponse() != null) return;

            try {
                var tiles = self.getTargetTiles().stream()
                        .filter(t -> t.getSelectable())
                        .toList();

                if (!tiles.isEmpty()) {
                    // Pick an opponent target (first selectable) in doubles
                    tiles.get((int)(Math.random() * tiles.size())).onClick();
                } else {
                    self.playDownSound(Minecraft.getInstance().getSoundManager());
                    self.getBattleGUI().changeActionSelection(null);
                }
            } catch (Exception e) {
                // Null-safe: do nothing if target data is unavailable
            }
        }, cfg.computeDelay());
    }
}

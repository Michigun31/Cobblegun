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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection.SwitchTile;
import com.gitlab.srcmc.cobblemon_autobattle.client.BattleStateTracker;
import com.gitlab.srcmc.cobblemon_autobattle.client.ModClient;
import com.gitlab.srcmc.cobblemon_autobattle.combat.AiMode;
import com.gitlab.srcmc.cobblemon_autobattle.combat.SmartSwitchSelector;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;

/**
 * Intercepts {@link BattleSwitchPokemonSelection} initialisation to
 * auto-select a Pokémon when switching.
 */
@Mixin(BattleSwitchPokemonSelection.class)
public abstract class BattleSwitchPokemonSelectionMixin {

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void injectInit(CallbackInfo ci) {
        final ClientConfig cfg = ClientConfig.INSTANCE;
        if (!cfg.autobattle_enabled()) return;

        final BattleSwitchPokemonSelection self = (BattleSwitchPokemonSelection)(Object)this;
        if (self.getRequest() == null || self.getRequest().getResponse() != null) return;

        if (!cfg.allow_switching() && !isMandatorySwitch(self)) {
            ModClient.runLater(() -> {
                if (self.getBattleGUI().getCurrentActionSelection() == self) {
                    self.getBattleGUI().changeActionSelection(null);
                }
            }, cfg.computeDelay());
            return;
        }

        ModClient.runLater(() -> {
            if (self.getBattleGUI().getCurrentActionSelection() != self) return;
            if (self.getRequest().getResponse() != null) return;

            boolean reviving = false;
            try {
                reviving = self.isReviving();
            } catch (Exception ignored) {}

            final boolean isReviving = reviving;
            SwitchTile chosen = selectSwitch(cfg, self, isReviving);

            try {
                self.playDownSound(Minecraft.getInstance().getSoundManager());
                if (chosen != null) {
                    self.getBattleGUI().selectAction(
                            self.getRequest(),
                            new SwitchActionResponse(chosen.getPokemon().getUuid()));
                } else {
                    self.getBattleGUI().changeActionSelection(null);
                }
            } catch (Exception ignored) {}
        }, cfg.computeDelay());
    }

    private static SwitchTile selectSwitch(ClientConfig cfg,
                                            BattleSwitchPokemonSelection selection,
                                            boolean isReviving) {
        if (cfg.ai_mode() == AiMode.SMART && cfg.smart_switching()) {
            List<ElementalType> opponentTypes = BattleStateTracker.INSTANCE.getOpponentTypes();
            SwitchTile smart = SmartSwitchSelector.select(selection, opponentTypes, isReviving);
            if (smart != null) return smart;
        }

        try {
            List<SwitchTile> eligible = selection.getTiles().stream()
                    .filter(t -> isReviving ? t.isFainted() : (!t.isFainted() && !t.isCurrentlyInBattle()))
                    .toList();
            if (eligible.isEmpty()) return null;
            return eligible.get((int)(Math.random() * eligible.size()));
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isMandatorySwitch(BattleSwitchPokemonSelection self) {
        try {
            return self.isReviving();
        } catch (Exception e) {
            return false;
        }
    }
}

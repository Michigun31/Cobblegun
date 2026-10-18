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

import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection.MoveTile;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection;
import com.gitlab.srcmc.cobblemon_autobattle.client.ModClient;
import com.gitlab.srcmc.cobblemon_autobattle.combat.AiMode;
import com.gitlab.srcmc.cobblemon_autobattle.combat.ForceMove;
import com.gitlab.srcmc.cobblemon_autobattle.combat.SmartMoveSelector;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

import net.minecraft.client.Minecraft;

/**
 * Intercepts {@link BattleMoveSelection} initialisation to auto-select a move.
 *
 * <p>Supports three modes:
 * <ul>
 *   <li><b>Force Move</b>: always picks the configured slot (if selectable).</li>
 *   <li><b>Smart AI</b>: delegates to {@link SmartMoveSelector}.</li>
 *   <li><b>Random</b>: original random selection.</li>
 * </ul>
 */
@Mixin(BattleMoveSelection.class)
public abstract class BattleMoveSelectionMixin {

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void injectInit(CallbackInfo ci) {
        final ClientConfig cfg = ClientConfig.INSTANCE;
        if (!cfg.autobattle_enabled()) return;

        final BattleMoveSelection self = (BattleMoveSelection)(Object)this;
        if (self.getRequest() == null || self.getRequest().getResponse() != null) return;

        ModClient.runLater(() -> {
            if (self.getBattleGUI().getCurrentActionSelection() != self) return;
            if (self.getRequest().getResponse() != null) return;

            List<MoveTile> selectable;
            try {
                selectable = self.getMoveTiles().stream()
                        .filter(MoveTile::getSelectable)
                        .toList();
            } catch (Exception e) {
                return;
            }

            if (selectable.isEmpty()) {
                // No usable moves — let Cobblemon handle Struggle
                try {
                    self.playDownSound(Minecraft.getInstance().getSoundManager());
                    self.getBattleGUI().changeActionSelection(null);
                } catch (Exception ignored) {}
                return;
            }

            MoveTile chosen = selectMove(cfg, self, selectable);
            if (chosen == null) return;

            try {
                boolean singleBattle = self.getRequest().getActivePokemon().getFormat()
                        .getBattleType().getPokemonPerSide() == 1;

                if (singleBattle) {
                    chosen.onClick();
                } else {
                    self.playDownSound(Minecraft.getInstance().getSoundManager());
                    self.getBattleGUI().changeActionSelection(
                            new BattleTargetSelection(self.getBattleGUI(), self.getRequest(),
                                    chosen.getMove(), null, null));
                }
            } catch (Exception e) {
                // Null-safe fall-through
            }
        }, cfg.computeDelay());
    }

    // -------------------------------------------------------------------------
    // Move selection strategy
    // -------------------------------------------------------------------------

    private static MoveTile selectMove(ClientConfig cfg,
                                       BattleMoveSelection selection,
                                       List<MoveTile> selectable) {
        // Force move takes precedence over everything
        ForceMove forced = cfg.force_move();
        if (forced != ForceMove.AUTO) {
            int idx = forced.slotIndex;
            if (idx >= 0 && idx < selectable.size()) {
                MoveTile candidate = getByOriginalSlot(selection, idx);
                if (candidate != null && candidate.getSelectable()) return candidate;
            }
            // Forced slot not selectable → fall through to AI
        }

        // Smart AI
        if (cfg.ai_mode() == AiMode.SMART) {
            MoveTile smart = SmartMoveSelector.select(selection);
            if (smart != null) return smart;
        }

        // Random fallback
        return selectable.get((int)(Math.random() * selectable.size()));
    }

    /**
     * Attempts to retrieve the move tile at the given 0-based original slot
     * index from the full (including non-selectable) tile list.
     */
    private static MoveTile getByOriginalSlot(BattleMoveSelection selection, int slotIndex) {
        try {
            var all = selection.getMoveTiles();
            if (all != null && slotIndex < all.size()) return all.get(slotIndex);
        } catch (Exception ignored) {}
        return null;
    }
}

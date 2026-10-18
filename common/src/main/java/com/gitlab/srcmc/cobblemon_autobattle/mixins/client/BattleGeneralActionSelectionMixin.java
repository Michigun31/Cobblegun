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

import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGeneralActionSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection;
import com.gitlab.srcmc.cobblemon_autobattle.ModCommon;
import com.gitlab.srcmc.cobblemon_autobattle.client.BattleStateTracker;
import com.gitlab.srcmc.cobblemon_autobattle.client.BattleStateTracker.Alert;
import com.gitlab.srcmc.cobblemon_autobattle.client.ModClient;
import com.gitlab.srcmc.cobblemon_autobattle.combat.TypeEffectivenessTable;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

/**
 * Intercepts the creation of {@link BattleGeneralActionSelection} to drive
 * the AutoBattle decision tree at the action-selection phase.
 */
@Mixin(BattleGeneralActionSelection.class)
public abstract class BattleGeneralActionSelectionMixin {

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void injectInit(CallbackInfo ci) {
        final ClientConfig cfg = ClientConfig.INSTANCE;
        if (!cfg.autobattle_enabled()) return;

        final BattleGeneralActionSelection self = (BattleGeneralActionSelection)(Object)this;
        if (self.getRequest() == null || self.getRequest().getResponse() != null) return;

        // Refresh live battle state directly from client battle
        BattleStateTracker.INSTANCE.updateFromBattle(self.getBattleGUI(), self.getRequest());
        var tracker = BattleStateTracker.INSTANCE;

        ModCommon.LOG.info("[AutoBattle] ==================== TURN ACTION ====================");
        ModCommon.LOG.info("[AutoBattle] Active: {} (Lv. {}, HP: {}/{}, Types: {}) vs Opponent: {} (Lv. {}, HP: {}/{}, Types: {})",
                tracker.getActivePokemonName(), tracker.getActivePokemonLevel(),
                (int)tracker.getActivePokemonHp(), (int)tracker.getActivePokemonMaxHp(), tracker.getActivePokemonTypes(),
                tracker.getOpponentDisplayName(), tracker.getOpponentLevel(),
                (int)tracker.getOpponentHpValue(), (int)tracker.getOpponentMaxHp(), tracker.getOpponentTypes());

        // ---- Shiny / Legendary guard ----
        if (cfg.protect_shiny() && tracker.isOpponentShiny()) {
            ModCommon.LOG.info("[AutoBattle] >>> SAFEGUARD TRIGGERED: Opponent is SHINY! AutoBattle paused.");
            triggerAlert(Alert.SHINY);
            return;
        }
        if (cfg.protect_legendary() && tracker.isOpponentLegendary()) {
            ModCommon.LOG.info("[AutoBattle] >>> SAFEGUARD TRIGGERED: Opponent is LEGENDARY/MYTHICAL! AutoBattle paused.");
            triggerAlert(Alert.LEGENDARY);
            return;
        }

        // ---- Species filter ----
        String speciesName = tracker.getOpponentSpeciesName();
        if (cfg.auto_flee_enabled() && shouldFlee(cfg, speciesName)) {
            ModCommon.LOG.info("[AutoBattle] >>> AUTO-FLEE: Opponent '{}' matches flee criteria. Fleeing battle.", speciesName);
            scheduleAction(() -> flee(self), cfg.computeDelay());
            return;
        }

        // ---- Normal combat action ----
        scheduleAction(() -> {
            if (self.getBattleGUI().getCurrentActionSelection() != self) return;
            if (self.getRequest().getResponse() != null) return;

            self.playDownSound(Minecraft.getInstance().getSoundManager());

            if (shouldAttack(self, cfg)) {
                self.getBattleGUI().changeActionSelection(
                        new BattleMoveSelection(self.getBattleGUI(), self.getRequest()));
            } else {
                self.getBattleGUI().changeActionSelection(
                        new BattleSwitchPokemonSelection(self.getBattleGUI(), self.getRequest()));
            }
        }, cfg.computeDelay());
    }

    private static void flee(BattleGeneralActionSelection self) {
        try {
            if (self.getBattleGUI().getCurrentActionSelection() != self) return;
            if (self.getRequest().getResponse() != null) return;
            self.playDownSound(Minecraft.getInstance().getSoundManager());
            var gui = self.getBattleGUI();
            if (gui != null) {
                gui.changeActionSelection(null);
            }
        } catch (Exception ignored) {}
    }



    private static void triggerAlert(Alert alert) {
        ModClient.panic();
        BattleStateTracker.INSTANCE.setActiveAlert(alert);

        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (alert == Alert.SHINY) {
                try {
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            com.gitlab.srcmc.cobblemon_autobattle.client.radar.CobbleRadar.SHINY_SOUND_EVENT, 1.0f, 1.0f));
                } catch (Throwable t) {
                    mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0f, 1.0f);
                }
            } else {
                mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0f, 1.0f);
            }
        }
    }

    private static void scheduleAction(Runnable r, long delay) {
        ModClient.runLater(r, delay);
    }

    private static boolean shouldAttack(BattleGeneralActionSelection self, ClientConfig cfg) {
        if (!cfg.allow_switching()) return true;
        if (cfg.force_move() != com.gitlab.srcmc.cobblemon_autobattle.combat.ForceMove.AUTO) return true;
        if (self.getRequest().getMoveSet() != null && self.getRequest().getMoveSet().getTrapped()) return true;

        if (cfg.ai_mode() != com.gitlab.srcmc.cobblemon_autobattle.combat.AiMode.SMART || !cfg.smart_switching()) {
            return true; // No random switching during combat
        }

        var tracker = BattleStateTracker.INSTANCE;
        float myHp = tracker.getActivePokemonHp();
        float myMaxHp = tracker.getActivePokemonMaxHp();
        float hpRatio = myMaxHp > 0 ? (myHp / myMaxHp) : 1.0f;

        // If active Pokémon has over 60% HP, never switch mid-combat
        if (hpRatio > 0.60f) {
            ModCommon.LOG.info("[AutoBattle] Action: ATTACK (Active {} healthy with {}% HP)",
                    tracker.getActivePokemonName(), (int)(hpRatio * 100));
            return true;
        }

        // If HP is low (< 40%) and opponent threatens with super effective typing (2x or 4x)
        double oppMaxThreat = 1.0;
        var oppTypes = tracker.getOpponentTypes();
        var myTypes = tracker.getActivePokemonTypes();
        if (oppTypes != null && myTypes != null && !oppTypes.isEmpty() && !myTypes.isEmpty()) {
            for (var ot : oppTypes) {
                double mult = TypeEffectivenessTable.getMultiplier(ot, myTypes);
                if (mult > oppMaxThreat) oppMaxThreat = mult;
            }
        }

        if (oppMaxThreat >= 2.0 && hpRatio < 0.40f) {
            ModCommon.LOG.info("[AutoBattle] Action: SWITCH (Active {} at severe disadvantage: {}x threat and {}% HP)",
                    tracker.getActivePokemonName(), oppMaxThreat, (int)(hpRatio * 100));
            return false;
        }

        // Tactical switch on critical HP (< 30%):
        // If active Pokémon is critical, check if a healthy bench teammate has a strong type advantage (>= 2x)
        if (hpRatio < 0.30f) {
            try {
                var party = com.cobblemon.mod.common.client.CobblemonClient.INSTANCE.getStorage().getParty();
                if (party != null) {
                    for (var p : party) {
                        if (p == null || p.isFainted() || p.getCurrentHealth() <= 0) continue;
                        if (p.getDisplayName(true).getString().equalsIgnoreCase(tracker.getActivePokemonName())) continue;

                        float benchHpRatio = (float) p.getCurrentHealth() / Math.max(1, p.getMaxHealth());
                        if (benchHpRatio > 0.60f) {
                            double offensiveAdvantage = 1.0;
                            if (oppTypes != null && !oppTypes.isEmpty()) {
                                for (var bType : p.getTypes()) {
                                    double m = com.gitlab.srcmc.cobblemon_autobattle.combat.TypeEffectivenessTable.getMultiplier(bType, oppTypes);
                                    if (m > offensiveAdvantage) offensiveAdvantage = m;
                                }
                            }
                            if (offensiveAdvantage >= 2.0) {
                                ModCommon.LOG.info("[AutoBattle] Action: SWITCH (Active {} critical at {}% HP, Bench {} has {}x advantage and {}% HP)",
                                        tracker.getActivePokemonName(), (int)(hpRatio * 100),
                                        p.getDisplayName(true).getString(), offensiveAdvantage, (int)(benchHpRatio * 100));
                                return false;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        ModCommon.LOG.info("[AutoBattle] Action: ATTACK (Matchup acceptable, HP: {}%)", (int)(hpRatio * 100));
        return true;
    }

    private static boolean shouldFlee(ClientConfig cfg, String speciesName) {
        if (speciesName == null || speciesName.isEmpty()) return false;
        String lower = speciesName.toLowerCase();

        List<String> blacklist = cfg.species_blacklist();
        if (blacklist != null && !blacklist.isEmpty()) {
            for (String bl : blacklist) {
                if (lower.equals(bl.toLowerCase())) return true;
            }
        }

        List<String> whitelist = cfg.species_whitelist();
        if (whitelist != null && !whitelist.isEmpty()) {
            for (String wl : whitelist) {
                if (lower.equals(wl.toLowerCase())) return false;
            }
            return true;
        }

        return false;
    }
}

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

import com.gitlab.srcmc.cobblemon_autobattle.client.BattleStateTracker.Alert;
import com.gitlab.srcmc.cobblemon_autobattle.config.ClientConfig;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Client-side entry point for the AutoBattle mod.
 *
 * <p>Manages the global panic token (used to cancel pending async actions),
 * the {@link KeyBindings}, and the {@link BattleStateTracker} reset on
 * world disconnect.
 */
public class ModClient {

    public static final KeyBindings KEY_BINDINGS = new KeyBindings();

    /**
     * Global monotonically-increasing panic token. Each time {@link #panic()}
     * is called the counter is incremented. Pending async actions compare
     * their captured token against this value and abort if they differ.
     */
    private static final AtomicLong PANIC_TOKEN = new AtomicLong(0L);

    public static void init() {
        ClientTickEvent.CLIENT_PRE.register(ModClient::handleClientTick);
    }

    // -------------------------------------------------------------------------
    // Async scheduling
    // -------------------------------------------------------------------------

    /**
     * Schedules {@code r} to run on the MC main thread after {@code delay} ms,
     * using the <em>current</em> panic token. If {@link #panic()} is called
     * before the delay expires the runnable is silently discarded.
     *
     * @param r     the action to run
     * @param delay delay in milliseconds
     */
    public static void runLater(Runnable r, long delay) {
        final long token = PANIC_TOKEN.get();
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Minecraft.getInstance().execute(() -> {
                if (PANIC_TOKEN.get() == token) {
                    r.run();
                }
            });
        }, "cobblemon-autobattle-delay").start();
    }

    // -------------------------------------------------------------------------
    // Panic
    // -------------------------------------------------------------------------

    /**
     * Immediately disables AutoBattle, increments the panic token (cancelling
     * all pending async actions), and sets the HUD alert to PAUSED.
     */
    public static void panic() {
        ClientConfig.INSTANCE.autobattle_enabled(false);
        PANIC_TOKEN.incrementAndGet();
        BattleStateTracker.INSTANCE.setActiveAlert(Alert.PAUSED);
    }

    /** Returns the current panic token value for capturing before scheduling. */
    public static long currentPanicToken() {
        return PANIC_TOKEN.get();
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    static void handleClientTick(Minecraft mc) {
        KEY_BINDINGS.tick(mc);
        AntiAfkManager.INSTANCE.tick(mc);
        com.gitlab.srcmc.cobblemon_autobattle.client.radar.CobbleRadar.INSTANCE.tick(mc);
        com.gitlab.srcmc.cobblemon_autobattle.client.jumpscare.JumpscareManager.INSTANCE.tick(mc);

        // Reset tracker when player leaves a world
        if (mc.level == null) {
            BattleStateTracker.INSTANCE.reset();
        }
    }
}

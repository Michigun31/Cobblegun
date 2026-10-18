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
package com.gitlab.srcmc.cobblemon_autobattle.config;

import com.gitlab.srcmc.cobblemon_autobattle.client.AntiAfkMode;
import com.gitlab.srcmc.cobblemon_autobattle.combat.AiMode;
import com.gitlab.srcmc.cobblemon_autobattle.combat.ForceMove;
import com.gitlab.srcmc.cobblemon_autobattle.hunt.HuntProfileManager;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side configuration for Cobblemon AutoBattle Suite.
 *
 * <p>Persisted via Forge Config API Port in
 * {@code cobblemon_autobattle-client.toml}.
 */
public class ClientConfig {

    public static final ClientConfig INSTANCE = new ClientConfig();

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    public static final long MIN_BASE_DELAY   = 50L;
    public static final long MAX_BASE_DELAY   = 5000L;
    public static final long DEFAULT_BASE_DELAY = 200L;

    public static final long MIN_JITTER   = 0L;
    public static final long MAX_JITTER   = 4000L;
    public static final long DEFAULT_MIN_JITTER = 250L;
    public static final long DEFAULT_MAX_JITTER = 750L;

    // -------------------------------------------------------------------------
    // Config values
    // -------------------------------------------------------------------------

    // === Core ===
    private ConfigValue<Boolean> autobattle_enabled;

    // === Delay / Anti-bot ===
    private ConfigValue<Long>    base_delay;
    private ConfigValue<Long>    min_jitter;
    private ConfigValue<Long>    max_jitter;

    // === Combat AI ===
    private ConfigValue<String>  ai_mode;
    private ConfigValue<String>  force_move;
    private ConfigValue<Boolean> allow_switching;
    private ConfigValue<Boolean> smart_switching;

    // === Auto-catch ===
    private ConfigValue<Boolean> auto_catch_enabled;
    private ConfigValue<Double>  catch_threshold;
    private ConfigValue<Double>  hp_threshold;

    // === Hunt / Filter ===
    private ConfigValue<List<? extends String>> species_whitelist;
    private ConfigValue<List<? extends String>> species_blacklist;
    private ConfigValue<Integer> min_iv_threshold;
    private ConfigValue<List<? extends String>> desired_natures;
    private ConfigValue<Boolean> protect_shiny;
    private ConfigValue<Boolean> protect_legendary;
    private ConfigValue<Boolean> auto_flee_enabled;
    private ConfigValue<String>  hunt_icon_mode;

    // === HUD & Position ===
    private ConfigValue<Boolean> hud_enabled;
    private ConfigValue<Integer> hud_x;
    private ConfigValue<Integer> hud_y;
    private ConfigValue<Double>  battle_hud_scale;
    private ConfigValue<Integer> catch_hud_x;
    private ConfigValue<Integer> catch_hud_y;
    private ConfigValue<Double>  catch_hud_scale;
    private ConfigValue<Integer> radar_hud_x;
    private ConfigValue<Integer> radar_hud_y;
    private ConfigValue<Double>  radar_hud_scale;

    // === World Catch & Radar ===
    private ConfigValue<Boolean> world_catch_hud;
    private ConfigValue<Boolean> radar_beacons;
    private ConfigValue<Boolean> radar_tracers;
    private ConfigValue<Boolean> radar_track_uncaught;
    private ConfigValue<Integer> radar_radius;

    // === Anti-AFK ===
    private ConfigValue<String>  anti_afk_mode;

    // === Colors & Themes ===
    private ConfigValue<Integer> tracer_color_hunt;
    private ConfigValue<Integer> tracer_color_shiny;
    private ConfigValue<Integer> tracer_color_legendary;
    private ConfigValue<Integer> tracer_color_uncaught;
    private ConfigValue<Integer> hud_bg_color;
    private ConfigValue<Integer> hud_border_color;
    private ConfigValue<Integer> gui_accent_color;

    // === Fun / Easter Egg ===
    private ConfigValue<Boolean> disable_fun_features;

    // === Cobbleit ===
    private ConfigValue<Boolean> cobbleit_crosshair_types;

    private final ModConfigSpec spec;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    ClientConfig() {
        var builder = new ModConfigSpec.Builder();

        // ---- Core ----
        builder.push("core");
        autobattle_enabled = builder
                .comment("#", "Toggle AutoBattle on/off.")
                .define("autobattle_enabled", false);
        builder.pop();

        // ---- Delay / Anti-bot ----
        builder.push("delay");
        base_delay = builder
                .comment("#", "Base delay in ms before making a choice. Actual delay = base + random jitter.")
                .defineInRange("base_delay", DEFAULT_BASE_DELAY, MIN_BASE_DELAY, MAX_BASE_DELAY);
        min_jitter = builder
                .comment("#", "Minimum random jitter added to base_delay (ms).")
                .defineInRange("min_jitter", DEFAULT_MIN_JITTER, MIN_JITTER, MAX_JITTER);
        max_jitter = builder
                .comment("#", "Maximum random jitter added to base_delay (ms).")
                .defineInRange("max_jitter", DEFAULT_MAX_JITTER, MIN_JITTER, MAX_JITTER);
        builder.pop();

        // ---- Combat AI ----
        builder.push("combat");
        ai_mode = builder
                .comment("#", "AI mode: RANDOM or SMART.")
                .define("ai_mode", AiMode.SMART.name());
        force_move = builder
                .comment("#", "Force a specific move slot: AUTO, MOVE_1, MOVE_2, MOVE_3, MOVE_4.")
                .define("force_move", ForceMove.AUTO.name());
        allow_switching = builder
                .comment("#", "Allow auto-switching Pokemon. Set false to always fight.")
                .define("allow_switching", true);
        smart_switching = builder
                .comment("#", "Use tactical switch logic (type coverage) instead of random.")
                .define("smart_switching", true);
        builder.pop();

        // ---- Auto-catch ----
        builder.push("auto_catch");
        auto_catch_enabled = builder
                .comment("#", "Enable automatic Pokéball throwing.")
                .define("auto_catch_enabled", true);
        catch_threshold = builder
                .comment("#", "Minimum catch probability (0.0–1.0) required to throw a ball.")
                .defineInRange("catch_threshold", 0.70, 0.0, 1.0);
        hp_threshold = builder
                .comment("#", "Alternative threshold: throw when opponent HP ratio is below this value.")
                .defineInRange("hp_threshold", 0.20, 0.0, 1.0);
        builder.pop();

        // ---- Hunt / Filter ----
        builder.push("hunt");
        species_whitelist = builder
                .comment("#", "Species whitelist. Only engage/capture listed species. Empty = allow all.")
                .defineListAllowEmpty("species_whitelist", new ArrayList<>(), o -> o instanceof String);
        species_blacklist = builder
                .comment("#", "Species blacklist. Auto-flee from listed species regardless of other settings.")
                .defineListAllowEmpty("species_blacklist", new ArrayList<>(), o -> o instanceof String);
        min_iv_threshold = builder
                .comment("#", "Minimum total IVs (0–186) required to capture. 0 = no filter.")
                .defineInRange("min_iv_threshold", 0, 0, 186);
        desired_natures = builder
                .comment("#", "List of desired natures (e.g. [\"Timid\",\"Jolly\"]). Empty = no filter.")
                .defineListAllowEmpty("desired_natures", new ArrayList<>(), o -> o instanceof String);
        protect_shiny = builder
                .comment("#", "Halt AutoBattle and alert when a Shiny is detected.")
                .define("protect_shiny", true);
        protect_legendary = builder
                .comment("#", "Halt AutoBattle and alert when a Legendary/Mythical is detected.")
                .define("protect_legendary", true);
        auto_flee_enabled = builder
                .comment("#", "Auto-flee battles when the opponent doesn't match hunt filters.")
                .define("auto_flee_enabled", false);
        hunt_icon_mode = builder
                .comment("#", "Pokemon icon display mode in Hunt Selection GUI: LARGE, NORMAL, or OFF.")
                .define("hunt_icon_mode", "LARGE");
        builder.pop();

        // ---- HUD & Visuals ----
        builder.push("hud");
        hud_enabled = builder
                .comment("#", "Show the AutoBattle status HUD on screen.")
                .define("hud_enabled", true);
        hud_x = builder
                .comment("#", "HUD X position in pixels.")
                .defineInRange("hud_x", 6, -1000, 5000);
        hud_y = builder
                .comment("#", "HUD Y position in pixels.")
                .defineInRange("hud_y", 6, -1000, 5000);
        battle_hud_scale = builder
                .comment("#", "Scale multiplier for the AutoBattle status HUD.")
                .defineInRange("battle_hud_scale", 1.0, 0.5, 2.5);
        catch_hud_x = builder
                .comment("#", "Catch Rate HUD X position (-1 = auto center).")
                .defineInRange("catch_hud_x", -1, -1000, 5000);
        catch_hud_y = builder
                .comment("#", "Catch Rate HUD Y position (-1 = auto bottom).")
                .defineInRange("catch_hud_y", -1, -1000, 5000);
        catch_hud_scale = builder
                .comment("#", "Scale multiplier for the Catch Rate HUD.")
                .defineInRange("catch_hud_scale", 1.0, 0.5, 2.5);
        radar_hud_x = builder
                .comment("#", "Radar Tracker HUD X position (-1 = auto top-right).")
                .defineInRange("radar_hud_x", -1, -1000, 5000);
        radar_hud_y = builder
                .comment("#", "Radar Tracker HUD Y position.")
                .defineInRange("radar_hud_y", 8, -1000, 5000);
        radar_hud_scale = builder
                .comment("#", "Scale multiplier for the Radar Tracker HUD.")
                .defineInRange("radar_hud_scale", 1.0, 0.5, 2.5);
        world_catch_hud = builder
                .comment("#", "Show Catch Rate Display HUD when looking at wild Pokemon.")
                .define("world_catch_hud", true);
        builder.pop();

        // ---- Radar & Hunt ----
        builder.push("radar");
        radar_beacons = builder
                .comment("#", "Show beacon glowing and nametags above Shinies, Legendaries, and Hunt targets.")
                .define("radar_beacons", true);
        radar_tracers = builder
                .comment("#", "Render on-screen 2D tracers and directional indicators towards tracked targets.")
                .define("radar_tracers", true);
        radar_track_uncaught = builder
                .comment("#", "Automatically highlight and track wild Pokemon that have not yet been caught in your client Pokedex.")
                .define("radar_track_uncaught", true);
        radar_radius = builder
                .comment("#", "Radar scan radius in blocks around the player.")
                .defineInRange("radar_radius", 48, 8, 128);
        builder.pop();

        // ---- Anti-AFK ----
        builder.push("anti_afk");
        anti_afk_mode = builder
                .comment("#", "Anti-AFK mode: OFF, MICRO_STEP, or SPRINT_RUNNER.")
                .define("anti_afk_mode", AntiAfkMode.OFF.name());
        builder.pop();

        // ---- Colors & Themes ----
        builder.push("colors");
        tracer_color_hunt = builder
                .comment("#", "Tracer and highlight color for Hunt Target Pokemon (ARGB/RGB integer).")
                .define("tracer_color_hunt", 0xFF2ECC71); // Emerald Green
        tracer_color_shiny = builder
                .comment("#", "Tracer and highlight color for Shiny Pokemon (ARGB/RGB integer).")
                .define("tracer_color_shiny", 0xFFFFD700); // Gold
        tracer_color_legendary = builder
                .comment("#", "Tracer and highlight color for Legendary Pokemon (ARGB/RGB integer).")
                .define("tracer_color_legendary", 0xFFFF55FF); // Light Purple
        tracer_color_uncaught = builder
                .comment("#", "Tracer and highlight color for Uncaught/Sin Dex Pokemon (ARGB/RGB integer).")
                .define("tracer_color_uncaught", 0xFF00E5FF); // Cyan
        hud_bg_color = builder
                .comment("#", "Background color for HUD panels (ARGB integer).")
                .define("hud_bg_color", 0xE5101820); // Dark Slate
        hud_border_color = builder
                .comment("#", "Border/accent color for HUD panels (ARGB integer).")
                .define("hud_border_color", 0xFF3498DB); // Blue
        gui_accent_color = builder
                .comment("#", "Accent color for UI highlights and headings (ARGB integer).")
                .define("gui_accent_color", 0xFFFFAA00); // Amber/Gold
        builder.pop();

        // ---- Fun / Easter Egg ----
        builder.push("fun");
        disable_fun_features = builder
                .comment("#", "Disable fun features.")
                .define("disable_fun_features", false);
        builder.pop();

        // ---- Cobbleit ----
        builder.push("cobbleit");
        cobbleit_crosshair_types = builder
                .comment("#", "Display elemental types next to crosshair when looking at any Pokemon (Cobbleit feature).")
                .define("cobbleit_crosshair_types", true);
        builder.pop();

        spec = builder.build();
    }

    public ModConfigSpec getSpec() { return spec; }

    // =========================================================================
    // Accessors — core
    // =========================================================================

    public boolean autobattle_enabled()            { return autobattle_enabled.get(); }
    public void    autobattle_enabled(boolean v)   { autobattle_enabled.set(v); }

    // =========================================================================
    // Accessors — delay
    // =========================================================================

    public long base_delay()          { return base_delay.get(); }
    public void base_delay(long v)    { base_delay.set(v); }

    public long min_jitter()          { return min_jitter.get(); }
    public void min_jitter(long v)    { min_jitter.set(v); }

    public long max_jitter()          { return max_jitter.get(); }
    public void max_jitter(long v)    { max_jitter.set(v); }

    /** Returns a randomised delay: {@code base_delay + random[min_jitter, max_jitter]}. */
    public long computeDelay() {
        long base = base_delay();
        long lo   = Math.min(min_jitter(), max_jitter());
        long hi   = Math.max(min_jitter(), max_jitter());
        long jitter = (hi > lo)
                ? java.util.concurrent.ThreadLocalRandom.current().nextLong(lo, hi + 1)
                : lo;
        return base + jitter;
    }

    // =========================================================================
    // Accessors — combat AI
    // =========================================================================

    public AiMode ai_mode() {
        try { return AiMode.valueOf(ai_mode.get()); } catch (Exception e) { return AiMode.RANDOM; }
    }
    public void ai_mode(AiMode v) { ai_mode.set(v.name()); }

    public ForceMove force_move() {
        try { return ForceMove.valueOf(force_move.get()); } catch (Exception e) { return ForceMove.AUTO; }
    }
    public void force_move(ForceMove v) { force_move.set(v.name()); }

    public boolean allow_switching()           { return allow_switching.get(); }
    public void    allow_switching(boolean v)  { allow_switching.set(v); }

    public boolean smart_switching()           { return smart_switching.get(); }
    public void    smart_switching(boolean v)  { smart_switching.set(v); }

    // =========================================================================
    // Accessors — auto-catch
    // =========================================================================

    public boolean auto_catch_enabled()           { return auto_catch_enabled.get(); }
    public void    auto_catch_enabled(boolean v)  { auto_catch_enabled.set(v); }

    public double catch_threshold()         { return catch_threshold.get(); }
    public void   catch_threshold(double v) { catch_threshold.set(v); }

    public double hp_threshold()            { return hp_threshold.get(); }
    public void   hp_threshold(double v)    { hp_threshold.set(v); }

    // =========================================================================
    // Accessors — hunt / filter
    // =========================================================================

    @SuppressWarnings("unchecked")
    public List<String> species_whitelist() {
        return HuntProfileManager.INSTANCE.getActiveSpecies();
    }
    public void species_whitelist(List<String> v) {
        HuntProfileManager.INSTANCE.setActiveSpecies(v);
        try {
            species_whitelist.set(v);
        } catch (Exception ignored) {}
    }

    public boolean isHunting(String speciesName) {
        return HuntProfileManager.INSTANCE.isHunting(speciesName);
    }

    public void toggleHunting(String speciesName) {
        HuntProfileManager.INSTANCE.toggleHunting(speciesName);
        try {
            species_whitelist.set(HuntProfileManager.INSTANCE.getActiveSpecies());
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    public List<String> species_blacklist()                       { return (List<String>) species_blacklist.get(); }
    public void         species_blacklist(List<String> v)         { species_blacklist.set(v); }

    public int  min_iv_threshold()          { return min_iv_threshold.get(); }
    public void min_iv_threshold(int v)     { min_iv_threshold.set(v); }

    @SuppressWarnings("unchecked")
    public List<String> desired_natures()                  { return (List<String>) desired_natures.get(); }
    public void         desired_natures(List<String> v)    { desired_natures.set(v); }

    public boolean protect_shiny()           { return protect_shiny.get(); }
    public void    protect_shiny(boolean v)  { protect_shiny.set(v); }

    public boolean protect_legendary()           { return protect_legendary.get(); }
    public void    protect_legendary(boolean v)  { protect_legendary.set(v); }

    public boolean auto_flee_enabled()           { return auto_flee_enabled.get(); }
    public void    auto_flee_enabled(boolean v)  { auto_flee_enabled.set(v); }

    public String hunt_icon_mode()          { return hunt_icon_mode.get(); }
    public void   hunt_icon_mode(String v) { hunt_icon_mode.set(v); }

    // =========================================================================
    // Accessors — HUD
    // =========================================================================

    public boolean hud_enabled()           { return hud_enabled.get(); }
    public void    hud_enabled(boolean v)  { hud_enabled.set(v); }

    public int  hud_x()          { return hud_x.get(); }
    public void hud_x(int v)     { hud_x.set(v); }

    public int  hud_y()          { return hud_y.get(); }
    public void hud_y(int v)     { hud_y.set(v); }

    public double battle_hud_scale()          { return battle_hud_scale.get(); }
    public void   battle_hud_scale(double v)  { battle_hud_scale.set(v); }

    public int  catch_hud_x()          { return catch_hud_x.get(); }
    public void catch_hud_x(int v)     { catch_hud_x.set(v); }

    public int  catch_hud_y()          { return catch_hud_y.get(); }
    public void catch_hud_y(int v)     { catch_hud_y.set(v); }

    public double catch_hud_scale()          { return catch_hud_scale.get(); }
    public void   catch_hud_scale(double v)  { catch_hud_scale.set(v); }

    public int  radar_hud_x()          { return radar_hud_x.get(); }
    public void radar_hud_x(int v)     { radar_hud_x.set(v); }

    public int  radar_hud_y()          { return radar_hud_y.get(); }
    public void radar_hud_y(int v)     { radar_hud_y.set(v); }

    public double radar_hud_scale()          { return radar_hud_scale.get(); }
    public void   radar_hud_scale(double v)  { radar_hud_scale.set(v); }

    public boolean world_catch_hud()          { return world_catch_hud.get(); }
    public void    world_catch_hud(boolean v) { world_catch_hud.set(v); }

    public boolean radar_beacons()          { return radar_beacons.get(); }
    public void    radar_beacons(boolean v) { radar_beacons.set(v); }

    public boolean radar_tracers()          { return radar_tracers.get(); }
    public void    radar_tracers(boolean v) { radar_tracers.set(v); }

    public boolean radar_track_uncaught()          { return radar_track_uncaught.get(); }
    public void    radar_track_uncaught(boolean v) { radar_track_uncaught.set(v); }

    public int  radar_radius()          { return radar_radius.get(); }
    public void radar_radius(int v)     { radar_radius.set(v); }

    public AntiAfkMode anti_afk_mode() {
        try {
            return AntiAfkMode.valueOf(anti_afk_mode.get());
        } catch (Exception e) {
            return AntiAfkMode.OFF;
        }
    }
    public void anti_afk_mode(AntiAfkMode v) {
        anti_afk_mode.set(v != null ? v.name() : AntiAfkMode.OFF.name());
    }

    public boolean anti_afk_enabled()          { return anti_afk_mode() != AntiAfkMode.OFF; }
    public void    anti_afk_enabled(boolean v) { anti_afk_mode(v ? AntiAfkMode.MICRO_STEP : AntiAfkMode.OFF); }

    // =========================================================================
    // Accessors — colors & themes
    // =========================================================================

    public int  tracer_color_hunt()          { return tracer_color_hunt.get(); }
    public void tracer_color_hunt(int v)     { tracer_color_hunt.set(v); }

    public int  tracer_color_shiny()          { return tracer_color_shiny.get(); }
    public void tracer_color_shiny(int v)     { tracer_color_shiny.set(v); }

    public int  tracer_color_legendary()          { return tracer_color_legendary.get(); }
    public void tracer_color_legendary(int v)     { tracer_color_legendary.set(v); }

    public int  tracer_color_uncaught()          { return tracer_color_uncaught.get(); }
    public void tracer_color_uncaught(int v)     { tracer_color_uncaught.set(v); }

    public int  hud_bg_color()          { return hud_bg_color.get(); }
    public void hud_bg_color(int v)     { hud_bg_color.set(v); }

    public int  hud_border_color()          { return hud_border_color.get(); }
    public void hud_border_color(int v)     { hud_border_color.set(v); }

    public int  gui_accent_color()          { return gui_accent_color.get(); }
    public void gui_accent_color(int v)     { gui_accent_color.set(v); }

    public boolean disable_fun_features()          { return disable_fun_features != null ? disable_fun_features.get() : false; }
    public void    disable_fun_features(boolean v) { if (disable_fun_features != null) disable_fun_features.set(v); }

    public boolean jumpscare_enabled()          { return !disable_fun_features(); }
    public void    jumpscare_enabled(boolean v) { disable_fun_features(!v); }

    public boolean cobbleit_crosshair_types()          { return cobbleit_crosshair_types != null ? cobbleit_crosshair_types.get() : true; }
    public void    cobbleit_crosshair_types(boolean v) { if (cobbleit_crosshair_types != null) cobbleit_crosshair_types.set(v); }

    // =========================================================================
    // Legacy compatibility (used internally by old code paths)
    // =========================================================================

    /** @deprecated Use {@link #base_delay()} and {@link #computeDelay()} instead. */
    @Deprecated
    public long select_delay() { return computeDelay(); }

    /** @deprecated No-op; jitter has replaced this. */
    @Deprecated
    public void select_delay(long value) { base_delay.set(value); }

    /** @deprecated No longer used; kept for binary compatibility. */
    @Deprecated
    public double switch_chance() { return 0.1; }
}

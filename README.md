# Cobblemon Autobattle

A client side mod to automate battles in Cobblemon.

## Description

![autobattle-keybind.png](https://gitlab.com/srcmc/cobblemon-autobattle/-/raw/master/assets/autobattle-keybind.png?ref_type=heads)

![autobattle-demo.webp](https://gitlab.com/srcmc/cobblemon-autobattle/-/raw/master/assets/autobattle-demo.webp?ref_type=heads)

Moves and switch targets are chosen **randomly**. Other actions (like item usage or position shifting) will never be performed, although you are free to interfere at any time.

## Configuration

The config file is located at `configs/cobblemon_autobattle-client.toml` and provides following options:

- `autobattle_enabled`: Toggle Autobattle.
- `select_delay`: Delay in ms before making a choice.
- `switch_chance`: Chance to randomly switch to another pokemon if possible.

You may configure a **key binding** in the **Minecraft options** to toggle `autobattle_enabled` ingame. The default key is `K`.

## Dependencies

- [Cobblemon](https://modrinth.com/mod/cobblemon)
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (**fabric only**)

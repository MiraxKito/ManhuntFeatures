# Manhunt Features

Server-side Manhunt utilities for Minecraft Java Edition 1.16.1–1.21.4 supported releases.

Manhunt Features adds hunter and runner roles, a special tracking compass, separate resettable Manhunt dimensions, optional game control, localization, and an in-game configuration panel.

<img width="1664" height="928" alt="1785593624" src="https://github.com/user-attachments/assets/e70c4a48-17a7-4091-9785-5574cd21648c" />

## Features

- Multiple hunters and runners.
- A special **Manhunt Compass** that is the only compass item able to track runners.
- The compass updates only when a hunter right-clicks it.
- The compass shows a direction only; runner coordinates are never displayed.
- The nearest runner in the same world/dimension is selected.
- Separate Manhunt Overworld, Nether, and End.
- Safe staged world reset that avoids synchronous in-game world-generation watchdog failures.
- Role labels in the player list (tab), without a scoreboard.
- Player selectors: `@s`, `@a`, `@p`, `@r`, and `@e[type=player]`.
- Hunters automatically receive a Manhunt Compass after joining, respawning, changing worlds, or being assigned the hunter role.
- Optional game lifecycle with internal timer, automatic win detection, and restart recovery.
- English and Russian localization.
- In-game admin configuration panel: `/mhfeatures config`.
- Bukkit/Spigot/Paper API implementation.

## Requirements and version files

This project publishes separate JAR files for different Minecraft generations:

| File | Minecraft target | Server API | Java |
|---|---|---|---|
| `mhfeatures-1.0.2-1.16.1.jar` | 1.16.x | Bukkit/Spigot/Paper 1.16.1 | 8 or newer |
| `mhfeatures-1.0.2-1.17.1.jar` | 1.17.x | Bukkit/Spigot/Paper 1.17.1 | 8 or newer |
| `mhfeatures-1.0.2-1.18.2.jar` | 1.18.x | Bukkit/Spigot/Paper 1.18.2 | 8 or newer |
| `mhfeatures-1.0.2-1.19.4.jar` | 1.19.x | Bukkit/Spigot/Paper 1.19.4 | 8 or newer |
| `mhfeatures-1.0.2-1.20.1.jar` | 1.20.x | Bukkit/Spigot/Paper 1.20.1 | 17 or newer |
| `mhfeatures-1.0.2-1.21.4.jar` | 1.21.x | Bukkit/Spigot/Paper 1.21.4 | 21 or newer |

Use the JAR matching your server's major Minecraft line. Do not install a JAR built for a newer major line on an older server.

Paper is recommended for server performance, but both builds are compiled against the corresponding Spigot API and do not require Paper-only APIs.

## Installation

1. Download the latest `mhfeatures-x.y.z.jar` from the Releases or Modrinth page.
2. Put the JAR into the server's `plugins` folder.
3. Start the server once.
4. Configure `plugins/MHFeatures/config.yml` if needed.
5. Run `/mhfeatures reset` and restart the server once to create the initial Manhunt worlds.

The original world is configured as `world` by default. Manhunt worlds are `world_mh`, `world_mh_nether`, and `world_mh_the_end`.

## Commands

Optional game control commands:

```text
/mhfeatures start
/mhfeatures stop
```

`start` requires at least one hunter, one runner, and all three Manhunt worlds loaded. It moves hunters to the Manhunt Overworld spawn and starts an internal timer. `stop` ends the game, reports the duration, clears inventories and roles, and returns players to the original world. The game also ends automatically when the last runner is eliminated or the Ender Dragon is defeated. The game system is optional; roles and compass features can still be used without starting a game.

Temporary player exits do not end an active game. The active game timer is saved and restored after a server restart. During the configured reset recovery window, reconnecting players are sent to the original world, their inventories are cleared, and the previous game state is discarded.

```text
/mhfeatures role <player|selector> <hunter|runner|clear>
/mhfeatures compass [player|selector]
/mhfeatures reset [seed]
/mhfeatures mhworld [player|selector]
/mhfeatures ogworld [player|selector]
/mhfeatures status
/mhfeatures config
/mhfeatures reload
```

Aliases:

```text
/mhf <subcommand>
```

If the player argument is omitted, the command targets the command sender (`@s`). The console must specify a player or selector.

Examples:

```text
/mhf role @a hunter
/mhf role Steve runner
/mhf compass @a
/mhf mhworld @a
/mhf ogworld @s
/mhf reset
/mhf reset 123456789
```

### Reset behavior

`/mhfeatures reset [seed]` does not generate three worlds while the server is actively serving players. It:

1. Moves online players to the original world.
2. Clears the configured inventory sections.
3. Saves a pending reset request and seed.
4. Requires one normal server restart.
5. Removes only configured Manhunt world folders marked as plugin-owned.
6. Generates fresh Manhunt Overworld, Nether, and End worlds during startup.

Without a seed, a random seed is used. With a numeric seed, the same seed is used for all three Manhunt dimensions.

Do not delete the original world. Always keep a backup before using reset on a production server.

## Permissions

```text
mhfeatures.admin.role      Assign or clear roles
mhfeatures.admin.reset     Request a world reset
mhfeatures.admin.mhworld   Move players to the Manhunt Overworld
mhfeatures.admin.ogworld   Move players to the original world
mhfeatures.admin.reload    Reload safe configuration values
mhfeatures.admin.game      Start or stop a Manhunt game
mhfeatures.admin.config    Open the in-game configuration panel
mhfeatures.use.compass     Use the Manhunt Compass (default: true)
```

All admin permissions default to server operators.

## Localization

The default language is English. Change this setting in `config.yml`:

```yaml
language: en
```

Use `language: ru` for Russian messages. The plugin creates and loads `messages-en.yml` or `messages-ru.yml` in its data folder. These files contain command replies, game messages, reset notices, compass name and compass tracking messages. Run `/mhfeatures reload` after changing the language.

## Configuration

The generated `config.yml` controls:

- original and Manhunt world names;
- compass name and no-target message;
- tab labels for hunters, runners, and unassigned players;
- inventory and Ender Chest reset behavior;
- message prefix.

Example tab labels:

```yaml
tab:
  enabled: true
  hunter-prefix: '&c[Hunter] &f'
  runner-prefix: '&a[Runner] &f'
  none-prefix: '&7'
```

## Compatibility and limitations

- This release provides separate build targets for the 1.16.x, 1.17.x, 1.18.x, 1.19.x, 1.20.x, and 1.21.x Minecraft lines.
- Each artifact is compiled against the oldest Bukkit/Spigot API in its line and uses only APIs shared by the supported profiles.
- Patch releases should normally work with their matching major/minor line, but server-platform behavior can vary between Spigot and Paper builds.
- The player selector support intentionally covers the documented selectors only. It is not a full vanilla selector parser with every possible filter.
- World generation can still be expensive. The staged reset prevents the plugin from blocking an already-running server during generation, but startup may take time while the server prepares the new worlds.
- The internal timer does not create or replace a sidebar scoreboard.
- The plugin can end a match automatically after the last runner is eliminated or the Ender Dragon is defeated.


## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

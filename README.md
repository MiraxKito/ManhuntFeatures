# Manhunt Features

Server-side Manhunt utilities for Minecraft Java Edition 1.16.1 and newer supported releases.

Manhunt Features adds hunter and runner roles, a named tracking compass, separate resettable Manhunt dimensions, player selectors, and automatic compass recovery for hunters.

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
- Bukkit/Spigot/Paper API implementation.

## Requirements and version files

This project publishes separate JAR files for different Minecraft generations:

| File | Minecraft target | Server API | Java |
|---|---|---|---|
| `mhfeatures-1.3.0-1.16.1.jar` | 1.16.1 baseline | Bukkit/Spigot/Paper 1.16.1 | 8 or newer |
| `mhfeatures-1.3.0-1.20.1.jar` | 1.20.1 baseline | Bukkit/Spigot/Paper 1.20.1 | 17 or newer |

The 1.16.1 JAR is intended for the 1.16.1 generation and the 1.20.1 JAR is intended for the 1.20.1 generation. Do not install the 1.20.1 JAR on a 1.16.x server.

Paper is recommended for server performance, but both builds are compiled against the corresponding Spigot API and do not require Paper-only APIs.

## Installation

1. Download the latest `mhfeatures-x.y.z.jar` from the Releases or Modrinth page.
2. Put the JAR into the server's `plugins` folder.
3. Start the server once.
4. Configure `plugins/MHFeatures/config.yml` if needed.
5. Run `/mhfeatures reset` and restart the server once to create the initial Manhunt worlds.

The original world is configured as `world` by default. Manhunt worlds are `world_mh`, `world_mh_nether`, and `world_mh_the_end`.

## Commands

```text
/mhfeatures role <player|selector> <hunter|runner|clear>
/mhfeatures compass [player|selector]
/mhfeatures reset [seed]
/mhfeatures mhworld [player|selector]
/mhfeatures ogworld [player|selector]
/mhfeatures status
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
mhfeatures.use.compass     Use the Manhunt Compass (default: true)
```

All admin permissions default to server operators.

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
  hunter-prefix: '&c[ОХОТНИК] &f'
  runner-prefix: '&a[БЕГУЩИЙ] &f'
  none-prefix: '&7'
```

## Compatibility and limitations

- This release provides tested build targets for 1.16.1 and 1.20.1. Other versions in between require their matching build/API validation before being advertised as supported.
- The plugin uses Bukkit/Spigot APIs; newer Minecraft versions require a separate compatibility pass.
- The player selector support intentionally covers the documented selectors only. It is not a full vanilla selector parser with every possible filter.
- World generation can still be expensive. The staged reset prevents the plugin from blocking an already-running server during generation, but startup may take time while the server prepares the new worlds.
- The plugin does not automatically determine a winner or end a Manhunt match.

## Russian

Manhunt Features — серверный плагин для Minecraft 1.16.1+ с отдельными сборками под поколения версий. Он добавляет роли охотников и бегущих, специальный именной компас, отдельные миры мэнханта, сброс миров через перезапуск сервера и визуальные метки ролей в табе.

Главные команды:

```text
/mhf role <игрок|селектор> <hunter|runner|clear>
/mhf compass [игрок|селектор]
/mhf reset [seed]
/mhf mhworld [игрок|селектор]
/mhf ogworld [игрок|селектор]
/mhf status
/mhf reload
```

Компас обновляет направление только после правого клика, показывает только стрелку и не выводит координаты бегущего. Обычный компас не работает.

После `/mhf reset` нужно выполнить обычный restart сервера: так старые миры удаляются, а новые создаются без зависания Spigot watchdog.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

# MythicTools

A Paper Minecraft plugin that adds **mythic tools** with special abilities. Break 3×3 areas, fell entire trees, automatically switch tool modes, and sell inventory contents with dynamic pricing — all with beautiful particle effects and proper mining speeds.

**License:** GNU GPL v3.0 with mandatory attribution (see [LICENSE](LICENSE))

---

## Features

### 🔨 Tool Abilities

| Ability | Description |
|---------|-------------|
| **DRILL_3X3** | Breaks a 3×3 area around the mined block at realistic mining speed with purple particles |
| **TREE_CHOPPER** | Mines connected logs (up to 150 blocks) and removes only natural leaves in the canopy — house-safe |
| **MULTITOOL** | Acts as pickaxe + axe + shovel. On Paper 1.20.5+, uses ToolComponent for correct speeds; older versions get Haste II |
| **SELL_CHEST** | Right-click containers to sell all items using dynamic shop plugin prices (ShopGUI+, EconomyShopGUI, EssentialsX) |
| **PURPLE_PARTICLES** | Decorative amethyst-style particles on breaks |

### Built-in Tools

- **Amethyst Drill** — 3×3 mining with Efficiency V
- **Amethyst Multitool** — Pickaxe + Axe + Shovel in one
- **Amethyst Tree Chopper** — Cascading tree felling
- **Sell Axe** — Chest liquidation tool

### Quality-of-Life

✨ **Automatic tool creation** from YAML  
✨ **Item expiry** — Set tool duration (`/mt give player id 7d`)  
✨ **Dynamic pricing** — Integrates with ShopGUI+, EconomyShopGUI, EssentialsX  
✨ **Version checker** — Async Modrinth API integration with in-game update notifications  
✨ **Multi-version support** — Paper 1.19–1.21+  

---

## Installation

1. Download the latest JAR from [Modrinth](https://modrinth.com/plugin/mythictools) or [Releases](https://github.com/blackowlzz/MythicTools/releases)
2. Drop `MythicTools-x.x.x.jar` into your `plugins/` folder
3. Install dependencies (soft-deps, optional):
   - [Vault](https://www.spigotmc.org/resources/vault.34315/) — Required for `/mt` sell chest to work
   - [ShopGUI+](https://www.spigotmc.org/resources/shopgui.13828/), [EconomyShopGUI](https://www.spigotmc.org/resources/economyshopgui.93571/), or [EssentialsX](https://essentialsx.net/) — For dynamic item pricing
4. Restart server
5. Customize `plugins/MythicTools/config.yml` and `tools.yml`

---

## Commands

```
/mt give <player> <tool_id> [duration]    # Give a tool (e.g., /mt give Steve amethyst_drill 7d)
/mt list                                   # List all registered tools
/mt reload                                 # Reload config & tools
/mt about                                  # Plugin info & license
```

### Permissions

| Permission | Default | Effect |
|-----------|---------|--------|
| `mythictools.give` | OP | Give tools to players |
| `mythictools.list` | OP | List tools |
| `mythictools.reload` | OP | Reload configuration |
| `mythictools.update-notify` | OP | Receive in-game update notifications |
| `mythictools.use` | TRUE | Use tool abilities |

---

## Configuration

### `config.yml`

```yaml
prefix: "&8[&dMythicTools&8] &r"

expiry:
  enabled: true
  check-interval-ticks: 1200        # Scan inventories every 1 minute
  remove-from-inventory: true        # Remove expired tools from inventory

update-check:
  enabled: true
  modrinth-project-id: "mythictools"  # Leave blank to disable
  frequency-minutes: 360              # How often to check (default: 6 hours)
```

### `tools.yml`

Define custom tools with abilities and properties:

```yaml
tools:
  my_drill:
    name: "&dMy Custom Drill"
    material: NETHERITE_PICKAXE
    worth: 500
    description:
      - "&7A powerful drill."
      - "&7Purple particles included."
    abilities:
      - DRILL_3X3
      - PURPLE_PARTICLES
    indestructible: true
    efficiency-level: 5              # Enchantment level (affects mining speed)
    efficiency-display: 26           # Display value in lore
```

---

## Requirements

- **Paper 1.19+** (Spigot/Bukkit unsupported)
- **Java 21+** (for build; server can run on 17+ with careful testing)
- **Vault** (soft-dep) — Required for sell chest to work

### Optional Dependencies

Install any of these for dynamic item pricing:

- [ShopGUI+](https://www.spigotmc.org/resources/shopgui.13828/) (recommended)
- [EconomyShopGUI / Pro](https://www.spigotmc.org/resources/economyshopgui.93571/)
- [EssentialsX](https://essentialsx.net/) (uses worth.yml)

---

## API

Plugin provides a public API via `MythicToolsAPI`:

```java
// Get the provider
MythicToolsProvider provider = MythicToolsAPI.get();

// Look up a tool
Optional<MythicTool> tool = provider.getTool("amethyst_drill");

// Check if an item is a tool and if it's expired
Optional<MythicTool> itemTool = provider.getToolFromItem(itemStack);
boolean expired = provider.isExpired(itemStack);
```

Listen for tool events:

```java
@EventHandler
public void onAbility(MythicToolAbilityEvent event) {
    if (event.getAbility() == ToolAbility.DRILL_3X3) {
        event.setCancelled(true); // Cancel if needed
    }
}
```

---

## Multi-Version Support

| Version | Status | Notes |
|---------|--------|-------|
| 1.19–1.20.4 | ✅ Full | Haste-based fallback for multitool; Efficiency V+ recommended |
| 1.20.5+ | ✅ Full | Uses ToolComponent API for precise block type detection |
| 1.21+ | ✅ Full | Supports MINING_EFFICIENCY attribute modifier |

---

## Troubleshooting

### "Plugin is disabled" on startup

Check console for error messages. Common causes:
- Missing Vault (if using sell chest)
- Invalid YAML in `tools.yml` or `config.yml`
- Unsupported server version

### Tools not appearing in `/mt list`

- Make sure `tools.yml` has valid YAML syntax
- Check for typos in ability names (case-sensitive: `DRILL_3X3`, not `DRILL_3x3`)
- Reload with `/mt reload`

### Sell chest returns "$0"

- Ensure Vault is installed
- Install a shop plugin (ShopGUI+, EconomyShopGUI, or EssentialsX)
- Check that items have prices configured in your shop plugin

### Drill/Tree Chopper feels slow

- Increase `efficiency-level` in `tools.yml`
- Efficiency V is the default; higher values = faster mining
- Paper 1.20.5+ also applies ToolComponent speed bonuses

---

## Building from Source

Requires **Java 21** and **Gradle 8.14+**:

```bash
git clone https://github.com/blackowlzz/MythicTools.git
cd MythicTools
./gradlew shadowJar
# Jar built to: build/libs/MythicTools-1.0.0.jar
```

---

## License & Attribution

This project is licensed under **GNU GPL v3.0** with an **additional mandatory attribution requirement**.

### For Forkers & Distributors

If you publish a fork or derivative work (on Modrinth, SpigotMC, GitHub, etc.):

1. **In-game credit** — Your `/about` or `/mt about` must display:
   ```
   Based on MythicTools by blackowlzz — https://github.com/blackowlzz/MythicTools
   ```

2. **Repository README** — Add a visible "Credits" section:
   ```markdown
   ## Credits
   This project is based on [MythicTools](https://github.com/blackowlzz/MythicTools) by blackowlzz.
   ```

3. **Distribution page** — Any Modrinth, SpigotMC, or forum post must mention the original project

**Private server use** (no public distribution) is exempt from these requirements.

---

## Support & Contribution

- **Issues:** [GitHub Issues](https://github.com/blackowlzz/MythicTools/issues)
- **Contributing:** Pull requests welcome — ensure code follows project style
- **License questions:** See [LICENSE](LICENSE) for full terms

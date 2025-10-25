# Lukkit 2.2.0 - The Paper Fix Update

**Release Date:** October 2025  
**Maintainer:** TheGamingMahi

After almost 6 years since the last update, Lukkit is back with full Paper compatibility!

## 📋 TL;DR

Lukkit now works on modern Paper servers! Fixed plugin loading issues caused by Paper's remapping system and added a bypass mode for Paper's plugin restrictions. Just set `bypass-plugin-registration: true` in config and your `.lkt` plugins work perfectly.

---

## 🎉 What's New

### Paper Server Compatibility
- **Fixed plugin loading on modern Paper servers** - Lukkit now correctly locates `.lkt` files even when Paper remaps plugins
- **Bypass mode for Paper's plugin restrictions** - New loading system that works around Paper's security changes
- **Configurable loading modes** - Switch between normal and bypass mode depending on your server software

### New Configuration Options
- **`debug-mode`** - Enable verbose logging for troubleshooting (default: false)
- **`bypass-plugin-registration`** - Enable Paper compatibility mode (default: true)
- Config version bumped from 3 to 4

### Improvements
- Better debug logging system
- Automatic config migration when updating

---

## 🔧 Configuration

```yaml
# Enable debug mode for verbose logging
debug-mode: false

# Paper compatibility mode (recommended for Paper servers)
# Set to 'true' for Paper, 'false' for Spigot
bypass-plugin-registration: true
```

---

## 📦 Installation

1. Download `Lukkit-2.2.0.jar`
2. Place in your server's `/plugins` folder
3. Place your `.lkt` plugins in the same `/plugins` folder
4. Start/restart your server

---

## 🐛 Bug Fixes

- Fixed: Lukkit unable to find `.lkt` files on Paper servers due to plugin remapping
- Fixed: Plugin loading failures on modern Paper versions

---

## ⚙️ Technical Details

### Paper Remapping Fix
Paper remaps plugin JARs to a `.paper-remapped` folder. The original code used `getFile().getParentFile()` which pointed to the remapped location, not the actual `/plugins` folder where `.lkt` files are located.

**Solution:** Changed to `getDataFolder().getParentFile()` which correctly points to `/plugins/`

### Bypass Mode
Paper's modern security restricts dynamic plugin loading through custom PluginLoaders. Bypass mode loads plugins manually without registering them with Paper's PluginManager, avoiding these restrictions while maintaining full functionality.

---

## 🔄 Migration Guide

### From Lukkit 2.1.2

1. Replace your old `Lukkit.jar` with `Lukkit-2.2.0.jar`
2. Your config will automatically update (old config backed up to `config.old.yml`)
3. All existing `.lkt` plugins work without changes
4. Recommended: Set `bypass-plugin-registration: true` for Paper servers

### Compatibility

- ✅ Paper 
- ✅ Spigot 
- ✅ All existing Lukkit plugins (no changes needed)

---

## 🎯 Known Limitations (Bypass Mode)

When using `bypass-plugin-registration: true`:

- Lukkit plugins don't appear in `/plugins` command (use `/lukkit plugins` instead)
- Other plugins can't detect Lukkit plugins via PluginManager
- Plugin dependencies (`depend:` in plugin.yml) aren't automatically handled
- Use `/lukkit dev reload <plugin>` instead of `/reload` for Lukkit plugins

**For most users, these limitations don't matter!** Your plugins will work perfectly.

---

## 🙏 Credits

- **Original Authors:** jammehcow, AL_1, mathhulk, ArtexDevelopment
- **Paper Fix:** TheGamingMahi
- **Special Thanks:** The Lukkit community for keeping it alive!

---

## 📝 Notes

The original Lukkit repository is archived and no longer accepting contributions. This is an unofficial fork/continuation focused on Paper compatibility. 

If you encounter issues, please report them with:
- Your server version (e.g., Paper 1.20.4)
- Config settings (`debug-mode` and `bypass-plugin-registration` values)
- Console logs when loading fails

---

## 🚀 Future Plans

**Luacord** - A modern fork is planned with:
- Kotlin rewrite for cleaner, more maintainable code
- .jar support alongside .lkt files
- Extended Spigot/Paper API coverage
- Paper-specific API implementations
- 100% backwards compatible with existing Lukkit plugins
- Improved developer tooling and documentation

Development will be gradual. Lukkit 2.2.0 remains stable in the meantime.

---

## 📄 License

Lukkit is open source. Original license terms apply.

---

**Download:** [Lukkit-2.2.0.jar](https://github.com/TheGamingMahi/Lukkit-PaperFix/releases/tag/Lukkit)  
**Support:** Use `/lukkit dev errors` for debugging  
**Version:** 2.2.0 

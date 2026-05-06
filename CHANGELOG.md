# Changelog
---
## 1.1.0-beta
### Added
- Halo shaders.
- Inserter module to send items back into storage.
- Server rendering for the Halo.
- Range Upgrades as a module.
- Halo Anchor as a module.
- Oculus support.
- Dynamic lighting.
- Mana level tooltip for the Quantization Device Wand overlay.
- New Lexica entries.
- Config settings for:
    - Halo insert consumption.
    - Halo extract consumption.
    - Quantization Device Mana capacity.
    - Quantization animation speed.
    - Per-module toggles for all Halo modules.
    - Base Halo range and per-upgrade range bonuses.
    - Halo particle toggle and particle multiplier.
    - Halo item animation toggle (client only).
    - Halo shader background visibility, debug logging, and quality (client only).
    - Per-sound volume scalers for Halo and quantum sounds.

### Updated
- Example image in the Lexica.
- Lexica entries for the new modules and shaders.
- Selection box texture rendering.

### Changed
- Default Mana buffer size for the Quantization Device is now 100k.
- The item list now renders in the center of the player view.

### Fixed
- Model rendering size on the ground.
- Halo access to items from interrupted Corporea networks.

### Removed
- Sorting module.
---
## 1.0.0-beta
### Added
- Initial port from 1.16.5 to 1.20.1.
# Development Environment (Nix Flakes)

Always use modern Nix commands (`nix develop`, `nix flake check`, `nix build`, `nix run`). Never use legacy `nix-shell`.

- **Enter development shell**: `nix develop`
- **Check flake**: `nix flake check`
- **Direnv support**: Run `direnv allow` to automatically load the environment via `.envrc`.

The development shell provides:
- OpenJDK 17
- Android SDK (API Level 34, Build-Tools 34.0.0, Platform-Tools, Cmdline-Tools)
- Android NDK (r26c / 26.3.11579264)
- Pre-configured `ANDROID_HOME`, `ANDROID_SDK_ROOT`, and `JAVA_HOME`

# Build and Verification Commands

Run commands within `nix develop` (or via direnv):

- **Build Debug APK**: `./gradlew assembleDebug`
- **Run Lint**: `./gradlew lintDebug`
- **Build Tools**:
  - Make Keyboard Text: `./gradlew :tools:make-keyboard-text:makeText`
  - Make Emoji Keys: `./gradlew :tools:make-emoji-keys:makeEmoji`
- **Clean Build Artifacts**: `./gradlew clean`

# Git Guidelines

- Enter a branch before making changes in the repository.
- Use atomic commits organized logically by component/layer.

# Code Quality

- Aim to use good habits in new code.
- Target modern Android APIs and standards.

# AGENTS.md

- Document in AGENTS.md new workflows and tooling.

<!-- pico-cli:plugin-context:pico-spatial-agentic-tools:start -->
## Plugin Context

Also read `./PICO-SPATIAL-AGENTIC-TOOLS.AGENTS.md` for PICO Spatial plugin guidance.
<!-- pico-cli:plugin-context:pico-spatial-agentic-tools:end -->

<!-- pico-cli:plugin-context:pico-general-agentic-tools:start -->
## Plugin Context

Also read `./PICO-GENERAL-AGENTIC-TOOLS.AGENTS.md` for PICO Spatial plugin guidance.
<!-- pico-cli:plugin-context:pico-general-agentic-tools:end -->

## Current app handoff — 2026-11-04 / 1.3
- Main.kt creates one shared GameViewModel and registers GameWindows.kt; the native DinerOrders window hosts EntryPanel until entry.entered.
- Accounts are device-local through AccountUseCase / AccountRepository, salted PBKDF2; never claim a configured cloud backend. Credentials are excluded from backup.
- Guest mode uses GuestDemoUseCase on the existing visible-app clock; TakeOver disables automation without discarding food. Login/register enters manual mode.
- GameViewModel rejects gameplay while the entry gate is closed. 37 unit tests passed for this release.
- Existing architecture checker assumes GameUiState data class and viewModel(factory) per screen; this app intentionally uses domain GameState alias and a shared multi-window owner. Full workflow checker also flags pre-existing Stage immersion metadata / wildcard imports; do not claim clean full workflow admission.
- Build: JAVA_HOME=/Users/apple/.pico/primer-cli/jdk/jdk-21.0.12.1+1/Contents/Home ./gradlew --offline --no-daemon assembleDebug testDebugUnitTest

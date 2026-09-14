# ChadPLUS

**ChadPLUS** is a high-performance, modular Paper 1.21+ (Java 21) chat management and server protection plugin engineered by **rabusoore**. Designed for modern Minecraft networks, it provides per-player chat controls, smart anti-bypass word filtering, scheduled multi-target broadcasts, and sensitive command masking.

## Key Features

* **Personal Chat Control:** Running `/chat off` mute-filters incoming player chat exclusively for your screen while keeping server broadcasts, system announcements, and action bar alerts fully intact. `/chat clear` wipes 100 chat lines for the executor only.
* **Multi-Task Auto Broadcaster:** Schedule independent timed announcements in `broadcasts.yml` featuring `SEQUENTIAL` or `RANDOM` execution, custom sound effects, and multi-display targets (Chat, Action Bar, Title).
* **Anti-Bypass Moderation Engine:** Catches evasion attempts via leetspeak normalization (`4su`, `@njing`), unicode normalization, and character repetition reduction (`kaaa-saaa-r`), operating in `CENSOR` or `BLOCK` mode.
* **False-Positive Whitelist Protection:** Tokenized whitelist checking resolves Scunthorpe problem issues, keeping clean words like `"masuk"` or `"assassin"` safe from false triggers.
* **Command Masking & Preprocess Intercept:** Hides sensitive commands (`/op`, `/plugins`, `/luckperms`) from tab-completion (`PlayerCommandSendEvent`) and returns a fake `Unknown command` message on direct execution attempts.
* **Anti-Spam & Cooldown Engine:** Separate chat and command cooldown timers + back-to-back duplicate message blocker.
* **Adventure Color Engine:** Paper MiniMessage support (`<gradient...>`, `<rainbow>`), Hex RGB (`#RRGGBB`, `&#RRGGBB`), and legacy ampersand codes (`&a`, `&l`).

---

## Commands & Permissions

| Command | Permission | Function |
| :--- | :--- | :--- |
| `/chat clear` (`/clearchat`) | `chadplus.use.clearchat` | Wipes 100 blank chat lines on your screen only. |
| `/chat on` / `/chat off` | `chadplus.use.chattoggle` | Enables or disables incoming player messages for yourself. |
| `/chat toggle` | `chadplus.use.chattoggle` | Toggles your personal chat reception state ON/OFF. |
| `/chat reload` | `chadplus.admin.reload` | Hot-reloads all 4 YAML config files without a server reboot. |
| `/broadcast send <msg>` | `chadplus.admin.broadcast.send` | Sends a manual global broadcast with MiniMessage/Hex support. |
| `/broadcast play <id>` | `chadplus.admin.broadcast.play` | Manually triggers a specific broadcast entry by ID. |
| `/autobroadcast toggle` | `chadplus.admin.autobroadcast` | Pauses or resumes scheduled automated broadcasts. |

### Passive & Bypass Permissions

* `chadplus.admin.unhidecommands` — Grants access to hidden sensitive commands in tab-completion.
* `chadplus.bypass.cooldown.chat` — Bypasses chat cooldowns and duplicate message prevention.
* `chadplus.bypass.cooldown.command` — Bypasses command execution cooldowns.
* `chadplus.bypass.filter` — Bypasses the bad word filtering engine.

---

## Multi-YAML Architecture

The plugin uses modular configuration management divided across 4 dedicated YAML files:

* `config.yml` — Prefix, global settings, chat cooldowns, duplicate anti-spam, and system messages.
* `broadcasts.yml` — Timed automated broadcast tasks, sound settings, and display targets.
* `badwords.yml` — Censorship engine settings, prohibited word list, and whitelist entries.
* `commands.yml` — Sensitive command list for tab-completion masking and execution blocking.

---

## Technical Specifications

* **Target Platform:** Paper 1.21+ (Purpur & forks supported)
* **Java Version:** Java 21
* **Build Tool:** Apache Maven
* **Soft Dependencies:** LuckPerms, Essentials

---

**Author:** rabusoore  
**License:** MIT

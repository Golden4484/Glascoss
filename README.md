<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/Golden4484/Glascoss/main/src/main/resources/assets/glascoss/textures/icon.png">
  <img alt="Glascoss" src="src/main/resources/assets/glascoss/textures/icon.png" width="128" align="right">
</picture>

# Glascoss

Glascoss is a Minecraft mod (Fabric 1.20.1) that places an artificial intelligence directly into your world. It listens to chat, executes commands, builds structures, teleports, gives items, and interacts with the environment — all driven by the Google Gemini API.

It does not add visible entities or new mobs. There is no NPC to follow you around. The AI reacts to whatever you type in chat and responds accordingly, with memory of past conversations and awareness of the world around you.

---

## How it works

A player types a message. The mod captures it, packages it with context (position, biome, health, inventory, facing direction, time of day, recent conversation history), and sends it to the Gemini API. The AI generates a response that may include tool calls between `[TOOL: ...]` brackets. The mod parses those, executes them server-side (build, teleport, give items, summon, etc.), and broadcasts the final response to all players.

The system uses a two-pass retry mechanism: if a tool call fails (e.g. invalid coordinates, missing blocks), the error is fed back to the AI so it can correct itself before the player sees anything.

---

## Features

- **Chat-based AI interaction** — talk to the AI through normal chat. No commands required.
- **Tool system** — the AI can use tools to affect the world:
  - `place_block` — place a block at a position
  - `fill_area` — fill a region with blocks
  - `give_item` — give items to the player
  - `summon_entity` — summon entities
  - `teleport_player` — teleport the player
  - `run_command` — execute any server command
  - `time_query`, `block_query`, `entity_query`, `player_query`, `inventory_query` — read world state
  - `set_block`, `till_soil`, `plant_seed`, `place_chest`, `fill_chest` — survival-oriented building
- **Personality system** — configure the AI's behavior and tone per world (sarcastic, aggressive, friendly, neutral, formal, custom)
- **Persistent memory** — the AI remembers past conversations and builds a semantic summary of the world over time
- **Trust and mood system** — the AI tracks how politely you speak and adjusts its responses accordingly
- **Command filtering** — `/tp`, `/locate`, `/teleport` commands used internally are hidden from chat
- **Full localization** — all interface text is translatable via Minecraft's language system (en_us and pt_br included)
- **Death tracking** — the AI is notified when you die and can react to it
- **Action log** — view AI tool calls and results in a built-in log screen

---

## Requirements

- Minecraft **1.20.1**
- **Fabric Loader** 0.15.0 or newer
- **Fabric API**
- Google Gemini **API key**

---

## Getting a Gemini API key

1. Go to https://aistudio.google.com/app/apikey
2. Sign in with your Google account
3. Click **"Create API key"**
4. Copy the generated key (starts with `AIzaSy...`)
5. Launch Minecraft and paste it in the Glascoss configuration screen (title screen button or `/glascoss apikey <key>`)

The free tier includes 500 requests per day on `gemini-2.1-flash-lite`, which is more than enough for normal gameplay. No credit card is required.

### Common API errors

| Error | Cause | Solution |
|-------|-------|----------|
| `Quota exceeded` / `RESOURCE_EXHAUSTED` | Free tier daily limit reached | Wait a few hours or switch to a different model in settings |
| `API key not valid` / `API key not found` | Wrong or revoked key | Generate a new key at aistudio.google.com |
| `Model not found` | Invalid model name | Use `gemini-2.1-flash-lite` or `gemini-2.0-flash`. Check available models in Google AI documentation |
| `Permission denied` | API key restrictions | Make sure the key has Generative Language API enabled in Google Cloud Console |
| `Rate limit` | Too many requests in a short period | The mod handles this automatically, but if it persists, wait a minute |

If the API key is missing or invalid, the mod simply does not respond to chat. No crashes or errors occur.

---

## Installation

1. Download the mod JAR from the Releases page
2. Place it in your `mods/` folder
3. Launch the game
4. Configure your API key:
   - Click the **Glascoss** button on the title screen (top-left corner), or
   - Run `/glascoss apikey <your_key>` in-game

The button on the title screen has the Glascoss logo and opens the full configuration screen, where you can change models, toggle command display, manage personalities, and reset memory.

---

## Usage

Open chat and type normally. The AI will respond to non-command messages automatically. Messages starting with `/` are ignored and handled by Minecraft as usual.

You can ask the AI to:
- Build structures ("build a 5x5 oak house at the nearest open area")
- Give you items ("give me a diamond sword with sharpness V")
- Teleport you ("take me to the nearest village")
- Tell you about the world ("what is this biome?", "where is the nearest desert?")
- Interact with your inventory ("what's in my inventory?")
- Remember things ("remember that this is my base")

---

## Configuration

| Setting | Description |
|---------|-------------|
| API Key | Your Gemini API key |
| Model | Gemini model name (default: gemini-2.1-flash-lite) |
| Show commands | Toggle display of executed commands in chat |
| Disable on online servers | Disable AI when connected to a remote server |
| Personality | Select or create AI personalities per world |

Personalities persist per world. Switching personalities resets the conversation history for that world. Each personality has a name, tone, and directive that controls how the AI behaves.

---

## Known limitations

- Designed for **single-player and LAN worlds**. Online server support is experimental and may have unpredictable behavior.
- **Mobile and Bedrock Edition** are not and will not be supported. The mod relies on Java Edition server-side mechanics that do not exist on other platforms.
- **Mods that add new blocks, items, or entities** may cause issues. The AI uses standard Minecraft identifiers and has no knowledge of custom content. It may try to use modded blocks incorrectly or fail to recognize them. Use with such mods at your own risk.
- **World type**: the mod was designed for standard Overworld gameplay. The AI may behave unexpectedly in modded dimensions, superflat worlds, or custom map worlds.
- The AI may occasionally fail to parse tool calls correctly. The automatic retry system handles most cases, but some edge cases may still produce visible errors in chat.
- The Gemini API free tier has a limit of 500 requests per day on `gemini-2.1-flash-lite`. Under heavy use this limit can be reached. Either wait for the daily reset, or switch to a paid tier model.

---

## Building from source

Requires JDK 17 or newer.

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`.

---

## Contributing

Contributions are welcome. Open an issue or submit a pull request. This is a personal project and may not see frequent updates, but suggestions and improvements are appreciated.

---

## License

MIT License — see [LICENSE](LICENSE) for details.

Copyright (c) 2026 JoaquimLegal

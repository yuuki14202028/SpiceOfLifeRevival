# Spice of Life: Revival

**Eat the same thing too often and it stops filling you up.**

Spice of Life: Revival brings back the diminishing-returns diet of the classic *The Spice of Life* mod for modern Minecraft. Steak every meal? Your character gets bored of it — each repeat restores less hunger and saturation and takes longer to chew. Mix up your meals and everything tastes good again.

No food groups, no journals, no extra items. Just one simple rule that makes every farm, garden and fishing trip worth it.

---

## How it works

Every player remembers their last **12 meals**. How much a food is worth depends on how many times it is already in that memory:

| Already eaten (in the last 12 meals) | Value | Time to eat |
|---|---|---|
| 0 times | 100% | normal |
| 1 time | 100% | normal |
| 2 times | 75% | ×1.3 |
| 3 times | 50% | ×2 |
| 4 times | 25% | ×4 |
| 5 or more | 0% | ×8 |

- **A second helping is free.** Eating something twice in a row costs you nothing — it is the third, fourth and fifth that start to hurt.
- **Boring food is slow food.** Like the original mod, food that has lost value takes longer to eat (normal time ÷ value).
- **Variety resets it.** Eat other things and the old meals are pushed out of memory, bringing the food back to full value.
- **Cake counts too.** Every slice is remembered, candle cakes included.
- While a food is worth anything at all, it still restores at least one hunger point.

A message above the hotbar tells you when you are getting tired of something, and the tooltip shows a food's current value once it has dropped.

## Features

- **AppleSkin support** — AppleSkin's tooltip and HUD previews show the reduced values, so what you see is what you get.
- **Server-side logic** — the server decides everything. Install it on a server alone and diminishing returns already work for every player; add it to clients for slower eating, tooltips, AppleSkin previews and translated messages. Players without the mod can still join.
- **Fully configurable** — history length, the value table, eating speed, death behaviour and exempt foods.
- **Commands**
  - `/solrevival` — shows your recent meals and what the food in your hand is worth right now.
  - `/solrevival reset [players]` — clears meal history (operators only).
- **Translated** into English, Japanese, Simplified and Traditional Chinese, Korean, German, French, Spanish (Spain and Mexico), Brazilian Portuguese, Russian, Ukrainian, Italian and Polish.

## Configuration

The config file is created at `config/solrevival.properties` on first launch. Restart the game or server after editing it.

| Option | Default | Description |
|---|---|---|
| `history_size` | `12` | How many recent meals are remembered |
| `multipliers` | `1.0,1.0,0.75,0.5,0.25,0.0` | Value by times already eaten. The last value repeats |
| `keep_history_on_death` | `true` | Keep meal history when you die. Set to `false` to reset it, like the original |
| `show_messages` | `true` | Show a message when food has lost value |
| `exempt_items` | *(empty)* | Comma-separated item IDs that never get boring, e.g. `minecraft:golden_apple` |
| `eating_speed_modifier` | `1.0` | Eating time = normal time ÷ value ^ this. `0` turns slower eating off |
| `max_eating_time_multiplier` | `8.0` | Longest eating time, as a multiple of normal. `0` means no limit, like the original |

## Compared with the original

*The Spice of Life* (1.6.4–1.12.2) used the formula `(1 − count/12) ^ min(8, hunger)`, so a single repeat of steak already halved its value. Revival keeps the spirit — and the slower eating — but uses a plain table that is easy to read and easy to tune, and lets you have seconds before it starts to bite. Want it harsher? Set `multipliers` to something like `1.0,0.5,0.25,0.0`.

## Requirements

- Minecraft **26.3**
- **Fabric** with [Fabric API](https://modrinth.com/mod/fabric-api), or **NeoForge**
- Optional: [AppleSkin](https://modrinth.com/mod/appleskin)

## Credits

Inspired by [The Spice of Life](https://www.curseforge.com/minecraft/mc-mods/the-spice-of-life) by squeek502. This is an independent reimplementation and is not affiliated with the original mod or its author.

Source code and issue tracker on [GitHub](https://github.com/yuuki14202028/SpiceOfLifeRevival). Licensed under MIT.

---

## 日本語

同じ食べ物を続けて食べると、回復量が減り、食べ終わるまでの時間も長くなります。いろいろな物を食べれば元に戻ります。

- 直近12食を記憶し、同じ物が何回あるかで回復量が決まります（2回目までは100%、そこから75%・50%・25%・0%）。
- 回復量が減った食べ物ほど、食べるのに時間がかかります（本家と同じ仕組み）。
- ケーキも対象です。AppleSkin に対応しています。
- サーバーだけに入れても減衰は効きます。食べる時間の延長・ツールチップ・AppleSkin 連携にはクライアントにも必要です。
- 設定は `config/solrevival.properties` で変更できます。

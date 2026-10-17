# Spice of Life: Revival

Minecraft 26.3向けの、本家The Spice of Life（squeek502作）と同じ「減衰型」の食事多様性Modです。
同じ食べ物を続けて食べるほど、満腹度と隠し満腹度の回復量が減っていきます。
別の物を食べて記憶から押し出せば、また元の回復量に戻ります。Fabric と NeoForge の両方に対応しています。

## 仕組み

- プレイヤーごとに直近 **12食** を記憶します。
- 食べた物がその12食の中に何回あるかで、回復量が決まります。

| 直近12食での回数 | 回復量 | 食べる時間 |
|---|---|---|
| 0回（久しぶり） | 100% | 通常 |
| 1回 | 100% | 通常 |
| 2回 | 75% | 約1.3倍 |
| 3回 | 50% | 2倍 |
| 4回 | 25% | 4倍 |
| 5回以上 | 0% | 8倍（上限） |

- 1回食べただけでは下がりません。2回目のおかわりから減り始めます。
- 本家と同じく、回復量が減った食べ物ほど食べ終わるまでに時間がかかります（`通常の時間 ÷ 回復割合`）。
- 回復量が少しでも残る間は、満腹度は最低1回復します。
- 回復量が減ったときは、ホットバーの上にメッセージが出ます。
- 回復量が減った食べ物は、ツールチップに今の回復量（例：`回復量: 50%`）が出ます。AppleSkin を入れている場合は、AppleSkin の表示が減衰後の値になるので、この行は出しません。
- [AppleSkin](https://modrinth.com/mod/appleskin) を入れていると、回復予測が減衰後の値になります。
- 食事の記憶はワールドに保存され、死んでも引き継がれます（設定で変更できます）。
- ケーキも1切れごとに記録され、同じように減ります（ロウソク付きケーキも同じ扱い）。
- `/solrevival` で直近の食事と、手に持っている食べ物の今の回復量を確認できます。
- `/solrevival reset [プレイヤー]` で食事の記録を消せます（OP権限が必要。省略すると自分）。
- 回復量の計算はサーバー側で行います。サーバーだけに入れても減衰は効きます。食べる時間の延長・ツールチップ・AppleSkin 連携・翻訳されたメッセージには、クライアントにも入れる必要があります。
- 日本語・英語のほか、中国語（簡体・繁体）、韓国語、ドイツ語、フランス語、スペイン語、ポルトガル語、ロシア語、ウクライナ語、イタリア語、ポーランド語に対応しています。

### 本家・派生版との違い

| | 減り方 | 1回で下がるか | 食べる時間 | 死亡時 |
|---|---|---|---|---|
| 本家 The Spice of Life（〜1.12.2） | `(1 - 回数/12)^min(8, 満腹度)`。ステーキは1回で50%、パンは60% | 下がる（最初の6食だけ猶予あり） | `通常 ÷ 回復割合`、上限なし | リセット |
| Classic Edition | 直近5食の回数で 1.0 / 0.9 / 0.75 / 0.5 / 0.05、さらに長期の回数で少し下がる | 下がる（90%） | 変わらない | — |
| Carrot / Potato / Onion Edition | 減らない。いろいろな種類を食べるとハートなどが増える | — | — | — |
| このMod（既定値） | 回数ごとの表で決まる（上の表） | 下がらない | `通常 ÷ 回復割合`、上限8倍 | 引き継ぐ |

## 設定

初回起動時に `config/solrevival.properties` が作られます。編集後はゲームかサーバーを再起動してください。

| 項目 | 既定値 | 内容 |
|---|---|---|
| `history_size` | `12` | 記憶する食事の数 |
| `multipliers` | `1.0,1.0,0.75,0.5,0.25,0.0` | 回数ごとの回復割合。最後の値が以降も続く |
| `keep_history_on_death` | `true` | 死亡時に記憶を引き継ぐか。`false` で本家と同じく死ぬとリセット |
| `show_messages` | `true` | 回復量が減ったときにメッセージを出すか |
| `exempt_items` | （空） | 減衰しない食べ物のID（カンマ区切り） |
| `eating_speed_modifier` | `1.0` | 食べる時間 = 通常の時間 ÷ 回復割合^この値。`0` で延長しない |
| `max_eating_time_multiplier` | `8.0` | 食べる時間の上限（通常の何倍まで）。`0` で上限なし（本家と同じ。0%の食べ物は30分近くかかる） |

## ビルド

**必要なもの：JDK 25**（Minecraft 26.x の要件です）

```sh
./gradlew build
```

Windows では `gradlew.bat build` を使います。

できたjarは次の場所に出力されます。

- Fabric：`fabric/build/libs/solrevival-fabric-26.3-<version>.jar`（Fabric API が必要）
- NeoForge：`neoforge/build/libs/solrevival-neoforge-26.3-<version>.jar`

開発用のクライアントを起動して試すには、次のコマンドを使います（AppleSkin も一緒に読み込まれます）。

```sh
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

IntelliJ IDEA でフォルダを開けば、Gradle プロジェクトとして読み込まれます。

## 構成

[MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template)（26.3ブランチ）をもとにしています。

```
common/    両ローダー共通のコード（ほぼ全部ここ）
  DietManager.java          減衰の計算とメッセージ（サーバー・クライアント両方）
  DietRules.java            クライアントに送る設定（回復割合と食べる時間の計算）
  ClientDietState.java      サーバーから届いた食事の記憶（クライアント側）
  FoodHistory.java          直近の食事の記憶
  network/SyncDietPayload   食事の記憶と設定をクライアントへ送るパケット
  SolConfig.java            設定ファイル
  SolCommands.java          /solrevival コマンド
  mixin/FoodPropertiesMixin  食べ終わった瞬間の回復量を差し替える
  mixin/PlayerMixin          食事の記憶をプレイヤーデータに保存する
  mixin/ServerPlayerMixin    リスポーン時に記憶を引き継ぐ／変わったらクライアントへ送る
  mixin/ItemMixin            食べる時間を延ばす
  mixin/ItemStackMixin       延ばした食事でも最初から咀嚼音を鳴らす
  mixin/CakeBlockMixin       ケーキにも減衰を適用する
fabric/    Fabric用の起動処理とコマンド登録
neoforge/  NeoForge用の起動処理とコマンド登録
```

回復量の差し替えは、バニラの `FoodProperties.onConsume` 内にある `FoodData.eat(...)` 呼び出しを、MixinExtras の `@WrapOperation` で包んで行います。
NeoForge はこの2つのクラスにパッチを当てていないため、両ローダーで同じMixinが使えます。

## 今後の候補

- 食事の記憶を時間経過でも忘れるようにする

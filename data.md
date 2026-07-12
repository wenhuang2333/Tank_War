# 坦克大战——JSON 存档数据结构设计

---

## 一、存档文件概述

### 1.1 文件命名与位置

- **默认文件名**：`save_data.json`
- **默认路径**：`save/save_data.json`（相对于游戏 JAR 包或项目根目录）
- **编码格式**：UTF-8
- **序列化工具**：Gson（`com.google.code.gson:gson:2.10.1`）

### 1.2 整体结构

```
PlayerSaveData (根对象)
├── meta              # 存档元信息
├── resources         # 玩家资源
├── ownedTanks[]      # 拥有的坦克列表
├── modificationInv   # 改装装备库存
├── gachaState        # 抽卡系统状态
└── settings          # 游戏设置（可选）
```

---

## 二、详细数据结构

### 2.1 根对象：PlayerSaveData

```json
{
  "meta": { ... },
  "resources": { ... },
  "ownedTanks": [ ... ],
  "modificationInv": { ... },
  "gachaState": { ... },
  "battleHistory": { ... },
  "settings": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `meta` | `SaveMeta` | 存档元信息（版本、时间戳、玩家名） |
| `resources` | `Resources` | 玩家拥有的货币与资源 |
| `ownedTanks` | `OwnedTank[]` | 玩家已拥有的所有坦克及其完整状态 |
| `modificationInv` | `ModificationInventory` | 改装装备碎片与成品库存 |
| `gachaState` | `GachaState` | 两个抽卡池的保底计数 |
| `battleHistory` | `BattleHistory` | 玩家历史对战统计（可选） |
| `settings` | `GameSettings` | 全局游戏设置（可选） |

### 2.2 元信息：SaveMeta

```json
{
  "version": "1.0",
  "saveTime": 1752624000000,
  "playerName": "Player1",
  "totalPlayTime": 36000,
  "totalBattles": 42
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `version` | `String` | 存档格式版本号，用于升级兼容 |
| `saveTime` | `long` | 存档时间戳（epoch ms） |
| `playerName` | `String` | 玩家名称 |
| `totalPlayTime` | `long` | 累计游戏时长（秒） |
| `totalBattles` | `int` | 累计完成对局数 |

### 2.3 资源：Resources

```json
{
  "iron": 5000,
  "steel": 300,
  "blueprints": 15
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `iron` | `int` | 粗铁数量（用于升级） |
| `steel` | `int` | 钢铁数量（用于进阶） |
| `blueprints` | `int` | 蓝图数量（用于抽卡） |

### 2.4 拥有的坦克：OwnedTank

```json
{
  "tankId": 1,
  "customName": "我的克伦威尔",
  "rank": 3,
  "level": 7,
  "combatStats": { ... },
  "installedMods": [ ... ],
  "cooldownSlots": [ ... ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `tankId` | `int` | 坦克类型 ID（1-8，对应克伦威尔~虎式） |
| `customName` | `String` | 用户自定义的坦克名称 |
| `rank` | `int` | 阶数（0-10） |
| `level` | `int` | 等级（1-10） |
| `combatStats` | `CombatStats` | 当前战斗属性（含养成加成后的实时值） |
| `installedMods` | `InstalledMod[]` | 已安装的改装装备列表 |
| `cooldownSlots` | `CooldownSlot[]` | 处于冷却中的改装槽 |

#### CombatStats（战斗属性）

```json
{
  "hp": 560,
  "attack": 85,
  "defense": 42,
  "speed": 40,
  "turnSpeed": 10,
  "fireRate": 900,
  "bulletSpeed": 42,
  "bulletDuration": 5,
  "ammo": 7,
  "reloadTime": 3,
  "maxDurability": 880
}
```

此对象保存的是**当前时态下的完整属性值**（基础值 + 升级/进阶累积加成）。读档后直接赋值，无需重复计算养成历史。

#### InstalledMod（已安装的改装装备）

```json
{
  "slotIndex": 0,
  "modType": "LASER_CANNON",
  "source": "CRAFTED_SPECIFIC"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `slotIndex` | `int` | 安装在第几个改装槽（0-4） |
| `modType` | `String` | 装备类型（枚举名，详见 2.5.1） |
| `source` | `String` | 来源——`CRAFTED_SPECIFIC`（特定碎片合成）、`CRAFTED_UNIVERSAL`（通用碎片合成） |

#### CooldownSlot（冷却中的改装槽）

```json
{
  "slotIndex": 1,
  "remainingBattles": 3
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `slotIndex` | `int` | 改装槽索引（0-4） |
| `remainingBattles` | `int` | 剩余冷却对局数（每次对局结束 -1，归零后该槽变为可用） |

### 2.5 改装装备库存：ModificationInventory

```json
{
  "specificFragments": { ... },
  "universalFragments": 12,
  "craftedEquipments": [ ... ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `specificFragments` | `Map<String, Integer>` | 各类型特定碎片数量（key = 装备类型枚举名） |
| `universalFragments` | `int` | 通用改装碎片数量 |
| `craftedEquipments` | `String[]` | 已合成但尚未安装的装备类型列表 |

#### 2.5.1 改装装备类型枚举

| 枚举名 | 中文名 | 稀有度 |
|--------|--------|--------|
| `ANTI_FRIENDLY_FIRE` | 反斜钢甲 | 5 |
| `INSTANT_TURN` | 扭绞轮台 | 6 |
| `EXPLOSION_PROOF` | 防爆油箱 | 7 |
| `LIGHT_MACHINE_GUN` | 轻机枪 | 3 |
| `HEAVY_MACHINE_GUN` | 重机枪 | 4 |
| `LASER_CANNON` | 激光炮 | 6 |
| `DENSE_LASER` | 高密度镭射群炮 | 7 |
| `AUTO_LOADER` | 自动装填器 | 7 |
| `EXTRA_AMMO` | 额外弹药架 | 4 |

**互斥规则**：轻机枪、重机枪、激光炮、高密度镭射群炮同一坦克只能安装其一。

#### 2.5.2 改装碎片库存示例

```json
"specificFragments": {
  "ANTI_FRIENDLY_FIRE": 8,
  "INSTANT_TURN": 3,
  "LASER_CANNON": 20,
  "AUTO_LOADER": 15,
  "EXTRA_AMMO": 5
}
```

未列出的类型默认为 0 碎片。

### 2.6 抽卡状态：GachaState

```json
{
  "tankPool": {
    "pityCounter": 37,
    "ownedTankIds": [1, 2]
  },
  "modPool": {
    "pityCounter": 12
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `tankPool.pityCounter` | `int` | 坦克池累计抽数（抽出新坦克时清零） |
| `tankPool.firstTimeBonus` | `Map<Integer, Boolean>` | 各坦克是否已领取过首次获得奖励（100 钢铁）|
| `modPool.pityCounter` | `int` | 改装装备池累计抽数（抽出目标大奖时清零） |

### 2.7 对战历史统计：BattleHistory

```json
{
  "totalPlayed": 42,
  "totalWins": 30,
  "totalLosses": 12,
  "byMode": {
    "easy":   { "played": 15, "wins": 12 },
    "hard":   { "played": 10, "wins": 7 },
    "super":  { "played": 8,  "wins": 4 },
    "pvp":    { "played": 9,  "wins": 7 }
  },
  "totalShotsFired": 1500,
  "totalDamageDealt": 50000,
  "totalDamageReceived": 38000
}
```

### 2.8 全局游戏设置：GameSettings

```json
{
  "bgmVolume": 0.8,
  "sfxVolume": 1.0,
  "language": "zh_CN",
  "defaultDifficulty": "hard"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `bgmVolume` | `float` | 背景音乐音量（0.0 - 1.0）|
| `sfxVolume` | `float` | 音效音量（0.0 - 1.0）|
| `language` | `String` | 界面语言 |
| `defaultDifficulty` | `String` | 默认 AI 难度 |

---

## 三、完整存档示例

```json
{
  "meta": {
    "version": "1.0",
    "saveTime": 1752624000000,
    "playerName": "坦克指挥官",
    "totalPlayTime": 72000,
    "totalBattles": 42
  },
  "resources": {
    "iron": 5200,
    "steel": 280,
    "blueprints": 15
  },
  "ownedTanks": [
    {
      "tankId": 1,
      "customName": "克伦威尔",
      "rank": 3,
      "level": 7,
      "combatStats": {
        "hp": 560,
        "attack": 85,
        "defense": 42,
        "speed": 40,
        "turnSpeed": 10,
        "fireRate": 900,
        "bulletSpeed": 42,
        "bulletDuration": 5,
        "ammo": 7,
        "reloadTime": 3,
        "maxDurability": 880
      },
      "installedMods": [
        {
          "slotIndex": 0,
          "modType": "LASER_CANNON",
          "source": "CRAFTED_SPECIFIC"
        }
      ],
      "cooldownSlots": [
        {
          "slotIndex": 1,
          "remainingBattles": 3
        }
      ]
    },
    {
      "tankId": 3,
      "customName": "萤火虫",
      "rank": 1,
      "level": 8,
      "combatStats": {
        "hp": 280,
        "attack": 140,
        "defense": 28,
        "speed": 65,
        "turnSpeed": 10,
        "fireRate": 450,
        "bulletSpeed": 72,
        "bulletDuration": 4,
        "ammo": 12,
        "reloadTime": 3,
        "maxDurability": 400
      },
      "installedMods": [],
      "cooldownSlots": []
    }
  ],
  "modificationInv": {
    "specificFragments": {
      "ANTI_FRIENDLY_FIRE": 8,
      "INSTANT_TURN": 3,
      "LASER_CANNON": 20,
      "AUTO_LOADER": 15,
      "EXTRA_AMMO": 5
    },
    "universalFragments": 12,
    "craftedEquipments": [
      "EXTRA_AMMO"
    ]
  },
  "gachaState": {
    "tankPool": {
      "pityCounter": 37
    },
    "modPool": {
      "pityCounter": 12
    },
    "firstTimeBonus": {}
  },
  "battleHistory": {
    "totalPlayed": 42,
    "totalWins": 30,
    "totalLosses": 12,
    "byMode": {
      "easy":  { "played": 15, "wins": 12 },
      "hard":  { "played": 10, "wins": 7 },
      "super": { "played": 8,  "wins": 4 },
      "pvp":   { "played": 9,  "wins": 7 }
    },
    "totalShotsFired": 1500,
    "totalDamageDealt": 50000,
    "totalDamageReceived": 38000
  },
  "settings": {
    "bgmVolume": 0.8,
    "sfxVolume": 1.0,
    "language": "zh_CN",
    "defaultDifficulty": "hard"
  }
}
```

---

## 四、Java 实体类定义

### 4.1 SaveManager（存档管理器，单例）

位于 `model.manager.SaveManager`，负责存档的序列化与反序列化。

```java
public class SaveManager {
    private static SaveManager instance;
    private static final String DEFAULT_SAVE_DIR = "save/";
    private static final String DEFAULT_SAVE_FILE = "save_data.json";
    private static final String SAVE_VERSION = "1.0";

    private Gson gson;
    private File currentSaveFile;  // 当前使用的存档文件路径

    private SaveManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static SaveManager getInstance() { /* DCL 单例 */ }

    // 保存存档到默认位置
    public boolean save(PlayerSaveData data);

    // 保存存档到指定路径
    public boolean saveAs(PlayerSaveData data, File file);

    // 从默认位置加载存档
    public PlayerSaveData load();

    // 从指定路径加载存档
    public PlayerSaveData load(File file);

    // 扫描默认目录下所有存档文件
    public List<File> listSaveFiles();

    // 检查默认存档文件是否存在
    public boolean hasExistingSave();

    // 删除存档文件
    public boolean deleteSave(File file);
}
```

### 4.2 PlayerSaveData（存档根对象）

位于 `model.vo.PlayerSaveData`。

```java
public class PlayerSaveData {
    private SaveMeta meta;
    private Resources resources;
    private List<OwnedTank> ownedTanks;
    private ModificationInventory modificationInv;
    private GachaState gachaState;
    private BattleHistory battleHistory;
    private GameSettings settings;

    // 工厂方法：创建新档（两个初始坦克 + 零资源）
    public static PlayerSaveData createNew(String playerName);

    // getters / setters ...
}
```

### 4.3 各子对象类

均位于 `model.vo` 包下，作为 POJO（Plain Old Java Object），字段与 JSON 一一对应：

```
model.vo.
├── PlayerSaveData.java        (根对象)
├── SaveMeta.java              (元信息)
├── Resources.java             (资源)
├── OwnedTank.java             (拥有的坦克)
├── CombatStats.java           (战斗属性——实时值快照)
├── InstalledMod.java          (已安装装备)
├── CooldownSlot.java          (冷却槽)
├── ModificationInventory.java (改装库存)
├── GachaState.java            (抽卡状态)
├── TankPoolState.java         (坦克池状态)
├── ModPoolState.java          (改装池状态)
├── BattleHistory.java         (对战历史)
├── ModeStats.java             (单模式统计)
└── GameSettings.java          (全局设置)
```

### 4.4 存档生命周期

```
程序启动 → 显示登录界面
  → 用户点击"登录"
    → SaveManager.listSaveFiles() 扫描 save/ 目录
    → 展示可用存档列表
      → 用户选择已有存档 → SaveManager.load(file) 加载
      → 用户选择"创建新档" → 输入玩家名 → PlayerSaveData.createNew(name)
  → 进入主菜单（数据已加载到内存）

游戏中（对战结算 / 养成操作 / 抽卡后）
  → 更新内存中的 PlayerSaveData 对象
  → 自动调用 SaveManager.save(data) 保存到当前存档文件

用户点击"退出游戏"
  → 弹出对话框：是否存档？
    → [是] → SaveManager.saveAs(data, userChosenFile) 保存到指定路径
    → [否] → 放弃本次会话数据变更，直接退出
    → [取消] → 返回游戏
```

### 4.5 存档兼容策略

```java
// SaveMeta 中包含版本号
// 加载时检查版本，若旧版本则执行迁移
public PlayerSaveData load(File file) {
    JsonObject root = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
    String version = root.getAsJsonObject("meta").get("version").getAsString();

    if (!SAVE_VERSION.equals(version)) {
        root = migrate(root, version);  // 升级到当前版本
    }
    return gson.fromJson(root, PlayerSaveData.class);
}
```

---

## 五、关键设计决策

| 决策 | 理由 |
|------|------|
| **CombatStats 存实时值而非基础值** | 避免读档时重放升级/进阶历史，简化逻辑。养成操作已经发生，属性已确定，存档只需记录结果 |
| **改装碎片按类型分字段而非数组** | 9 种装备类型固定，直接用 Map 比数组更清晰，未出现的类型默认为 0 |
| **冷却槽只存剩余对局数** | 冷却触发条件固定（5 次对局），无需记录总数 |
| **GachaState 独立于坦克池** | 两个池的保底分开计算，数据结构也分开存储 |
| **不存档中间对战状态** | 存档只在非战斗状态下进行——登录时、主菜单退出时、养成操作后自动保存。对战过程中的暂停状态不存档，简化复杂度 |
| **JSON 格式而非二进制** | 可读、可调试、跨平台、方便玩家手动修改或备份 |

---

*文档版本: v1.0*
*编写日期: 2026年7月*

# 坦克大战——Java Swing 落地实施方案

---

## 一、项目骨架搭建

### 1.1 构建方式选择

**方案：IntelliJ IDEA 项目 + 手动 jar 依赖**

不使用 Maven/Gradle。项目已在 IDEA 中创建，依赖 jar 直接放入 `lib/` 目录，通过 IDEA 的 Project Structure → Libraries 添加。原因：项目依赖极少（仅 Gson 和 commons-collections），引入构建工具反而增加复杂度。

**操作步骤：**
1. 在项目根目录创建 `lib/` 文件夹
2. 下载 `gson-2.10.1.jar` 和 `commons-collections4-4.4.jar` 放入 `lib/`
3. 在 IDEA 中右键 jar → Add as Library
4. `src/` 为源码根目录，`resource/` 为资源根目录

### 1.2 包结构创建顺序

按依赖关系从底向上创建，确保每一层写完后可独立编译验证：

```
第 1 批（零依赖）：util/GameConfig.java, util/CollisionUtil.java
第 2 批（实体基类）：model/vo/SuperElement.java
第 3 批（实体子类）：model/vo/Players.java, Boss.java, Bullet.java, Brick.java, Iron.java, Background.java, Explosion.java
第 4 批（数据模板）：model/vo/TankData.java, model/vo/Modification.java
第 5 批（存档实体）：model/vo/PlayerSaveData.java 及所有子 VO
第 6 批（资源加载）：model/load/ElementLoad.java, model/load/MapData.java
第 7 批（管理器）：model/manager/ElementManager.java, ElementFactory.java, TankDataManager.java, SaveManager.java, GachaManager.java
第 8 批（控制层）：thread/GameThread.java, thread/GameKeyListener.java, thread/ai/*
第 9 批（视图层）：frame/MyJFrame.java, frame/MyGameJPanel.java, frame/ui/*
第 10 批（入口）：main/Main.java
```

### 1.3 全局常量配置（GameConfig.java）

`GameConfig` 为静态常量类，所有模块引用此处而非常量散落各处：

| 常量 | 值 | 说明 |
|------|-----|------|
| `WINDOW_WIDTH` | 1380 | 窗口宽度 |
| `WINDOW_HEIGHT` | 820 | 窗口高度 |
| `TARGET_FPS` | 60 | 目标帧率 |
| `FRAME_DURATION` | 16 | 每帧时长 ms |
| `TILE_SIZE` | 40 | 网格/地砖大小 |
| `TANK_SIZE` | 38 | 坦克渲染大小（略小于地砖） |
| `BULLET_SIZE` | 8 | 子弹渲染大小 |
| `TANK_SPEED_FACTOR` | 0.04 | 坦克移动换算系数 |
| `TURN_SPEED_FACTOR` | 0.01 | 转向换算系数 |
| `BULLET_SPEED_FACTOR` | 0.06 | 子弹速度换算系数 |
| `LASER_SPEED_FACTOR` | 0.6 | 激光速度换算系数 |

---

## 二、资源加载系统

### 2.1 图片资源目录结构

所有图片统一放在 `resource/img/` 下，按模块分目录（已在 `pic.md` 中定义）。加载逻辑：

1. `ElementLoad.init()` 在程序启动时调用
2. 使用 `ImageIO.read(new File(path))` 逐个加载图片
3. 加载后的 `BufferedImage` 存入 `Map<String, BufferedImage> imageCache`，key 为相对路径
4. 后续通过 `ElementLoad.getImage(path)` 从缓存获取
5. 若文件不存在，返回占位图（品红色 40×40 方块 + 黑色叉号），打印警告日志，**程序不崩溃**

### 2.2 地图数据加载

`.map` 文件位于 `resource/data/`，命名格式为 `{关卡号}.map`（如 `1.map`）。

**解析逻辑：**
1. 按行读取文件（UTF-8）
2. 每行格式：`类型=x,y;x,y;...`
3. 以 `BRICK=` 开头 → 解析为砖墙坐标列表
4. 以 `IRON=` 开头 → 解析为钢铁墙坐标列表
5. 坐标对以分号分隔，x 和 y 以逗号分隔
6. 解析失败的行跳过，打印警告，继续处理后续行
7. 全部解析失败（无有效坐标）→ 返回空地图（仅边界墙）

**重要实现细节：**
- 坐标单位是像素，直接用于创建墙壁元素
- 地图数据对象 `MapData` 存储两组 `List<Point>`（砖墙坐标、钢铁墙坐标）
- 地图数据在首次加载后缓存到 `Map<Integer, MapData>`，避免重复读取文件

### 2.3 坦克属性模板加载

8 种坦克的初始属性硬编码在 `TankDataManager` 中（依据《坦克对象.txt》），提供 `getTankData(int id)` 查询接口。每个 `TankData` 包含基础属性和成长属性，用于：
- 创建新坦克时初始化属性
- 升级/进阶时计算属性增幅
- 对战前选择坦克时展示属性

---

## 三、元素基类体系（SuperElement）

### 3.1 SuperElement 抽象类设计

这是整个游戏世界的核心抽象。所有可视、可移动、可碰撞的对象都继承它。

**字段设计（具体实现，非概念）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `x` | `int` | 左上角 x 坐标（像素） |
| `y` | `int` | 左上角 y 坐标（像素） |
| `width` | `int` | 宽度（像素） |
| `height` | `int` | 高度（像素） |
| `direction` | `double` | 朝向角度（度，0=正上方，顺时针） |
| `visible` | `boolean` | 是否可见（false=待移除） |
| `rect` | `Rectangle` | 碰撞矩形（由 x,y,width,height 动态计算） |

**抽象方法（子类必须实现）：**

- `show(Graphics2D g)`：将自己绘制到缓冲区
- `update()`：每帧更新自身状态（移动、计时等）

**通用方法（子类可直接使用）：**

- `destroy()`：将 visible 设为 false，GameThread 在下次清理循环中移除
- `isStrike(SuperElement other)`：AABB 碰撞检测，比较两个 Rectangle 是否相交
- `getRect()`：返回碰撞矩形，根据当前 x,y 更新后返回

**设计要点：**
- `direction` 使用角度制（0-360），因为 `Graphics2D.rotate()` 接收的是弧度，在 show 中转换
- `rect` 每次调用 `getRect()` 时根据当前 x,y 重新计算，避免状态不一致
- `destroy()` 不立即从 ElementManager 移除，避免在遍历中修改集合导致 `ConcurrentModificationException`

### 3.2 各子类实现要点

#### Players（玩家坦克）

- 属性分为三组：**战斗属性**（hp, attack, defense, speed...）、**成长属性**（upgradeAtkGain 等）、**改装系统**（installedMods 数组, cooldownSlots 数组）
- `update()` 中处理：移动状态持续执行、换弹倒计时、改装效果持续生效
- `show(Graphics2D g)` 中：`g.rotate(rad, centerX, centerY)` 后绘制坦克图片，坦克图片本身朝上（0°方向）
- `fire()` 中：检查弹药、射速冷却 → 根据改装装备决定子弹类型 → 创建 Bullet 加入 ElementManager → 更新统计
- `takeDamage(int raw)` 中：先算耐久减伤比例 → 再扣 hp → 再扣 durability → 检查是否死亡/触发防爆油箱

#### Boss（AI 坦克）

- 继承 Players，复用所有属性和行为
- 重写 `update()`：先调用 `ai.decide(this)` 设置本帧移动/射击意图，再调用 `super.update()` 执行
- AI 控制器作为构造参数注入，支持不同难度

#### Bullet（子弹）

- 速度按换算系数转换（设计值 × 0.06）
- `update()` 中：按朝向计算下一帧位置 → 检查生命周期超时
- `show()` 中：普通子弹画圆形，激光画线段，散射子弹画小圆
- 特有标记：`isLaser`（穿透判定用）、`rebound`（反弹判定用）、`maxRebounds`（最大反弹次数）

#### Brick / Iron（墙壁）

- Brick：被子弹击中后 destroy()，被激光穿透后 destroy()
- Iron：普通子弹反弹，激光穿透/反弹（取决于装备），永不销毁
- 墙壁本身无 `update()` 逻辑（静止物体）
- `show()` 中绘制 40×40 的墙壁贴图

#### Background（背景）

- 无碰撞体，不参与碰撞检测
- 仅用于视觉装饰，绘制地面纹理
- `isStrike()` 始终返回 false

#### Explosion（爆炸特效）

- 临时元素，创建后经过固定帧数（如 15 帧，~0.25s）自动 destroy()
- `update()` 中递增帧计数
- `show()` 中根据帧数切换爆炸动画帧（多帧图片）

---

## 四、元素管理器与工厂（ElementManager / ElementFactory）

### 4.1 ElementManager 实现逻辑

**数据结构：**
```
Map<String, CopyOnWriteArrayList<SuperElement>>
```
Key 为类型名（"players", "boss", "bullet", "brick", "iron", "background", "explosion"）。

**为什么用 CopyOnWriteArrayList：**
- 渲染线程需要遍历元素列表（高频读）
- 逻辑线程可能增删元素（低频写）
- COW 的特性：读操作无锁，写操作复制一份——恰好适合"读多写少"的游戏循环

**核心方法：**
- `addElement(type, element)`：获取对应列表，add
- `removeElement(type, element)`：获取对应列表，remove
- `getElements(type)`：直接返回对应列表引用（调用方不可修改）
- `getAllElements()`：返回整个 Map 的不可修改视图
- `clearAll()`：清空所有列表（切换对战时调用）

**实现注意：**
- DCL（双重检查锁定）单例模式
- 构造函数中初始化所有 7 个列表
- `removeElement` 不抛异常——元素可能已被移除（如子弹同时击中墙壁和坦克）

### 4.2 ElementFactory 实现逻辑

单例模式。每个创建方法封装了对象的构造、属性赋值、初始状态的设置。

**核心方法：**

- `createPlayer(tankId, x, y)`：从 TankDataManager 获取属性模板 → new Players(tankData) → 设置出生坐标
- `createBoss(tankId, x, y, aiController)`：同 createPlayer，额外注入 AI 控制器
- `createBullet(x, y, direction, owner, isLaser, rebound, maxRebounds, ...)`：创建 Bullet 对象并填充所有参数。参数巨多，建议用 Builder 模式或重载多个工厂方法（如 `createNormalBullet`, `createLaser`, `createSpreadBullets`）
- `createBrick(x, y)`：new Brick(x, y, 40, 40)
- `createIron(x, y)`：new Iron(x, y, 40, 40)

**子弹创建的参数来源：**
- 基础参数（速度、伤害）来自发射者 Players 的属性
- 改装效果（散射、激光、反弹）在 `fire()` 方法中判断后选择不同的工厂方法
- 射速冷却由 Players 自身维护（lastFireTime / fireRate），工厂不关心

---

## 五、存档系统（SaveManager）

### 5.1 核心实现逻辑

**Gson 配置：**
```java
new GsonBuilder().setPrettyPrinting().create()
```
美观输出方便玩家手动编辑。不使用自定义 TypeAdapter，完全依赖 Gson 的默认反射序列化。

**存储路径：**
- 默认目录：`save/`（相对于程序工作目录，即项目根目录）
- 默认文件名：`save_data.json`
- 用户在保存/加载时可选择其他路径

### 5.2 序列化流程

**保存（save / saveAs）：**
1. 检查 save 目录是否存在，不存在则 `mkdirs()`
2. 将 PlayerSaveData 对象传入 `gson.toJson(data)` 得到 JSON 字符串
3. 先写入临时文件 `save_data.tmp`
4. 备份当前存档为 `save_data.bak`（如果存在）
5. 使用 `Files.move(tmp, target, ATOMIC_MOVE)` 原子替换
6. 删除临时文件
7. 写入失败时尝试从 `.bak` 文件恢复

**加载（load）：**
1. 读取文件到 String（UTF-8）
2. 空文件检查
3. `JsonParser.parseString(json)` 验证 JSON 合法性
4. 检查必需字段（meta / resources / ownedTanks）
5. 版本检查：若旧版本则调用 `migrate()` 升级
6. `gson.fromJson(json, PlayerSaveData.class)` 反序列化
7. 任何步骤失败 → 弹窗提示，引导用户选择其他存档或创建新档

### 5.3 存档时机

| 时机 | 触发方式 | 说明 |
|------|----------|------|
| 对战结算后 | 自动调用 `save()` | 资源奖励、统计写入内存后持久化 |
| 养成操作后 | 自动调用 `save()` | 升级/进阶/改装安装/碎片合成 |
| 抽卡后 | 自动调用 `save()` | 新坦克获得、碎片获得 |
| 退出游戏 | 弹窗询问用户 | 选择保存路径或放弃 |

### 5.4 PlayerSaveData 工厂方法

`createNew(playerName)`：
1. new PlayerSaveData，设置 meta（版本号、时间戳、玩家名）
2. resources 全零
3. ownedTanks 添加两辆初始坦克：ID=1 克伦威尔（0阶1级）、ID=2 M24霞飞（0阶1级），CombatStats 从 TankDataManager 获取基础值
4. modificationInv 空
5. gachaState 双池计数均为 0
6. battleHistory 全零
7. settings 默认值（音量 0.8/1.0，语言 zh_CN，默认难度 hard）

---

## 六、登录与主菜单

### 6.1 程序启动流程（Main.main）

```
1. SwingUtilities.invokeLater() 包裹，确保 UI 在 EDT 线程创建
2. ElementLoad.getInstance().init() —— 加载所有图片和地图到缓存
3. SaveManager.getInstance() —— 初始化存档管理器
4. new MyJFrame() —— 创建主窗体（1380×820，居中）
5. 显示 LoginPanel
```

### 6.2 LoginPanel 实现逻辑

**布局：**
- 居中一个面板，包含"登录"按钮、"退出"按钮
- 背景图（登录页背景）

**"登录"按钮逻辑：**
1. 调用 `SaveManager.getInstance().listSaveFiles()`，扫描 `save/` 目录下所有 `.json` 文件
2. 若无存档 → 弹出输入对话框 `JOptionPane.showInputDialog("请输入玩家名")` → 调用 `PlayerSaveData.createNew(name)` → 存入 GameContext → 跳转主菜单
3. 若有存档 → 弹出存档选择对话框：
   - 使用 `JList<String>` 展示存档文件名列表
   - 每个存档显示：文件名 + 存档中的玩家名 + 保存时间（读取 JSON 的 meta 字段显示）
   - 底部两个按钮："加载选中存档"、"创建新档"
   - 加载 → `SaveManager.getInstance().load(selectedFile)` → 存入 GameContext → 跳转主菜单
   - 创建新档 → 同上输入玩家名逻辑

### 6.3 MyJFrame 导航设计

**方案：CardLayout + JPanel**

`MyJFrame` 使用 `BorderLayout`：
- **NORTH**：共享资源栏（生铁、钢铁、蓝图的实时显示栏），所有子页面可见
- **CENTER**：CardLayout 管理的卡片容器，切换不同功能面板

**卡片名称映射：**
| 卡片名 | 面板类 | 说明 |
|--------|--------|------|
| `"login"` | LoginPanel | 登录 |
| `"menu"` | MainMenuPanel | 主菜单（四个入口按钮） |
| `"tankSelect"` | TankSelectPanel | 坦克/地图选择 |
| `"battle"` | MyGameJPanel | 对战画面 |
| `"develop"` | DevelopPanel | 坦克养成 |
| `"gacha"` | GachaPanel | 坦克获取 |
| `"battleResult"` | BattleResultPanel | 对战结算 |
| `"settings"` | SettingsPanel | 游戏设置 |

**切换方法：** `frame.showPanel("menu")` → 内部调用 `cardLayout.show(contentArea, "menu")` + `resourceBar.refresh()`

### 6.4 MainMenuPanel 实现逻辑

**布局：**
- 左侧大图/Logo
- 右侧四个按钮垂直排列：对战、坦克养成、坦克获取、退出游戏

**按钮行为：**
- "对战" → `frame.showPanel("tankSelect")`，进入前需设置对战模式标志
- "坦克养成" → `frame.showPanel("develop")`，初始化坦克列表
- "坦克获取" → `frame.showPanel("gacha")`
- "退出游戏" → 弹出保存对话框 → 保存/不保存/取消

**退出保存对话框（关键逻辑）：**
1. `JOptionPane.showConfirmDialog(frame, "是否保存当前存档？", "退出", YES_NO_CANCEL)`
2. YES → `JFileChooser` 选择路径（默认 `save/save_data.json`）→ `SaveManager.saveAs()` → `System.exit(0)`
3. NO → `System.exit(0)`
4. CANCEL → 返回

**注意：** 养成/抽卡后的自动保存只涉及 `save()`（保存到当前存档文件），退出时的保存可能涉及 `saveAs()`（另存为）。共享的资源栏需要在每次面板切换时刷新数据。

---

## 七、地图加载与渲染

### 7.1 地图数据解析（MapData.parse）

`.map` 文件格式：
```
BRICK=340,40;340,60;340,80;...
IRON=360,160;380,160;...
```

**解析逻辑（必须健壮）：**
1. 逐行读取，`trim()` 去除空白
2. 空行、`#` 开头（注释）跳过
3. 按 `=` 分割：左边是类型（BRICK/IRON），右边是坐标串
4. 坐标串按 `;` 分割，每个坐标对按 `,` 分割得到 x, y
5. 捕获 `NumberFormatException` → 跳过该坐标对
6. 解析结果存入 `List<Point>`

**边界墙自动生成：** 地图四周自动放置 Iron 墙壁（即使 .map 未定义），防止坦克出界。四个边的坐标范围由窗口大小计算：水平 0 到 1380/40=34 格，垂直 0 到 820/40=20 格（需减去 UI 区域）。

### 7.2 地图墙壁元素创建

`startBattle()` 中：
1. `ElementManager.getInstance().clearAll()` 清空上一局
2. 加载 MapData
3. 遍历 brickPositions：`ElementFactory.createBrick(x, y)` → 加入 "brick" 列表
4. 遍历 ironPositions：`ElementFactory.createIron(x, y)` → 加入 "iron" 列表
5. 注意：墙壁 40×40 = 一个 tile 大小，坐标直接对齐 tile 网格

### 7.3 背景渲染

背景元素（Background）不存储每个 tile，而是用一个大的 `Background` 对象覆盖整个地图区域。`show()` 中绘制地面纹理（可平铺也可单张大图）。

**分层绘制顺序（z-order）：**
1. background（最底层）
2. iron, brick（墙壁层）
3. players, boss（坦克层）
4. bullet（子弹层）
5. explosion（特效层）
6. HUD 叠加层（最顶层）

---

## 八、对战核心——双线程游戏循环

### 8.1 整体架构

两条线程并行运行，通过 ElementManager 共享数据：

- **游戏逻辑线程（GameThread extends Thread）**：60 FPS，每帧 ~16ms，负责 update、碰撞检测、清理、胜负判定
- **渲染线程（MyGameJPanel 内部 Timer）**：Swing Timer 60 FPS，每帧 repaint → paintComponent → 遍历元素 show

### 8.2 GameThread 实现逻辑

**run() 方法结构：**
```
while (running) {
    if (!paused) {
        long frameStart = System.currentTimeMillis();

        // 第 1 步：更新所有元素
        for (每个类型的每个元素) {
            element.update();
        }

        // 第 2 步：碰撞检测（按顺序，不可调换）
        checkTankWallCollisions();      // 坦克移动前预检
        checkBulletWallCollisions();    // 子弹对墙壁
        checkBulletTankCollisions();    // 子弹对坦克

        // 第 3 步：清理已销毁元素
        for (每个类型的列表) {
            列表.removeIf(e -> !e.isVisible());
        }

        // 第 4 步：检查胜负条件
        checkWinCondition();

        // 第 5 步：帧率控制
        long elapsed = System.currentTimeMillis() - frameStart;
        long sleepTime = FRAME_DURATION - elapsed;
        if (sleepTime > 0) Thread.sleep(sleepTime);
    }
}
```

**关键注意点：**
- 碰撞检测顺序不可调换——坦克先确认移动不撞墙，子弹再检测碰撞
- 使用 `Iterable` 遍历 COW 列表时不要直接调用 list.remove()，而是标记 visible=false 后统一清理
- 帧率控制用 `sleep` 而非 `yield`，避免空转消耗 CPU

### 8.3 MyGameJPanel 渲染线程

**实现方式：** 使用 `javax.swing.Timer`（每 16ms 触发一次 actionPerformed → 调用 `repaint()`）

**paintComponent 实现：**
1. `super.paintComponent(g)` 清空画布
2. 转换为 Graphics2D，开启抗锯齿
3. 按 z-order 依次调用每个类型的 drawElements(g2d, type)
4. `drawElements` 中遍历列表，对每个 visible 元素调用 `element.show(g2d)`
5. 绘制 HUD 叠加层（血量、弹药、倒计时、改装图标）
6. `Toolkit.getDefaultToolkit().sync()` 保证画面同步

**坦克绘制（Graphics2D 旋转）：**
1. 获取坦克的 `direction`（角度）
2. `AffineTransform oldTransform = g2d.getTransform()` 保存旧变换矩阵
3. `g2d.rotate(Math.toRadians(direction), tankCenterX, tankCenterY)` 旋转画布
4. `g2d.drawImage(tankImage, x, y, width, height, null)` 绘制（图片朝上）
5. `g2d.setTransform(oldTransform)` 恢复变换矩阵

**重要：** 所有 `show()` 中使用了旋转的，必须在 finally 中恢复变换矩阵。或者统一在 drawElements 层面做变换，而不是让每个元素自己做。

### 8.4 线程安全

- ElementManager 的 COW 列表保证遍历安全
- GameThread 的 running/paused 用 `volatile` 修饰
- 不在 paintComponent 中修改任何元素状态
- KeyListener 修改 Players 状态时通过 synchronized 方法

---

## 九、坦克移动与操控

### 9.1 移动计算

坦克只能沿朝向方向前后移动，不能横向平移。

**moveForward() 逻辑：**
1. 将 direction（角度）转为弧度 `rad = Math.toRadians(direction)`
2. 计算位移分量：
   - `dx = speed × sin(rad)`（x 分量）
   - `dy = -speed × cos(rad)`（y 分量，注意屏幕坐标系 y 轴向下）
3. 新位置：`newX = x + dx`, `newY = y + dy`
4. 碰撞预检：用新位置构建 Rectangle，检查是否与任何墙壁重叠
5. 无碰撞 → 更新 x, y；有碰撞 → 不更新位置（保持原地）

**moveBackward() 逻辑：** 同 moveForward，dx/dy 取反。

**旋转逻辑：**
- `rotateCW()`：`direction += turnSpeed`（度）
- `rotateCCW()`：`direction -= turnSpeed`
- 方向标准化：`direction = (direction + 360) % 360`
- 若有扭绞轮台改装 → 直接设置 direction 为目标角度（瞬间完成）

**速度取值：** `int actualSpeed = (int)(tankData.getBaseSpeed() * TankData.TANK_SPEED_FACTOR)`

### 9.2 键盘操控映射

**人机对战（仅 P1 操作）：**

| 按键 | 行为 |
|------|------|
| W | P1 前进（持续按下持续前进） |
| S | P1 后退 |
| A | P1 逆时针旋转 |
| D | P1 顺时针旋转 |
| 空格 | P1 发射子弹 |
| R | P1 换弹（替换弹匣） |
| ESC | 暂停 |

**双人对战（P1 + P2）：**

| P1 按键 | P2 按键 | 行为 |
|---------|---------|------|
| W | ↑ | 前进 |
| S | ↓ | 后退 |
| A | ← | 逆时针旋转 |
| D | → | 顺时针旋转 |
| 空格 | 鼠标左键 | 发射 |
| R | 鼠标右键 | 换弹 |

### 9.3 KeyListener 实现要点

- 使用 `Set<Integer> pressedKeys` 记录当前按下的所有键
- `keyPressed` → 加入集合，设置对应行为标志
- `keyReleased` → 从集合移除，清除对应行为标志
- 在 GameThread 每帧根据 pressedKeys 调用 Players 的移动/射击方法，而非在 keyPressed 中直接调用——这样可以支持"持续按键"（按住 W 持续前进）
- 方向冲突处理：同时按下 W 和 S → 都不移动（或按后按下的优先）
- 帧边界：每帧最多处理一次射击（由空格触发 + 射速冷却限制）

### 9.4 换弹逻辑

1. 按 R → `isReloading = true`，记录 `reloadStartTime`
2. `update()` 中检查：若 `isReloading && now - reloadStartTime >= reloadTime` → `ammo = maxAmmo`, `isReloading = false`
3. 换弹中再按 R → 忽略
4. 换弹中不能射击
5. 弹匣打空（ammo=0）后若无自动装填器 → 自动触发 `reload()`，若有自动装填器 → 瞬间恢复弹药（reloadTime=0）

---

## 十、子弹系统

### 10.1 普通子弹

**发射：** 在坦克中心位置创建 Bullet，方向 = 坦克朝向，速度 = designSpeed × 0.06
**飞行：** `update()` 每帧按方向计算新位置
**碰撞行为：**
- 碰 Brick（砖墙）→ 子弹 destroy()，砖墙 destroy()
- 碰 Iron（钢铁墙）→ 子弹反弹（方向取反，或根据入射角计算反射角）
- 碰边界 → 反弹
- 碰坦克（非发射者）→ 计算伤害 → 坦克 takeDamage() → 子弹 destroy()
- 碰友方坦克（无反斜钢甲）→ 同上，有反斜钢甲 → 跳过

### 10.2 散射子弹（轻机枪/重机枪）

**扇形生成算法：**
1. 以坦克朝向为中心方向
2. 计算起始角度：`startAngle = direction - (count-1) × spreadAngle / 2`
3. spacing = 15°（每颗子弹间隔）
4. 循环 count 次：每个子弹方向 = `startAngle + i × spreadAngle`
5. 伤害倍率：轻机枪 40%，重机枪 30%
6. 子弹标记 `rebound = false`（碰墙立即消失，不反弹）

### 10.3 激光（激光炮/高密度镭射群炮）

**激光的实现不同于子弹：**

激光不是逐帧移动的抛射体，而是在发射瞬间计算整条激光线的位置。实现方式有两种：

**方案 A（推荐——线段求交法）：**
1. 创建 LaserBeam 元素，包含一条从发射点到终点的线段
2. 从发射点出发，沿方向逐 tile 步进（每次前进 tile_size=40）
3. 每步检查该位置是否有墙壁：有 Brick → 穿透（销毁 Brick），继续前进；有 Iron → 反弹（激光炮）/ 反弹最多5次（镭射群炮）
4. 记录激光完整路径（所有线段：从发射点到第一次碰撞、反弹到第二次碰撞...）
5. 路径计算完成后，检查路径上所有坦克，对每辆敌方坦克造成一次伤害
6. `show()` 中绘制整条路径线段
7. 激光在下一帧销毁（瞬发，不持续存在）

**方案 B（逐帧移动法）：**
1. 创建 Bullet 标记 `isLaser = true`，速度 = 最快子弹速度 × 10（~66 px/frame）
2. 每帧移动，碰撞检测改为线段求交（防止穿透）
3. 穿透 Brick，在 Iron 上反弹
4. 对路径经过的坦克造成伤害（每个坦克仅一次）

**推荐方案 A**，因为激光"极快"的特性更适合瞬间计算而非逐帧移动。

**激光伤害：** 攻击力 × 10%，对路径上每个敌方单位造成一次伤害。

### 10.4 子弹生命周期

1. 创建 → 加入 bullet 列表
2. 每帧 update() 移动
3. 满足以下条件之一则 destroy()：
   - 飞行时间超过 bulletDuration（普通子弹）
   - 碰墙销毁（散射子弹）
   - 碰坦克（普通子弹）
   - 超出地图边界（不反弹的子弹）
   - 下一帧（激光）

---

## 十一、碰撞检测系统

### 11.1 整体结构

`CollisionUtil` 为静态工具类，提供三个检测方法，在 GameThread 每帧调用。

### 11.2 坦克与墙壁碰撞（移动阻挡）

**时机：** 坦克移动前进行预检测

**逻辑：**
1. 对每个坦克（players + boss），根据其速度和方向计算下一步位置
2. 用新位置构建矩形
3. 遍历 brick 和 iron 列表，检查新矩形是否与任何墙壁相交
4. 相交 → 阻止移动（不更新 x, y）
5. 不相交 → 允许移动

**实现技巧：** 可以分别在 x 方向和 y 方向独立检测，允许"贴墙滑行"——若 x 方向被阻挡但 y 方向无阻挡，则只在 y 方向移动。

### 11.3 子弹与墙壁碰撞

**逻辑：**
1. 遍历所有子弹
2. 对每个子弹，先计算下一步位置（或对激光用线段）
3. 检查碰撞的墙壁类型：
   - Brick：子弹 destroy()（普通/散射），Brick destroy()；激光穿透，Brick destroy()
   - Iron：子弹反弹（方向改变），Iron 不变；激光穿透/反弹（取决于装备）
4. 方向取反逻辑：水平墙壁 → dy 取反；垂直墙壁 → dx 取反

**高速子弹穿透问题：** 子弹速度 > 墙壁厚度时，子弹可能"跳过"墙壁。解决方案：
- 用插值法：在子弹当前位置和下一帧位置之间插值（步长 = 子弹宽度/2），每步检查碰撞
- 或确保 tile 大小（40）远大于单帧子弹位移（最大 ~6.6px），这样不存在穿透问题——本项目中 40 > 6.6，所以不需要插值

### 11.4 子弹与坦克碰撞

**逻辑：**
1. 双重循环：遍历所有子弹 × (players + boss)
2. 跳过发射者自身（`bullet.getOwner() == tank`）
3. 跳过已销毁的子弹/坦克
4. 矩形相交判定
5. 碰撞处理：
   - 计算伤害 = 子弹伤害值
   - 调用 `tank.takeDamage(bullet.getDamage())`
   - 子弹 destroy()
   - 友伤检查：若子弹发射者与受击者为同阵营且没有反斜钢甲 → 伤害生效
6. 若子弹是激光：用线-矩形相交代替矩形-矩形

### 11.5 碰撞优化的空间网格（可选，在性能瓶颈时实施）

当子弹 + 墙壁数量 > 200 时，O(n²) 暴力法可能超过 16ms 预算。此时引入空间网格：

1. 地图划分为 40px 的均匀网格（34×20 格）
2. 每帧：清空网格 → 插入所有碰撞体 → 只检查同格/邻格元素
3. 复杂度从 O(n²) 降至 O(n × k)，k ≈ 10
4. 实现类：`SpatialGrid`，方法：`clear()`, `insert(element)`, `getCandidates(element)`

---

## 十二、AI 系统

### 12.1 整体架构

三层架构：感知 → 决策 → 执行

- **感知层**：从 ElementManager 读取玩家位置、子弹列表、墙壁布局
- **决策层**：Utility AI 对候选行为打分，选最高分
- **执行层**：A* 寻路、预判瞄准、闪避机动

### 12.2 AIController 接口

```java
public interface AIController {
    void decide(Boss boss);
}
```

三个实现类：`EasyAI`, `HardAI`, `SuperAI`。

### 12.3 感知层逻辑

每 N 帧调用一次（N 取决于难度：简单 30 帧=500ms，困难 12 帧=200ms，超级 3 帧=50ms）。

**采集内容：**
- 从 ElementManager 获取玩家坦克：位置、朝向、血量、弹药
- 遍历子弹列表：筛选飞向自身的子弹，计算时间和危险度
- 读取墙壁网格：构建 `boolean[][] wallGrid` 用于寻路
- 检测敌方开火：比较前后两帧敌方弹药数，减少则触发"敌人开火"事件

**威胁评估：**
- 子弹距离自身轨迹线的垂直距离 < 30px → 视为威胁
- 预计碰撞时间 = 子弹到自身距离 / 子弹速度
- 时间 < 0.5s → 高威胁，时间 < 1.5s → 中威胁

### 12.4 Utility AI 决策逻辑

**候选行为（AIAction 枚举）：** PURSUE, RETREAT, STRAFE, HOLD, ATTACK, DODGE, RELOAD, SEEK_COVER, PATROL

**评分方式（乘法合成）：**
```
最终分数 = HealthScore × AmmoScore × ThreatScore × ProximityScore × CoverScore
```
任何轴返回 0 直接否决该行为（乘法特性）。

**每 10 帧重新评估一次**（而非每帧决策，避免行为抖动）。

**加权随机选择：** 取分数在最高分 85% 以内的行为，按分数加权随机选一个（避免行为可预测）。精英阈值随难度变化：简单 0.6，困难 0.8，超级 0.95。

### 12.5 A* 路径规划实现

**网格化：** 地图划分为 40px 格子。墙壁所在格子标记为 blocked。

**A* 核心：**
- 使用 PriorityQueue<Node> 作为开放列表
- Node 包含：x, y（网格坐标）, gCost（实际代价）, hCost（启发式），parent（回溯指针）
- 启发式：切比雪夫距离（8 方向移动的最优启发式）—— `max(|dx|, |dy|)`
- 8 方向移动，对角线代价 = √2 ≈ 1.414，直线代价 = 1
- 对角线移动时检查"贴墙角"：两个相邻格不能同时被 blocked

**路径平滑（视线法）：**
1. 从路径起点开始，依次检查是否能直接看到更远的路径点
2. 如果能直接看到（Bresenham 画线算法检查线段经过的格子），则跳过中间点
3. 输出仅保留拐点的精简路径

**路径跟随：**
- 当前目标 = 路径的第一个点
- 坦克旋转朝向目标方向
- 朝向误差 < 5° 时前进
- 到达目标点（距离 < 阈值）→ 移除该点，继续下一个
- 重新规划频率取决于难度（500ms/200ms/100ms）

### 12.6 射击预判

**线性预判（困难 AI）：**
1. 读取目标当前位置、速度、朝向
2. 计算子弹飞行时间 = 距离 / 子弹速度
3. 预判目标位置 = 当前位置 + 速度向量 × 飞行时间
4. 迭代 1-3 次（每次用新的预判位置重算飞行时间），直到收敛
5. 返回瞄准角度

**二次方程精确解算（超级 AI）：**
1. 建立子弹命中方程：相对位置 + 相对速度 × t = 子弹飞行距离
2. 转化为二次方程 a×t² + b×t + c = 0 求解
3. 取最小的正根
4. 若无解（discriminant < 0）→ 回退到线性预判 3 次迭代

**难度差异化：**
- 简单：直接瞄准当前位置 + 随机偏移 ±10°
- 困难：线性预判 1 次迭代 + 随机偏移 ±3°
- 超级：二次方程解算，无随机偏移

### 12.7 子弹躲避

**简单 AI：** 不主动躲避。

**困难 AI（开火检测躲避）：**
- 检测到敌方开火 → 立即转向 90°（垂直于敌我连线）→ 全速移动 300ms

**超级 AI（危险地图）：**
1. 将地图网格化（40px 格）
2. 对每颗有威胁的子弹，预测未来 30 帧的位置
3. 沿子弹轨迹的格子累加危险值（越近越危险，因子 = 10 / (帧数+1)）
4. 搜索自身周围 200px 内最安全的格子（危险值 + 距离惩罚的综合评分）
5. A* 导航到最安全格子
6. 配合垂直接近闪避（对最近的子弹垂直逃离）

### 12.8 势场法局部避障（超级 AI）

在 A* 路径基础上叠加三个力：
- **吸引力**：指向路径上的下一个路点
- **墙壁排斥力**：推开附近墙壁（力与距离平方成反比）
- **子弹排斥力**：推离飞来的子弹轨迹（垂直于子弹方向）

合力的方向为坦克移动的目标方向。势场法让 AI 的移动看起来自然流畅，而不是沿网格中心线走直线。

**卡住检测：** 若 1 秒内移动距离 < 5px → 判定为"陷入局部极小值"→ 临时切换到纯 A* 脱离。

---

## 十三、改装装备效果实现

### 13.1 效果分散切入（非集中处理）

改装效果不是在一个地方集中判断，而是在各自对应的系统节点切入。每个装备需要在特定的逻辑点加入判断。

### 13.2 各装备切入点

**反斜钢甲（ANTI_FRIENDLY_FIRE）：**
- 切入：`CollisionUtil.checkBulletTankCollisions()`
- 逻辑：检查 `bullet.getOwner()` 与 `tank` 是否同阵营（都是 Players 或都是 Boss）→ 是则跳过伤害
- 注意：子弹发射者自身永远跳过（已有逻辑），额外跳过的是友方

**扭绞轮台（INSTANT_TURN）：**
- 切入：`Players.rotateCW()` / `Players.rotateCCW()`
- 逻辑：跳过逐帧旋转，直接设置 `direction = targetDirection`

**防爆油箱（EXPLOSION_PROOF）：**
- 切入：`Players.takeDamage()`
- 逻辑：若 `hp - actualDamage <= 0` → 改为 `hp = 1`，设置 `invincibleUntil = now + 5000`（5秒无敌），设置 `explosionProofUsed = true`
- `takeDamage()` 开头检查：若 `now < invincibleUntil` → 直接 return
- 对战开始时重置 `explosionProofUsed = false`

**轻/重机枪（LIGHT_MACHINE_GUN / HEAVY_MACHINE_GUN）：**
- 切入：`Players.fire()`
- 逻辑：替换默认的 `createNormalBullet()` 为 `createSpreadBullets(count, damageMult, false, 0)`
- 互斥检查：若已安装激光炮/镭射群炮 → 机枪不生效（优先激光）

**激光炮（LASER_CANNON）：**
- 切入：`Players.fire()`
- 逻辑：替换普通子弹为 `createLaser(false, 0)`（不反弹）
- 无需消耗弹药，无需换弹
- 攻击间隔 = 基础射速

**高密度镭射群炮（DENSE_LASER）：**
- 切入：`Players.fire()`
- 逻辑：替换为 `createLaser(true, 5)`（反弹最多5次）

**自动装填器（AUTO_LOADER）：**
- 切入：`Players.reload()`
- 逻辑：`reloadTime = 0`，即 `reload()` 被调用时直接 `ammo = maxAmmo, isReloading = false`

**额外弹药架（EXTRA_AMMO）：**
- 切入：`Players` 构造时
- 逻辑：`maxAmmo = baseAmmo × 2`

### 13.3 改装槽冷却系统

**数据结构：** `int[] cooldownSlots`，长度 5，值 = 剩余冷却对局数（0=可用）

**安装装备：**
1. 检查 `unlockedSlots >= slotIndex+1`（槽已解锁）
2. 检查 `cooldownSlots[slotIndex] == 0`（槽可用）
3. `installedMods[slotIndex] = modification`
4. 装备从库存移除（若来自 `craftedEquipments` 则移除，若直接使用碎片合成则跳过库存步骤）

**拆卸装备：**
1. `installedMods[slotIndex] = null`
2. `cooldownSlots[slotIndex] = 5`（需完成 5 次对局后冷却）
3. 装备数据直接丢弃（不可归还）

**冷却推进：** 每次对战结算后，遍历所有坦克的所有冷却槽，`cooldownSlots[i]--`（最小为 0）

### 13.4 碎片合成

**特定碎片（25 个 → 装备）：**
- `specificFragments.get(modType) >= 25` → 扣除 25 碎片 → `craftedEquipments.add(modType)`

**通用碎片（50 个 → 任意装备）：**
- `universalFragments >= 50` → 扣除 50 → `craftedEquipments.add(selectedModType)`

**碎片转换（2 个特定碎片 → 1 个通用碎片）：**
- 任选一个 `specificFragments` 中 >= 2 的 → 扣除 2 → `universalFragments++`

---

## 十四、坦克养成系统

### 14.1 升级逻辑

**触发：** 用户在 DevelopPanel 点击"升级"按钮

**逻辑流程：**
1. 检查 `level < 10`（当前阶未满级）
2. 计算消耗：`cost = rank × 100 + level × 10`（粗铁）
3. 检查 `playerIron >= cost`
4. 扣除粗铁 → `level++` → 调用 `applyUpgradeBonus()` 增加战斗属性
5. `applyUpgradeBonus()` 逻辑：
   - `attack += upgradeAtkGain`
   - `defense += upgradeDefGain`
   - `speed += upgradeSpdGain`
   - `maxHp += upgradeHpGain`（同时 hp 回满）
   - （其他属性的升级成长同理）
6. 自动保存

### 14.2 进阶逻辑

**触发：** 用户在 DevelopPanel 点击"进阶"按钮

**逻辑流程：**
1. 检查 `level == 10`（必须满级）
2. 检查 `rank < 10`（未到最高阶）
3. 计算消耗：`cost = (rank + 1) × 10`（钢铁）
4. 检查 `playerSteel >= cost`
5. 扣除钢铁 → `rank++` → `level = 1` → 调用 `applyEvolveBonus()` 增加战斗属性
6. `checkUnlockModSlot()`：根据新 rank 解锁改装槽
7. 自动保存

### 14.3 改装槽解锁规则

```
rank 0-2: unlockedSlots = 0
rank 3-4: unlockedSlots = 1
rank 5-6: unlockedSlots = 2
rank 7-9: unlockedSlots = 3
rank 10:  unlockedSlots = 5
```

### 14.4 型号计算

```
if (unlockedSlots == 0) → 无型号
else → 第一位 = 'A' + (unlockedSlots - 1)
        第二位 = "-"
        第三位 = 罗马数字(已安装数)
示例：解锁 3 槽、安装 2 个 → "C-II"
```

型号由系统计算，不可手动修改。

---

## 十五、抽卡系统（GachaManager）

### 15.1 坦克获取池

**权重表（总计权重 405）：**

| 奖励 | 权重 | 概率 |
|------|------|------|
| 生铁 ×10 | 200 | 49.4% |
| 生铁 ×20 | 100 | 24.7% |
| 生铁 ×100 | 50 | 12.3% |
| 生铁 ×200 | 20 | 4.9% |
| 钢铁 ×100 | 15 | 3.7% |
| 钢铁 ×200 | 10 | 2.5% |
| 钢铁 ×500 | 5 | 1.2% |
| 新坦克 | 5 | 1.2% |

**新坦克分配（权重 5 中的再分配）：**
- 初始两辆（坦克 1、2）：各 20%
- 剩余 6 辆新坦克：随机分配 60%（即新坦克中出现 3-8 号坦克）

**重复坦克处理：**
- 抽到已拥有的坦克 → 改为获得钢铁 ×500
- 首次获得某坦克 → 额外奖励钢铁 ×100

**费用：** 单抽 3 蓝图，十连 27 蓝图（9 折）

**保底机制：**
- 累计 < 50 抽：正常权重
- 50 ≤ 累计 < 70：每抽 +5 新坦克权重
- 70 ≤ 累计 < 90：每抽 +10 新坦克权重
- 累计 = 90：强制出新坦克
- 抽出新坦克后累计清零

### 15.2 改装装备获取池

**权重表（总计权重 425）：**

| 奖励 | 权重 |
|------|------|
| 生铁 ×10 | 100 |
| 生铁 ×20 | 100 |
| 生铁 ×100 | 50 |
| 生铁 ×200 | 20 |
| 随机碎片 ×1 | 20 |
| 随机碎片 ×5 | 10 |
| 随机碎片 ×10 | 5 |
| 通用碎片 ×10 | 10 |
| 通用碎片 ×20 | 5 |

**目标大奖：** 随机碎片和通用碎片（四种类型均算）

**保底机制：** 同坦克池逻辑，但目标大奖变为四种碎片类型

**注意：** 改装池无重复补偿

### 15.3 抽卡实现逻辑

**单抽流程：**
1. 检查蓝图数量（单抽 ≥ 3，十连 ≥ 27）
2. 扣除蓝图
3. 循环 times 次：
   - piryCounter++
   - 计算保底加成 → 调整权重
   - 按权重随机选择奖励类型
   - 若为目标大奖（新坦克/碎片）→ piryCounter = 0
   - 若为重复坦克 → 奖励替换为钢铁 ×500
   - 累计奖励
4. 更新 PlayerSaveData（新增坦克、资源增加、碎片增加）
5. 自动保存
6. 返回 GachaResult（包含所有奖励列表，供 UI 展示）

**权重随机算法：**
1. 计算总权重 = 各奖励权重之和（含保底加成）
2. 生成随机数 r = random(0, totalWeight)
3. 遍历奖励列表，累加权重，累加到 >= r 时选中该奖励

### 15.4 保底权重计算

```java
if (pityCounter >= 90 && !alreadyGotThisRound) {
    // 强制出大奖
    return forceJackpot();
}
int bonus = 0;
if (pityCounter >= 50) bonus += (pityCounter - 49) * 5;
if (pityCounter >= 70) bonus += (pityCounter - 69) * 5;
// bonus 加到目标大奖权重上
```

---

## 十六、对战设置与结算

### 16.1 BattleConfig（对战配置）

**数据结构字段：**
- `matchDuration`：比赛时长（秒），默认 300（5 分钟）
- `overtimeEnabled`：是否加时赛（与 damagePenalty 互斥）
- `damagePenaltyEnabled`：是否超时惩罚（与 overtime 互斥）
- `cheatEnabled`：是否允许作弊
- 作弊子选项：P1/P2 属性覆盖、友伤开关、无敌开关、穿墙开关、耐久禁用开关、子弹反弹开关

**约束（UI 层面强制）：**
- 人机对战不能进行对战设置（TankSelectPanel 中隐藏设置入口）
- 加时赛和超时惩罚互斥（勾选一个自动取消另一个）
- 默认值：5 分钟，无加时赛，无超时惩罚，无作弊

### 16.2 对战结算逻辑

**结束条件（GameThread 中检测）：**
1. 一方坦克 `hp <= 0 && visible == false` → 另一方获胜
2. 倒计时结束 → HP 多者胜；HP 相同判玩家失败
3. 玩家按"结束对战" → 判玩家失败

**结算流程：**
1. GameThread 设置 `running = false`
2. 切换到 BattleResultPanel
3. 展示：胜负结果、双方数据对比（开火数/造成伤害/受到伤害）
4. 人机对战：根据难度和胜负发放资源奖励
5. 推进所有坦克的冷却槽（cooldownSlots 各减 1，最小 0）
6. 更新 battleHistory 统计
7. 自动保存

**资源奖励（人机对战）：**
| 难度 | 胜利生铁 | 胜利钢铁 | 蓝图（无论输赢） |
|------|----------|----------|-----------------|
| 简单 | 200 | 30 | 1 |
| 困难 | 400 | 60 | 2 |
| 超级 | 500 | 100 | 3 |

**失败：** 无生铁和钢铁，但蓝图照常发放。

### 16.3 加时赛实现

若开启加时赛且倒计时结束双方 HP > 0：
1. 显示"进入加时赛"提示
2. 加载一张无墙壁的空地图
3. 双方以当前 HP 继续对战，时间重置为 5 分钟
4. 加时赛结束时按 HP 判定胜负

### 16.4 超时惩罚实现

若开启超时惩罚且倒计时结束双方 HP > 0：
1. 不结束对战，启动惩罚计时器
2. 每隔相同时间间隔（如 30 秒），双方同时扣除相同血量（如 50 HP）
3. 一方 HP ≤ 0 或全部阵亡时结束

---

## 十七、暂停系统

### 17.1 触发

ESC 键按下 → 在 KeyListener 中处理

### 17.2 实现逻辑

1. 设置 `GameThread.paused = true`（逻辑线程停止更新）
2. 渲染线程继续运行（最后一帧画面保持）
3. 在 MyGameJPanel 上层覆盖一个半透明遮罩（`new Color(0,0,0,128)` 填充）
4. 遮罩上显示三个按钮：继续、重新开始、结束对战

**继续：**
- 关闭遮罩 → `GameThread.resume()` → 隐藏暂停面板
- 注意：`resume()` 中重置 `lastFrameTime` 防止暂停期间累积的时间差导致一帧跳过多步

**重新开始：**
- `GameThread.running = false` → 等待线程结束
- 重新调用 `startBattle()` 以相同配置重新初始化

**结束对战：**
- `GameThread.running = false`
- 判玩家失败
- 跳转到结算界面

### 17.3 实现方式

PausePanel 使用 `JLayeredPane` 或直接作为 GlassPane 添加到 JFrame：
- `frame.setGlassPane(pausePanel)`
- `pausePanel.setVisible(true)`
- 暂停面板拦截所有键盘/鼠标事件直到用户做出选择

---

## 十八、HUD（对战界面叠加层）

### 18.1 绘制内容

HUD 在 paintComponent 的最后绘制，在所有游戏元素之上。

**左右两侧：**
- 左侧（P1）：血量条、弹药指示器、换弹进度条、耐久值指示器
- 右侧（P2/Boss）：同上

**顶部中间：**
- 对战倒计时（格式 MM:SS）

**每个坦克上方：**
- 已安装的改装装备小图标（最多 5 个）

### 18.2 血量条绘制

```
背景矩形（深灰）+ 前景矩形（绿色→黄色→红色，根据血量百分比）+ 数值文字
位置：屏幕左侧 P1（x=20），屏幕右侧 P2（x=1360-220）
大小：200×18
```

使用 `g2d.fillRect()` 绘制矩形，`g2d.drawString()` 绘制文字"H P: 350 / 420"。

### 18.3 弹药显示

- 当前弹药 / 最大弹药：图标 + 数字（如 "🔫 5/7"）
- 换弹中：显示进度条或闪烁文字 "换弹中..."

### 18.4 改装装备图标

- 在坦克上方绘制小图标（20×20 或 24×24）
- 冷却中的槽用灰色/半透明图标
- 改装槽未解锁的不绘制

---

## 十九、UI 面板复用设计

### 19.1 BasePanel 模板方法模式

所有功能面板（DevelopPanel, GachaPanel, TankSelectPanel, BattleResultPanel）继承 BasePanel。

**BasePanel 提供：**
- 顶部标题栏（Panel 标题 + 返回主菜单按钮）
- 统一的按钮样式工厂方法 `createStyledButton(text)`
- 背景图绘制
- 子类只需实现 `buildContent()` 方法

### 19.2 共享资源栏（ResourceBar）

资源栏放在 MyJFrame 的 NORTH 位置（CardLayout 外部），所有页面共享同一个实例。

**显示内容：** 粗铁数量、钢铁数量、蓝图数量（三个图标 + 数字）

**更新：** `frame.updateResourceBar(iron, steel, blueprints)`，每次面板切换或资源变更后调用

### 19.3 延迟实例化

面板不在启动时全部创建，而是在首次切换时创建并缓存：

```java
Map<String, JPanel> panelCache = new HashMap<>();

public void showPanel(String name) {
    if (!panelCache.containsKey(name)) {
        panelCache.put(name, createPanel(name));
    }
    cardLayout.show(contentArea, name);
    if (name != "battle") {
        updateResourceBar();
    }
}
```

---

## 二十、资源管理与异常处理

### 20.1 图片加载降级

**三级降级策略：**
1. 文件不存在 → 使用占位图（品红色 40×40，开发期显眼），打印 WARN 日志
2. 文件存在但格式损坏 → 同上
3. 文件存在但读取 IO 异常 → 同上

**占位图生成：** 在 static 块中创建一个 BufferedImage，绘制品红色填充 + 黑色叉号。

**原则：** 图片缺失不应导致程序崩溃。玩家最多看到紫色方块，但游戏可继续进行。

### 20.2 地图加载降级

1. .map 文件缺失 → 返回空地图（仅边界 Iron 墙）
2. 坐标解析错误 → 跳过错误行，解析剩余行
3. 地图数据全空 → 返回空地图

### 20.3 存档加载降级

1. 文件不存在 → 走创建新档流程（正常情况）
2. JSON 格式错误 → 弹窗提示，引导选择其他存档或创建新档
3. 必需字段缺失 → 同上
4. 版本不匹配 → 尝试 migrate() 升级，失败则弹窗提示

### 20.4 存档写入保护（原子写入）

三步法防止写入过程中崩溃导致数据丢失：
1. 写临时文件 `.tmp`
2. 备份当前文件为 `.bak`
3. 原子重命名 `.tmp` → 正式文件名
4. 失败时尝试从 `.bak` 恢复

---

## 二十一、跨模块关注点

### 21.1 GameContext（全局上下文）

需要一个简单的全局持有类，存储当前会话的运行状态：

```java
public class GameContext {
    public static PlayerSaveData currentSave;     // 当前存档数据
    public static boolean isInBattle;             // 是否在对战中
    public static String battleMode;              // "pve" / "pvp"
    public static String difficulty;              // "easy" / "hard" / "super"
}
```

各模块通过 GameContext 访问共享状态，避免模块间直接引用。

### 21.2 线程安全

| 共享数据 | 访问者 | 安全策略 |
|----------|--------|----------|
| ElementManager 列表 | GameThread（写）+ 渲染线程（读） | CopyOnWriteArrayList |
| Players 属性 | KeyListener（写）+ GameThread（读写） | volatile + synchronized 关键方法 |
| GameThread.running/paused | 多个线程 | volatile |
| PlayerSaveData | UI 线程 | 单线程访问（EDT） |

### 21.3 事件通知

模块间松耦合通知使用简单的回调/监听器模式：

- `GameThread.OnBattleEnd` 回调 → 通知结算模块
- `Players.OnTankDestroyed` 回调 → 通知 HUD 更新
- `SaveManager.OnSaveComplete` 回调 → 通知 UI 显示"保存成功"

不需要完整的事件总线，简单的 `Runnable` 回调或 `Consumer<T>` 即可满足需求。

---

## 二十二、开发顺序建议

按依赖关系和风险优先级排列：

| 阶段 | 内容 | 产出 |
|------|------|------|
| **第 1 步** | 项目骨架：GameConfig, SuperElement, 空子类 | 编译通过，可运行空白窗口 |
| **第 2 步** | 资源加载：ElementLoad, MapData, TankDataManager | 地图和图片能正确加载 |
| **第 3 步** | 元素管理：ElementManager, ElementFactory | 能创建和存储元素 |
| **第 4 步** | 对战基础：GameThread, MyGameJPanel, KeyListener | 坦克能在空白地图中移动和射击 |
| **第 5 步** | 墙壁与碰撞：Brick, Iron, CollisionUtil | 墙壁阻挡、子弹碰撞、反弹 |
| **第 6 步** | 子弹系统完善：散射、激光、各武器类型 | 五种武器效果的完整实现 |
| **第 7 步** | 存档系统：SaveManager, PlayerSaveData, 所有 VO | 存档读写正常 |
| **第 8 步** | 登录与菜单：LoginPanel, MainMenuPanel, CardLayout | 完整的登录→菜单→退出流程 |
| **第 9 步** | 坦克选择与对战初始化：TankSelectPanel, 地图加载 | 选择坦克和地图后进入对战 |
| **第 10 步** | AI 系统：EasyAI, HardAI, SuperAI | 人机对战可玩 |
| **第 11 步** | HUD 与暂停：血量/弹药显示, ESC 暂停菜单 | 完整对战体验 |
| **第 12 步** | 结算系统：BattleResultPanel, 资源发放, 冷却推进 | 对战→结算→保存的完整闭环 |
| **第 13 步** | 养成系统：DevelopPanel, 升级/进阶/改装 | 坦克养成功能完整 |
| **第 14 步** | 抽卡系统：GachaPanel, GachaManager, 保底 | 抽卡功能完整 |
| **第 15 步** | 对战设置：PvP 配置、加时赛、超时惩罚、作弊 | 双人对战的完整配置 |
| **第 16 步** | UI 优化：BasePanel 重构、资源栏、延迟加载 | 代码整洁，体验优化 |
| **第 17 步** | 异常处理与测试：降级策略、边界测试 | 健壮性完善 |

每一阶段完成后进行编译验证和基本功能测试，确保不积累问题。

---

*文档版本: v1.0*
*编写日期: 2026年7月*

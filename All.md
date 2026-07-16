# 坦克大战游戏——软件开发文档

---

## 一、项目概述

### 1.1 项目背景与选题意义

本项目是一款基于 Java Swing 的**坦克对战桌面游戏**。玩家通过键盘操控坦克在地图中移动、射击，与 AI 对手或其他玩家进行对战。项目以游戏为业务载体，核心目标是实践软件工程的完整开发流程——从需求分析、架构设计到编码实现与测试验证，重点训练 MVC 设计模式的落地能力、面向对象设计思维以及多线程编程技术。

### 1.2 核心功能与项目目标

系统围绕"坦克对战"这一核心玩法，向外延伸出四大功能模块：

| 模块 | 核心功能 |
|------|----------|
| **登录与导航** | 用户登录（含存档选择/创建新档）、主界面导航（对战/坦克养成/坦克获取/退出）、退出存档提示 |
| **对战系统** | 人机对战（简单/困难/超级三级难度）、双人对战、地图选择、坦克选择、对战设置（时长/加时赛/超时惩罚/作弊）、数据统计、暂停/继续/重新开始 |
| **坦克养成** | 坦克升级（消耗粗铁）、坦克进阶（消耗钢铁）、改装槽解锁与装备、改装碎片合成、详情查看 |
| **坦克获取** | 蓝图抽卡获取新坦克、改装装备碎片抽取、保底机制、双池独立概率 |

项目目标是实现一个**可运行、可演示、架构清晰**的桌面游戏，完整覆盖上述功能，代码遵循 MVC 架构，具备良好的可扩展性与可维护性。

### 1.3 技术栈与设计模式

| 技术选型 | 说明 |
|----------|------|
| **开发语言** | Java（JDK 8+） |
| **UI 框架** | Java Swing（`javax.swing`） |
| **绘图机制** | `Graphics2D` + 双缓冲技术 |
| **资源加载** | `ImageIO` 读取图片资源，自定义解析器读取 `.map` 地图文件 |
| **数据结构** | `Map<String, List<SuperElement>>` 统一管理游戏元素 |
| **依赖库** | `commons-collections`（提供集合工具支持） |
| **运行环境** | Windows / macOS / Linux，JRE 8+ |

**采用的设计模式**：

| 设计模式 | 应用场景 | 选型理由 |
|----------|----------|----------|
| **MVC** | 系统总体架构 | 将数据、界面、控制逻辑分离，各层独立变化，降低耦合 |
| **单例模式** | `ElementManager`、`ElementLoad`、`ElementFactory` | 确保全局唯一入口，避免资源重复加载与数据多副本不一致 |
| **工厂模式** | `ElementFactory` 创建所有游戏元素 | 封装对象创建逻辑，上层模块无需关心构造细节 |
| **模板方法模式** | `SuperElement` 基类的 `update()` / `show()` | 定义算法骨架，子类重写特定步骤，实现多态 |
| **策略模式** | 不同难度 AI 的行为逻辑 | 将 AI 决策算法封装为可替换的策略类 |

### 1.4 团队分工与开发周期

| 阶段 | 周期 | 主要任务 |
|------|------|----------|
| **需求分析与设计** | 第 1-2 周 | 需求拆解、架构设计、类图绘制、技术预研 |
| **基础框架搭建** | 第 3 周 | 项目骨架、MVC 三层基类、资源加载模块 |
| **对战核心开发** | 第 4-5 周 | 坦克移动、子弹发射、碰撞检测、双线程游戏循环 |
| **养成与获取系统** | 第 6 周 | 坦克升级进阶、改装系统、抽卡系统 |
| **界面完善与联调** | 第 7 周 | 登录、导航、设置界面、各模块联调 |
| **测试与文档** | 第 8 周 | 功能测试、Bug 修复、文档完善 |

---

## 二、前期准备与技术预研

### 2.1 MVC 设计模式

MVC（Model-View-Controller）是本项目的核心架构模式，其核心思想是**分层解耦、职责单一**：

- **Model（模型层）**：存储所有游戏元素的数据，封装元素自身行为（移动、攻击、状态更新），是系统唯一的数据中心。Model 层完全独立于 View 和 Control，不处理界面绘制，不直接响应用户输入。
- **View（视图层）**：负责界面渲染、画面定时刷新、UI 元素绘制。View 层只从 Model 层**读取**数据，绝不修改任何元素状态，也不包含业务逻辑。
- **Control（控制层）**：接收用户输入（键盘、鼠标事件），驱动游戏主循环（定时更新所有元素状态），调度碰撞检测、边界判定等全局逻辑，控制游戏流程（开始、暂停、结束、关卡切换）。Control 层是 Model 与 View 的桥梁——它修改 Model 层数据，触发 View 层重绘，但本身不直接执行绘制操作。

**数据流向约束**：

```
用户输入 → Control（捕获事件）→ Model（修改数据）→ View（读取数据并绘制）
```

这一单向数据流保证了：修改界面不影响逻辑，修改数据不影响渲染，三层可独立开发与测试。

### 2.2 Java Swing 绘图机制

Swing 的绘图基于 **AWT 的绘制管线**，核心是 `JPanel.paintComponent(Graphics g)` 方法：

- **双缓冲技术**：默认情况下 Swing 已启用双缓冲。先将所有图形绘制到后台缓冲区，再一次性地将完整画面输出到屏幕，避免逐元素绘制导致的画面闪烁与撕裂。
- **帧率控制**：通过 `javax.swing.Timer` 或独立线程中的 `Thread.sleep()` 控制重绘间隔。本项目采用**渲染线程**以固定帧率（目标 60 FPS）周期性调用 `repaint()`，触发 `paintComponent()`。
- **Graphics2D**：相比 `Graphics` 提供更丰富的绘图能力——抗锯齿、旋转变换、透明度控制，适合坦克旋转、子弹轨迹等视觉需求。

### 2.3 多线程游戏循环

游戏同时运行两个核心线程，通过 Model 层共享数据：

| 线程 | 职责 | 帧率 | 所属层 |
|------|------|------|--------|
| **游戏逻辑线程** (`GameThread`) | 遍历所有元素执行 `update()`、碰撞检测、边界判定、元素销毁与新增 | 60 FPS（约 16ms/帧） | Control |
| **界面渲染线程** (`MyGameJPanel` 内部) | 从 `ElementManager` 获取所有元素，调用每个元素的 `show()` 方法完成绘制 | 60 FPS | View |

两条线程的协作建立在**共享 Model 数据**的基础上：逻辑线程修改元素状态，渲染线程读取元素状态。对 `ElementManager` 中集合的访问需要**线程安全保护**——使用 `CopyOnWriteArrayList` 或 `synchronized` 块确保并发安全。

### 2.4 碰撞检测算法

采用**外接矩形碰撞检测（AABB）**作为基础算法：将每个游戏元素定义为一个矩形碰撞体（`x, y, width, height`），两两比较矩形是否相交。

对于子弹（高速小体积）与坦克的碰撞，需要在每帧对子弹的运动轨迹做**插值检测**，防止子弹因速度过快而"穿透"目标。

边界碰撞（子弹与墙壁）的检测流程：
1. 获取子弹下一步位置
2. 查询该位置的墙壁元素（从 `ElementManager` 的墙壁列表中获取）
3. 若存在墙壁，根据墙壁类型（砖墙/钢铁）决定子弹行为（销毁/反弹/穿透）

### 2.5 开发环境与依赖

| 项目 | 说明 |
|------|------|
| JDK 版本 | JDK 8 及以上 |
| 开发工具 | IntelliJ IDEA / Eclipse |
| 依赖库 | `commons-collections-4.x`（提供 `MultiMap` 等工具类）、`gson-2.10.1`（JSON 序列化，存档系统） |
| 资源文件 | `resource/image/`（关卡预览图 PNG），`resource/data/`（地图数据 `.map`） |

### 2.6 需求拆解与功能规划

基于《系统功能架构设计》文档，将系统拆解为以下可落地的功能模块，按优先级排列：

| 优先级 | 模块 | 功能点 |
|--------|------|--------|
| P0（基础骨架） | 程序入口、窗体框架 | `main` 启动、`MyJFrame` 窗体、`MyGameJPanel` 画板 |
| P0 | 资源加载 | `ElementLoad` 读取地图、图片资源 |
| P0 | 元素基类与工厂 | `SuperElement`、`ElementFactory`、`ElementManager` |
| P0 | 存档系统 | `SaveManager`（JSON 序列化）、`PlayerSaveData` 数据结构、登录读档/退出存档 |
| P1（核心玩法） | 对战系统 | 双线程游戏循环、坦克移动与射击、子弹飞行、碰撞检测、地图渲染 |
| P1 | AI 对手 | 简单/困难/超级三级难度的 AI 行为 |
| P2（外围系统） | 登录与导航 | 用户登录、主界面、模块间导航 |
| P2 | 坦克养成 | 升级、进阶、改装装备、碎片合成 |
| P2 | 坦克获取（抽卡） | 蓝图抽卡、双池概率、保底机制 |
| P3（体验优化） | 对战设置 | 时长设置、加时赛、超时惩罚、作弊选项 |
| P3 | 对战统计 | 开火数、造成伤害、受到伤害统计与结算展示 |
| P3 | 暂停系统 | ESC 暂停菜单、继续/重新开始/结束对战 |

---

## 三、系统架构设计

### 3.1 总体分层架构

系统严格遵循 MVC 三层架构，每层具有明确的职责边界与交互规则。下图展示了三层之间的依赖关系与数据流向：

```
┌─────────────────────────────────────────────────┐
│                   View 层 (frame)                 │
│  ┌──────────┐  ┌──────────────────┐              │
│  │ MyJFrame │  │ MyGameJPanel      │              │
│  │ (窗体)    │  │  (画板+渲染线程)   │              │
│  └──────────┘  └──────────────────┘              │
│        │                │                         │
│        │     读取数据    │                         │
│        ▼                ▼                         │
├─────────────────────────────────────────────────┤
│                  Model 层                          │
│  ┌──────────┐  ┌───────────┐  ┌───────────────┐  │
│  │ Element  │  │ Element   │  │ Element       │  │
│  │ Load     │  │ Factory   │  │ Manager       │  │
│  │(资源加载) │  │(元素工厂)  │  │(元素管理器)    │  │
│  └──────────┘  └───────────┘  └───────────────┘  │
│  ┌──────────┐                                      │
│  │ Save     │  (存档读写，JSON 序列化)              │
│  │ Manager  │                                      │
│  └──────────┘                                      │
│                      │               │            │
│                      ▼               ▼            │
│         ┌──────────────────────────────────┐      │
│         │         model.vo 实体层            │      │
│         │  SuperElement (抽象基类)           │      │
│         │  ├── Players (玩家坦克)            │      │
│         │  ├── Bullet (子弹)                 │      │
│         │  ├── Boss (AI坦克)                 │      │
│         │  ├── Brick (砖墙)                  │      │
│         │  ├── Iron (钢铁墙)                 │      │
│         │  └── Background (背景)             │      │
│         └──────────────────────────────────┘      │
│                      ▲                            │
│       修改数据       │                            │
├─────────────────────│────────────────────────────┤
│                  Control 层 (thread)               │
│  ┌──────────┐  ┌──────────────┐  ┌────────────┐  │
│  │GameThread│  │KeyListener   │  │MouseListener│  │
│  │(游戏主线程)│  │(键盘事件监听) │  │(鼠标事件监听) │  │
│  └──────────┘  └──────────────┘  └────────────┘  │
└─────────────────────────────────────────────────┘
```

#### 三层职责划分

| 层级 | 对应模块 | 核心职责 | 边界规则 |
|------|----------|----------|----------|
| **Model** | `model.load`、`model.vo`、`model.manager` | 存储所有游戏元素数据、封装元素行为、管理资源与对象、系统的唯一数据中心 | 完全独立，不依赖 View 和 Control；只暴露数据访问接口，不处理界面绘制 |
| **View** | `frame` | 界面渲染、画面定时刷新、UI 元素绘制、地图背景展示 | 只读 Model 层数据，不修改任何元素状态；不处理业务逻辑 |
| **Control** | `thread` | 接收用户输入、驱动游戏主循环、调度碰撞检测、位置更新、控制游戏流程 | View 与 Model 的桥梁；只修改 Model 层数据，不直接执行界面绘制 |

### 3.2 核心运行流程

#### （1）初始化流程

```
main() 启动
  → ElementLoad.getInstance() 读取地图配置文件与关卡图片资源
  → ElementFactory 创建初始元素实例（地图墙壁、玩家坦克、AI 坦克、背景等）
  → ElementManager 统一分类存储所有元素
  → MyJFrame 初始化窗体，绑定 MyGameJPanel
  → MyGameJPanel 绑定键盘监听器（KeyListener）
  → 启动 GameThread（游戏逻辑线程）
  → 启动渲染线程（JPanel 内部 Timer）
```

#### （2）运行时主流程

系统运行时存在**三条链路**并行运作：

**输入链路**（用户操作→数据变化）：
```
用户按下/释放键盘 → KeyListener 捕获事件（Control 层）
  → 根据键位判断操作类型（移动/射击/换弹/暂停）
  → 修改 Model 层对应 Players 对象的状态属性（方向、速度、开火标志等）
```

**逻辑链路**（数据更新→世界变化）：
```
GameThread 每帧循环（~16ms）→ 遍历 ElementManager 中所有元素
  → 调用每个元素的 update() 方法更新自身状态
  → 执行碰撞检测（子弹vs墙壁、子弹vs坦克、坦克vs墙壁）
  → 处理边界判定（元素是否超出地图范围）
  → 处理元素的创建（新子弹）与销毁（死亡、超时）
```

**渲染链路**（数据→画面）：
```
渲染线程每帧触发 repaint() → paintComponent() 调用
  → 从 ElementManager 获取所有可见元素列表
  → 按 z-order 排序（背景→墙壁→坦克→子弹→UI叠加层）
  → 调用每个元素的 show(Graphics2D) 方法绘制到缓冲区
  → 一次性将缓冲区输出到屏幕
```

### 3.3 核心机制设计

#### （1）双线程机制

游戏逻辑线程（`GameThread`）与界面渲染线程（`MyGameJPanel` Timer）完全分离，通过统一的 Model 层实现数据共享。这种设计确保即使逻辑计算出现短暂卡顿（如碰撞检测密集时），画面渲染仍可保持流畅（继续绘制上一帧的状态）。

**线程安全策略**：`ElementManager` 中存储元素的集合使用 `CopyOnWriteArrayList`，确保渲染线程遍历元素时不会因逻辑线程的并发修改而抛出 `ConcurrentModificationException`。

#### （2）单例模式

| 单例类 | 职责 | 单例理由 |
|--------|------|----------|
| `ElementLoad` | 资源加载（地图解析、图片读取） | 配置文件与图片只需加载一次，全局共享 |
| `ElementFactory` | 元素对象创建 | 统一管理对象创建逻辑，避免分散的 `new` 调用 |
| `ElementManager` | 元素存储与检索 | 全局唯一的元素容器，确保逻辑层与渲染层访问同一份数据 |

#### （3）工厂模式

`ElementFactory` 封装了所有游戏元素的创建逻辑。上层模块（如 `GameThread`）只需调用 `ElementFactory.createBullet(x, y, direction, owner)` 即可获得子弹对象，无需关心子弹的构造参数（速度、伤害、持续时间等由工厂从配置中读取）。

**扩展性**：新增元素类型时，仅在工厂中增加对应的创建方法，不影响调用方代码。

#### （4）统一元素容器

`ElementManager` 内部使用 `Map<String, List<SuperElement>>` 按类型存储所有元素：

| Key | Value 类型 | 说明 |
|-----|-----------|------|
| `"players"` | `List<SuperElement>` | 玩家坦克列表（1-2个） |
| `"boss"` | `List<SuperElement>` | AI 坦克列表 |
| `"bullet"` | `List<SuperElement>` | 所有子弹 |
| `"brick"` | `List<SuperElement>` | 砖墙（可破坏） |
| `"iron"` | `List<SuperElement>` | 钢铁墙（不可破坏） |
| `"background"` | `List<SuperElement>` | 背景元素 |

这种分类存储方式使得碰撞检测可按需筛选（如子弹只与坦克、墙壁做碰撞检测），避免全量暴力遍历。

### 3.4 包结构设计

```
src/
├── main/                      # 程序入口
│   └── Main.java              # main() 方法，启动流程
│
├── frame/                     # 视图层
│   ├── MyJFrame.java          # 主窗体（JFrame）
│   ├── MyGameJPanel.java      # 游戏画板（JPanel + 渲染线程）
│   └── ui/                    # UI 子组件
│       ├── LoginPanel.java    # 登录界面
│       ├── MainMenuPanel.java # 主菜单导航
│       ├── TankSelectPanel.java # 坦克/地图选择
│       ├── DevelopPanel.java  # 坦克养成界面
│       ├── GachaPanel.java    # 坦克获取（抽卡）界面
│       ├── GachaRevealPanel.java # 抽卡翻牌展示层（牌背翻转 + 大奖金光）
│       └── PausePanel.java    # 暂停菜单
│
├── model/
│   ├── vo/                    # 实体类（Value Object）
│   │   ├── SuperElement.java  # 抽象基类
│   │   ├── Players.java       # 玩家坦克
│   │   ├── Boss.java          # AI 坦克
│   │   ├── Bullet.java        # 子弹
│   │   ├── Brick.java         # 砖墙
│   │   ├── Iron.java          # 钢铁墙
│   │   ├── Background.java    # 背景
│   │   ├── Explosion.java     # 爆炸特效
│   │   ├── PlayerSaveData.java # 存档根对象
│   │   ├── OwnedTank.java     # 存档中的坦克数据快照
│   │   ├── Modification.java   # 改装装备（含枚举 Type）
│   │   └── TankData.java      # 坦克属性模板
│   │
│   ├── load/                  # 资源加载
│   │   ├── ElementLoad.java   # 资源配置解析与图片加载
│   │   └── MapData.java       # 地图数据对象
│   │
│   └── manager/               # 管理器
│       ├── ElementManager.java # 元素管理器（单例）
│       ├── ElementFactory.java # 元素工厂（单例）
│       ├── TankDataManager.java # 坦克属性数据管理
│       ├── GachaManager.java  # 抽卡概率管理
│       └── SaveManager.java   # 存档管理器（单例，JSON 序列化）
│
├── thread/                    # 控制层
│   ├── GameThread.java        # 游戏主逻辑线程
│   ├── KeyListener.java       # 键盘监听器
│   ├── MouseListener.java     # 鼠标监听器
│   └── ai/                    # AI 子系统
│       ├── AIController.java  # AI 控制器接口
│       ├── EasyAI.java        # 简单难度 AI
│       ├── HardAI.java        # 困难难度 AI
│       └── SuperAI.java       # 超级难度 AI
│
└── util/                      # 工具类
    ├── GameConfig.java        # 游戏全局常量配置
    └── CollisionUtil.java     # 碰撞检测工具类
```

### 3.5 类层级设计

以继承体系为核心，体现面向对象的架构设计：

```
SuperElement (抽象基类)
│  属性: x, y, width, height, visible, direction
│  抽象方法: show(Graphics2D), update()
│  通用方法: move(), destroy(), isStrike(SuperElement)
│
├── Players (玩家坦克)
│    特有属性: hp, attack, defense, speed, turnSpeed, fireRate,
│             bulletSpeed, bulletDuration, ammo, reloadTime,
│             durability, name, model, rank, level,
│             modifications[], cooldownSlots[]
│    特有方法: fire(), reload(), takeDamage(int), upgrade(), evolve()
│
├── Boss (AI 坦克，继承自 Players 的能力集，但由 AI 驱动)
│    特有属性: aiController (AI策略)
│    特有方法: aiDecide() (AI 决策)
│
├── Bullet (子弹)
│    特有属性: speed, damage, owner, lifeTime, isRebound,
│             isLaser (是否为激光), reboundCount
│    特有方法: calculateTrajectory(), onHit()
│
├── Brick (砖墙——可被子弹破坏)
│    特有方法: onDestroyed()
│
├── Iron (钢铁墙——不可破坏)
│
├── Background (背景元素)
│
└── Explosion (爆炸特效——临时元素，动画结束后自动销毁)
```

### 3.6 类关系说明

| 关系类型 | 具体关系 | 说明 |
|----------|----------|------|
| **继承** | `Players`、`Boss`、`Bullet`、`Brick`、`Iron`、`Background` → `SuperElement` | 所有可视元素共享基类属性与方法，是多态渲染与统一管理的基础 |
| **组合** | `ElementManager` 组合 `Map<String, List<SuperElement>>` | 元素的分类存储与生命周期由管理器全权负责 |
| **组合** | `Players` 组合 `Modification[]`、`hp`、`ammo` 等属性 | 坦克的属性划分为多个子对象/子属性 |
| **依赖** | `GameThread` 依赖 `ElementManager` → 获取元素列表进行更新与碰撞检测 | 控制层通过管理器读写 Model 数据 |
| **依赖** | `MyGameJPanel` 依赖 `ElementManager` → 获取元素列表进行绘制 | 视图层通过管理器读取 Model 数据 |
| **依赖** | `ElementFactory` 依赖 `ElementLoad` → 获取资源配置创建对象 | 工厂使用加载器提供的资源参数 |
| **依赖** | `Boss` 依赖 `AIController` → 决策移动与攻击行为 | AI 通过策略接口注入 |

---

## 四、详细类设计

### 4.1 SuperElement（抽象基类）

所有游戏元素的公共祖先，定义了统一的操作接口。

```
public abstract class SuperElement {
    // === 通用属性 ===
    protected int x, y;           // 坐标（左上角）
    protected int width, height;  // 宽高
    protected int direction;      // 朝向角度（0-360，0=正上方）
    protected boolean visible;    // 是否可见
    protected Rectangle rect;     // 碰撞矩形（由x,y,width,height计算）

    // === 通用方法 ===
    public abstract void show(Graphics2D g);  // 绘制自身
    public abstract void update();             // 更新自身状态（每帧调用）

    public void move(int dx, int dy);          // 移动
    public void destroy();                     // 标记销毁（visible = false）
    public boolean isStrike(SuperElement other); // 碰撞检测（矩形相交判定）
    public Rectangle getRect();                // 获取碰撞矩形
}
```

**设计要点**：
- `show()` 与 `update()` 为抽象方法，强制子类实现，保证多态调用。
- `isStrike()` 使用 AABB 算法（`Rectangle.intersects()`），子类可按需重写为更精确的碰撞检测（如圆形碰撞）。
- `destroy()` 将 `visible` 设为 `false`，`GameThread` 在下一次循环中移除不可见元素。

### 4.2 Players（玩家坦克）

继承 `SuperElement`，封装坦克的所有属性与行为。

```
public class Players extends SuperElement {
    // === 坦克基本信息 ===
    private String name;             // 坦克名称（用户可修改）
    private String modelName;        // 型号（由系统计算，不可修改）
    private int rank;                // 阶数（0-10）
    private int level;               // 等级（1-10）

    // === 战斗属性 ===
    private int hp;                  // 当前血量
    private int maxHp;               // 最大血量
    private int attack;              // 攻击力
    private int defense;             // 防御力
    private int speed;               // 移动速度
    private int turnSpeed;           // 转向速度（rad/帧）
    private int fireRate;            // 射击间隔（ms）
    private long lastFireTime;       // 上次射击时间戳
    private int bulletSpeed;         // 子弹速度
    private int bulletDuration;      // 子弹持续时间（s）
    private int ammo;                // 当前备弹数
    private int maxAmmo;             // 弹匣容量
    private int reloadTime;          // 换弹时间（ms）
    private boolean isReloading;     // 是否正在换弹
    private long reloadStartTime;    // 换弹开始时间戳
    private int durability;          // 当前耐久值
    private int maxDurability;       // 初始耐久值

    // === 成长属性 ===
    private int upgradeAtkGain;      // 每级攻击力成长
    private int upgradeDefGain;      // 每级防御力成长
    private int upgradeSpdGain;      // 每级速度成长
    private int evolveAtkGain;       // 每阶攻击力成长
    private int evolveDefGain;       // 每阶防御力成长
    private int evolveSpdGain;       // 每阶速度成长

    // === 改装系统 ===
    private int unlockedSlots;       // 已解锁的改装槽数量
    private Modification[] installedMods; // 已安装的改装装备（最多5个）
    private int[] cooldownSlots;     // 改装槽冷却剩余对局数（0=可用）

    // === 对战统计 ===
    private int shotsFired;          // 开火数
    private int damageDealt;         // 造成的伤害
    private int damageReceived;      // 受到的伤害

    // === 核心方法 ===
    public void fire();              // 发射子弹（生成 Bullet 对象加入 ElementManager）
    public void reload();            // 开始换弹
    public void takeDamage(int rawDamage); // 受到伤害（含耐久减免计算）
    public void moveForward();       // 向前移动
    public void moveBackward();      // 向后移动
    public void rotateCW();          // 顺时针旋转
    public void rotateCCW();         // 逆时针旋转
    public void upgrade();           // 升级（消耗粗铁）
    public void evolve();            // 进阶（消耗钢铁）
    public void installMod(Modification mod, int slotIndex); // 安装改装装备
    public void removeMod(int slotIndex); // 拆除改装装备（装备销毁 + 冷却）
    public float getDamageReduction(); // 根据耐久值百分比计算伤害减免率

    @Override
    public void update();            // 每帧更新（处理移动状态、换弹计时等）
    @Override
    public void show(Graphics2D g);  // 绘制坦克（按朝向旋转后绘制）
}
```

**伤害计算（耐久减免）逻辑**：

```java
public void takeDamage(int rawDamage) {
    float reduction = getDamageReduction();  // 基于耐久百分比的减免
    int actualDamage = rawDamage - (int)(defense * reduction);
    if (actualDamage < 0) actualDamage = 0;
    hp -= actualDamage;
    durability -= rawDamage;  // 耐久值减少 = 原始攻击力
    if (durability < 0) durability = 0;
    if (hp <= 0) destroy();
}

private float getDamageReduction() {
    float ratio = (float) durability / maxDurability;
    if (ratio > 0.75f) return 1.0f;       // > 75%: 100% 防御减免
    if (ratio > 0.50f) return 0.5f;        // 50-75%: 50% 防御减免
    if (ratio > 0.25f) return 0.25f;       // 25-50%: 25% 防御减免
    return 0.0f;                            // < 25%: 无减免
}
```

**型号计算逻辑**：

```
型号格式: {槽位数字母}{-}{已安装数罗马数字}
  槽位数字母: A=1槽, B=2槽, C=3槽, D=4槽, E=5槽
  已安装数罗马数字: I=1, II=2, III=3, IV=4, V=5

示例: 解锁3槽且安装2个 → 型号为 "C-II"
```

### 4.3 Bullet（子弹）

```java
public class Bullet extends SuperElement {
    private int speed;               // 子弹速度
    private int damage;              // 伤害值（已考虑改装影响）
    private Players owner;           // 发射者（用于友伤判断）
    private long spawnTime;          // 创建时间戳
    private int lifeTime;            // 存活时间（ms）
    private boolean isLaser;         // 是否为激光
    private boolean rebound;         // 是否可反弹
    private int maxRebounds;         // 最大反弹次数（激光炮=5）
    private int reboundCount;        // 已反弹次数
    private int sprayIndex;          // 散射序号（轻/重机枪的扇形子弹）

    @Override
    public void update() {
        // 1. 按朝向计算新位置
        // 2. 检查生命周期（超时则 destroy()）
        // 3. 碰撞检测由 GameThread 统一处理
    }

    @Override
    public void show(Graphics2D g) {
        if (isLaser) {
            // 绘制激光线（从发射点到当前端点 / 反弹路径）
        } else {
            // 绘制圆形子弹
        }
    }
}
```

**子弹行为差异（由改装装备决定）**：

| 改装装备 | 子弹行为改变 |
|----------|-------------|
| 无特殊装备 | 单发子弹，遇普通墙壁反弹，遇钢铁墙反弹，遇边界反弹 |
| 轻机枪 | 扇形 3 发散射，每发伤害=攻击力×40%，遇墙立即销毁 |
| 重机枪 | 扇形 5 发散射，每发伤害=攻击力×30%，遇墙立即销毁 |
| 激光炮 | 单束激光，直线穿透所有单位（伤害=攻击力×10%），不反弹 |
| 高密度镭射群炮 | 单束激光，直线穿透 + 遇墙反弹（最多5次），伤害=攻击力×10% |
| 反斜钢甲 | 友方子弹不再对友方造成伤害 |

### 4.4 Boss（AI 坦克）

`Boss` 继承自 `Players`，复用坦克的所有属性与战斗机制，但移动与攻击决策由 AI 控制器驱动。

```java
public class Boss extends Players {
    private AIController ai;  // AI 策略对象

    public Boss(TankData data, AIController ai) {
        super(data);
        this.ai = ai;
    }

    @Override
    public void update() {
        ai.decide(this);  // AI 决策：移动方向、是否开火
        super.update();    // 执行基类的状态更新
    }
}
```

**AI 难度分级**：

| 难度 | 移动策略 | 射击策略 | 特点 |
|------|----------|----------|------|
| 简单 | 随机方向漫游，偶尔转向玩家 | 固定间隔射击 | 反应慢、精度低 |
| 困难 | 主动追踪玩家，保持一定距离 | 朝向玩家时射击，预判走位 | 攻击性中等 |
| 超级 | 路径规划（最短路径接近）、走位规避子弹 | 精确瞄准 + 预判射击 | 攻击性强、生存能力强 |

### 4.5 AIController（AI 控制器接口）

```java
public interface AIController {
    void decide(Boss boss);  // 每帧调用，设置 boss 的移动/射击状态
}
```

具体实现：`EasyAI`、`HardAI`、`SuperAI`。

### 4.6 ElementManager（元素管理器，单例）

```java
public class ElementManager {
    private static ElementManager instance;
    private Map<String, List<SuperElement>> elementMap;

    private ElementManager() {
        elementMap = new HashMap<>();
        elementMap.put("players", new CopyOnWriteArrayList<>());
        elementMap.put("boss", new CopyOnWriteArrayList<>());
        elementMap.put("bullet", new CopyOnWriteArrayList<>());
        elementMap.put("brick", new CopyOnWriteArrayList<>());
        elementMap.put("iron", new CopyOnWriteArrayList<>());
        elementMap.put("background", new CopyOnWriteArrayList<>());
        elementMap.put("explosion", new CopyOnWriteArrayList<>());
    }

    public static ElementManager getInstance() { /* DCL 单例 */ }

    public void addElement(String type, SuperElement e) { /* ... */ }
    public void removeElement(String type, SuperElement e) { /* ... */ }
    public List<SuperElement> getElements(String type) { /* ... */ }
    public Map<String, List<SuperElement>> getAllElements() { /* ... */ }
    public void clearAll() { /* 清空所有元素（重新开始时调用） */ }
}
```

### 4.7 ElementFactory（元素工厂，单例）

```java
public class ElementFactory {
    private static ElementFactory instance;

    public static ElementFactory getInstance() { /* DCL 单例 */ }

    public Players createPlayer(int tankId, int x, int y);       // 创建玩家坦克
    public Boss createBoss(int tankId, int x, int y, AIController ai); // 创建AI坦克
    public Bullet createBullet(int x, int y, int direction, Players owner,
                               boolean isLaser, boolean rebound, int maxRebounds,
                               float damageMultiplier, int bulletCount, int spreadAngle);
    public Brick createBrick(int x, int y);                       // 创建砖墙
    public Iron createIron(int x, int y);                         // 创建钢铁墙
    public Explosion createExplosion(int x, int y);               // 创建爆炸特效
}
```

### 4.8 ElementLoad（资源加载器，单例）

```java
public class ElementLoad {
    private static ElementLoad instance;
    private Map<Integer, BufferedImage> levelImages;  // 关卡预览图
    private Map<Integer, MapData> mapDataCache;       // 地图数据缓存
    private Map<Integer, TankData> tankDataMap;       // 坦克属性模板

    public static ElementLoad getInstance() { /* DCL 单例 */ }

    public MapData loadMap(int level);     // 加载指定关卡的地图数据
    public BufferedImage loadImage(String path);  // 加载图片资源
    public TankData getTankData(int tankId);      // 获取坦克属性模板
}
```

### 4.9 MapData（地图数据对象）

```java
public class MapData {
    private List<Point> brickPositions;  // 砖墙坐标列表
    private List<Point> ironPositions;   // 钢铁墙坐标列表
    private int width, height;           // 地图尺寸

    public static MapData parse(String filePath) {
        // 解析 .map 文件格式：
        // BRICK=x,y;x,y;...
        // IRON=x,y;x,y;...
    }
}
```

**`.map` 文件格式规范**：

```
BRICK=340,40;340,60;340,80;340,100;...
IRON=360,160;380,160;720,300;...
```

每行以 `BRICK=` 或 `IRON=` 开头，后跟分号分隔的坐标对 `x,y`。坐标表示墙壁块的像素位置。

### 4.10 TankData（坦克数据模板）

```java
public class TankData {
    private int id;
    private String name;
    private int baseHp, baseAttack, baseDefense, baseSpeed;
    private int baseTurnSpeed;          // rad/帧
    private int baseFireRate;           // ms
    private int baseBulletSpeed;
    private int baseBulletDuration;     // s
    private int baseAmmo;
    private int baseReloadTime;         // s
    private int baseDurability;
    private int upgradeAtkGain, upgradeDefGain, upgradeSpdGain;
    private int evolveAtkGain, evolveDefGain, evolveSpdGain;
}
```

**八种坦克初始属性**（依据《坦克对象》文档）：

| ID | 名称 | HP | 攻击 | 防御 | 速度 | 转向 | 射速 | 子弹速 | 持续 | 弹容 | 换弹 | 耐久 |
|----|------|-----|------|------|------|------|------|--------|------|------|------|------|
| 1 | 克伦威尔 | 420 | 67 | 30 | 30 | 10 | 1s | 30 | 5s | 5 | 3s | 880 |
| 2 | M24霞飞 | 340 | 30 | 45 | 50 | 15 | 0.5s | 40 | 5.5s | 7 | 1s | 520 |
| 3 | 谢尔曼萤火虫 | 200 | 120 | 20 | 55 | 10 | 0.5s | 60 | 4s | 10 | 3s | 400 |
| 4 | 约瑟夫IS | 450 | 150 | 100 | 15 | 2 | 4s | 25 | 3s | 3 | 8s | 300 |
| 5 | 猎豹 | 370 | 250 | 70 | 60 | 10 | 3s | 75 | 6s | 5 | 4s | 450 |
| 6 | T-34 | 300 | 40 | 35 | 70 | 10 | 1s | 40 | 5s | 15 | 5s | 330 |
| 7 | 豹式坦克 | 400 | 100 | 50 | 75 | 10 | 3s | 100 | 3s | 4 | 2s | 540 |
| 8 | 虎式坦克 | 350 | 80 | 56 | 65 | 10 | 2s | 110 | 4s | 4 | 2s | 540 |

#### 4.10.1 速度换算标准

《坦克对象》中给出的速度、转向速度、子弹速度均为**设计值**，不可直接作为每帧像素位移使用。若设计速度 50 直接对应每帧 50 像素，则坦克在 60 FPS 下每秒移动 3000 像素——远超 1380×820 的窗口范围，按一下键坦克就飞出屏幕。

**换算基准：**

| 属性 | 基准 | 公式 |
|------|------|------|
| 坦克移动速度 | 设计值 50 → 2.0 px/frame（120 px/s） | `实际速度 = 设计速度 × 0.04` |
| 坦克转向速度 | 设计值 10 → 0.10 rad/frame（约 343°/s，1 秒转一圈） | `实际转向 = 设计值 × 0.01` |
| 子弹飞行速度 | 设计值 50 → 3.0 px/frame（180 px/s） | `实际弹速 = 设计值 × 0.06` |
| 激光速度 | 取最快子弹速度 × 10 | 近乎瞬间到达 |

子弹速度因子（0.06）大于坦克速度因子（0.04），保证子弹一定能追上坦克。

**换算后各坦克的实际速度（60 FPS 下）：**

| ID | 名称 | 设计速度 | 实际速度 (px/frame) | 实际速度 (px/s) | 转向 (rad/frame) | 子弹设计速 | 实际弹速 (px/frame) |
|----|------|----------|---------------------|------------------|-------------------|------------|---------------------|
| 1 | 克伦威尔 | 30 | 1.2 | 72 | 0.10 | 30 | 1.8 |
| 2 | M24霞飞 | 50 | 2.0 | 120 | 0.15 | 40 | 2.4 |
| 3 | 谢尔曼萤火虫 | 55 | 2.2 | 132 | 0.10 | 60 | 3.6 |
| 4 | 约瑟夫IS | 15 | 0.6 | 36 | 0.02 | 25 | 1.5 |
| 5 | 猎豹 | 60 | 2.4 | 144 | 0.10 | 75 | 4.5 |
| 6 | T-34 | 70 | 2.8 | 168 | 0.10 | 40 | 2.4 |
| 7 | 豹式坦克 | 75 | 3.0 | 180 | 0.10 | 100 | 6.0 |
| 8 | 虎式坦克 | 65 | 2.6 | 156 | 0.10 | 110 | 6.6 |

**激光速度：** 6.6 × 10 = 66 px/frame，近似瞬间命中。

以上换算因子定义为 `TankData` 中的常量，方便后续调整手感：

```java
public class TankData {
    // 速度换算常量（60 FPS 基准）
    public static final double TANK_SPEED_FACTOR = 0.04;    // 坦克移动
    public static final double TURN_SPEED_FACTOR = 0.01;    // 坦克转向 (rad)
    public static final double BULLET_SPEED_FACTOR = 0.06;  // 子弹飞行
    public static final double LASER_SPEED_FACTOR = 0.6;    // 激光（子弹因子的10倍）
    // ...
}
```

移动逻辑中的调用方式：

```java
// Players 中存储的是设计值，运行时换算
int actualPixels = (int)(tankData.getBaseSpeed() * TankData.TANK_SPEED_FACTOR);
int newX = x + (int)(actualPixels * Math.sin(rad));
int newY = y - (int)(actualPixels * Math.cos(rad));
```

### 4.11 Modification（改装装备）

```java
public class Modification {
    public enum Type {
        ANTI_FRIENDLY_FIRE(5),   // 反斜钢甲（稀有度5）
        INSTANT_TURN(6),         // 扭绞轮台（稀有度6）
        EXPLOSION_PROOF(7),      // 防爆油箱（稀有度7）
        LIGHT_MACHINE_GUN(3),    // 轻机枪（稀有度3）
        HEAVY_MACHINE_GUN(4),    // 重机枪（稀有度4）
        LASER_CANNON(6),         // 激光炮（稀有度6）
        DENSE_LASER(7),          // 高密度镭射群炮（稀有度7）
        AUTO_LOADER(7),          // 自动装填器（稀有度7）
        EXTRA_AMMO(4);           // 额外弹药架（稀有度4）

        private final int rarity;
    }

    private Type type;
    private boolean isUniversal;  // 是否为通用碎片合成

    public String getDescription(); // 获取装备效果描述
    public int getRarity();         // 获取稀有度
}
```

### 4.12 GachaManager（抽卡管理器）

```java
public class GachaManager {
    private int tankPityCounter;         // 坦克池累计抽数
    private int modPityCounter;          // 改装池累计抽数
    private Map<Integer, Boolean> ownedTanks; // 已拥有的坦克

    // 坦克获取池
    public GachaResult drawTank(int times);    // 抽坦克（1次或10连）

    // 改装装备获取池
    public GachaResult drawModification(int times); // 抽改装碎片

    // 保底计算
    private int calculatePityBonus(int counter); // 根据累计抽数返回权重加成
}
```

**保底机制算法**：

```
当累计抽数 < 50: 权重不变
当 50 ≤ 累计抽数 < 70: 每多一抽，目标大奖权重 +5
当 70 ≤ 累计抽数 < 90: 每多一抽，目标大奖权重 +10
当累计抽数 = 90: 必出目标大奖
抽出目标大奖后: 累计抽数清零
```

### 4.13 SaveManager（存档管理器，单例）

位于 `model.manager.SaveManager`，负责整个存档生命周期的管理——创建、加载、保存、删除存档文件。使用 Gson 进行 JSON 序列化与反序列化。

```java
public class SaveManager {
    private static SaveManager instance;
    private static final String SAVE_VERSION = "1.0";
    private static final String DEFAULT_SAVE_DIR = "save/";
    private static final String DEFAULT_SAVE_FILE = "save_data.json";

    private Gson gson;
    private File currentSaveFile;          // 当前会话使用的存档文件

    private SaveManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static SaveManager getInstance() { /* DCL 单例 */ }

    // === 读档 ===
    public PlayerSaveData load();                   // 从默认路径加载
    public PlayerSaveData load(File file);          // 从指定路径加载

    // === 存档 ===
    public boolean save(PlayerSaveData data);       // 保存到当前存档文件
    public boolean saveAs(PlayerSaveData data, File file); // 另存为指定路径

    // === 管理 ===
    public List<File> listSaveFiles();              // 扫描 save/ 目录下列出所有 .json 存档
    public boolean hasExistingSave();               // 检查默认存档文件是否存在
    public boolean deleteSave(File file);           // 删除指定存档文件

    // === 内部方法 ===
    private String serialize(PlayerSaveData data);  // 序列化为 JSON 字符串
    private PlayerSaveData deserialize(String json); // 反序列化
}
```

**存档文件格式**：详见项目根目录 `data.md`。存档为 UTF-8 编码的 JSON 文件，根对象为 `PlayerSaveData`，包含元信息、资源、拥有的坦克列表、改装库存、抽卡状态、对战历史等子对象。

### 4.14 PlayerSaveData（存档根对象）

```java
public class PlayerSaveData {
    private SaveMeta meta;                           // 存档元信息
    private Resources resources;                     // 玩家资源（生铁、钢铁、蓝图）
    private List<OwnedTank> ownedTanks;              // 拥有的坦克及其完整状态
    private ModificationInventory modificationInv;  // 改装装备碎片与成品库存
    private GachaState gachaState;                   // 双池保底计数
    private BattleHistory battleHistory;             // 对战历史统计
    private GameSettings settings;                   // 全局游戏设置

    // 工厂方法：创建新档
    public static PlayerSaveData createNew(String playerName) {
        PlayerSaveData data = new PlayerSaveData();
        data.meta = new SaveMeta(playerName);
        data.resources = new Resources();            // 全零初始资源
        data.ownedTanks = new ArrayList<>();
        // 新玩家默认拥有两辆初始坦克（克伦威尔 + M24霞飞）
        data.ownedTanks.add(OwnedTank.createNew(1)); // 坦克ID=1 克伦威尔
        data.ownedTanks.add(OwnedTank.createNew(2)); // 坦克ID=2 M24霞飞
        data.modificationInv = new ModificationInventory();
        data.gachaState = new GachaState();
        data.battleHistory = new BattleHistory();
        data.settings = new GameSettings();
        return data;
    }
}
```

**存档生命周期**：

```
程序启动 → 显示登录界面
  → 用户点击"登录"
    → SaveManager.listSaveFiles() 扫描 save/ 目录
    → 展示可用存档列表
      → 选择已有存档 → SaveManager.load(file) 加载到内存
      → 创建新档 → 输入玩家名 → PlayerSaveData.createNew(name)
  → 进入主菜单（数据存在于内存中）

游戏运行中（对战结算 / 养成操作 / 抽卡后）
  → 更新内存中的 PlayerSaveData 对象
  → SaveManager.save(data) 自动保存到当前存档文件

用户点击"退出游戏"
  → 弹出对话框："是否保存存档？"
    → [保存] → 选择保存位置（默认游戏目录）→ SaveManager.saveAs()
    → [不保存] → 放弃本次会话数据变更，直接退出
```

### 4.15 GameThread（游戏主逻辑线程）

```java
public class GameThread extends Thread {
    private boolean running;
    private boolean paused;
    private long lastFrameTime;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_DURATION = 1000 / TARGET_FPS; // ~16ms

    @Override
    public void run() {
        while (running) {
            if (!paused) {
                long startTime = System.currentTimeMillis();

                // 1. 更新所有元素
                updateAllElements();

                // 2. 碰撞检测
                checkCollisions();

                // 3. 清理已销毁的元素
                cleanupDestroyed();

                // 4. 检查胜负条件
                checkWinCondition();

                // 帧率控制
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed < FRAME_DURATION) {
                    Thread.sleep(FRAME_DURATION - elapsed);
                }
            }
        }
    }

    private void updateAllElements() {
        Map<String, List<SuperElement>> all = ElementManager.getInstance().getAllElements();
        for (List<SuperElement> list : all.values()) {
            for (SuperElement e : list) {
                e.update();
            }
        }
    }

    private void checkCollisions() {
        // 子弹 vs 墙壁
        // 子弹 vs 坦克（Players / Boss）
        // 坦克 vs 墙壁（移动碰撞）
        CollisionUtil.checkBulletWallCollisions();
        CollisionUtil.checkBulletTankCollisions();
        CollisionUtil.checkTankWallCollisions();
    }
}
```

### 4.16 CollisionUtil（碰撞检测工具类）

```java
public class CollisionUtil {
    // 子弹与墙壁碰撞
    public static void checkBulletWallCollisions();

    // 子弹与坦克碰撞
    // - 检查友伤规则（反斜钢甲）
    // - 计算伤害并应用到坦克
    // - 触发子弹销毁 / 穿透 / 反弹
    public static void checkBulletTankCollisions();

    // 坦克与墙壁碰撞（移动阻挡）
    // - 坦克下一步位置若与墙壁重叠则阻止移动
    public static void checkTankWallCollisions();

    // 两矩形碰撞判定
    public static boolean intersects(SuperElement a, SuperElement b);

    // 子弹插值碰撞检测（高速子弹防穿透）
    public static boolean sweptCollision(Bullet bullet, SuperElement target);
}
```

### 4.17 视图层类

```java
// MyJFrame: 主窗体，管理界面切换
public class MyJFrame extends JFrame {
    private CardLayout cardLayout;     // 卡片布局，用于界面切换
    private JPanel mainPanel;

    public void showLoginPanel();
    public void showMainMenu();
    public void showTankSelect();
    public void showGamePanel(int mapId, Players p1, Players p2);
    public void showDevelopPanel();
    public void showGachaPanel();
}

// MyGameJPanel: 游戏画板，包含渲染线程
public class MyGameJPanel extends JPanel {
    private Timer renderTimer;  // Swing Timer 驱动渲染（60 FPS）

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. 绘制背景
        // 2. 按 z-order 排序元素
        // 3. 遍历所有可见元素，调用 show(g2d)
        // 4. 绘制 UI 叠加层（血量、弹药、倒计时等 HUD）

        Toolkit.getDefaultToolkit().sync(); // 保证画面同步
    }
}
```

### 4.18 键盘监听器

```java
public class GameKeyListener implements KeyListener {
    private Set<Integer> pressedKeys;  // 当前按下的键集合（支持多键同时按下）

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:     // P1 前进
            case KeyEvent.VK_S:     // P1 后退
            case KeyEvent.VK_A:     // P1 逆时针旋转
            case KeyEvent.VK_D:     // P1 顺时针旋转
            case KeyEvent.VK_SPACE: // P1 开火
            case KeyEvent.VK_R:     // P1 换弹
            case KeyEvent.VK_UP:    // P2 前进
            case KeyEvent.VK_DOWN:  // P2 后退
            case KeyEvent.VK_LEFT:  // P2 逆时针旋转
            case KeyEvent.VK_RIGHT:// P2 顺时针旋转
            case KeyEvent.VK_ESCAPE:// 暂停菜单
                // 修改对应 Players 对象的状态
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        // 对应的移动/旋转状态置为 false
    }
}
```

---

## 五、系统核心实现

### 5.1 启动与初始化流程

```java
// Main.java
public class Main {
    public static void main(String[] args) {
        // 1. 初始化资源（地图、坦克属性模板等——不依赖存档）
        ElementLoad.getInstance().init();

        // 2. 初始化空的管理器
        ElementManager.getInstance();

        // 3. 初始化存档管理器
        SaveManager.getInstance();

        // 4. 启动主窗体（显示登录界面）
        SwingUtilities.invokeLater(() -> {
            MyJFrame frame = new MyJFrame();
            frame.showLoginPanel();
            frame.setVisible(true);
        });
    }
}
```

**登录流程（含存档选择）**：

```java
// LoginPanel 中"登录"按钮的逻辑
public void onLoginClick() {
    List<File> saveFiles = SaveManager.getInstance().listSaveFiles();

    if (saveFiles.isEmpty()) {
        // 无已有存档 → 直接创建新档
        String playerName = getPlayerNameInput();
        PlayerSaveData newData = PlayerSaveData.createNew(playerName);
        GameContext.setCurrentSave(newData);
        switchToMainMenu();
    } else {
        // 有存档 → 展示存档列表，用户选择加载或创建新档
        showSaveSelectionDialog(saveFiles, (selectedFile) -> {
            if (selectedFile != null) {
                // 加载已有存档
                PlayerSaveData data = SaveManager.getInstance().load(selectedFile);
                GameContext.setCurrentSave(data);
            } else {
                // 用户选择创建新档
                String name = getPlayerNameInput();
                GameContext.setCurrentSave(PlayerSaveData.createNew(name));
            }
            switchToMainMenu();
        });
    }
}
```

### 5.2 对战初始化流程

```java
public void startBattle(int mapId, Players player1, Players player2, BattleConfig config) {
    // 1. 清空上一局残留
    ElementManager.getInstance().clearAll();

    // 2. 加载地图，创建墙壁元素
    MapData mapData = ElementLoad.getInstance().loadMap(mapId);
    for (Point p : mapData.getBrickPositions()) {
        ElementManager.getInstance().addElement("brick", ElementFactory.getInstance().createBrick(p.x, p.y));
    }
    for (Point p : mapData.getIronPositions()) {
        ElementManager.getInstance().addElement("iron", ElementFactory.getInstance().createIron(p.x, p.y));
    }

    // 3. 添加玩家坦克（设置出生位置）
    player1.setPosition(spawnPoint1.x, spawnPoint1.y);
    player2.setPosition(spawnPoint2.x, spawnPoint2.y);
    ElementManager.getInstance().addElement("players", player1);
    // player2 可能是 Players（双人对战）或 Boss（人机对战）
    ElementManager.getInstance().addElement(
        player2 instanceof Boss ? "boss" : "players", player2);

    // 4. 启动游戏线程
    GameThread gameThread = new GameThread(config);
    gameThread.start();

    // 5. 切换到游戏画板
    MyGameJPanel gamePanel = new MyGameJPanel();
    gamePanel.startRenderThread();
    frame.switchTo(gamePanel);

    // 6. 绑定键盘监听
    gamePanel.addKeyListener(new GameKeyListener(player1, player2));
    gamePanel.requestFocus();
}
```

### 5.3 游戏主循环的碰撞检测实现

游戏主循环每帧执行以下碰撞检测，顺序保证：

```
（1）坦克与墙壁碰撞检测（先于移动确认）
    遍历 players、boss 列表中的每个坦克 → 根据其速度与朝向计算下一步位置
    → 检查新位置是否与 brick、iron 列表中的墙壁重叠
    → 若重叠则阻止该方向移动（仅更新朝向，不更新位置）

（2）子弹与墙壁碰撞检测
    遍历 bullet 列表 → 根据子弹下一步位置
    → 若碰撞 iron（钢铁墙）：子弹反弹（方向取反）
    → 若碰撞 brick（砖墙）：子弹调用 destroy()，墙壁调用 destroy()
    → 若为激光且碰 iron 且反弹次数 < 5：反弹并将该位置的碰撞记录加入激光路径
    → 若为激光且碰 brick：穿透（砖墙 destroy()，激光继续）

（3）子弹与坦克碰撞检测
    遍历 bullet 列表 × (players + boss) 列表
    → 跳过子弹发射者自身（除非改装了反斜钢甲且为友方）
    → 若碰撞：计算伤害 → 坦克 takeDamage() → 子弹 destroy()
    → 若为激光：检查该坦克坐标是否在激光线段路径上
```

### 5.4 坦克移动机制

坦克沿其朝向方向进行前后移动，具体实现如下：

```java
// Players.moveForward()
public void moveForward() {
    double rad = Math.toRadians(direction);
    int newX = x + (int)(speed * Math.sin(rad));  // x 分量
    int newY = y - (int)(speed * Math.cos(rad));  // y 分量（屏幕坐标系y轴向下）

    // 碰撞预检测：检查新位置是否与墙壁重叠
    if (!CollisionUtil.wouldCollideWithWall(newX, newY, width, height, this)) {
        x = newX;
        y = newY;
    }
}
```

坦克仅能沿朝向前后移动（不可横向平移），旋转通过独立的 `rotateCW()` / `rotateCCW()` 方法控制，每次旋转角度 = `turnSpeed` 弧度。

### 5.5 子弹发射机制

```java
// Players.fire()
public void fire() {
    // 1. 检查是否可以开火
    long now = System.currentTimeMillis();
    if (ammo <= 0 || isReloading) return;
    if (now - lastFireTime < fireRate) return;  // 射速冷却

    // 2. 根据改装装备决定子弹类型与数量
    Modification weapon = getWeaponMod();
    if (weapon == null) {
        // 普通子弹：单发
        createNormalBullet(direction);
    } else {
        switch (weapon.getType()) {
            case LIGHT_MACHINE_GUN:
                createSpreadBullets(3, 0.4f, false, 0);  // 3发散射，40%伤害，不反弹
                break;
            case HEAVY_MACHINE_GUN:
                createSpreadBullets(5, 0.3f, false, 0);  // 5发散射，30%伤害，不反弹
                break;
            case LASER_CANNON:
                createLaser(false, 0);  // 激光，不反弹
                break;
            case DENSE_LASER:
                createLaser(true, 5);   // 激光，反弹5次
                break;
        }
    }

    // 3. 更新状态
    ammo--;
    lastFireTime = now;
    shotsFired++;

    // 4. 若弹匣空，触发自动换弹（除非有自动装填器）
    if (ammo <= 0 && !hasMod(Modification.Type.AUTO_LOADER)) {
        reload();
    }
}
```

**散射子弹的扇形生成**：

```java
private void createSpreadBullets(int count, float damageMult, boolean rebound, int maxRebounds) {
    int spreadAngle = 15; // 相邻子弹夹角 15°
    int totalSpread = (count - 1) * spreadAngle;
    int startAngle = direction - totalSpread / 2;

    for (int i = 0; i < count; i++) {
        int bulletDir = startAngle + i * spreadAngle;
        int damage = (int)(attack * damageMult);
        Bullet b = ElementFactory.getInstance().createBullet(
            x + width/2, y + height/2, bulletDir, this,
            false, rebound, maxRebounds, 1.0f, 1, 0);
        b.setDamage(damage);
        ElementManager.getInstance().addElement("bullet", b);
    }
}
```

### 5.6 改装装备效果实现

每个改装装备的效果需要在对应的系统节点切入，而非在单一位置集中处理：

| 改装装备 | 切入节点 | 实现方式 |
|----------|----------|----------|
| 反斜钢甲 | 碰撞检测（子弹vs坦克） | `checkBulletTankCollisions()` 中，若子弹发射者与受击者为友方，跳过伤害计算 |
| 扭绞轮台 | 坦克旋转 | `rotateCW()` / `rotateCCW()` 中，直接设置 `direction` 为目标角度（跳过逐帧过渡） |
| 防爆油箱 | 伤害计算 | `takeDamage()` 中，若 `hp - actualDamage <= 0`，改为 `hp = 1`，并设置 5 秒无敌状态 |
| 轻/重机枪 | 子弹发射 | `fire()` 中替换为散射子弹生成逻辑 |
| 激光炮/镭射群炮 | 子弹发射 + 碰撞检测 | 子弹类型标记为 `isLaser`，碰撞时按线段求交判定 |
| 自动装填器 | 换弹 | `reload()` 中，`reloadTime = 0`（瞬间完成） |
| 额外弹药架 | 弹匣容量 | `maxAmmo = baseAmmo * 2` |

### 5.7 界面渲染（双缓冲 + 分层绘制）

```java
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    // 抗锯齿
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // 分层绘制（z-order: 背景 → 墙壁 → 坦克 → 子弹 → 特效 → HUD）
    drawElements(g2d, "background");
    drawElements(g2d, "iron");
    drawElements(g2d, "brick");
    drawElements(g2d, "players");
    drawElements(g2d, "boss");
    drawElements(g2d, "bullet");
    drawElements(g2d, "explosion");
    drawHUD(g2d);  // 血量条、弹药显示、倒计时等
}

private void drawElements(Graphics2D g2d, String type) {
    for (SuperElement e : ElementManager.getInstance().getElements(type)) {
        if (e.isVisible()) {
            e.show(g2d);
        }
    }
}
```

### 5.8 HUD 绘制

对战界面叠加层绘制以下信息：

- 双方血量条（屏幕左右两侧）
- 当前弹匣 / 最大弹匣
- 换弹进度条
- 对战倒计时
- 改装装备图标（坦克上方小图标）
- 耐久值指示器

### 5.9 坦克养成系统实现

#### 升级逻辑

```java
public boolean upgrade() {
    if (level >= 10) return false;  // 已达当前阶最大等级
    int cost = rank * 100 + level * 10;  // 粗铁消耗
    if (playerIron < cost) return false;

    playerIron -= cost;
    level++;
    // 全属性提升（升级属性值）
    applyUpgradeBonus();
    return true;
}
```

#### 进阶逻辑

```java
public boolean evolve() {
    if (level != 10 || rank >= 10) return false;  // 必须满级且未满阶
    int cost = (rank + 1) * 10;  // 钢铁消耗
    if (playerSteel < cost) return false;

    playerSteel -= cost;
    rank++;
    level = 1;  // 等级重置
    // 全属性提升（进阶属性值）
    applyEvolveBonus();
    // 检查是否解锁新改装槽
    checkUnlockModSlot();
    return true;
}
```

#### 改装槽解锁规则

```
rank < 3:   unlockedSlots = 0
rank = 3-4: unlockedSlots = 1（新解锁1槽）
rank = 5-6: unlockedSlots = 2（再解锁1槽）
rank = 7-9: unlockedSlots = 3（再解锁1槽）
rank = 10:  unlockedSlots = 5（再解锁2槽）
```

### 5.10 抽卡系统实现

```java
public GachaResult drawTank(int times) {
    List<Reward> rewards = new ArrayList<>();
    for (int i = 0; i < times; i++) {
        tankPityCounter++;
        int pityBonus = calculatePityBonus(tankPityCounter);
        Reward reward = rollTankPool(pityBonus);
        rewards.add(reward);
        if (reward.isNewTank()) {
            tankPityCounter = 0;  // 抽出大奖，清零保底
        }
    }
    return new GachaResult(rewards);
}

private Reward rollTankPool(int pityBonus) {
    // 权重列表（基础权重 + 保底加成）
    int[] weights = {
        200,                // 生铁*10
        100,                // 生铁*20
        50,                 // 生铁*100
        20,                 // 生铁*200
        15,                 // 钢铁*100
        10,                 // 钢铁*200
        5,                  // 钢铁*500
        5 + pityBonus       // 新坦克（目标大奖，享受保底加成）
    };
    // 若 pityCounter >= 90 且此前未中奖：直接返回新坦克
    if (tankPityCounter >= 90) {
        return drawNewTank();
    }
    return weightedRandom(weights);
}
```

**GachaResult / Reward 结构**：每个 `Reward` 必须携带 `isJackpot` 标记（坦克池 = 新坦克；改装池 = 四种碎片），供翻牌展示层决定是否叠加金光特效：

```java
public class Reward {
    private RewardType type;     // 生铁 / 钢铁 / 新坦克 / 碎片
    private int amount;
    private boolean isJackpot;   // 是否目标大奖（新坦克 / 碎片）
    private Image cardFace;      // 对应结果卡正面图
}
```

**注意**：奖励在 `drawTank()` / `drawModification()` 返回前已全部结算并写入 `PlayerSaveData` 且自动保存，翻牌仅是**纯展示层交互**——中途关闭程序不会丢失奖励。

### 5.10.1 抽卡翻牌展示流程（GachaRevealPanel）

点击单抽/十连并结算成功后，不直接弹出结果列表，而是切入全屏翻牌展示层：

```
抽卡按钮 → GachaManager 结算+存档 → GachaRevealPanel(rewards)
  1. 展示 N 张牌背（单抽 1 张居中；十连 2 行 × 5 列居中）
  2. 玩家逐张点击 → 该卡播放翻转动画 → 显示结果卡正面
  3. 若该卡 isJackpot → 正面亮出瞬间叠加金光特效（旋转 + 呼吸闪烁）
  4. 提供"一键翻开"按钮：按间隔依次翻开所有未翻卡
  5. 全部翻开后显示"确认"按钮 → 返回 GachaPanel 并刷新资源栏/保底进度条
```

**翻转动画（Swing 实现）**：用 `javax.swing.Timer`（约 16ms/帧）驱动卡片横向缩放模拟 3D 翻转，总时长约 300ms：

```java
// progress ∈ [0,1]；前半段画牌背收窄，后半段画正面展开
double scaleX = Math.abs(Math.cos(progress * Math.PI)); // 1 → 0 → 1
Image img = (progress < 0.5) ? cardBack : reward.getCardFace();
int w = (int) (CARD_W * scaleX);
g2d.drawImage(img, cx - w / 2, cy - CARD_H / 2, w, CARD_H, null);
```

**大奖金光特效**：卡片翻开完成后，在该卡背后持续绘制 `gacha_light_2.png`（金光），由同一 Timer 驱动：

```java
// 每帧更新：angle += 0.02（缓慢旋转），alpha 在 0.6~1.0 间正弦呼吸
Graphics2D g2 = (Graphics2D) g.create();
g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
g2.rotate(angle, cx, cy);
g2.drawImage(goldenLight, cx - 200, cy - 200, 400, 400, null); // 先画光，再画卡
g2.dispose();
```

**点击命中判定**：`GachaRevealPanel` 注册 `MouseListener`，用每张卡的 `Rectangle` 做 `contains(e.getPoint())` 判定，已翻开的卡忽略点击。

### 5.11 暂停系统

按下 ESC 键时触发：
1. `GameThread.pause()`：暂停逻辑循环（`paused = true`）
2. `MyGameJPanel` 停止渲染更新（HUD 保持在最后一帧）
3. 显示 `PausePanel`（三个按钮：继续/重新开始/结束对战）

```java
// GameKeyListener 中 ESC 处理
case KeyEvent.VK_ESCAPE:
    gameThread.pause();
    PausePanel panel = new PausePanel(
        () -> gameThread.resume(),        // 继续
        () -> restartBattle(),            // 重新开始
        () -> endBattle(false)            // 结束对战（判负）
    );
    showOverlay(panel);
    break;
```

### 5.12 对战结算

对战结束条件（任意一条满足）：
1. 一方 HP ≤ 0（被击杀）
2. 倒计时结束（以 HP 多的一方获胜；HP 相同判玩家失败）
3. 玩家主动退出（判负）

结算展示内容：
- 胜负结果
- 双方数据统计对比：开火数、造成伤害、受到伤害
- 获得的资源（人机对战）：生铁、钢铁、蓝图
- 返回主菜单按钮
- **结算完成后自动保存**：资源奖励和统计更新写入 `PlayerSaveData`，调用 `SaveManager.save()` 自动持久化

**人机对战资源奖励**：

| 难度 | 胜利生铁 | 胜利钢铁 | 蓝图（无论输赢） |
|------|----------|----------|-----------------|
| 简单 | 200 | 30 | 1 |
| 困难 | 400 | 60 | 2 |
| 超级 | 500 | 100 | 3 |

### 5.13 对战设置系统（仅双人对战）

```java
public class BattleConfig {
    private int matchDuration = 300;     // 默认 5 分钟（秒）
    private boolean overtimeEnabled = false;
    private boolean damagePenaltyEnabled = false;
    private boolean cheatEnabled = false;
    private int overtimeDuration = 300;  // 加时赛 5 分钟

    // 作弊选项（仅 cheatEnabled=true 时生效）
    private Map<Integer, Integer> p1Overrides;  // P1 属性覆盖
    private Map<Integer, Integer> p2Overrides;  // P2 属性覆盖
    private boolean friendlyFireEnabled = true;
    private boolean p1Invincible = false;
    private boolean p2Invincible = false;
    private boolean p1WallPass = false;
    private boolean p2WallPass = false;
    private boolean durabilityDisabled = false;
    private boolean bulletReboundEnabled = true;
}
```

**约束规则**：
- 人机对战**不允许**进行对战设置
- 加时赛与超时惩罚**不可同时设置**
- 默认设置：5 分钟，无加时赛，无超时惩罚，无作弊

### 5.14 退出游戏与存档流程

用户点击退出游戏时的处理逻辑：

```java
// 退出按钮逻辑
public void onExitClick() {
    PlayerSaveData current = GameContext.getCurrentSave();

    // 保存选择对话框
    int choice = JOptionPane.showConfirmDialog(
        frame,
        "是否保存当前存档？",
        "退出游戏",
        JOptionPane.YES_NO_CANCEL_OPTION
    );

    switch (choice) {
        case JOptionPane.YES_OPTION:
            // 用户选择存档 → 打开文件选择器（默认路径为游戏目录）
            JFileChooser fileChooser = new JFileChooser(SaveManager.DEFAULT_SAVE_DIR);
            fileChooser.setSelectedFile(new File("save_data.json"));
            if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File targetFile = fileChooser.getSelectedFile();
                SaveManager.getInstance().saveAs(current, targetFile);
            }
            System.exit(0);
            break;

        case JOptionPane.NO_OPTION:
            // 用户选择不存档 → 直接退出
            System.exit(0);
            break;

        case JOptionPane.CANCEL_OPTION:
            // 用户取消 → 返回游戏
            break;
    }
}
```

**存档触发时机汇总**：

| 时机 | 行为 |
|------|------|
| 对战结算后 | **自动保存**到当前存档文件 |
| 养成操作后（升级/进阶/改装安装） | **自动保存**到当前存档文件 |
| 抽卡后 | **自动保存**到当前存档文件 |
| 用户点击"退出游戏" | 弹出对话框，用户手动选择是否存档及存档位置 |
| 用户主动选择"另存为" | 可选择新路径保存副本 |

---

## 六、运行与功能验证

### 6.1 运行环境

| 要求 | 说明 |
|------|------|
| 操作系统 | Windows 10+ / macOS 12+ / Linux（需图形环境） |
| Java 版本 | JRE 8 及以上 |
| 依赖库 | `commons-collections-4.4.jar`（放置于 `lib/` 目录） |
| 资源文件 | `resource/` 目录需与 `src/` 同级，程序通过相对路径读取 |

**启动命令**：

```bash
# 编译
javac -cp "lib/commons-collections-4.4.jar:lib/gson-2.10.1.jar" -d out/ \
  src/main/*.java src/frame/*.java src/model/**/*.java src/thread/**/*.java src/util/*.java

# 运行
java -cp "out:lib/commons-collections-4.4.jar:lib/gson-2.10.1.jar:resource/" main.Main
```

### 6.2 核心功能测试清单

| 测试项 | 验证内容 | 预期结果 |
|--------|----------|----------|
| 程序启动 | `Main.main()` 能否正常启动窗体 | 显示登录界面，无异常报错 |
| 资源加载 | 10 张地图和所有图片能否正确加载 | `ElementLoad.init()` 无异常，地图渲染正确 |
| 坦克移动 | WASD 键能否操控 P1 前后移动与旋转 | 坦克沿朝向移动，旋转角度正确 |
| 墙壁碰撞 | 坦克移向墙壁是否被阻挡 | 坦克停在墙壁前，不发生穿透 |
| 子弹发射 | 空格键发射子弹，R 键换弹 | 子弹沿朝向飞行，弹匣递减，换弹后恢复 |
| 子弹-墙壁碰撞 | 子弹击中砖墙/钢铁墙 | 砖墙被破坏 + 子弹消失 / 钢铁墙反弹子弹 |
| 子弹-坦克碰撞 | 子弹击中敌方坦克 | 坦克扣血、耐久降低、伤害按减免计算 |
| AI 行为 | 人机对战中 AI 坦克能否自主移动和射击 | AI 追踪玩家并射击（难度越高越精准） |
| 对战结束 | 一方 HP=0 或倒计时结束 | 显示结算界面，数据统计正确 |
| 暂停系统 | ESC 键暂停/继续/重新开始/结束 | 各按钮功能正确 |
| 坦克升级 | 养成界面执行升级操作 | 消耗粗铁，等级+1，属性提升 |
| 坦克进阶 | 10级满级后执行进阶 | 消耗钢铁，阶数+1，等级重置为1 |
| 改装装备 | 安装/拆除改装装备 | 装备效果生效，拆除后冷却计数开始 |
| 抽卡系统 | 蓝图抽坦克/改装碎片 | 概率符合配置，保底机制正确触发 |
| 双人对战 | P1(WASD+空格) vs P2(方向键+鼠标) | 双方独立控制，对战设置生效 |
| 存档-创建新档 | 无存档时点击登录 | 提示输入玩家名，创建新档，获得两辆初始坦克 |
| 存档-加载 | 有存档时点击登录，选择已有存档 | 正确加载所有资源、坦克、养成进度、抽卡计数 |
| 存档-自动保存 | 完成对战/养成/抽卡操作后 | 关闭程序重新启动，数据与操作前一致 |
| 存档-退出保存 | 点击退出游戏，选择"保存"并指定路径 | JSON 文件生成在指定路径，内容完整 |
| 存档-退出不保存 | 点击退出游戏，选择"不保存" | 程序退出，本次会话变更不保留 |

### 6.3 边界条件测试

| 测试场景 | 预期行为 |
|----------|----------|
| 弹匣打空后继续按空格 | 不发射，等待换弹完成 |
| 换弹中再次按 R | 忽略（已在换弹） |
| 改装槽冷却中尝试安装 | 拒绝，提示剩余冷却对局数 |
| 等级已达 10 级时升级 | 拒绝，提示需要进阶 |
| 粗铁/钢铁不足时升级/进阶 | 拒绝，提示资源不足 |
| 蓝图不足 3 张时抽卡 | 拒绝（单抽需 3 张）/ 可十连（27张） |
| 坦克 HP 降为负数 | 锁定为 0，坦克销毁 |
| 倒计时结束双方 HP 相同 | 判玩家失败 |
| 存档文件被外部删除 | 登录时重新创建新档，不崩溃 |
| 存档 JSON 格式损坏 | 提示存档损坏，引导用户选择其他存档或创建新档 |

---

## 七、总结与反思

### 7.1 设计亮点

1. **严格的 MVC 分层**：Model、View、Control 三层边界清晰，数据流向单向可控。View 层只读数据，Control 层只写数据，Model 层纯粹存储与封装——这种设计使得各层可独立开发、独立测试、独立修改，新增功能时不会牵一发而动全身。

2. **面向对象继承体系**：所有游戏元素统一继承 `SuperElement` 抽象基类，在多态的支持下，View 层的渲染逻辑只需遍历基类类型列表即可完成所有元素的绘制；Control 层的更新循环同样适用。新增元素类型时无需修改渲染与更新代码，符合开闭原则。

3. **双线程分离**：游戏逻辑线程与界面渲染线程通过 Model 层解耦，避免了逻辑计算密集时画面掉帧，也避免了界面重绘阻塞逻辑更新。`CopyOnWriteArrayList` 保证了线程安全且读操作零锁开销。

4. **数据与代码分离**：坦克属性、地图数据均存储在外部文件（`.map` 配置文件、`TankData` 模板）中，修改游戏数值或新增坦克/地图无需改动 Java 代码，降低了策划调整与开发改动的耦合。

5. **策略模式驱动 AI**：三级难度的 AI 通过 `AIController` 接口封装为独立策略类，AI 行为的调整、替换或新增难度均不影响 `Boss` 类的核心逻辑。

### 7.2 存在的不足与解决方案

#### 困难一：碰撞检测性能 O(n²) 瓶颈

**问题描述**：当前采用 O(n²) 暴力遍历（每帧对子弹×坦克×墙壁做全量两两比较）。在地图复杂、子弹数量多时，碰撞检测可能成为帧率瓶颈。

**解决方案——均匀网格空间分区（Uniform Grid Spatial Partitioning）**：

选择均匀网格而非四叉树，理由如下：
- 本项目实体尺寸相近（坦克 40×40、子弹 8×8、墙壁 20×20），均匀网格最优
- 插入 O(1)，查询 O(cells × entities_per_cell)，比四叉树的 O(log n) 更快
- 实现简单，调试方便，适合本项目规模

**工作原理**：将地图划分为固定大小的格子（建议 cell = 40px，与坦克尺寸对齐），每个格子存储与其重叠的元素的引用。碰撞检测时，只检查目标元素所在格子及其相邻格子的元素。

```java
public class SpatialGrid {
    private int cellSize;                          // 格子大小（像素）
    private int cols, rows;                        // 网格行列数
    private List<SuperElement>[][] cells;          // 二维格子数组

    public SpatialGrid(int mapWidth, int mapHeight, int cellSize) {
        this.cellSize = cellSize;
        this.cols = (mapWidth + cellSize - 1) / cellSize;
        this.rows = (mapHeight + cellSize - 1) / cellSize;
        this.cells = new List[cols][rows];
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++)
                cells[i][j] = new ArrayList<>();
    }

    // 清空网格（每帧开始前调用）
    public void clear() {
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++)
                cells[i][j].clear();
    }

    // 将元素插入其覆盖的所有格子
    public void insert(SuperElement e) {
        int minCol = Math.max(0, e.getX() / cellSize);
        int minRow = Math.max(0, e.getY() / cellSize);
        int maxCol = Math.min(cols - 1, (e.getX() + e.getWidth()) / cellSize);
        int maxRow = Math.min(rows - 1, (e.getY() + e.getHeight()) / cellSize);

        for (int col = minCol; col <= maxCol; col++)
            for (int row = minRow; row <= maxRow; row++)
                cells[col][row].add(e);
    }

    // 获取可能与指定元素碰撞的候选列表
    public List<SuperElement> getCandidates(SuperElement e) {
        Set<SuperElement> candidates = new HashSet<>();
        int minCol = Math.max(0, e.getX() / cellSize);
        int minRow = Math.max(0, e.getY() / cellSize);
        int maxCol = Math.min(cols - 1, (e.getX() + e.getWidth()) / cellSize);
        int maxRow = Math.min(rows - 1, (e.getY() + e.getHeight()) / cellSize);

        for (int col = minCol; col <= maxCol; col++)
            for (int row = minRow; row <= maxRow; row++)
                candidates.addAll(cells[col][row]);

        candidates.remove(e);  // 排除自身
        return new ArrayList<>(candidates);
    }
}
```

**改造后的碰撞检测流程（在 GameThread 每帧中调用）**：

```java
public void checkCollisions() {
    SpatialGrid grid = new SpatialGrid(mapW, mapH, 40);
    ElementManager em = ElementManager.getInstance();

    // 1. 将需要参与碰撞检测的元素插入网格
    for (SuperElement e : em.getElements("bullet"))  grid.insert(e);
    for (SuperElement e : em.getElements("players"))  grid.insert(e);
    for (SuperElement e : em.getElements("boss"))     grid.insert(e);
    for (SuperElement e : em.getElements("brick"))    grid.insert(e);
    for (SuperElement e : em.getElements("iron"))     grid.insert(e);

    // 2. 只对同格子/相邻格子的元素做精确碰撞检测
    // 子弹 vs 坦克
    for (SuperElement bullet : em.getElements("bullet")) {
        for (SuperElement candidate : grid.getCandidates(bullet)) {
            if (candidate instanceof Players && bullet.isStrike(candidate)) {
                handleBulletHitTank((Bullet) bullet, (Players) candidate);
            }
        }
    }
    // 子弹 vs 墙壁 ... 同理，只检查网格候选集中的墙壁元素
    // 坦克 vs 墙壁 ... 同理
}
```

**性能效果**：假设 800×600 地图，cell=40，共 20×15=300 个格子，每格平均 2-3 个元素。碰撞检测从 O(n²) 降至约 O(n × k)，其中 k 为每个元素所在格子的平均邻居数（通常 < 10）。当 n=100 时，检查次数从 ~10,000 降至 ~1,000，减少了约 90%。

---

#### 困难二：UI 组件复用不足

**问题描述**：登录界面、养成界面、抽卡界面等各自独立实现，导航栏、按钮样式、背景面板等公共 UI 元素重复编写，维护时改一处需同步多处。

**解决方案——"BasePanel 模板 + 共享组件外置 + 延迟实例化"三层策略**：

**（1）BasePanel 模板方法模式**：所有功能面板继承同一个抽象基类，公共部分（标题栏、返回按钮、背景）由基类统一处理，子类只负责填充内容区域。

```java
public abstract class BasePanel extends JPanel {
    protected MyJFrame frame;  // 主窗体引用（用于界面切换）

    public BasePanel(MyJFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        // === 公共：顶部标题栏（所有面板共享） ===
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // === 公共：返回按钮 ===
        JButton backBtn = new JButton("返回主菜单");
        backBtn.addActionListener(e -> frame.showMainMenu());
        header.add(backBtn);

        // === 子类填充：内容区域 ===
        JComponent content = buildContent();  // 模板方法——子类实现
        add(content, BorderLayout.CENTER);

        // === 子类可选覆盖：底部状态栏 ===
        JComponent footer = buildFooter();
        if (footer != null) add(footer, BorderLayout.SOUTH);
    }

    // 子类必须实现：构建内容区域
    protected abstract JComponent buildContent();

    // 子类可选覆盖：构建底部状态栏（默认无）
    protected JComponent buildFooter() { return null; }

    // 公共工具方法：创建统一样式的按钮
    protected JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        btn.setPreferredSize(new Dimension(200, 40));
        return btn;
    }
}
```

**子类示例——坦克养成面板只需关注自身内容**：

```java
public class DevelopPanel extends BasePanel {
    private int currentTankIndex = 0;

    public DevelopPanel(MyJFrame frame) {
        super(frame);  // 基类自动构建标题栏、返回按钮
    }

    @Override
    protected JComponent buildContent() {
        JPanel content = new JPanel(new BorderLayout());

        // 左侧：坦克展示（仅此面板特有的布局）
        JPanel tankDisplay = createTankDisplay();
        content.add(tankDisplay, BorderLayout.CENTER);

        // 右侧：详情页（属性/养成/改装，三页签）
        JTabbedPane tabs = createDetailTabs();
        content.add(tabs, BorderLayout.EAST);

        return content;
    }

    // ... 面板特有的私有方法
}
```

**（2）共享组件外置于 CardLayout**：对于跨面板不变的 UI 元素（如侧边导航栏、资源显示栏），不放入 CardLayout 内部，而是在 JFrame 层面放置一次。

```java
public class MyJFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentArea;       // CardLayout 管理的卡片容器
    private JPanel resourceBar;       // 顶部资源栏（所有页面共享，只实例化一次）
    private JPanel navSidebar;        // 侧边导航（可选，主菜单以外页面共享）

    public MyJFrame() {
        setLayout(new BorderLayout());

        // 共享组件——只创建一次，放在 CardLayout 外部
        resourceBar = new ResourceBar();  // 显示生铁/钢铁/蓝图数量
        add(resourceBar, BorderLayout.NORTH);

        // CardLayout 区域——仅切换的页面内容
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        add(contentArea, BorderLayout.CENTER);
    }

    // 供 BasePanel 子类更新资源显示
    public void updateResourceBar(int iron, int steel, int blueprints) {
        resourceBar.refresh(iron, steel, blueprints);
    }
}
```

**（3）延迟实例化（Lazy Initialization）**：面板不在启动时全部创建，而是在首次使用时才初始化，减少启动内存和初始化时间。

```java
private Map<String, BasePanel> panelCache = new HashMap<>();

public void showPanel(String name, Supplier<BasePanel> factory) {
    BasePanel panel = panelCache.computeIfAbsent(name, k -> factory.get());
    cardLayout.show(contentArea, name);
}
```

---

#### 困难三：异常处理不够全面

**问题描述**：资源文件缺失、`.map` 格式错误、存档 JSON 损坏等场景缺乏系统性的容错机制，程序可能因文件问题直接崩溃。

**解决方案——"分级降级 + 校验验证 + 备份兜底"三层防护**：

**（1）资源加载分级降级（ResourceManager 模式）**

```java
public class ResourceManager {
    // 占位资源——所有缺失资源统一显示的替代品
    private static BufferedImage PLACEHOLDER_IMAGE;

    static {
        // 初始化占位图（品红色 40×40 方块，开发时显眼）
        PLACEHOLDER_IMAGE = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = PLACEHOLDER_IMAGE.createGraphics();
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, 40, 40);
        g.setColor(Color.BLACK);
        g.drawLine(0, 0, 40, 40);  // 叉号标识
        g.drawLine(40, 0, 0, 40);
        g.dispose();
    }

    // 加载图片——失败时返回占位图，程序继续运行
    public static BufferedImage loadImage(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) {
                logWarning("图片格式不支持: " + path);
                return PLACEHOLDER_IMAGE;  // 降级 1：不支持的格式
            }
            return img;
        } catch (FileNotFoundException e) {
            logWarning("图片文件不存在: " + path);
            return PLACEHOLDER_IMAGE;  // 降级 2：文件缺失
        } catch (IOException e) {
            logError("图片读取失败: " + path, e);
            return PLACEHOLDER_IMAGE;  // 降级 3：读取异常（损坏/权限）
        }
    }

    // 加载地图——失败时返回空地图（只有边界墙），程序继续运行
    public static MapData loadMap(int level) {
        String path = "resource/data/" + level + ".map";
        try {
            MapData data = MapData.parse(path);

            // 校验：检查解析结果是否合理
            if (data == null) {
                logWarning("地图文件为空: " + path);
                return MapData.createEmpty();  // 降级 1：空文件
            }
            if (data.getBrickPositions().isEmpty() && data.getIronPositions().isEmpty()) {
                logWarning("地图无墙壁数据: " + path);
                return MapData.createEmpty();  // 降级 2：无有效数据
            }
            return data;

        } catch (IOException e) {
            logError("地图文件读取失败: " + path, e);
            return MapData.createEmpty();  // 降级 3：IO 异常
        } catch (NumberFormatException e) {
            logError("地图坐标格式错误: " + path, e);
            return MapData.createEmpty();  // 降级 4：格式异常
        }
    }

    private static void logWarning(String msg) {
        System.err.println("[WARN] " + msg);
        // 生产环境可写入日志文件
    }

    private static void logError(String msg, Exception e) {
        System.err.println("[ERROR] " + msg);
        e.printStackTrace();
    }
}
```

**降级策略汇总**：

| 失败场景 | 降级策略 | 用户感知 |
|----------|----------|----------|
| 图片文件缺失 | 显示品红色占位图（开发期显眼）+ 日志警告 | 界面出现紫色块但程序不崩溃 |
| 图片格式不支持 | 同上 | 同上 |
| .map 文件缺失 | 返回空地图（仅边界墙） | 地图无墙壁，但不影响对战 |
| .map 坐标格式错误（如 "BRICK=abc"） | 跳过错误行，解析剩余有效数据 + 日志警告 | 部分墙壁缺失 |
| 地图数据全空 | 返回空地图 | 同上 |
| 存档 JSON 文件缺失（首次运行） | 走创建新档流程 | 正常，无感知 |
| 存档 JSON 格式损坏（外部编辑导致） | 弹窗提示 + 引导创建新档或选择其他文件 | 有感知，可恢复 |
| 存档版本不匹配（旧版本格式） | 执行版本迁移（`migrate()`）或提示用户 | 尽量自动修复 |

**（2）存档完整性校验**

```java
public PlayerSaveData load(File file) {
    try {
        String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        // 空文件检查
        if (json.trim().isEmpty()) {
            throw new IOException("存档文件为空");
        }

        // JSON 格式合法性校验
        JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            throw new IOException("存档 JSON 格式错误: " + e.getMessage());
        }

        if (!element.isJsonObject()) {
            throw new IOException("存档根元素不是 JSON 对象");
        }

        JsonObject root = element.getAsJsonObject();

        // 必需字段校验
        if (!root.has("meta") || !root.has("resources") || !root.has("ownedTanks")) {
            throw new IOException("存档缺少必需字段（meta/resources/ownedTanks）");
        }

        // 版本检查与迁移
        String version = root.getAsJsonObject("meta").get("version").getAsString();
        if (!SAVE_VERSION.equals(version)) {
            root = migrate(root, version);  // 尝试升级旧版本格式
        }

        return gson.fromJson(root, PlayerSaveData.class);

    } catch (IOException e) {
        // 展示错误对话框，让用户选择下一步操作
        int choice = JOptionPane.showOptionDialog(
            null,
            "存档文件损坏：\n" + file.getName() + "\n\n" + e.getMessage(),
            "存档加载失败",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            new String[]{"选择其他存档", "创建新档"},
            "选择其他存档"
        );
        if (choice == 0) {
            // 重新打开文件选择器
            return promptUserSelectSave();
        } else {
            // 创建新档
            return PlayerSaveData.createNew(
                JOptionPane.showInputDialog("请输入玩家名："));
        }
    }
}
```

**（3）原子写入 + 备份防数据丢失**

存档写入时采用"先写临时文件 → 备份旧文件 → 原子重命名"三步策略，避免写入过程中程序崩溃导致数据全丢：

```java
public boolean save(PlayerSaveData data) {
    File tempFile = new File(currentSaveFile.getParent(), "save_data.tmp");
    File backupFile = new File(currentSaveFile.getParent(), "save_data.bak");

    try {
        // 步骤 1：写入临时文件
        String json = gson.toJson(data);
        Files.write(tempFile.toPath(), json.getBytes(StandardCharsets.UTF_8));

        // 步骤 2：备份现有存档（如果存在）
        if (currentSaveFile.exists()) {
            Files.move(currentSaveFile.toPath(), backupFile.toPath(),
                       StandardCopyOption.REPLACE_EXISTING);
        }

        // 步骤 3：原子重命名（操作系统级保证，不会出现半写文件）
        Files.move(tempFile.toPath(), currentSaveFile.toPath(),
                   StandardCopyOption.ATOMIC_MOVE,
                   StandardCopyOption.REPLACE_EXISTING);

        return true;
    } catch (IOException e) {
        logError("存档写入失败", e);
        // 尝试从备份恢复
        if (backupFile.exists()) {
            try {
                Files.move(backupFile.toPath(), currentSaveFile.toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                logError("备份恢复也失败了", ex);
            }
        }
        return false;
    } finally {
        // 清理临时文件
        tempFile.delete();
    }
}
```

### 7.3 剩余改进方向

以下方向在与核心功能不冲突的前提下，可择机迭代：

1. **状态机管理游戏流程**：引入有限状态机（FSM）统一管理 登录→菜单→选择→对战→结算 的状态转换，替代零散的布尔标志位（`isLoggedIn`、`isInBattle` 等），提高流程的可控性和可调试性。

2. **属性配置外置化**：将坦克属性、改装装备属性、抽卡概率等从 Java 代码中提取到 JSON 配置文件，使数值调整变为纯配置修改，无需重新编译。

3. **事件总线解耦**：引入简单的事件发布/订阅机制，模块间通信不再直接依赖具体类引用，而是通过事件进行解耦。例如"坦克被击杀"事件由碰撞检测模块发布，结算模块和 UI 模块各自订阅处理。

### 7.4 项目收获

本项目的设计与实现完整覆盖了软件工程的核心流程——从需求分析、架构设计、类图绘制到编码实现、功能验证。其中最具价值的收获在于：**将 MVC 设计模式从一个抽象的概念落地为具体可运行的代码架构**。通过严格界定各层的职责边界与交互规则，我们体会到分层架构对大型项目的意义——它不是增加代码量的"过度设计"，而是降低复杂度的结构化手段。同时，双线程游戏循环、碰撞检测算法、策略模式 AI 等具体技术的实践，深化了对 Java 并发编程、图形编程和设计模式的理解，为后续更复杂的软件项目开发奠定了坚实基础。

---

*文档版本: v1.0*
*编写日期: 2026年7月*


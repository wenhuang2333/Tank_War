# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tank Battle Game (坦克大战) — a Java Swing desktop game using MVC architecture with dual-threaded game loop (logic + render at 60 FPS). Window size: **1380×820**.

**Tech stack**: Java (JDK), Swing, Gson (JSON serialization — planned, not yet added to the project; no `lib/` directory exists)

**Current state**: `src/` is empty (no `.java` files yet). All classes in the Architecture section are planned, not implemented. Resources are ready: `resource/image/` (17 subdirectories, ~185 PNGs), `resource/map-now-data/` (6 formal `.map` files, 40px grid). Dependencies not yet added: `lib/` directory doesn't exist — needs `gson-2.10.1.jar` and `commons-collections4-4.4.jar`.

## Build & Run

Plain IntelliJ IDEA project (`softwar_Training.iml`) — no Maven/Gradle, no tests. Dependencies (`lib/`): needs `gson-2.10.1.jar` and `commons-collections4-4.4.jar`.

```bash
# Compile (all sources under src/, output to out/production/softwar_Training)
javac -encoding UTF-8 -d out/production/softwar_Training $(find src -name "*.java")

# Run (with dependencies and resources on classpath)
java -cp "out/production/softwar_Training:lib/*:resource" main.Main
```

When `src/` is empty (no `.java` files yet), the compile command will error — that's expected. Dependencies must be added to `lib/` before compilation.

## Mandatory Reference Documents

以下五份文档是开发的权威依据，**每次开发变更前必须阅读**：

| 文档 | 作用 |
|------|------|
| `All.md` | **开发总文档**——完整架构、15+ 类详细设计、核心实现流程、碰撞/UI/异常处理方案 |
| `PK.md` | **人机 AI 方案**——三层架构（感知→Utility AI决策→A*+势场执行）、三种难度参数表、射击预判算法 |
| `data.md` | **JSON 存档数据结构**——PlayerSaveData 完整字段定义、Java 实体类、存档生命周期 |
| `pic.md` | **图片资源清单**——约 185 张图片的命名、尺寸（以 1380×820 为基准）、目录结构 |
| `action.md` | **落地实施方案**——17 个模块的详细逻辑实现方案（Java Swing 可行）、开发顺序、每个模块的切入点和实现要点 |

## Key Rules

1. **每次完成代码或文档更新后，必须将变更 push 到 GitHub**：
   ```bash
   git add -A && git commit -m "<简要描述>" && git push
   ```
2. 开发前先阅读上述五份参考文档，动手前先查阅 `action.md` 对应模块的实现方案
3. 速度换算标准（定义在 `All.md` 4.10.1）：坦克实际移速 = 设计值 × 0.04，子弹 = 设计值 × 0.06，转向 = 设计值 × 0.01（均为 60 FPS 下 px/frame 或 rad/frame）
4. 开发顺序遵循 `action.md` 第二十二章的 17 步计划，当前处于第 1 步（项目骨架）之前
5. 所有新类必须放在对应包路径下：`src/main/`（入口）、`src/frame/`（视图）、`src/model/vo/|load/|manager/`（模型）、`src/thread/|ai/`（控制）、`src/util/`（工具）

## Git Push 故障排查

push 失败时按以下顺序排查：

1. **SSL / 网络超时**：先确认网络连通性，再重试 push
2. **HTTP/2 兼容问题**（如 `Recv failure`）：
   ```bash
   git config --global http.version HTTP/1.1
   ```
3. **大文件截断**（如资源图片 push 失败）：
   ```bash
   git config --global http.postBuffer 524288000
   ```
4. 以上配置已全局生效，后续 push 无需重复设置

## Design Documents (source of truth)

- `系统功能架构设计.txt` — 完整功能规格（登录、对战、养成、抽卡、存档）
- `坦克设计机制.txt` — 坦克属性系统、耐久减伤、改装机制
- `坦克对象.txt` — 8 种坦克的初始数值
- `坦克对战操作设计.txt` — 对战操作键位、暂停菜单

## Architecture (planned)

All visible entities extend `SuperElement` (abstract: `show()`, `move()`, `update()`, `destroy()`, `isStrike()`).

| Layer | Package | Key Classes |
|-------|---------|-------------|
| Entry | `main` | `Main` |
| View | `frame` | `MyJFrame`, `MyGameJPanel` |
| Control | `thread` | `GameThread`, AI controllers (`thread/ai/`) |
| Model | `model.vo`, `model.load`, `model.manager` | `SuperElement`, `Players`, `Bullet`, `ElementManager`, `ElementFactory`, `SaveManager` |

Map files: `resource/map-now-data/*.map`（正式地图 1.map–6.map，40px 网格，含 `#` 注释行）— semicolon-separated `BRICK=x,y` / `IRON=x,y` entries. `resource/map-data/` 为旧 20px 网格样例，不再使用。

### Coordinate System Quick Reference

| Item | Value |
|------|-------|
| Window | 1380 × 820 |
| Map area | 1080 × 680, centered with offset (150, 70) |
| Tile size | 40 × 40 px |
| Grid | 27 cols × 17 rows |
| Boundary iron | Automatically generated on outermost ring (x=150/x=1190, y=70/y=710) — NOT in `.map` files |
| Inner usable grid | 25 cols × 15 rows: `x = 190 + col×40` (col 0–24), `y = 110 + row×40` (row 0–14) |
| P1 spawn | (191, 391) — left side, center row |
| P2 spawn | (1151, 391) — right side, center row |
| Tank render size | 38 × 38, centered in 40 × 40 tile |

`.map` file format (in `resource/map-now-data/`):
```
# comment lines starting with # are ignored
BRICK=310,150;350,150;390,150;...
IRON=430,350;910,350;...
```

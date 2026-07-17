# newpic.md —— 新增 UI 资源需求清单

本文档记录本次开发迭代中新增 UI 功能所需的图片资源。当前实现使用原生 Swing 组件作为降级方案，图片资源为可选增强项。

---

## 1. 加时赛（Overtime）过渡界面

**触发时机**：PvP 倒计时结束且开启加时赛模式时，清除所有障碍物并续期 5 分钟。

| 资源名 | 建议路径 | 尺寸 | 用途 |
|--------|----------|------|------|
| `overtime_banner` | `resource/image/battle/overtime_banner.png` | 600×120 | 加时赛开始时短暂展示的提示横幅，居中显示"加时赛" |
| `overtime_flash` | `resource/image/effect/overtime_flash.png` | 1380×820 | 场景切换时的全屏闪白/闪黑过渡效果 |

**当前降级方案**：无过渡动画，直接清除障碍物并重置计时器。HUD 上的倒计时自动刷新为新时间。

---

## 2. PvP 属性修改作弊（SettingsPanel）

**位置**：`SettingsPanel` 作弊设置区，"P1 属性修改"和"P2 属性修改"标题下方。

| 资源名 | 建议路径 | 尺寸 | 用途 |
|--------|----------|------|------|
| `settings_attr_panel_bg` | `resource/image/settings/attr_panel_bg.png` | 700×120 | P1/P2 属性修改区域的半透明背景面板，增强视觉分组 |

**当前降级方案**：使用标准 Swing `JSpinner` + `JLabel` 组件，无背景面板。属性修改 Spinner 以 3 行 × 6 列网格排列（每行 3 个属性，每个属性占 label + spinner 两列）。

### Spinner 布局规格

每个玩家 9 个 Spinner，排列为 3 行，每行 3 个属性：

```
行1: [HP: spinner] [攻击: spinner] [防御: spinner]
行2: [速度: spinner] [转向: spinner] [弹速: spinner]
行3: [弹药: spinner] [换弹: spinner] [耐久: spinner]
```

Spinner 配置：`SpinnerNumberModel(0, -999, 999, 1)` —— 默认 0（不修改），范围 -999 到 999，步长 1。

---

## 3. 资源文件命名规范

新增资源遵循现有 `pic.md` 中的约定：
- **目录**：按功能模块放入 `resource/image/` 对应子目录
- **命名**：小写蛇形命名（snake_case），英文描述
- **格式**：PNG，透明背景（按钮/图标）或不透明（背景/面板）
- **尺寸基准**：窗口 1380×820，按钮/图标按比例缩放

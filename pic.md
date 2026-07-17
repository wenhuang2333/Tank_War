# 坦克大战——图片资源清单

---

## 窗口基准

游戏窗口固定为 **1380 × 820** 像素，所有图片尺寸以此为基准设计。

## 命名规范

- 文件名使用英文小写 + 下划线，方便代码中直接引用
- 目录结构：`resource/img/` 为根目录，按模块分子目录

```
resource/img/
├── common/          # 公共 UI 元素
├── login/           # 登录界面
├── menu/            # 主菜单
├── select/          # 坦克/地图选择
├── battle/          # 对战界面
├── tank/            # 坦克素材
├── bullet/          # 子弹/弹道素材
├── mod/             # 改装装备图标
├── map/             # 地图缩略图
├── gacha/           # 抽卡界面
├── result/          # 结算界面
├── effect/          # 特效动画
├── hud/             # 对战 HUD
├── training/        # 坦克养成界面
└── settings/        # 对战设置界面
```

---

## 一、公共 UI 元素（common/）

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `btn_normal.png` | 按钮——普通态 | 220×60（标准按钮，可根据具体场景调整） |
| `btn_hover.png` | 按钮——悬停态 | 同上 |
| `btn_pressed.png` | 按钮——按下态 | 同上 |
| `btn_disabled.png` | 按钮——禁用态 | 同上 |
| `panel_bg.png` | 通用面板背景 | 可平铺或九宫格拉伸 |
| `dialog_bg.png` | 对话框背景 | 480×280 |
| `icon_iron.png` | 生铁图标 | 28×28 |
| `icon_steel.png` | 钢铁图标 | 28×28 |
| `icon_blueprint.png` | 蓝图图标 | 28×28 |
| `icon_lock.png` | 锁定图标 | 28×28 |
| `icon_unlock.png` | 解锁图标 | 28×28 |
| `arrow_left.png` | 左箭头 | 36×36 |
| `arrow_right.png` | 右箭头 | 36×36 |
| `scroll_bar.png` | 滚动条 | 宽 14，高按内容动态 |
| `tab_active.png` | 页脚——选中态 | 130×36 |
| `tab_inactive.png` | 页脚——未选中态 | 130×36 |
| `star_filled.png` | 星标——实心（阶数/稀有度） | 18×18 |
| `star_empty.png` | 星标——空心 | 18×18 |
| `checkbox_on.png` | 复选框——选中 | 22×22 |
| `checkbox_off.png` | 复选框——未选中 | 22×22 |

> 按钮图片建议做九宫格（9-patch），以适应不同文字长度的按钮。

---

## 二、登录界面（login/）

界面布局：全屏背景 + 居中 Logo + 下方存档列表 + 底部按钮行。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `login_bg.png` | 登录界面背景 | 1380×820 |
| `login_logo.png` | 游戏 Logo / 标题 | 480×140 |
| `btn_login.png` | 登录按钮 | 220×60 |
| `btn_exit.png` | 退出按钮 | 220×60 |
| `save_slot_bg.png` | 存档槽背景（有存档时） | 640×72 |
| `save_slot_empty.png` | 空存档槽（无存档占位） | 640×72 |
| `btn_new_save.png` | 创建新档按钮 | 200×50 |
| `input_name_bg.png` | 玩家名输入框背景 | 340×42 |

---

## 三、主菜单界面（menu/）

界面布局：顶部资源栏（1380×44）+ 中央 2×2 按钮矩阵。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `menu_bg.png` | 主菜单背景 | 1380×820 |
| `btn_battle.png` | 对战按钮 | 260×90 |
| `btn_training.png` | 坦克养成按钮 | 260×90 |
| `btn_gacha.png` | 坦克获取按钮 | 260×90 |
| `btn_exit_game.png` | 退出游戏按钮 | 260×90 |
| `resource_bar_bg.png` | 顶部资源栏底框 | 1380×44 |

---

## 四、坦克 / 地图选择界面（select/）

界面布局：顶部 3 张地图缩略图横排 → 下方 8 张坦克卡片（2 行 × 4 列）→ 底部确认按钮。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `select_bg.png` | 选择界面背景 | 1380×820 |
| `map_thumb_1.png` | 地图 1 缩略图 | 320×220 |
| `map_thumb_2.png` | 地图 2 缩略图 | 320×220 |
| `map_thumb_3.png` | 地图 3 缩略图 | 320×220 |
| `map_selected_border.png` | 地图选中高亮边框 | 328×228（比缩略图多 4px 边距） |
| `tank_card_bg.png` | 坦克卡片背景 | 140×160 |
| `tank_card_selected.png` | 坦克卡片——选中高亮 | 144×164（含边框 2px） |
| `tank_card_locked.png` | 坦克卡片——未拥有（置灰） | 140×160 |
| `btn_pve.png` | 人机对战按钮 | 240×75 |
| `btn_pvp.png` | 双人对战按钮 | 240×75 |
| `btn_easy.png` | 简单难度按钮 | 180×55 |
| `btn_hard.png` | 困难难度按钮 | 180×55 |
| `btn_super.png` | 超级难度按钮 | 180×55 |
| `btn_start_battle.png` | 开始对战按钮 | 240×70 |
| `p1_indicator.png` | 玩家 1 标识 | 64×26 |
| `p2_indicator.png` | 玩家 2 标识 | 64×26 |

---

## 五、对战界面（battle/）

### 5.1 地图素材（battle/map/）

地图区域约为 1080×680，居中于窗口中。每个格子为 40×40 像素。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `tile_ground.png` | 地面方格（可平铺） | 40×40 |
| `wall_brick.png` | 砖墙（可破坏，BRICK） | 40×40 |
| `wall_brick_damaged.png` | 砖墙——受损中间态（可选） | 40×40 |
| `wall_iron.png` | 钢铁墙壁（不可破坏，IRON） | 40×40 |

### 5.2 对战 UI（battle/）

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `battle_bg.png` | 对战背景（地图外围装饰） | 1380×820 |
| `pause_overlay.png` | 暂停半透明遮罩 | 1380×820 |
| `btn_continue.png` | 继续对战按钮 | 240×60 |
| `btn_restart.png` | 重新开始按钮 | 240×60 |
| `btn_end_battle.png` | 结束对战按钮 | 240×60 |
| `countdown_3.png` | 倒计时——3 | 80×100 |
| `countdown_2.png` | 倒计时——2 | 80×100 |
| `countdown_1.png` | 倒计时——1 | 80×100 |
| `countdown_go.png` | 倒计时——GO | 160×100 |

---

## 六、坦克素材（tank/）

坦克朝向通过 `Graphics2D.rotate()` 旋转实现，每个坦克仅需一张朝上的素材图，运行时按朝向弧度动态旋转渲染。

### 6.1 坦克本体（tank/body/）

| 文件名 | 中文名 | 参考尺寸 |
|--------|--------|----------|
| `tank_cromwell.png` | 克伦威尔 | 38×38 |
| `tank_chaffee.png` | M24 霞飞 | 38×38 |
| `tank_firefly.png` | 谢尔曼萤火虫 | 38×38 |
| `tank_joseph_is.png` | 约瑟夫 IS | 38×38 |
| `tank_jagdpanther.png` | 猎豹 | 38×38 |
| `tank_t34.png` | T-34 | 38×38 |
| `tank_panther.png` | 豹式坦克 | 38×38 |
| `tank_tiger.png` | 虎式坦克 | 38×38 |

> 38×38 略小于 40×40 的格子，避免坦克在格子间移动时摩擦墙壁。

### 6.2 坦克详情展示（tank/detail/）

用于养成界面的坦克详情页，左侧大幅展示。

| 文件名 | 中文名 | 参考尺寸 |
|--------|--------|----------|
| `tank_cromwell_large.png` | 克伦威尔——大图 | 280×280 |
| `tank_chaffee_large.png` | M24 霞飞——大图 | 280×280 |
| `tank_firefly_large.png` | 谢尔曼萤火虫——大图 | 280×280 |
| `tank_joseph_is_large.png` | 约瑟夫 IS——大图 | 280×280 |
| `tank_jagdpanther_large.png` | 猎豹——大图 | 280×280 |
| `tank_t34_large.png` | T-34——大图 | 280×280 |
| `tank_panther_large.png` | 豹式坦克——大图 | 280×280 |
| `tank_tiger_large.png` | 虎式坦克——大图 | 280×280 |

### 6.3 坦克缩略图（tank/icon/）

用于选择界面的坦克卡片和列表。

| 文件名 | 中文名 | 参考尺寸 |
|--------|--------|----------|
| `tank_cromwell_icon.png` | 克伦威尔——图标 | 120×100 |
| `tank_chaffee_icon.png` | M24 霞飞——图标 | 120×100 |
| `tank_firefly_icon.png` | 谢尔曼萤火虫——图标 | 120×100 |
| `tank_joseph_is_icon.png` | 约瑟夫 IS——图标 | 120×100 |
| `tank_jagdpanther_icon.png` | 猎豹——图标 | 120×100 |
| `tank_t34_icon.png` | T-34——图标 | 120×100 |
| `tank_panther_icon.png` | 豹式坦克——图标 | 120×100 |
| `tank_tiger_icon.png` | 虎式坦克——图标 | 120×100 |

---

## 七、子弹 / 弹道素材（bullet/）

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `bullet_normal.png` | 普通子弹 | 8×8 |
| `bullet_scatter.png` | 散射子弹（轻机枪/重机枪） | 6×6 |
| `beam_laser.png` | 激光束（激光炮） | 6×40（沿路径平铺拉伸） |
| `beam_laser_bounce.png` | 激光束——反弹段（镭射群炮） | 6×40 |
| `muzzle_flash.png` | 炮口火光特效 | 24×24 |

---

## 八、改装装备图标（mod/）

| 文件名 | 中文名 | 稀有度 | 参考尺寸 |
|--------|--------|--------|----------|
| `mod_anti_friendly_fire.png` | 反斜钢甲 | 5 | 56×56 |
| `mod_instant_turn.png` | 扭绞轮台 | 6 | 56×56 |
| `mod_explosion_proof.png` | 防爆油箱 | 7 | 56×56 |
| `mod_light_mg.png` | 轻机枪 | 3 | 56×56 |
| `mod_heavy_mg.png` | 重机枪 | 4 | 56×56 |
| `mod_laser_cannon.png` | 激光炮 | 6 | 56×56 |
| `mod_dense_laser.png` | 高密度镭射群炮 | 7 | 56×56 |
| `mod_auto_loader.png` | 自动装填器 | 7 | 56×56 |
| `mod_extra_ammo.png` | 额外弹药架 | 4 | 56×56 |
| `mod_fragment.png` | 改装碎片（特定） | 40×40 |
| `mod_fragment_universal.png` | 通用改装碎片 | 40×40 |
| `slot_locked.png` | 改装槽——未解锁 | 64×64 |
| `slot_empty.png` | 改装槽——已解锁可用 | 64×64 |
| `slot_cooldown.png` | 改装槽——冷却中 | 64×64 |

---

## 九、对战 HUD（hud/）

HUD 元素分布在游戏地图区域的外侧（左右或底部）。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `hud_hp_bar_bg.png` | 血条底框 | 200×18 |
| `hud_hp_bar_fill.png` | 血条填充（宽 1px，代码拉伸） | 1×18 |
| `hud_durability_bar_bg.png` | 耐久条底框 | 200×10 |
| `hud_durability_bar_fill.png` | 耐久条填充（宽 1px，代码拉伸） | 1×10 |
| `hud_ammo_icon.png` | 弹药图标 | 22×22 |
| `hud_reload_icon.png` | 换弹中提示图标 | 22×22 |
| `hud_timer_bg.png` | 倒计时显示框 | 110×36 |
| `hud_p1_tag.png` | 玩家 1 标签 | 80×22 |
| `hud_p2_tag.png` | 玩家 2 标签 | 80×22 |
| `hud_ai_tag.png` | 人机标签 | 80×22 |

---

## 十、结算界面（result/）

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `result_victory_bg.png` | 胜利结算背景 | 1380×820 |
| `result_defeat_bg.jpg` | 失败结算背景 | 1380×820 |
| `result_draw_bg.jpg` | 平局结算背景 | 1380×820 |
| `result_victory_text.png` | "胜利" 标题大字 | 340×90 |
| `result_defeat_text.png` | "失败" 标题大字 | 340×90 |
| `result_stat_panel.png` | 数据统计对比面板背景 | 680×380 |
| `btn_return_menu.png` | 返回主菜单按钮 | 220×60 |

---

## 十一、抽卡界面（gacha/）

界面布局：左右两个池子并排，每个池子下方有单抽/十连按钮和保底进度条。抽卡成功后切入全屏翻牌展示层：先显示对应抽数的牌背（单抽 1 张居中，十连 2×5 居中），玩家点击翻转，大奖卡翻开时叠加金光特效。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `gacha_bg.jpg` | 抽卡界面背景 | 1380×820 |
| `gacha_tank_pool_bg.png` | 坦克池区域背景 | 580×520 |
| `gacha_mod_pool_bg.png` | 改装池区域背景 | 580×520 |
| `btn_pull_one.png` | 单抽按钮（消耗 3 蓝图） | 200×60 |
| `btn_pull_ten.png` | 十连抽按钮（消耗 27 蓝图） | 220×60 |
| `gacha_animation_bg.jpg` | 翻牌展示层全屏背景 | 1380×820 |
| `gacha_card_back.png` | 牌背（未翻开状态，单抽/十连通用） | 120×170 |
| `btn_flip_all.png` | 一键翻开按钮（翻牌展示层右下角） | 200×60 |
| `gacha_light_1.png` | 翻牌光效——普通（翻开瞬间闪烁一次后淡出） | 300×300 |
| `gacha_light_2.png` | 金光特效——大奖（翻开后持续旋转+呼吸闪烁） | 400×400 |
| `gacha_card_tank.png` | 结果卡正面——新坦克 | 120×170 |
| `gacha_card_mod.jpg` | 结果卡正面——改装碎片 | 100×140 |
| `gacha_card_resource.png` | 结果卡正面——生铁/钢铁 | 100×120 |
| `gacha_pity_bar_bg.png` | 保底计数条底框 | 380×16 |
| `gacha_pity_bar_fill.png` | 保底计数条填充（宽 1px，代码拉伸） | 1×16 |

**说明**：牌背与结果卡正面均绘制在统一的 120×170 卡框内（`gacha_card_mod` / `gacha_card_resource` 尺寸较小，翻开后居中显示于卡框内，下方叠加数量文字）；翻转动画由代码横向缩放实现，无需多帧翻转序列图。

---

## 十二、对战设置界面（settings/）

仅双人对战时可用，以弹窗形式覆盖在选择界面上方。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `settings_panel_bg.png` | 设置面板背景 | 520×540 |
| `btn_time_3min.png` | 比赛时间——三分钟 | 160×50 |
| `btn_time_5min.png` | 比赛时间——五分钟 | 160×50 |
| `btn_time_10min.png` | 比赛时间——十分钟 | 160×50 |
| `btn_overtime_on.png` | 加时赛——开 | 100×40 |
| `btn_overtime_off.png` | 加时赛——关 | 100×40 |
| `btn_penalty_on.png` | 超时惩罚——开 | 100×40 |
| `btn_penalty_off.png` | 超时惩罚——关 | 100×40 |
| `btn_cheat_on.png` | 允许作弊——开 | 100×40 |
| `btn_cheat_off.png` | 允许作弊——关 | 100×40 |
| `cheat_panel_bg.png` | 作弊选项展开区域背景 | 500×400 |

---

## 十三、坦克养成界面（training/）

界面布局：中央大幅坦克展示，下方左右箭头切换，底部"查看详情"按钮。  
详情页：左侧 25%（~300px）坦克大图，右侧 75% 内容区，页脚可切换。

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `training_bg.png` | 养成界面背景 | 1380×820 |
| `training_tank_display_bg.png` | 坦克展示区底框 | 320×340 |
| `btn_upgrade.png` | 升级按钮 | 200×55 |
| `btn_rank_up.png` | 进阶按钮 | 200×55 |
| `btn_upgrade_disabled.png` | 升级按钮——不可用 | 200×55 |
| `btn_rank_up_disabled.png` | 进阶按钮——不可用 | 200×55 |
| `btn_view_detail.png` | 查看详情按钮 | 200×55 |
| `btn_synthesize.png` | 碎片合成按钮 | 200×55 |
| `btn_synthesize_disabled.png` | 合成按钮——不可用 | 200×55 |
| `btn_install_mod.png` | 安装装备按钮 | 180×50 |
| `btn_dismantle_mod.png` | 拆卸/销毁装备按钮 | 180×50 |
| `detail_tab_stats.png` | 属性页页脚 | 130×36 |
| `detail_tab_training.png` | 养成页页脚 | 130×36 |
| `detail_tab_mod.png` | 改装页页脚 | 130×36 |
| `attr_panel_bg.png` | 属性面板背景（右侧内容区） | 800×520 |
| `progress_bar_bg.png` | 进度条底框 | 360×16 |
| `progress_bar_fill.png` | 进度条填充（宽 1px，代码拉伸） | 1×16 |

---

## 十四、特效动画（effect/）

| 文件名 | 说明 | 参考尺寸 |
|--------|------|----------|
| `explosion_01.png` ~ `explosion_08.png` | 坦克爆炸动画（8 帧序列） | 56×56 |
| `hit_spark_01.png` ~ `hit_spark_04.png` | 子弹命中火花（4 帧序列） | 20×20 |
| `shield_effect.png` | 无敌护盾（防爆油箱触发） | 50×50 |
| `laser_beam_segment.png` | 激光束路径段（沿路径平铺） | 6×6 |
| `wall_destroy_01.png` ~ `wall_destroy_04.png` | 砖墙破坏动画（4 帧序列） | 40×40 |

---

## 十五、尺寸汇总

| 分类 | 图片张数 | 典型尺寸 |
|------|----------|----------|
| 公共 UI | 20 | 18~220 px |
| 登录界面 | 8 | 42~820 px |
| 主菜单 | 6 | 44~820 px |
| 选择界面 | 16 | 26~820 px |
| 对战地图 | 4 | 40×40 |
| 对战 UI | 8 | 80~820 px |
| 坦克本体 | 8 | 38×38 |
| 坦克大图 | 8 | 280×280 |
| 坦克图标 | 8 | 120×100 |
| 子弹 | 5 | 6~40 px |
| 改装装备 | 15 | 40~64 px |
| 对战 HUD | 10 | 10~200 px |
| 结算界面 | 7 | 60~820 px |
| 抽卡界面 | 14 | 16~820 px |
| 对战设置 | 10 | 40~540 px |
| 养成界面 | 16 | 16~820 px |
| 特效动画 | ~22（含多帧） | 6~56 px |
| **总计** | **约 185 张** | |

---

## 十六、代码中引用示例

```java
// 图片加载统一入口
public class ResourceManager {
    private static final String IMG_ROOT = "resource/img/";

    // 坦克素材
    public static final String TANK_CROMWELL = IMG_ROOT + "tank/body/tank_cromwell.png";

    // 改装装备图标
    public static final String MOD_LASER_CANNON = IMG_ROOT + "mod/mod_laser_cannon.png";

    // 公共 UI
    public static final String ICON_IRON  = IMG_ROOT + "common/icon_iron.png";

    // 爆炸动画帧
    public static String[] explosionFrames() {
        return IntStream.rangeClosed(1, 8)
            .mapToObj(i -> IMG_ROOT + "effect/explosion_" + String.format("%02d", i) + ".png")
            .toArray(String[]::new);
    }
}
```

---

*文档版本: v2.0*
*编写日期: 2026年7月*
*基准窗口: 1380×820*

# 人机对战 AI 实现方案——PK.md

---

## 一、AI 架构总览

### 1.1 核心思路

本项目的 AI 坦克（Boss）需具备三个核心能力：**移动决策**（去哪、怎么走）、**攻击决策**（何时射击、瞄哪）、**生存决策**（躲避子弹、利用掩体）。这三个能力不是孤立的——移动到射击位置的同时需要考虑规避威胁，射击决策受弹药和换弹时间约束，生存行为要在"躲避"和"进攻"之间动态平衡。

针对本项目的需求，推荐采用 **"三层感知-决策-执行"架构 + 工具系统（Utility System）** 作为整体框架：

```
┌─────────────────────────────────────────────────┐
│                  感知层 (Perception)               │
│  雷达扫描 │ 威胁评估 │ 资源感知 │ 地形分析          │
│  (敌人位置)│(子弹轨迹)│(HP/弹药)│(墙壁/掩体)        │
└───────────────────────┬─────────────────────────┘
                        ▼
┌─────────────────────────────────────────────────┐
│                  决策层 (Decision)                 │
│  ┌─────────────────────────────────────────┐    │
│  │         Utility AI 工具系统                │    │
│  │  对每个候选行为打分，选择最高分执行          │    │
│  │  候选行为: 追击/撤退/射击/躲避/巡逻/换弹     │    │
│  └─────────────────────────────────────────┘    │
└───────────────────────┬─────────────────────────┘
                        ▼
┌─────────────────────────────────────────────────┐
│                  执行层 (Execution)                │
│  路径规划(A*) │ 预测瞄准 │ 闪避机动 │ 资源管理     │
│  (怎么走)     │(往哪打)  │(躲子弹)  │(换弹/走位)   │
└─────────────────────────────────────────────────┘
```

**为什么选择 Utility AI 而非纯状态机？**

| 对比维度 | 状态机(FSM) | 行为树(BT) | 工具系统(Utility AI) |
|----------|:-----------:|:----------:|:--------------------:|
| 行为过渡 | 硬切换（突变） | 条件分支 | **平滑过渡**（分数渐变） |
| 多因素权衡 | 困难（状态爆炸） | 中等 | **天然支持**（多因素相乘） |
| 可扩展性 | 差（新增状态影响全局） | 好 | **极好**（新增行为独立打分） |
| 调试难度 | 简单 | 中等 | 较难（需可视化分数） |
| 行为多样性 | 固定 | 较丰富 | **最丰富**（加权随机选择） |

由于本项目要求三种难度梯度的 AI，且 AI 需要在"走位射击"和"规避子弹"之间做动态权衡，工具系统的多因素评分机制天然适合这种场景。难度差异通过调整评分曲线参数即可实现，无需为三个难度写三套逻辑。

### 1.2 难度梯度的核心差异

三种难度在**同一套算法框架**下通过**参数差异化**实现，不写三套独立逻辑：

| 维度 | 简单(Easy) | 困难(Hard) | 超级(Super) |
|------|-----------|-----------|------------|
| **反应延迟** | 500-800ms | 200-400ms | 50-150ms |
| **路径规划** | 随机漫游 + 偶尔A* | 周期性 A* + 势场避障 | 持续 A* + 势场避障 + 预判 |
| **射击精度** | 直接瞄准（无预判） | 线性预判（1次迭代） | 二次方程精确解算 |
| **射击频率** | 固定间隔射击 | 仅在朝向敌人时射击 | 持续瞄准 + 最大化射速 |
| **子弹躲避** | 无主动躲避 | 检测到开火后闪避 | 危险地图 + 主动规避 |
| **掩体利用** | 无 | 偶尔 | 持续评估 + 主动卡掩体 |
| **弹药管理** | 打空才换弹 | 弹匣过半时评估 | 主动控弹（保持充足备弹） |
| **走位策略** | 随机方向 | 保持中距离 + 小幅横移 | 控制最优距离 + 预判横移 |

---

## 二、感知层设计

感知层负责从游戏世界中提取 AI 决策所需的所有信息。

### 2.1 数据结构

```java
public class AIPerception {
    // === 自身状态 ===
    int selfHp, selfMaxHp;
    int selfAmmo, selfMaxAmmo;
    boolean isReloading;
    int direction;          // 当前朝向
    int x, y;              // 当前位置
    int speed, turnSpeed;

    // === 敌人信息（玩家） ===
    int enemyX, enemyY;
    int enemyDirection;
    int enemyHp;
    int enemySpeed;
    long lastEnemySeenTime;  // 最后观测到敌人的时间戳

    // === 威胁信息 ===
    List<BulletThreat> incomingBullets;  // 正在飞向自身的子弹
    List<Point> nearbyExplosions;        // 附近的爆炸点

    // === 环境信息 ===
    int mapWidth, mapHeight;
    boolean[][] wallGrid;    // 墙壁占据网格（用于路径规划）
    List<Point> coverPoints;  // 可用掩体位置

    // === 历史信息 ===
    double enemyPrevEnergy;   // 用于检测敌方开火（能量突然下降）
    List<Point> enemyRecentPath;  // 敌人最近的移动轨迹（用于预判）
}
```

### 2.2 信息采集方法

#### 敌方位置追踪

AI 不需要像玩家一样受视野限制——可以直接从 `ElementManager` 读取玩家位置。但为了真实感，不同难度可以添加"感知噪声"（给位置加随机偏移）。

```java
public void updatePerception() {
    Players enemy = ElementManager.getInstance().getElements("players").get(0);
    // 直接读取真实位置（AI 不需要 FoW 限制）
    perception.enemyX = enemy.getX();
    perception.enemyY = enemy.getY();
    perception.enemyDirection = enemy.getDirection();
    perception.enemyHp = enemy.getHp();
    perception.enemySpeed = enemy.getSpeed();
}
```

#### 子弹威胁感知

遍历 `ElementManager` 中的子弹列表，筛选出对自身构成威胁的子弹：

```java
public List<BulletThreat> detectThreats() {
    List<BulletThreat> threats = new ArrayList<>();
    List<SuperElement> bullets = ElementManager.getInstance().getElements("bullet");
    Boss self = this.boss;

    for (SuperElement e : bullets) {
        Bullet b = (Bullet) e;
        // 跳过友方子弹和自己发射的子弹
        if (b.getOwner() == self) continue;
        if (b.getOwner() instanceof Boss) continue;

        // 计算子弹与自身的最近距离
        double dist = distanceToBulletTrajectory(b, self);
        double timeToImpact = estimateTimeToImpact(b, self);

        if (dist < THREAT_RADIUS && timeToImpact < THREAT_TIME_HORIZON) {
            BulletThreat threat = new BulletThreat();
            threat.bullet = b;
            threat.distance = dist;
            threat.timeToImpact = timeToImpact;
            threat.dangerLevel = calculateDanger(b, self);
            threats.add(threat);
        }
    }
    return threats;
}
```

#### 敌方开火检测（用于闪避触发）

模仿 Robocode 中的能量检测机制——监听敌方坦克的属性变化：

```java
// 每帧检查
int prevEnemyAmmo = perception.enemyPrevAmmo;
int currEnemyAmmo = enemy.getAmmo();
if (currEnemyAmmo < prevEnemyAmmo) {
    // 敌人开火了！触发闪避逻辑
    perception.enemyJustFired = true;
    perception.enemyFiredTimestamp = System.currentTimeMillis();
}
perception.enemyPrevAmmo = currEnemyAmmo;
```

---

## 三、决策层设计——Utility AI 工具系统

### 3.1 候选行为定义

AI 每帧（或每 N 帧）对以下候选行为打分，选择分数最高的执行：

```java
public enum AIAction {
    PURSUE,          // 追击：向敌人移动
    RETREAT,         // 撤退：远离敌人
    STRAFE,          // 横移：垂直于敌人方向移动（保持距离）
    HOLD_POSITION,   // 原地不动（有利位置时）
    ATTACK,          // 开火
    DODGE,           // 紧急闪避（检测到子弹威胁）
    RELOAD,          // 换弹
    SEEK_COVER,      // 寻找掩体
    PATROL,          // 随机巡逻（敌方不可见时）
}
```

### 3.2 评分轴（Considerations）

每个候选行为通过多个评分轴（Consideration）的综合乘积得出最终分数。每个轴输出 0.0~1.0 的值，任何轴返回 0 则直接否决该行为。

```java
public interface Consideration {
    // 返回 0.0 ~ 1.0， 0 = 完全不应执行此行为，1 = 非常应该执行
    double score(AIAction action, AIPerception perception, Boss self);
}
```

#### 核心评分轴定义

| 评分轴 | 作用 | 响应曲线 |
|--------|------|----------|
| **HealthScore** | 自身血量百分比越高，越倾向进攻；越低，越倾向撤退 | 反 S 曲线（高血量时进攻权重高，低血量时急剧下降） |
| **AmmoScore** | 弹药充足时允许射击；弹药不足时倾向换弹 | 分段函数（弹药 > 30% = 1.0，弹药 = 0 = 0 否决射击） |
| **EnemyProximityScore** | 敌人距离越近，进攻和闪避权重越高 | 线性（近=高分，远=低分） |
| **ThreatScore** | 敌方子弹威胁越大，闪避/撤退权重越高 | 指数增长（威胁时间 < 0.5s 时急剧升高） |
| **CoverScore** | 附近是否有可用掩体 | 分段（有掩体=1.0，无=0.3） |
| **TacticalPositionScore** | 当前位置是否有利于射击 | 评估是否有清晰的射击线、是否处于开阔地 |
| **ReloadUrgencyScore** | 弹匣剩余量和当前战况决定是否需要换弹 | 考虑弹匣比例 + 敌人距离 + 是否安全 |
| **StalemateScore** | 检测是否在原地无效对射（双方都不掉血） | 持续对射 N 秒无命中时触发变招 |

### 3.3 行为评分计算示例

#### ATTACK（攻击）评分

```java
public double scoreAttack(AIPerception p) {
    double score = 1.0;

    // 弹药检查——空弹匣直接否决
    if (p.selfAmmo <= 0 && !p.isReloading) return 0.0;

    // 射击冷却检查——冷却中直接否决
    if (System.currentTimeMillis() - lastFireTime < fireRate) return 0.0;

    // 敌人距离评分——太远打不中
    double dist = distance(p.selfX, p.selfY, p.enemyX, p.enemyY);
    double distScore = 1.0 - clamp(dist / MAX_ENGAGE_DISTANCE, 0, 1);
    score *= distScore;

    // 射击线检查——是否有墙壁遮挡
    boolean hasLineOfSight = checkLineOfSight(p.selfX, p.selfY, p.enemyX, p.enemyY, p.wallGrid);
    score *= hasLineOfSight ? 1.0 : 0.0;  // 无射击线则否决

    // 威胁评分——自身受威胁时不射击，优先闪避
    double threatScore = 1.0 - p.getMaxThreatLevel();
    score *= Math.max(threatScore, 0.2);  // 最低 0.2 防止彻底不攻击

    return score;
}
```

#### DODGE（闪避）评分

```java
public double scoreDodge(AIPerception p) {
    if (p.incomingBullets.isEmpty()) return 0.0;  // 无威胁则不需要闪避

    // 取最紧急的威胁
    BulletThreat worst = p.getWorstThreat();

    // 威胁紧急度——越近越需要闪避
    double urgencyScore = clamp(1.0 / (worst.timeToImpact + 0.1), 0, 1);
    // timeToImpact=0.1s → urgency≈0.91
    // timeToImpact=1.0s → urgency≈0.48

    // 可闪避空间——检查是否有闪避空间（不被墙壁包围）
    double escapeSpace = evaluateEscapeSpace(p);
    score *= escapeSpace;

    return score;
}
```

#### PURSUE（追击）评分

```java
public double scorePursue(AIPerception p) {
    double score = 1.0;

    // 血量充足才追击（血量低时撤退优先）
    double healthRatio = (double) p.selfHp / p.selfMaxHp;
    double healthScore = sigmoid(healthRatio, 0.3, 10);  // 30% 血量转折点
    score *= healthScore;

    // 威胁低才追击
    double safetyScore = 1.0 - p.getMaxThreatLevel();
    score *= safetyScore;

    // 敌人不在最优距离内才调整位置
    double dist = distance(p.selfX, p.selfY, p.enemyX, p.enemyY);
    double distFromOptimal = Math.abs(dist - OPTIMAL_DISTANCE) / OPTIMAL_DISTANCE;
    score *= clamp(distFromOptimal, 0.2, 1.0);  // 已在最优距离则降低追击权重

    return score;
}
```

### 3.4 加权随机选择（避免行为可预测）

```java
public AIAction decide() {
    Map<AIAction, Double> scores = new EnumMap<>(AIAction.class);
    for (AIAction action : AIAction.values()) {
        scores.put(action, evaluateAction(action, perception));
    }

    // 找到最高分
    double bestScore = scores.values().stream().max(Double::compare).orElse(0.0);

    // 取分数在最高分 85% 以内的候选行为
    List<AIAction> candidates = scores.entrySet().stream()
        .filter(e -> e.getValue() >= bestScore * ELITE_THRESHOLD)  // 0.85
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    // 按分数加权随机选择一个
    // 这使 AI 的行为不完全可预测，增加拟人感
    return weightedRandomSelect(candidates, scores);
}
```

**难度差异化**：`ELITE_THRESHOLD` 随难度变化——简单=0.6（更多随机性），困难=0.8，超级=0.95（几乎总是选最优）。

---

## 四、执行层——移动/射击/躲避算法详解

### 4.1 移动系统：A* + 势场法混合

#### 为什么用混合方案？

纯 A* 的路径最短但显得机械（坦克沿网格中心线走直线），纯势场法轨迹自然但可能陷入局部极小值（被 U 形墙壁困住）。**混合方案**：A* 计算全局路径，势场法处理局部避障和轨迹平滑。

#### A* 路径规划

将地图划分为网格（建议 20×20 像素/格），墙壁所在格子标记为不可通过。使用 8 方向移动（允许对角线）。

```java
public class AStarPathfinder {
    // 地图格子大小（与墙壁块大小对齐，通常为 20px）
    private static final int CELL_SIZE = 20;

    // A* 核心数据结构
    static class Node implements Comparable<Node> {
        int x, y;           // 网格坐标
        double gCost;       // 从起点到当前节点的实际代价
        double hCost;       // 启发式估计——到终点的估计代价
        Node parent;        // 父节点（用于回溯路径）

        double fCost() { return gCost + hCost; }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost(), other.fCost());
        }
    }

    public List<Point> findPath(int startX, int startY, int goalX, int goalY,
                                 boolean[][] blocked) {
        int gridW = blocked.length;
        int gridH = blocked[0].length;

        int sx = startX / CELL_SIZE;
        int sy = startY / CELL_SIZE;
        int gx = goalX / CELL_SIZE;
        int gy = goalY / CELL_SIZE;

        // 边界检查
        if (sx < 0 || sy < 0 || gx < 0 || gy < 0) return null;
        if (sx >= gridW || sy >= gridH || gx >= gridW || gy >= gridH) return null;
        if (blocked[sx][sy] || blocked[gx][gy]) return null;

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[gridW][gridH];
        // gCosts[x][y] 记录到达每个格子的最小 g 值
        double[][] gCosts = new double[gridW][gridH];
        for (double[] row : gCosts) Arrays.fill(row, Double.MAX_VALUE);

        Node start = new Node();
        start.x = sx; start.y = sy;
        start.gCost = 0;
        start.hCost = chebyshevDistance(sx, sy, gx, gy);
        openSet.add(start);
        gCosts[sx][sy] = 0;

        // 8 个方向：上、下、左、右、四对角
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0},{-1,-1},{-1,1},{1,-1},{1,1}};

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == gx && current.y == gy) {
                // 找到目标——回溯路径
                return reconstructPath(current);
            }

            if (closedSet[current.x][current.y]) continue;
            closedSet[current.x][current.y] = true;

            for (int i = 0; i < dirs.length; i++) {
                int nx = current.x + dirs[i][0];
                int ny = current.y + dirs[i][1];

                if (nx < 0 || ny < 0 || nx >= gridW || ny >= gridH) continue;
                if (blocked[nx][ny] || closedSet[nx][ny]) continue;

                // 对角线移动时防止贴墙穿过
                if (i >= 4) {  // 对角线方向
                    if (blocked[current.x + dirs[i][0]][current.y]) continue;
                    if (blocked[current.x][current.y + dirs[i][1]]) continue;
                }

                double moveCost = (i < 4) ? 1.0 : 1.414;  // 对角线代价 = √2
                double newG = current.gCost + moveCost;

                if (newG < gCosts[nx][ny]) {
                    gCosts[nx][ny] = newG;
                    Node neighbor = new Node();
                    neighbor.x = nx; neighbor.y = ny;
                    neighbor.gCost = newG;
                    neighbor.hCost = chebyshevDistance(nx, ny, gx, gy);
                    neighbor.parent = current;
                    openSet.add(neighbor);
                }
            }
        }
        return null; // 不可达
    }

    // 切比雪夫距离（8方向移动时的最优启发式）
    private double chebyshevDistance(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    // 回溯构建路径
    private List<Point> reconstructPath(Node end) {
        List<Point> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(new Point(current.x * CELL_SIZE + CELL_SIZE/2,
                               current.y * CELL_SIZE + CELL_SIZE/2));
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
```

#### 路径平滑（消除不必要的拐点）

A* 输出的路径沿网格走，会包含大量 L 形拐弯。使用**视线法（Line-of-Sight）**简化：

```java
public List<Point> smoothPath(List<Point> rawPath, boolean[][] blocked) {
    if (rawPath == null || rawPath.size() <= 2) return rawPath;

    List<Point> smoothed = new ArrayList<>();
    smoothed.add(rawPath.get(0));
    int anchor = 0;

    for (int i = 2; i < rawPath.size(); i++) {
        // 如果从 anchor 直接到 i 被墙壁阻挡，则 anchor 到 i-1 是当前最远直线
        if (!hasLineOfSight(rawPath.get(anchor), rawPath.get(i), blocked)) {
            smoothed.add(rawPath.get(i - 1));
            anchor = i - 1;
        }
    }
    smoothed.add(rawPath.get(rawPath.size() - 1));
    return smoothed;
}

private boolean hasLineOfSight(Point from, Point to, boolean[][] blocked) {
    // Bresenham 画线算法——检查线段经过的所有格子
    int x0 = from.x / CELL_SIZE, y0 = from.y / CELL_SIZE;
    int x1 = to.x / CELL_SIZE, y1 = to.y / CELL_SIZE;

    int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
    int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
    int err = dx - dy;

    while (x0 != x1 || y0 != y1) {
        if (blocked[x0][y0]) return false;
        int e2 = 2 * err;
        if (e2 > -dy) { err -= dy; x0 += sx; }
        if (e2 < dx)  { err += dx; y0 += sy; }
    }
    return true;
}
```

#### 路径跟随（沿路径移动）

AI 坦克不需要逐帧跑 A*。每 500ms（简单）/ 200ms（困难）/ 100ms（超级）重新规划一次，其余帧沿当前路径移动：

```java
public void followPath(List<Point> path) {
    if (path == null || path.isEmpty()) return;
    Point target = path.get(0);

    double dx = target.x - self.getX();
    double dy = target.y - self.getY();
    double targetDirection = Math.toDegrees(Math.atan2(dx, -dy));

    // 旋转朝向目标方向
    double angleDiff = normalizeAngle(targetDirection - self.getDirection());
    if (Math.abs(angleDiff) > 5) {
        // 需要旋转
        self.rotateTowards(targetDirection);
    } else {
        // 朝向正确，前进
        self.moveForward();
    }

    // 到达当前路点，移除
    if (Math.hypot(dx, dy) < ARRIVAL_THRESHOLD) {
        path.remove(0);
    }
}
```

#### 势场法局部避障

在 A* 路径的基础上叠加势场力，实现动态避障和平滑轨迹：

```java
public Vector2D computePotentialForce(int x, int y, AIPerception p) {
    Vector2D force = new Vector2D(0, 0);

    // 1. 吸引力——指向路径上的下一个路点
    if (currentPath != null && !currentPath.isEmpty()) {
        Point waypoint = currentPath.get(0);
        Vector2D attract = new Vector2D(waypoint.x - x, waypoint.y - y);
        attract.normalize().scale(ATTRACT_STRENGTH);
        force.add(attract);
    }

    // 2. 墙壁排斥力——推开墙壁
    for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
        for (int dy = -CHECK_RADIUS; dy <= CHECK_RADIUS; dy++) {
            int gx = (x / CELL_SIZE) + dx;
            int gy = (y / CELL_SIZE) + dy;
            if (gx >= 0 && gy >= 0 && gx < gridW && gy < gridH && wallGrid[gx][gy]) {
                double wx = gx * CELL_SIZE + CELL_SIZE / 2;
                double wy = gy * CELL_SIZE + CELL_SIZE / 2;
                double dist = Math.hypot(wx - x, wy - y);
                if (dist < WALL_REPEL_RADIUS && dist > 0) {
                    Vector2D repel = new Vector2D(x - wx, y - wy);
                    repel.normalize().scale(WALL_REPEL_STRENGTH / (dist * dist));
                    force.add(repel);
                }
            }
        }
    }

    // 3. 子弹排斥力——远离飞来的子弹
    for (BulletThreat threat : p.incomingBullets) {
        Vector2D bulletDir = threat.getVelocity();
        Vector2D perpendicular = bulletDir.perpendicular();
        // 选择远离子弹轨迹线的方向
        if (threat.isToLeft(x, y)) {
            force.add(perpendicular.scale(BULLET_REPEL_STRENGTH / threat.timeToImpact));
        } else {
            force.add(perpendicular.scale(-BULLET_REPEL_STRENGTH / threat.timeToImpact));
        }
    }

    return force;
}
```

#### 局部极小值检测与逃脱

势场法最著名的缺陷是可能陷入局部极小值。检测方法：如果 AI 在 1 秒内移动距离 < 5 像素，判定为"卡住"。

```java
public void checkStuck(AIPerception p) {
    double moved = Math.hypot(p.selfX - lastPosition.x, p.selfY - lastPosition.y);
    stuckAccumulator += moved;

    if (stuckTimer++ > STUCK_THRESHOLD_FRAMES) {  // 60 帧 ~ 1秒
        if (stuckAccumulator < 5.0) {
            // 陷入局部极小值——切换到纯 A* 并沿路径强力执行
            escapeMode = true;
            escapePath = findPath(...);  // 重新规划
            stuckTimer = 0;
        }
        stuckAccumulator = 0;
    }
}
```

---

### 4.2 射击系统——预测瞄准算法

AI 的射击需要预判目标的移动，否则快速移动的玩家几乎不可能被打中。

#### 方案一：线性预判（Linear Prediction）

假设目标保持当前速度与方向匀速运动，解算子弹飞行时间内的目标位移。

```java
public double calculateAimAngle(Players target, Boss self) {
    double bulletSpeed = self.getBulletSpeed();
    double targetSpeed = target.getSpeed();

    // 目标当前位置
    double tx = target.getX();
    double ty = target.getY();
    // 目标朝向（角度转弧度）
    double targetAngle = Math.toRadians(target.getDirection());
    // 目标速度分量
    double tvx = targetSpeed * Math.sin(targetAngle);
    double tvy = -targetSpeed * Math.cos(targetAngle);

    // 自身位置
    double sx = self.getX() + self.getWidth() / 2.0;
    double sy = self.getY() + self.getHeight() / 2.0;

    // 迭代预判（通常 2-3 次迭代即收敛）
    double predictedX = tx;
    double predictedY = ty;
    for (int iter = 0; iter < 3; iter++) {
        double dx = predictedX - sx;
        double dy = predictedY - sy;
        double dist = Math.hypot(dx, dy);
        double flightTime = dist / bulletSpeed;

        // 更新预判位置
        predictedX = tx + tvx * flightTime;
        predictedY = ty + tvy * flightTime;
    }

    // 返回瞄准角度
    return Math.toDegrees(Math.atan2(predictedX - sx, -(predictedY - sy)));
}
```

#### 方案二：二次方程精确解算（Circular / Intercept）

考虑子弹和目标都是恒定速度，建立运动方程求解精确的命中时间。这是精度最高的方法。

推导思路：子弹命中目标时，子弹飞行距离 = bulletSpeed × t，这段时间内目标移动了 targetSpeed × t。联立后得到一个关于 t 的二次方程。

```java
public Double solveIntercept(Players target, Boss self) {
    double bx = self.getX() + self.getWidth() / 2.0;
    double by = self.getY() + self.getHeight() / 2.0;
    double tx = target.getX();
    double ty = target.getY();
    double tvx = target.getSpeed() * Math.sin(Math.toRadians(target.getDirection()));
    double tvy = -target.getSpeed() * Math.cos(Math.toRadians(target.getDirection()));
    double bs = self.getBulletSpeed();

    // 相对位置
    double dx = tx - bx;
    double dy = ty - by;

    // 二次方程系数: a*t² + b*t + c = 0
    // a = Vt² - Vb²
    // b = 2*(dx*tvx + dy*tvy)
    // c = dx² + dy²
    double a = tvx * tvx + tvy * tvy - bs * bs;
    double b = 2 * (dx * tvx + dy * tvy);
    double c = dx * dx + dy * dy;

    double discriminant = b * b - 4 * a * c;

    if (discriminant < 0) {
        // 无解——子弹追不上目标或目标正在远离且太快
        // 退化到线性预判
        return null;
    }

    // 取最小的正根（最早命中时间）
    double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
    double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

    double t = Double.POSITIVE_INFINITY;
    if (t1 > 0) t = Math.min(t, t1);
    if (t2 > 0) t = Math.min(t, t2);

    if (Double.isInfinite(t)) return null;  // 两个根都是负数

    // 计算预判位置
    double px = tx + tvx * t;
    double py = ty + tvy * t;

    return Math.toDegrees(Math.atan2(px - bx, -(py - by)));
}
```

#### 难度差异化

| 难度 | 瞄准方法 | 精度调整 |
|------|----------|----------|
| 简单 | 直接瞄准当前位置（无预判） | 随机偏移 ±10° |
| 困难 | 线性预判，1 次迭代 | 随机偏移 ±3° |
| 超级 | 二次方程精确解算 | 无随机偏移；若无解则回退线性预判 3 次迭代 |

---

### 4.3 子弹躲避系统

#### 方案一：基于开火检测的闪避（适用于简单/困难）

最简单的方式——检测到敌人开火时立即改变移动方向：

```java
public void onEnemyFired() {
    Boss self = this.boss;
    // 立即转向 90°（垂直于敌人方向）
    double toEnemy = angleTo(self, enemy);
    // 随机选择左转或右转
    double dodgeAngle = toEnemy + (Math.random() > 0.5 ? 90 : -90);
    self.setDirection(dodgeAngle);
    self.moveForward();  // 全速脱离子弹轨迹

    // 闪避持续 300ms 后恢复正常移动
    dodgeTimer = 300;
}
```

#### 方案二：危险地图（Danger Map）——适用于超级难度

将地图网格化，为每个格子计算"危险值"，然后 A* 导航到最安全的格子。这是处理密集子弹的标准方案。

```java
public class DangerMap {
    private static final int CELL = 20;
    private double[][] dangerGrid;

    public void compute(Boss self, List<Bullet> allBullets, int mapW, int mapH) {
        int gw = mapW / CELL, gh = mapH / CELL;
        dangerGrid = new double[gw][gh];

        for (Bullet b : allBullets) {
            if (b.getOwner() == self) continue;

            // 每颗子弹为其轨迹前方格子增加危险值
            double bx = b.getX();
            double by = b.getY();
            double bdx = b.getSpeed() * Math.sin(Math.toRadians(b.getDirection()));
            double bdy = -b.getSpeed() * Math.cos(Math.toRadians(b.getDirection()));

            // 预测未来 N 帧的子弹位置
            for (int frame = 0; frame < 30; frame++) {  // 预测 0.5 秒
                double fx = bx + bdx * frame;
                double fy = by + bdy * frame;
                int gx = (int)(fx / CELL);
                int gy = (int)(fy / CELL);
                if (gx >= 0 && gy >= 0 && gx < gw && gy < gh) {
                    // 危险值随时间衰减（越近越危险）
                    dangerGrid[gx][gy] += 10.0 / (frame + 1);
                }
            }
        }
    }

    public Point findSafestCell(int currentX, int currentY, int searchRadius) {
        int cx = currentX / CELL, cy = currentY / CELL;
        double minDanger = Double.MAX_VALUE;
        Point safest = new Point(currentX, currentY);

        int r = searchRadius / CELL;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                int gx = cx + dx, gy = cy + dy;
                if (gx < 0 || gy < 0 || gx >= dangerGrid.length || gy >= dangerGrid[0].length)
                    continue;
                if (wallGrid[gx][gy]) continue;  // 墙壁不可通行

                // 综合评分：危险值 - 距离奖励（优先选近的安全格子）
                double dist = Math.abs(dx) + Math.abs(dy);
                double score = dangerGrid[gx][gy] + dist * 0.5;
                if (score < minDanger) {
                    minDanger = score;
                    safest = new Point(gx * CELL + CELL/2, gy * CELL + CELL/2);
                }
            }
        }
        return safest;
    }
}
```

#### 方案三：垂直接近闪避（适用于所有难度，作为基础反应）

对每颗有威胁的子弹，计算垂直接近方向并移动。这是最轻量级的方法，在超级难度中与 Danger Map 配合使用：

```java
public void dodgeBullet(Bullet b, Boss self) {
    // 子弹速度方向
    double bAngle = Math.toRadians(b.getDirection());
    double bdx = Math.sin(bAngle);
    double bdy = -Math.cos(bAngle);

    // 从自身到子弹轨迹线的垂足
    double sx = self.getX() - b.getX();
    double sy = self.getY() - b.getY();
    double proj = sx * bdx + sy * bdy;
    double closestX = b.getX() + proj * bdx;
    double closestY = b.getY() + proj * bdy;

    // 从垂足指向自身的向量——即"远离子弹轨迹"的方向
    double awayX = self.getX() - closestX;
    double awayY = self.getY() - closestY;

    double dodgeAngle = Math.toDegrees(Math.atan2(awayX, -awayY));
    self.setDirection(dodgeAngle);
    self.moveForward();  // 垂直于子弹轨迹逃离
}
```

#### 难度差异化

| 难度 | 闪避策略 | 参数 |
|------|----------|------|
| 简单 | 无主动闪避 | —— |
| 困难 | 检测开火 → 90°闪避 | 闪避持续 300ms |
| 超级 | Danger Map + 垂直接近 + 开火检测 | 预测 30 帧，搜索半径 200px，持续评估 |

---

## 五、难度梯度完整参数配置

### 5.1 简单难度（EasyAI）

定位：**入门对手**——让新玩家熟悉操作，偶尔对玩家构成威胁但不至于挫败。

```java
public class EasyAI extends AIController {
    // === 感知 ===
    private long reactionDelay = 500;  // 500ms 反应延迟
    private double aimingNoise = 10.0; // ±10° 瞄准误差

    // === 移动 ===
    private int pathRecalcInterval = 500; // 每 500ms 重新规划路径（单位 ms）
    private boolean useAStar = false;     // 不使用 A*，仅随机漫游

    // === 射击 ===
    private boolean usePrediction = false; // 不使用预判瞄准
    private double fireIntervalBonus = 0; // 无额外射击频率加成

    // === 躲避 ===
    private boolean dodgeEnabled = false; // 不主动躲避子弹

    @Override
    public void decide(Boss boss) {
        // 添加反应延迟——500ms 内不做决策
        if (System.currentTimeMillis() - lastDecisionTime < reactionDelay) return;

        // 移动：随机漫游
        if (Math.random() < 0.03) { // 每帧 3% 概率改变方向
            boss.setDirection(Math.random() * 360);
        }
        boss.moveForward();

        // 射击：固定间隔，瞄准当前位置（含噪声）
        if (canFire(boss)) {
            double aim = angleTo(boss, enemy) + (Math.random() - 0.5) * 2 * aimingNoise;
            boss.setDirection(aim);
            boss.fire();
        }
    }
}
```

### 5.2 困难难度（HardAI）

定位：**有挑战的对手**——在中等距离上精确射击，主动追击，会基础闪避。

```java
public class HardAI extends AIController {
    // === 感知 ===
    private long reactionDelay = 200;    // 200ms 反应延迟
    private double aimingNoise = 3.0;   // ±3° 瞄准误差

    // === 移动 ===
    private int pathRecalcInterval = 200; // 每 200ms 重新规划
    private boolean useAStar = true;      // 使用 A* 路径规划
    private double optimalDistance = 200; // 保持 200px 中距离

    // === 射击 ===
    private boolean usePrediction = true;        // 使用线性预判
    private int predictionIterations = 1;         // 1 次迭代
    private boolean fireOnlyWhenFacing = true;    // 只在朝向敌人时射击

    // === 躲避 ===
    private boolean dodgeEnabled = true;     // 主动闪避
    private int dodgeDuration = 300;         // 闪避持续 300ms

    @Override
    public void decide(Boss boss) {
        if (System.currentTimeMillis() - lastDecisionTime < reactionDelay) return;

        updatePerception(boss);

        // 1. 检测威胁——决定是否闪避
        if (dodgeEnabled && perception.enemyJustFired) {
            executeDodge(boss);
            return;  // 闪避优先
        }

        // 2. 距离管理——保持最优距离
        double dist = distance(boss, enemy);
        if (dist > optimalDistance * 1.3) {
            pursueEnemy(boss);   // 太远 → 追击
        } else if (dist < optimalDistance * 0.7) {
            retreatFromEnemy(boss); // 太近 → 撤退
        } else {
            strafeAround(boss);  // 合适距离 → 横移
        }

        // 3. 攻击决策
        if (canFire(boss)) {
            if (!fireOnlyWhenFacing || isFacingEnemy(boss, 20)) { // 朝向误差 < 20°
                boss.fire();
            }
        }
    }
}
```

### 5.3 超级难度（SuperAI）

定位：**顶尖对手**——精确预判 + 主动规避 + 掩体利用 + 资源管理，代表 AI 能力的上限。

```java
public class SuperAI extends AIController {
    // === 感知 ===
    private long reactionDelay = 50;     // 50ms 反应延迟（近乎实时）
    private double aimingNoise = 0.0;   // 零误差

    // === 移动 ===
    private int pathRecalcInterval = 100; // 每 100ms 重新规划
    private boolean useAStar = true;
    private boolean usePotentialField = true;   // 叠加势场平滑
    private double optimalDistance = 180;

    // === 射击 ===
    private boolean usePrediction = true;
    private boolean useExactSolve = true;        // 二次方程精确解算
    private int predictionIterations = 3;         // 退化为线性预判时的迭代次数

    // === 躲避 ===
    private boolean dodgeEnabled = true;
    private boolean useDangerMap = true;          // 使用危险地图
    private int dangerMapRadius = 200;            // 搜索半径
    private boolean useCoverSeeking = true;       // 主动寻找掩体

    // === 弹药管理 ===
    private boolean smartReload = true;           // 智能换弹决策

    @Override
    public void decide(Boss boss) {
        if (System.currentTimeMillis() - lastDecisionTime < reactionDelay) return;

        updatePerception(boss);

        // === 优先级 1: 生存 ===
        // 致命威胁——紧急闪避
        if (hasCriticalThreat()) {
            executeEmergencyDodge(boss);
            return;
        }

        // 低血量——寻找掩体
        if (boss.getHp() < boss.getMaxHp() * 0.25 && useCoverSeeking) {
            seekCover(boss);
            // 掩体后仍可射击
            if (hasLineOfSight(boss, enemy)) {
                attemptPrecisionShot(boss);
            }
            return;
        }

        // === 优先级 2: 战术机动 ===
        // 计算危险地图
        if (useDangerMap) {
            dangerMap.compute(boss, allBullets, mapW, mapH);
        }

        // 综合 Utility AI 决策
        AIAction bestAction = utilityDecide(boss);  // 使用第四节描述的 Utility 系统
        executeAction(bestAction, boss);

        // === 优先级 3: 资源管理 ===
        if (smartReload && shouldReload(boss)) {
            boss.reload();
            // 换弹时向掩体移动
            seekCover(boss);
        }
    }

    private boolean hasCriticalThreat() {
        return perception.incomingBullets.stream()
            .anyMatch(t -> t.timeToImpact < 0.3 && t.dangerLevel > 0.7);
    }

    private void executeEmergencyDodge(Boss boss) {
        if (useDangerMap) {
            // 找最安全的方向
            Point safest = dangerMap.findSafestCell(boss.getX(), boss.getY(), dangerMapRadius);
            moveTowards(boss, safest);
        } else {
            // 退化为垂直接近
            BulletThreat worst = perception.getWorstThreat();
            dodgeBullet(worst.bullet, boss);
        }
    }

    private void seekCover(Boss boss) {
        // 找最近的掩体（在墙壁后方，且能射击到敌人）
        Point bestCover = findBestCover(boss, enemy, wallGrid);
        if (bestCover != null) {
            List<Point> path = pathfinder.findPath(
                boss.getX(), boss.getY(), bestCover.x, bestCover.y, wallGrid);
            followPath(path);
        }
    }

    // 智能换弹：弹匣 < 25% 且不在交火中 → 换弹
    private boolean shouldReload(Boss boss) {
        double ammoRatio = (double) boss.getAmmo() / boss.getMaxAmmo();
        double dist = distance(boss, enemy);
        return ammoRatio < 0.25 && dist > 150; // 距离足够安全
    }
}
```

---

## 六、核心算法时间复杂度分析

| 算法 | 时间复杂度 | 调用频率 | 适用难度 |
|------|-----------|----------|----------|
| A* 路径规划 | O(N log N)，N = 网格数 | 每 100-500ms 一次 | 困难、超级 |
| 势场法 | O(R²)，R = 感知半径 | 每帧 | 超级 |
| 线性预判 | O(1) | 每次射击前 | 困难、超级 |
| 二次方程解算 | O(1) | 每次射击前 | 超级 |
| Danger Map | O(B × F)，B = 子弹数，F = 预判帧数 | 每帧 | 超级 |
| Utility 评分 | O(A × C)，A = 行为数，C = 评分轴数 | 每 5-10 帧 | 所有难度（参数不同） |

对于本项目的地图规模（~800×600 像素 → 40×30 网格），A* 的 N ≈ 1200，每次规划约 0.5-2ms，完全在帧预算（16ms）内。Danger Map 的 B 通常不超过 20，F=30，计算量约 600 次，同样在毫秒级。

---

## 七、Java 类结构建议

```
src/thread/ai/
├── AIController.java       # AI 控制器接口
├── EasyAI.java             # 简单难度实现
├── HardAI.java             # 困难难度实现
├── SuperAI.java            # 超级难度实现
│
├── perception/
│   ├── AIPerception.java   # 感知数据结构
│   └── ThreatDetector.java # 威胁检测器（子弹追踪、开火检测）
│
├── decision/
│   ├── UtilityAI.java      # Utility 系统核心（评分+选择）
│   ├── AIAction.java       # 候选行为枚举
│   ├── Consideration.java  # 评分轴接口
│   └── considerations/     # 具体评分轴实现
│       ├── HealthScore.java
│       ├── AmmoScore.java
│       ├── ThreatScore.java
│       ├── ProximityScore.java
│       └── CoverScore.java
│
├── movement/
│   ├── AStarPathfinder.java # A* 路径规划器
│   ├── PotentialField.java  # 势场法避障
│   └── DangerMap.java       # 危险地图（子弹预测）
│
└── combat/
    ├── AimCalculator.java   # 瞄准算法（线性预判 / 二次解算）
    └── DodgeExecutor.java   # 闪避执行器
```

---

## 八、渐进式开发建议

不需要一次性实现所有功能。建议按以下阶段递进开发：

| 阶段 | 内容 | 验证标准 |
|------|------|----------|
| **阶段 1** | 实现 AI 基础框架（`AIController` 接口 + `EasyAI` 随机漫游+固定射击） | AI 坦克能在地图中移动并发射子弹 |
| **阶段 2** | 添加 A* 路径规划 + 简单的"朝敌人前进"行为 | AI 能穿过墙壁迷宫找到玩家 |
| **阶段 3** | 添加线性预判瞄准 + 开火检测闪避 | AI 能在移动中命中玩家，检测到子弹时闪避 |
| **阶段 4** | 实现 Utility AI 决策系统（多行为评分选择） | AI 在"追击/射击/撤退/换弹"间平滑切换 |
| **阶段 5** | 添加 Danger Map + 二次方程解算（SuperAI 独有） | AI 在密集子弹中主动寻找安全位置 |
| **阶段 6** | 参数调优 + 三种难度的差异化调整 | 简单 AI 让新手有体验感，超级 AI 具有高胜率 |

---

## 九、参考资料

### 算法与理论基础
- **A* 寻路**：经典的启发式搜索算法，使用 `PriorityQueue` 的 Java 实现参考 Caltech CS11 Lab（courses.cms.caltech.edu）
- **势场法（Potential Fields）**：源自机器人路径规划，在游戏 AI 中广泛用于自然移动轨迹和动态避障
- **Utility AI 系统**：Dave Mark 在 GDC 的系列演讲（"Improving AI Decision Modeling Through Utility Theory"），XCOM: Enemy Unknown 的实际应用案例
- **Robocode 社区**：Java 坦克 AI 的完整实践体系，包含雷达锁定、能量检测、Wave Surfing、GuessFactor Targeting 等技术。权威参考：《The Book of Robocode》（github.com/robocode-dev/book-of-robocode）

### 射击预判
- **线性预判（Iterative Intercept）**：Joe Hocking 的实现（github.com/jhocking/MovingTargetAiming），通过迭代收敛逼近命中点
- **二次方程精确解**：对匀速目标的最优解，当 discriminant < 0 时退化到线性预判（Godot Forum: "predictive aim algorithm"）

### 子弹躲避
- **危险地图（Danger Map / Influence Map）**：将子弹未来轨迹投影到网格上计算综合危险值，用 A* 寻路到最安全格子
- **垂直接近躲避**：最轻量级方案，计算自身到子弹轨迹线的垂足方向逃离

---

*文档版本: v1.0*
*编写日期: 2026年7月*

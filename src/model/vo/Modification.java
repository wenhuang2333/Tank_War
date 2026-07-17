package model.vo;

public class Modification {
    public enum Type {
        ANTI_FRIENDLY_FIRE(5, "反斜钢甲"),
        INSTANT_TURN(6, "扭绞轮台"),
        EXPLOSION_PROOF(7, "防爆油箱"),
        LIGHT_MACHINE_GUN(3, "轻机枪"),
        HEAVY_MACHINE_GUN(4, "重机枪"),
        LASER_CANNON(6, "激光炮"),
        DENSE_LASER(7, "高密度镭射群炮"),
        AUTO_LOADER(7, "自动装填器"),
        EXTRA_AMMO(4, "额外弹药架");

        private final int rarity;
        private final String chineseName;

        Type(int rarity, String chineseName) {
            this.rarity = rarity;
            this.chineseName = chineseName;
        }

        public int getRarity() { return rarity; }
        public String getChineseName() { return chineseName; }
    }

    private Type type;
    private boolean universal;

    public Modification() {}

    public Modification(Type type, boolean universal) {
        this.type = type;
        this.universal = universal;
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public boolean isUniversal() { return universal; }
    public void setUniversal(boolean universal) { this.universal = universal; }

    public static boolean isWeapon(Type type) {
        return type == Type.LIGHT_MACHINE_GUN || type == Type.HEAVY_MACHINE_GUN
            || type == Type.LASER_CANNON || type == Type.DENSE_LASER;
    }

    public static boolean isWeaponConflict(Type a, Type b) {
        return isWeapon(a) && isWeapon(b);
    }
}

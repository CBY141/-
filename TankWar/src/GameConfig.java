public class GameConfig {
    // 窗口设置
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final String WINDOW_TITLE = "像素坦克大战 - 精致版";

    // 地图设置
    public static final int TILE_SIZE = 16;  // 从20改为16，更精细
    public static final int MAP_WIDTH = 50;  // 增加地图宽度
    public static final int MAP_HEIGHT = 37; // 增加地图高度

    // 坦克设置
    public static final int TANK_WIDTH = 20;  // 坦克宽度缩小
    public static final int TANK_HEIGHT = 20; // 坦克高度缩小
    public static final int TANK_TURRET_LENGTH = 12; // 炮管长度

    // 游戏设置
    public static final int PLAYER_LIVES = 3;
    public static final int ENEMY_COUNT = 10;
    public static final int PLAYER_SPEED = 2;  // 速度适当调整
    public static final int ENEMY_SPEED = 1;
    public static final int BULLET_SPEED = 6;

    // 子弹设置
    public static final int BULLET_SIZE = 3;  // 子弹更小

    // 地图元素类型
    public static final int TILE_EMPTY = 0;
    public static final int TILE_BRICK = 1;
    public static final int TILE_STEEL = 2;
    public static final int TILE_GRASS = 3;
    public static final int TILE_WATER = 4;

    // 方向
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    // 颜色定义
    public static final java.awt.Color PLAYER_TANK_COLOR = new java.awt.Color(0, 180, 0);     // 玩家绿色
    public static final java.awt.Color ENEMY_TANK_COLOR = new java.awt.Color(220, 0, 0);      // 敌人红色
    public static final java.awt.Color TANK_TURRET_COLOR = new java.awt.Color(100, 100, 100); // 炮管灰色
    public static final java.awt.Color TANK_DETAIL_COLOR = new java.awt.Color(60, 60, 60);    // 细节深灰
}
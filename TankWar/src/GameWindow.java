import javax.swing.*;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;

    public GameWindow() {
        setTitle(GameConfig.WINDOW_TITLE);
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        add(gamePanel);

        addKeyListener(gamePanel);
        addMouseListener(gamePanel);
        addMouseMotionListener(gamePanel);

        setFocusable(true);
        requestFocus();
        setVisible(true);

        startGameLoop();
    }

    private void startGameLoop() {
        new Thread(() -> {
            while (true) {
                gamePanel.updateGame();
                gamePanel.repaint();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
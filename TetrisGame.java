import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;


import javax.swing.JPanel;
import javax.swing.JFrame;

/**
 * @author Adarsh Varshney
 * Reference: Brendan Jones
 */
public class TetrisGame extends JFrame {
	
    private static final long serialVersionUID = 12345L;
    private static final long FRAME_TIME = 1000L / 50L;
    private static final int TYPE_COUNT = TileType.values().length;
    private BoardPanel board;
    private SidePanel side;
    private boolean isPaused;
    private boolean isNewGame;
    private boolean isGameOver;
    private int level;
    private int score;
    private Random random;
    private Clock logicTimer;
    private TileType currentType;
    private TileType nextType;
    private int currentCol;
    private int currentRow;
    private int currentRotation;
    private int dropCooldown;
    private float gameSpeed;

    private TetrisGame() {
        super("Adarsh's Tetris");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        
        this.board = new BoardPanel(this);
        this.side = new SidePanel(this);
        
        add(board, BorderLayout.CENTER);
        add(side, BorderLayout.EAST);
        
        addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent e) {
                                
                switch(e.getKeyCode()) {
                
                case KeyEvent.VK_S:
                    if(!isPaused && dropCooldown == 0) {
                        logicTimer.setCyclesPerSecond(25.0f);
                    }
                    break;
                    
                case KeyEvent.VK_A:
                    if(!isPaused && board.isValidAndEmpty(currentType, currentCol - 1, currentRow, currentRotation)) {
                        currentCol--;
                    }
                    break;
                    
                case KeyEvent.VK_D:
                    if(!isPaused && board.isValidAndEmpty(currentType, currentCol + 1, currentRow, currentRotation)) {
                        currentCol++;
                    }
                    break;
                    
                case KeyEvent.VK_Q:
                    if(!isPaused) {
                        rotatePiece((currentRotation == 0) ? 3 : currentRotation - 1);
                    }
                    break;
                
                case KeyEvent.VK_E:
                    if(!isPaused) {
                        rotatePiece((currentRotation == 3) ? 0 : currentRotation + 1);
                    }
                    break;
                    
                case KeyEvent.VK_P:
                    if(!isGameOver && !isNewGame) {
                        isPaused = !isPaused;
                        logicTimer.setPaused(isPaused);
                    }
                    break;
                
                case KeyEvent.VK_ENTER:
                    if(isGameOver || isNewGame) {
                        resetGame();
                    }
                    break;
                
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                
                switch(e.getKeyCode()) {
                
                case KeyEvent.VK_S:
                    logicTimer.setCyclesPerSecond(gameSpeed);
                    logicTimer.reset();
                    break;
                }
                
            }
            
        });
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void startGame() {
        this.random = new Random();
        this.isNewGame = true;
        this.gameSpeed = 1.0f;
        
        this.logicTimer = new Clock(gameSpeed);
        logicTimer.setPaused(true);
        
        while(true) {
            long start = System.nanoTime();
            
            logicTimer.update();
            
            if(logicTimer.hasElapsedCycle()) {
                updateGame();
            }
        
            if(dropCooldown > 0) {
                dropCooldown--;
            }
            
            renderGame();
            
            long delta = (System.nanoTime() - start) / 1000000L;
            if(delta < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - delta);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void updateGame() {
        if(board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
            currentRow++;
        } else {
            board.addPiece(currentType, currentCol, currentRow, currentRotation);
            
            int cleared = board.checkLines();
            if(cleared > 0) {
                score += 50 << cleared;
            }
            
            gameSpeed += 0.035f;
            logicTimer.setCyclesPerSecond(gameSpeed);
            logicTimer.reset();
            
            dropCooldown = 25;
            
            level = (int)(gameSpeed * 1.70f);
            
            spawnPiece();
        }		
    }
    
    private void renderGame() {
        board.repaint();
        side.repaint();
    }
    
    private void resetGame() {
        this.level = 1;
        this.score = 0;
        this.gameSpeed = 1.0f;
        this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
        this.isNewGame = false;
        this.isGameOver = false;		
        board.clear();
        logicTimer.reset();
        logicTimer.setCyclesPerSecond(gameSpeed);
        spawnPiece();
    }
        
    private void spawnPiece() {
        this.currentType = nextType;
        this.currentCol = currentType.getSpawnColumn();
        this.currentRow = currentType.getSpawnRow();
        this.currentRotation = 0;
        this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
        
        if(!board.isValidAndEmpty(currentType, currentCol, currentRow, currentRotation)) {
            this.isGameOver = true;
            logicTimer.setPaused(true);
        }		
    }

    private void rotatePiece(int newRotation) {
        int newColumn = currentCol;
        int newRow = currentRow;
        
        int left = currentType.getLeftInset(newRotation);
        int right = currentType.getRightInset(newRotation);
        int top = currentType.getTopInset(newRotation);
        int bottom = currentType.getBottomInset(newRotation);
        
        if(currentCol < -left) {
            newColumn -= currentCol - left;
        } else if(currentCol + currentType.getDimension() - right >= BoardPanel.COL_COUNT) {
            newColumn -= (currentCol + currentType.getDimension() - right) - BoardPanel.COL_COUNT + 1;
        }
        
        if(currentRow < -top) {
            newRow -= currentRow - top;
        } else if(currentRow + currentType.getDimension() - bottom >= BoardPanel.ROW_COUNT) {
            newRow -= (currentRow + currentType.getDimension() - bottom) - BoardPanel.ROW_COUNT + 1;
        }
        
        if(board.isValidAndEmpty(currentType, newColumn, newRow, newRotation)) {
            currentRotation = newRotation;
            currentRow = newRow;
            currentCol = newColumn;
        }
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public boolean isGameOver() {
        return isGameOver;
    }
    
    public boolean isNewGame() {
        return isNewGame;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getLevel() {
        return level;
    }
    
    public TileType getPieceType() {
        return currentType;
    }
    
    public TileType getNextPieceType() {
        return nextType;
    }
    
    public int getPieceCol() {
        return currentCol;
    }
    
    public int getPieceRow() {
        return currentRow;
    }
    
    public int getPieceRotation() {
        return currentRotation;
    }

    public static void main(String[] args) {
        TetrisGame TetrisGame = new TetrisGame();
        TetrisGame.startGame();
    }

    public class BoardPanel extends JPanel {

        private static final long serialVersionUID = 12345L;
        public static final int COLOR_MIN = 35;
        public static final int COLOR_MAX = 255 - COLOR_MIN;
        private static final int BORDER_WIDTH = 5;
        public static final int COL_COUNT = 10;
        private static final int VISIBLE_ROW_COUNT = 20;
        private static final int HIDDEN_ROW_COUNT = 2;
        public static final int ROW_COUNT = VISIBLE_ROW_COUNT + HIDDEN_ROW_COUNT;
        public static final int TILE_SIZE = 24;
        public static final int SHADE_WIDTH = 4;
        private static final int CENTER_X = COL_COUNT * TILE_SIZE / 2;
        private static final int CENTER_Y = VISIBLE_ROW_COUNT * TILE_SIZE / 2;
        public static final int PANEL_WIDTH = COL_COUNT * TILE_SIZE + BORDER_WIDTH * 2;
        public static final int PANEL_HEIGHT = VISIBLE_ROW_COUNT * TILE_SIZE + BORDER_WIDTH * 2;
        private static final Font LARGE_FONT = new Font("Tahoma", Font.BOLD, 16);
        private static final Font SMALL_FONT = new Font("Tahoma", Font.BOLD, 12);
        private TetrisGame TetrisGame;
        private TileType[][] tiles;
        public BoardPanel(TetrisGame TetrisGame) {
            this.TetrisGame = TetrisGame;
            this.tiles = new TileType[ROW_COUNT][COL_COUNT];
            
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setBackground(Color.BLACK);
        }
        
        public void clear() {
            for(int i = 0; i < ROW_COUNT; i++) {
                for(int j = 0; j < COL_COUNT; j++) {
                    tiles[i][j] = null;
                }
            }
        }
        
        public boolean isValidAndEmpty(TileType type, int x, int y, int rotation) {
                    
            if(x < -type.getLeftInset(rotation) || x + type.getDimension() - type.getRightInset(rotation) >= COL_COUNT) {
                return false;
            }
            
            if(y < -type.getTopInset(rotation) || y + type.getDimension() - type.getBottomInset(rotation) >= ROW_COUNT) {
                return false;
            }
            
            for(int col = 0; col < type.getDimension(); col++) {
                for(int row = 0; row < type.getDimension(); row++) {
                    if(type.isTile(col, row, rotation) && isOccupied(x + col, y + row)) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        public void addPiece(TileType type, int x, int y, int rotation) {
            for(int col = 0; col < type.getDimension(); col++) {
                for(int row = 0; row < type.getDimension(); row++) {
                    if(type.isTile(col, row, rotation)) {
                        setTile(col + x, row + y, type);
                    }
                }
            }
        }
        
        public int checkLines() {
            int completedLines = 0;
            
            for(int row = 0; row < ROW_COUNT; row++) {
                if(checkLine(row)) {
                    completedLines++;
                }
            }
            return completedLines;
        }
                
        private boolean checkLine(int line) {
            for(int col = 0; col < COL_COUNT; col++) {
                if(!isOccupied(col, line)) {
                    return false;
                }
            }
            
            for(int row = line - 1; row >= 0; row--) {
                for(int col = 0; col < COL_COUNT; col++) {
                    setTile(col, row + 1, getTile(col, row));
                }
            }
            return true;
        }
        
        
        private boolean isOccupied(int x, int y) {
            return tiles[y][x] != null;
        }
        
        private void setTile(int  x, int y, TileType type) {
            tiles[y][x] = type;
        }
            
        private TileType getTile(int x, int y) {
            return tiles[y][x];
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.translate(BORDER_WIDTH, BORDER_WIDTH);
            
            if(TetrisGame.isPaused()) {
                g.setFont(LARGE_FONT);
                g.setColor(Color.WHITE);
                String msg = "PAUSED";
                g.drawString(msg, CENTER_X - g.getFontMetrics().stringWidth(msg) / 2, CENTER_Y);
            } else if(TetrisGame.isNewGame() || TetrisGame.isGameOver()) {
                g.setFont(LARGE_FONT);
                g.setColor(Color.WHITE);
                
                String msg = TetrisGame.isNewGame() ? "Tetris" : "GAME OVER";
                g.drawString(msg, CENTER_X - g.getFontMetrics().stringWidth(msg) / 2, 150);
                g.setFont(SMALL_FONT);
                msg = "Press Enter to Play" + (TetrisGame.isNewGame() ? "" : " Again");
                g.drawString(msg, CENTER_X - g.getFontMetrics().stringWidth(msg) / 2, 300);
            } else {
                
                for(int x = 0; x < COL_COUNT; x++) {
                    for(int y = HIDDEN_ROW_COUNT; y < ROW_COUNT; y++) {
                        TileType tile = getTile(x, y);
                        if(tile != null) {
                            drawTile(tile, x * TILE_SIZE, (y - HIDDEN_ROW_COUNT) * TILE_SIZE, g);
                        }
                    }
                }
                
                TileType type = TetrisGame.getPieceType();
                int pieceCol = TetrisGame.getPieceCol();
                int pieceRow = TetrisGame.getPieceRow();
                int rotation = TetrisGame.getPieceRotation();
                
                for(int col = 0; col < type.getDimension(); col++) {
                    for(int row = 0; row < type.getDimension(); row++) {
                        if(pieceRow + row >= 2 && type.isTile(col, row, rotation)) {
                            drawTile(type, (pieceCol + col) * TILE_SIZE, (pieceRow + row - HIDDEN_ROW_COUNT) * TILE_SIZE, g);
                        }
                    }
                }
                
                Color base = type.getBaseColor();
                base = new Color(base.getRed(), base.getGreen(), base.getBlue(), 20);
                for(int lowest = pieceRow; lowest < ROW_COUNT; lowest++) {
                    if(isValidAndEmpty(type, pieceCol, lowest, rotation)) {					
                        continue;
                    }
                    
                    lowest--;
                    
                    for(int col = 0; col < type.getDimension(); col++) {
                        for(int row = 0; row < type.getDimension(); row++) {
                            if(lowest + row >= 2 && type.isTile(col, row, rotation)) {
                                drawTile(base, base.brighter(), base.darker(), (pieceCol + col) * TILE_SIZE, (lowest + row - HIDDEN_ROW_COUNT) * TILE_SIZE, g);
                            }
                        }
                    }
                    
                    break;
                }
                
                g.setColor(Color.DARK_GRAY);
                for(int x = 0; x < COL_COUNT; x++) {
                    for(int y = 0; y < VISIBLE_ROW_COUNT; y++) {
                        g.drawLine(0, y * TILE_SIZE, COL_COUNT * TILE_SIZE, y * TILE_SIZE);
                        g.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, VISIBLE_ROW_COUNT * TILE_SIZE);
                    }
                }
            }
            
            g.setColor(Color.WHITE);
            g.drawRect(0, 0, TILE_SIZE * COL_COUNT, TILE_SIZE * VISIBLE_ROW_COUNT);
        }
        
        private void drawTile(TileType type, int x, int y, Graphics g) {
            drawTile(type.getBaseColor(), type.getLightColor(), type.getDarkColor(), x, y, g);
        }
        
        private void drawTile(Color base, Color light, Color dark, int x, int y, Graphics g) {
            
            g.setColor(base);
            g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            
            g.setColor(dark);
            g.fillRect(x, y + TILE_SIZE - SHADE_WIDTH, TILE_SIZE, SHADE_WIDTH);
            g.fillRect(x + TILE_SIZE - SHADE_WIDTH, y, SHADE_WIDTH, TILE_SIZE);
            
            g.setColor(light);
            for(int i = 0; i < SHADE_WIDTH; i++) {
                g.drawLine(x, y + i, x + TILE_SIZE - i - 1, y + i);
                g.drawLine(x + i, y, x + i, y + TILE_SIZE - i - 1);
            }
        }
    }

    public class Clock {
	
        private float millisPerCycle;
        private long lastUpdate;
        private int elapsedCycles;
        private float excessCycles;
        private boolean isPaused;
    
        public Clock(float cyclesPerSecond) {
            setCyclesPerSecond(cyclesPerSecond);
            reset();
        }
    
        public void setCyclesPerSecond(float cyclesPerSecond) {
            this.millisPerCycle = (1.0f / cyclesPerSecond) * 1000;
        }
        
        public void reset() {
            this.elapsedCycles = 0;
            this.excessCycles = 0.0f;
            this.lastUpdate = getCurrentTime();
            this.isPaused = false;
        }
        
        public void update() {
            long currUpdate = getCurrentTime();
            float delta = (float)(currUpdate - lastUpdate) + excessCycles;
    
            if(!isPaused) {
                this.elapsedCycles += (int)Math.floor(delta / millisPerCycle);
                this.excessCycles = delta % millisPerCycle;
            }
            
            this.lastUpdate = currUpdate;
        }
        
        public void setPaused(boolean paused) {
            this.isPaused = paused;
        }
        
        public boolean isPaused() {
            return isPaused;
        }
        
        public boolean hasElapsedCycle() {
            if(elapsedCycles > 0) {
                this.elapsedCycles--;
                return true;
            }
            return false;
        }
        
        public boolean peekElapsedCycle() {
            return (elapsedCycles > 0);
        }
        
        private static final long getCurrentTime() {
            return (System.nanoTime() / 1000000L);
        }
    
    }

    public class SidePanel extends JPanel {
	
        private static final long serialVersionUID = 2181495598854992747L;
        private static final int TILE_SIZE = BoardPanel.TILE_SIZE >> 1;
        private static final int SHADE_WIDTH = BoardPanel.SHADE_WIDTH >> 1;
        private static final int TILE_COUNT = 5;
        private static final int SQUARE_CENTER_X = 130;
        private static final int SQUARE_CENTER_Y = 65;
        private static final int SQUARE_SIZE = (TILE_SIZE * TILE_COUNT >> 1);
        private static final int SMALL_INSET = 20;
        private static final int LARGE_INSET = 40;
        private static final int STATS_INSET = 125;
        private static final int CONTROLS_INSET = 210;
        private static final int TEXT_STRIDE = 25;
        private static final Font SMALL_FONT = new Font("Tahoma", Font.BOLD, 11);
        private static final Font LARGE_FONT = new Font("Tahoma", Font.BOLD, 13);
        private static final Color DRAW_COLOR = new Color(128, 192, 128);
        private TetrisGame TetrisGame;
        public SidePanel(TetrisGame TetrisGame) {
            this.TetrisGame = TetrisGame;
            
            setPreferredSize(new Dimension(200, BoardPanel.PANEL_HEIGHT));
            setBackground(Color.BLACK);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.setColor(DRAW_COLOR);
            
            int offset;
            
            g.setFont(LARGE_FONT);
            g.drawString("Stats", SMALL_INSET, offset = STATS_INSET);
            g.setFont(SMALL_FONT);
            g.drawString("Level: " + TetrisGame.getLevel(), LARGE_INSET, offset += TEXT_STRIDE);
            g.drawString("Score: " + TetrisGame.getScore(), LARGE_INSET, offset += TEXT_STRIDE);
            
            g.setFont(LARGE_FONT);
            g.drawString("Controls", SMALL_INSET, offset = CONTROLS_INSET);
            g.setFont(SMALL_FONT);
            g.drawString("A - Move Left", LARGE_INSET, offset += TEXT_STRIDE);
            g.drawString("D - Move Right", LARGE_INSET, offset += TEXT_STRIDE);
            g.drawString("Q - Rotate Anticlockwise", LARGE_INSET, offset += TEXT_STRIDE);
            g.drawString("E - Rotate Clockwise", LARGE_INSET, offset += TEXT_STRIDE);
            g.drawString("S - Drop", LARGE_INSET, offset += TEXT_STRIDE);
            g.drawString("P - Pause Game", LARGE_INSET, offset += TEXT_STRIDE);

            g.drawString("Designed By", getWidth() / 2 - g.getFontMetrics().stringWidth("Designed By") / 2,
            offset += 2*TEXT_STRIDE);
            g.setFont(LARGE_FONT);
            g.drawString("Adarsh Varshney", getWidth() / 2 - g.getFontMetrics().stringWidth("Adarsh Varshney") / 2, offset += TEXT_STRIDE);
            
            g.setFont(LARGE_FONT);
            g.drawString("Next Piece:", SMALL_INSET, 70);
            g.drawRect(SQUARE_CENTER_X - SQUARE_SIZE, SQUARE_CENTER_Y - SQUARE_SIZE, SQUARE_SIZE * 2, SQUARE_SIZE * 2);
            
            TileType type = TetrisGame.getNextPieceType();
            if(!TetrisGame.isGameOver() && type != null) {
                int cols = type.getCols();
                int rows = type.getRows();
                int dimension = type.getDimension();
            
                int startX = (SQUARE_CENTER_X - (cols * TILE_SIZE / 2));
                int startY = (SQUARE_CENTER_Y - (rows * TILE_SIZE / 2));
            
                int top = type.getTopInset(0);
                int left = type.getLeftInset(0);
            
                for(int row = 0; row < dimension; row++) {
                    for(int col = 0; col < dimension; col++) {
                        if(type.isTile(col, row, 0)) {
                            drawTile(type, startX + ((col - left) * TILE_SIZE), startY + ((row - top) * TILE_SIZE), g);
                        }
                    }
                }
            }
        }
        
        private void drawTile(TileType type, int x, int y, Graphics g) {
            g.setColor(type.getBaseColor());
            g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            g.setColor(type.getDarkColor());
            g.fillRect(x, y + TILE_SIZE - SHADE_WIDTH, TILE_SIZE, SHADE_WIDTH);
            g.fillRect(x + TILE_SIZE - SHADE_WIDTH, y, SHADE_WIDTH, TILE_SIZE);
            g.setColor(type.getLightColor());
            for(int i = 0; i < SHADE_WIDTH; i++) {
                g.drawLine(x, y + i, x + TILE_SIZE - i - 1, y + i);
                g.drawLine(x + i, y, x + i, y + TILE_SIZE - i - 1);
            }
        }
        
    }

    public enum TileType {

        TypeI(new Color(BoardPanel.COLOR_MIN, BoardPanel.COLOR_MAX, BoardPanel.COLOR_MAX), 4, 4, 1, new boolean[][] {
            {
                false,	false,	false,	false,
                true,	true,	true,	true,
                false,	false,	false,	false,
                false,	false,	false,	false,
            },
            {
                false,	false,	true,	false,
                false,	false,	true,	false,
                false,	false,	true,	false,
                false,	false,	true,	false,
            },
            {
                false,	false,	false,	false,
                false,	false,	false,	false,
                true,	true,	true,	true,
                false,	false,	false,	false,
            },
            {
                false,	true,	false,	false,
                false,	true,	false,	false,
                false,	true,	false,	false,
                false,	true,	false,	false,
            }
        }),
        
        TypeJ(new Color(BoardPanel.COLOR_MIN, BoardPanel.COLOR_MIN, BoardPanel.COLOR_MAX), 3, 3, 2, new boolean[][] {
            {
                true,	false,	false,
                true,	true,	true,
                false,	false,	false,
            },
            {
                false,	true,	true,
                false,	true,	false,
                false,	true,	false,
            },
            {
                false,	false,	false,
                true,	true,	true,
                false,	false,	true,
            },
            {
                false,	true,	false,
                false,	true,	false,
                true,	true,	false,
            }
        }),
        
        TypeL(new Color(BoardPanel.COLOR_MAX, 127, BoardPanel.COLOR_MIN), 3, 3, 2, new boolean[][] {
            {
                false,	false,	true,
                true,	true,	true,
                false,	false,	false,
            },
            {
                false,	true,	false,
                false,	true,	false,
                false,	true,	true,
            },
            {
                false,	false,	false,
                true,	true,	true,
                true,	false,	false,
            },
            {
                true,	true,	false,
                false,	true,	false,
                false,	true,	false,
            }
        }),
        
        TypeO(new Color(BoardPanel.COLOR_MAX, BoardPanel.COLOR_MAX, BoardPanel.COLOR_MIN), 2, 2, 2, new boolean[][] {
            {
                true,	true,
                true,	true,
            },
            {
                true,	true,
                true,	true,
            },
            {	
                true,	true,
                true,	true,
            },
            {
                true,	true,
                true,	true,
            }
        }),
        
        TypeS(new Color(BoardPanel.COLOR_MIN, BoardPanel.COLOR_MAX, BoardPanel.COLOR_MIN), 3, 3, 2, new boolean[][] {
            {
                false,	true,	true,
                true,	true,	false,
                false,	false,	false,
            },
            {
                false,	true,	false,
                false,	true,	true,
                false,	false,	true,
            },
            {
                false,	false,	false,
                false,	true,	true,
                true,	true,	false,
            },
            {
                true,	false,	false,
                true,	true,	false,
                false,	true,	false,
            }
        }),
        
        TypeT(new Color(128, BoardPanel.COLOR_MIN, 128), 3, 3, 2, new boolean[][] {
            {
                false,	true,	false,
                true,	true,	true,
                false,	false,	false,
            },
            {
                false,	true,	false,
                false,	true,	true,
                false,	true,	false,
            },
            {
                false,	false,	false,
                true,	true,	true,
                false,	true,	false,
            },
            {
                false,	true,	false,
                true,	true,	false,
                false,	true,	false,
            }
        }),

        TypeZ(new Color(BoardPanel.COLOR_MAX, BoardPanel.COLOR_MIN, BoardPanel.COLOR_MIN), 3, 3, 2, new boolean[][] {
            {
                true,	true,	false,
                false,	true,	true,
                false,	false,	false,
            },
            {
                false,	false,	true,
                false,	true,	true,
                false,	true,	false,
            },
            {
                false,	false,	false,
                true,	true,	false,
                false,	true,	true,
            },
            {
                false,	true,	false,
                true,	true,	false,
                true,	false,	false,
            }
        });
        private Color baseColor;
        private Color lightColor;
        private Color darkColor;
        private int spawnCol;
        private int spawnRow;
        private int dimension;
        private int rows;
        private int cols;
        private boolean[][] tiles;
        
        private TileType(Color color, int dimension, int cols, int rows, boolean[][] tiles) {
            this.baseColor = color;
            this.lightColor = color.brighter();
            this.darkColor = color.darker();
            this.dimension = dimension;
            this.tiles = tiles;
            this.cols = cols;
            this.rows = rows;
            
            this.spawnCol = 5 - (dimension >> 1);
            this.spawnRow = getTopInset(0);
        }
        
        public Color getBaseColor() {
            return baseColor;
        }
        
        public Color getLightColor() {
            return lightColor;
        }
        
        public Color getDarkColor() {
            return darkColor;
        }
        
        public int getDimension() {
            return dimension;
        }
        
        public int getSpawnColumn() {
            return spawnCol;
        }
        
        public int getSpawnRow() {
            return spawnRow;
        }
        
        public int getRows() {
            return rows;
        }
        
        public int getCols() {
            return cols;
        }
        
        public boolean isTile(int x, int y, int rotation) {
            return tiles[rotation][y * dimension + x];
        }
        
        public int getLeftInset(int rotation) {
            for(int x = 0; x < dimension; x++) {
                for(int y = 0; y < dimension; y++) {
                    if(isTile(x, y, rotation)) {
                        return x;
                    }
                }
            }
            return -1;
        }
        
        public int getRightInset(int rotation) {
            for(int x = dimension - 1; x >= 0; x--) {
                for(int y = 0; y < dimension; y++) {
                    if(isTile(x, y, rotation)) {
                        return dimension - x;
                    }
                }
            }
            return -1;
        }
        
        public int getTopInset(int rotation) {
            for(int y = 0; y < dimension; y++) {
                for(int x = 0; x < dimension; x++) {
                    if(isTile(x, y, rotation)) {
                        return y;
                    }
                }
            }
            return -1;
        }
        
        public int getBottomInset(int rotation) {
            for(int y = dimension - 1; y >= 0; y--) {
                for(int x = 0; x < dimension; x++) {
                    if(isTile(x, y, rotation)) {
                        return dimension - y;
                    }
                }
            }
            return -1;
        }
        
    }


}
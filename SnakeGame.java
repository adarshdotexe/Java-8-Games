import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Adarsh Varshney
 * Reference: Brendan Jones
 */
public class SnakeGame extends JFrame {

	private static final long serialVersionUID = 12345L;
	private static final long FRAME_TIME = 1000L / 50L;
	private static final int MIN_SNAKE_LENGTH = 5;
	private static final int MAX_DIRECTIONS = 3;
	private BoardPanel board;
	private SidePanel side;
	private Random random;
	private Clock logicTimer;
	private boolean isNewGame;
	private boolean isGameOver;
	private boolean isPaused;
	private LinkedList<Point> snake;
	private LinkedList<Direction> directions;
	private int score;
	private int fruitsEaten;
	private int nextFruitScore;

	private SnakeGame() {
		super("Adarsh's Snake Game");
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
				switch (e.getKeyCode()) {

					case KeyEvent.VK_W:
					case KeyEvent.VK_UP:
						if (!isPaused && !isGameOver) {
							if (directions.size() < MAX_DIRECTIONS) {
								Direction last = directions.peekLast();
								if (last != Direction.South && last != Direction.North) {
									directions.addLast(Direction.North);
								}
							}
						}
						break;

					case KeyEvent.VK_S:
					case KeyEvent.VK_DOWN:
						if (!isPaused && !isGameOver) {
							if (directions.size() < MAX_DIRECTIONS) {
								Direction last = directions.peekLast();
								if (last != Direction.North && last != Direction.South) {
									directions.addLast(Direction.South);
								}
							}
						}
						break;

					case KeyEvent.VK_A:
					case KeyEvent.VK_LEFT:
						if (!isPaused && !isGameOver) {
							if (directions.size() < MAX_DIRECTIONS) {
								Direction last = directions.peekLast();
								if (last != Direction.East && last != Direction.West) {
									directions.addLast(Direction.West);
								}
							}
						}
						break;

					case KeyEvent.VK_D:
					case KeyEvent.VK_RIGHT:
						if (!isPaused && !isGameOver) {
							if (directions.size() < MAX_DIRECTIONS) {
								Direction last = directions.peekLast();
								if (last != Direction.West && last != Direction.East) {
									directions.addLast(Direction.East);
								}
							}
						}
						break;

					case KeyEvent.VK_P:
						if (!isGameOver) {
							isPaused = !isPaused;
							logicTimer.setPaused(isPaused);
						}
						break;

					case KeyEvent.VK_ENTER:
						if (isNewGame || isGameOver) {
							resetGame();
						}
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
		this.snake = new LinkedList<>();
		this.directions = new LinkedList<>();
		this.logicTimer = new Clock(10.0f);
		this.isNewGame = true;

		logicTimer.setPaused(true);

		while (true) {
			long start = System.nanoTime();

			logicTimer.update();

			if (logicTimer.hasElapsedCycle()) {
				updateGame();
			}

			board.repaint();
			side.repaint();

			long delta = (System.nanoTime() - start) / 1000000L;
			if (delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updateGame() {
		TileType collision = updateSnake();

		if (collision == TileType.Fruit) {
			fruitsEaten++;
			score += nextFruitScore;
			logicTimer.setCyclesPerSecond(10.0f + fruitsEaten / 9);
			spawnFruit();
		} else if (collision == TileType.SnakeBody) {
			isGameOver = true;
			logicTimer.setPaused(true);
		} else if (nextFruitScore > 10) {
			nextFruitScore--;
		}
	}

	private TileType updateSnake() {

		Direction direction = directions.peekFirst();

		Point head = new Point(snake.peekFirst());
		switch (direction) {
			case North:
				head.y--;
				break;

			case South:
				head.y++;
				break;

			case West:
				head.x--;
				break;

			case East:
				head.x++;
				break;
		}

		if (head.x < 0) {
			head.x = BoardPanel.COL_COUNT - 1;
		} else if (head.x >= BoardPanel.COL_COUNT) {
			head.x = 0;
		} else if (head.y < 0) {
			head.y = BoardPanel.ROW_COUNT - 1;
		} else if (head.y >= BoardPanel.ROW_COUNT) {
			head.y = 0;
		}

		TileType old = board.getTile(head.x, head.y);
		if (old != TileType.Fruit && snake.size() > MIN_SNAKE_LENGTH) {
			Point tail = snake.removeLast();
			board.setTile(tail, null);
			old = board.getTile(head.x, head.y);
		}

		if (old != TileType.SnakeBody) {
			board.setTile(snake.peekFirst(), TileType.SnakeBody);
			snake.push(head);
			board.setTile(head, TileType.SnakeHead);
			if (directions.size() > 1) {
				directions.poll();
			}
		}

		return old;
	}

	private void resetGame() {
		this.score = 0;
		this.fruitsEaten = 0;

		this.isNewGame = false;
		this.isGameOver = false;

		Point head = new Point(BoardPanel.COL_COUNT / 2, BoardPanel.ROW_COUNT / 2);

		snake.clear();
		snake.add(head);

		board.clearBoard();
		board.setTile(head, TileType.SnakeHead);

		directions.clear();
		directions.add(Direction.North);

		logicTimer.reset();

		spawnFruit();
	}

	public boolean isNewGame() {
		return isNewGame;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public boolean isPaused() {
		return isPaused;
	}

	private void spawnFruit() {
		this.nextFruitScore = 100;

		int index = random.nextInt(BoardPanel.COL_COUNT * BoardPanel.ROW_COUNT - snake.size());

		int freeFound = -1;
		for (int x = 0; x < BoardPanel.COL_COUNT; x++) {
			for (int y = 0; y < BoardPanel.ROW_COUNT; y++) {
				TileType type = board.getTile(x, y);
				if (type == null || type == TileType.Fruit) {
					if (++freeFound == index) {
						board.setTile(x, y, TileType.Fruit);
						break;
					}
				}
			}
		}
	}

	public int getScore() {
		return score;
	}

	public int getFruitsEaten() {
		return fruitsEaten;
	}

	public int getNextFruitScore() {
		return nextFruitScore;
	}

	public Direction getDirection() {
		return directions.peek();
	}

	public static void main(String[] args) {
		SnakeGame snake = new SnakeGame();
		snake.startGame();
	}

	public static class SidePanel extends JPanel {

		private static final Font LARGE_FONT = new Font("Tahoma", Font.BOLD, 30);

		private static final Font MEDIUM_FONT = new Font("Tahoma", Font.BOLD, 20);

		private static final Font SMALL_FONT = new Font("Tahoma", Font.BOLD, 12);

		private SnakeGame game;

		public SidePanel(SnakeGame game) {
			this.game = game;

			setPreferredSize(new Dimension(300, BoardPanel.COL_COUNT * BoardPanel.TILE_SIZE));
			setBackground(Color.BLACK);
		}

		private static final int STATISTICS_OFFSET = 150;

		private static final int CONTROLS_OFFSET = 320;

		private static final int MESSAGE_STRIDE = 30;

		private static final int SMALL_OFFSET = 30;

		private static final int LARGE_OFFSET = 50;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.setColor(Color.WHITE);

			g.setFont(LARGE_FONT);
			g.drawString("Snake Game", getWidth() / 2 - g.getFontMetrics().stringWidth("Snake Game") / 2, 50);

			g.setFont(MEDIUM_FONT);
			g.drawString("Statistics", SMALL_OFFSET, STATISTICS_OFFSET);
			g.drawString("Controls", SMALL_OFFSET, CONTROLS_OFFSET);

			g.setFont(SMALL_FONT);

			int drawY = STATISTICS_OFFSET;

			g.drawString("Total Score: " + game.getScore(), LARGE_OFFSET, drawY += MESSAGE_STRIDE);
			g.drawString("Fruit Eaten: " + game.getFruitsEaten(), LARGE_OFFSET, drawY += MESSAGE_STRIDE);
			g.drawString("Fruit Score: " + game.getNextFruitScore(), LARGE_OFFSET, drawY += MESSAGE_STRIDE);

			drawY = CONTROLS_OFFSET;
			g.drawString("Move Up: W / Up Arrowkey", LARGE_OFFSET, drawY += MESSAGE_STRIDE);
			g.drawString("Move Down: S / Down Arrowkey", LARGE_OFFSET, drawY += MESSAGE_STRIDE);
			g.drawString("Move Left: A / Left Arrowkey", LARGE_OFFSET, drawY += MESSAGE_STRIDE);
			g.drawString("Move Right: D / Right Arrowkey", LARGE_OFFSET, drawY += MESSAGE_STRIDE);
			g.drawString("Pause Game: P", LARGE_OFFSET, drawY += MESSAGE_STRIDE);

			g.drawString("Designed By", getWidth() / 2 - g.getFontMetrics().stringWidth("Designed By") / 2,
					drawY += 80);
			g.setFont(MEDIUM_FONT);
			g.drawString("Adarsh Varshney", getWidth() / 2 - g.getFontMetrics().stringWidth("Adarsh Varshney") / 2,
					drawY += 25);
		}

	}

	public enum TileType {

		Fruit,

		SnakeHead,

		SnakeBody

	}

	public enum Direction {

		North,

		East,

		South,

		West

	}

	public static class Clock {

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
			float delta = (float) (currUpdate - lastUpdate) + excessCycles;

			if (!isPaused) {
				this.elapsedCycles += (int) Math.floor(delta / millisPerCycle);
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
			if (elapsedCycles > 0) {
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

	public static class BoardPanel extends JPanel {

		private static final long serialVersionUID = -54321L;

		public static final int COL_COUNT = 25;

		public static final int ROW_COUNT = 25;

		public static final int TILE_SIZE = 30;

		public static final float STROKE_SIZE = TILE_SIZE / 10;

		private static final int EYE_LARGE_INSET = TILE_SIZE / 3;

		private static final int EYE_SMALL_INSET = TILE_SIZE / 6;

		private static final int EYE_LENGTH = TILE_SIZE / 5;

		private static final Font FONT = new Font("Tahoma", Font.BOLD, 40);

		private SnakeGame game;

		private TileType[] tiles;

		public BoardPanel(SnakeGame game) {
			this.game = game;
			this.tiles = new TileType[ROW_COUNT * COL_COUNT];

			setPreferredSize(new Dimension(COL_COUNT * TILE_SIZE, ROW_COUNT * TILE_SIZE));
			setBackground(Color.BLACK);
		}

		public void clearBoard() {
			for (int i = 0; i < tiles.length; i++) {
				tiles[i] = null;
			}
		}

		public void setTile(Point point, TileType type) {
			setTile(point.x, point.y, type);
		}

		public void setTile(int x, int y, TileType type) {
			tiles[y * ROW_COUNT + x] = type;
		}

		public TileType getTile(int x, int y) {
			return tiles[y * ROW_COUNT + x];
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			for (int x = 0; x < COL_COUNT; x++) {
				for (int y = 0; y < ROW_COUNT; y++) {
					TileType type = getTile(x, y);
					if (type != null) {
						drawTile(x * TILE_SIZE, y * TILE_SIZE, type, g);
					}
				}
			}

			g.setColor(Color.DARK_GRAY);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			for (int x = 0; x < COL_COUNT; x++) {
				for (int y = 0; y < ROW_COUNT; y++) {
					g.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, getHeight());
					g.drawLine(0, y * TILE_SIZE, getWidth(), y * TILE_SIZE);
				}
			}

			if (game.isGameOver() || game.isNewGame() || game.isPaused()) {
				g.setColor(Color.WHITE);

				int centerX = getWidth() / 2;
				int centerY = getHeight() / 2;

				String largeMessage = null;
				String smallMessage = null;
				if (game.isNewGame()) {
					largeMessage = "Snake Game!";
					smallMessage = "Press Enter to Start";
				} else if (game.isGameOver()) {
					largeMessage = "Game Over!";
					smallMessage = "Press Enter to Restart";
				} else if (game.isPaused()) {
					largeMessage = "Paused";
					smallMessage = "Press P to Resume";
				}

				g.setFont(FONT);
				g.drawString(largeMessage, centerX - g.getFontMetrics().stringWidth(largeMessage) / 2, centerY - 50);
				g.drawString(smallMessage, centerX - g.getFontMetrics().stringWidth(smallMessage) / 2, centerY + 50);
			}
		}

		private void drawTile(int x, int y, TileType type, Graphics g) {
			switch (type) {

				case Fruit:
					g.setColor(Color.RED);
					g.fillOval(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
					break;

				case SnakeBody:
					g.setColor(Color.GREEN);
					g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
					break;

				case SnakeHead:
					g.setColor(Color.GREEN);
					g.fillRect(x, y, TILE_SIZE, TILE_SIZE);

					g.setColor(Color.BLACK);
					Graphics2D g2d = (Graphics2D) g;
					g2d.setStroke(new BasicStroke(STROKE_SIZE));

					switch (game.getDirection()) {
						case North: {
							int baseY = y + EYE_SMALL_INSET;
							g.drawLine(x + EYE_LARGE_INSET, baseY, x + EYE_LARGE_INSET, baseY + EYE_LENGTH);
							g.drawLine(x + TILE_SIZE - EYE_LARGE_INSET, baseY, x + TILE_SIZE - EYE_LARGE_INSET,
									baseY + EYE_LENGTH);
							break;
						}

						case South: {
							int baseY = y + TILE_SIZE - EYE_SMALL_INSET;
							g.drawLine(x + EYE_LARGE_INSET, baseY, x + EYE_LARGE_INSET, baseY - EYE_LENGTH);
							g.drawLine(x + TILE_SIZE - EYE_LARGE_INSET, baseY, x + TILE_SIZE - EYE_LARGE_INSET,
									baseY - EYE_LENGTH);
							break;
						}

						case West: {
							int baseX = x + EYE_SMALL_INSET;
							g.drawLine(baseX, y + EYE_LARGE_INSET, baseX + EYE_LENGTH, y + EYE_LARGE_INSET);
							g.drawLine(baseX, y + TILE_SIZE - EYE_LARGE_INSET, baseX + EYE_LENGTH,
									y + TILE_SIZE - EYE_LARGE_INSET);
							break;
						}

						case East: {
							int baseX = x + TILE_SIZE - EYE_SMALL_INSET;
							g.drawLine(baseX, y + EYE_LARGE_INSET, baseX - EYE_LENGTH, y + EYE_LARGE_INSET);
							g.drawLine(baseX, y + TILE_SIZE - EYE_LARGE_INSET, baseX - EYE_LENGTH,
									y + TILE_SIZE - EYE_LARGE_INSET);
							break;
						}

					}
					g2d.setStroke(new BasicStroke(1));
					break;
			}
		}
	}
}
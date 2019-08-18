package game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import game.GFG;

import org.gicentre.handy.HandyPresets;
import org.gicentre.handy.HandyRenderer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * todo algorithm to detect win / can't place
 * todo MODES: score, first to edge, play until full; skip if can't go, timing (like chess)
 * todo powerups in grid (+ can place over your squares / change enemy player color)
 * todo scores in corresponding corners
 * https://9gag.com/gag/aGZRpDG
 * https://i.pinimg.com/originals/3a/02/0f/3a020f3b700bbc682a870a7800cfeefb.jpg
 * @author micycle1
 * default mode: until full
 */

public final class OutSpread extends PApplet {

	public static void main(String[] args) {
		PApplet.main(OutSpread.class);
	}

	private final static int WIDTH = 1000, HEIGHT = 1000; // stage pixel dimensions
	private final static int gridWIDTH = PApplet.min(800, WIDTH), gridHEIGHT = PApplet.min(800, HEIGHT); // game grid pixel dimensions
	private final static int divisionsX = 10, divisionsY = 10; // grid divisions / logical dimensions

	private static int borderX = (WIDTH - gridWIDTH) / 2; // border between stage and grid
	private static int borderY = (HEIGHT - gridHEIGHT) / 2; // border between stage and grid

	private final static float divisionWidth = (float) gridWIDTH / divisionsX; // width of divisions
	private final static float divisionHeight = (float) gridHEIGHT / divisionsY; // height of divisions

	private static byte[][] array = new byte[divisionsX][divisionsY]; // player ownership for each division
	final static int maxRectDimension = 5; // both X and Y

	private static final String helpDescription = "Outspread your opponents.\n"
			+ "At your turn, you are given a rectangle of random dimension to extend your area. This must be placed ajoining your current area. The first to win (depending on gamemode) is the player to haved covered the most area once the area is filled.";

	private byte currentPlayer;
	private static byte players = 2; // player count (max 4)
	private final HashMap<Byte, ArrayList<Float[]>> outlineData = new HashMap<>(); // rectangle corners per player (for drawing outline)

	private final int[] playerColorHues = new int[]{30, 120, 210, 300};
	private int[] scores;
	private boolean[] firstMove;
	private final ArrayList<Integer> winners = new ArrayList<>();

	private int w, h; // player rectangle division dimensions
	private int mouseGridX, mouseGridY; // used for grid snapping
	protected HandyRenderer r;
	private PFont Luna;

	private PGraphics gridCache, playerRegionCache;
	private PImage endgameCache;
	private Button play, settings, help, back;

	private Stage stage;

	private final ArrayList<float[]> menuAnimation = new ArrayList<>();

	protected enum modes {
		MENU, GAME, SETTINGS, HELP, ENDGAME
	}
	private modes mode = modes.MENU;

	private enum gameModes {
		FULL
	}
	private final gameModes gameMode = gameModes.FULL;

	@Override
	public void settings() {
		size(WIDTH, HEIGHT, FX2D);
	}

	@Override
	public void setup() {
		surface.setTitle("OutSpread");

		frameRate(60);
		Luna = createFont("Luna.ttf", 12, true);
		colorMode(HSB, 360, 100, 100);

		r = HandyPresets.createWaterAndInk(this);
		r.setRoughness(1);

		reset(); // init

		gridCache = createGraphics(WIDTH, HEIGHT);
		renderGrid(gridCache);

		playerRegionCache = createGraphics(WIDTH, HEIGHT); // todo

		play = new Button(this, width / 2, 250, 300, 125, "Play");
		settings = new Button(this, width / 2, 450, 300, 125, "Settings");
		help = new Button(this, width / 2, 650, 300, 125, "Help");
		back = new Button(this, 200, 150, 100, 50, "Back", 24);
		back.setPosition(width - 100, height - 100);
		unregisterMethod("mouseEvent", back);

		stage = (Stage) ((Canvas) surface.getNative()).getScene().getWindow();
		stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				javafx.application.Platform.runLater(() -> {
					resizeEvent();
				});
			}
		});
		stage.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				resizeEvent();
				stage.setHeight((double) newValue);
			}
		});
		stage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				resizeEvent();
			}
		});

		final int width = round(random(divisionWidth, divisionWidth * maxRectDimension));
		final int height = round(random(divisionHeight, divisionHeight * maxRectDimension));
		// menuAnimation.add(new float[]{round(random(width, this.width - width)),
		// round(random(height, this.height - height)), width, height, frameCount}); // x,y,w,h
	}

	private void resizeEvent() {
		borderX = (int) ((stage.getWidth() - gridWIDTH) / 2);
		borderY = (int) ((stage.getHeight() - gridHEIGHT) / 2);
		play.setPositionX(width / 2);
		settings.setPositionX(width / 2);
		help.setPositionX(width / 2);
		if (mode == modes.GAME) {
			back.setPosition(width - 75, 50);
		} else {
			back.setPosition(width - 100, height - 100);
		}
		gridCache = createGraphics(WIDTH, HEIGHT);
		renderGrid(gridCache);
	}

	private void drawMenu() {

		if (frameCount % (30 + round(random(-5, 5))) == 0) {
			final int width = round(random(divisionWidth, divisionWidth * maxRectDimension));
			final int height = round(random(divisionHeight, divisionHeight * maxRectDimension));
			menuAnimation.add(
					new float[]{round(random(width, this.width - width)), round(random(height, this.height - height)),
							width, height, frameCount, map(noise(frameCount * 0.04f), 0, 1, 0, 360)}); // x,y,w,h, spawn time, hue
		}

		final Iterator<float[]> i = menuAnimation.iterator();
		while (i.hasNext()) {
			final float[] f = i.next();
			final int fc = (int) (frameCount - f[4]);
			if (fc == 180) {
				i.remove();
			} else {
				int opacity;
				if (fc % 180 >= (180 / 2)) {
					opacity = round(map(fc % 90, 0, 90, 255, 0));
				} else {
					opacity = round(map(fc % 90, 0, 90, 0, 255));
				}
				fill(f[5], 50, 100, opacity);
				r.setStrokeColour(color(f[5], 50, 30, opacity + 1));
				r.setSeed(10);
				r.rect(f[0], f[1], f[2], f[3]);
			}
		}

		textFont(Luna);
		fill(0);
		textAlign(CENTER, CENTER);
		textSize(48);
		text("Out-Spread", width / 2, 80); // title

		strokeWeight(5);
		r.setStrokeColour(0);
		r.setSeed(10);
		play.draw();
		settings.draw();
		help.draw();
	}

	@Override
	public void draw() {
		switch (mode) {
			case MENU :
				background(210, 20, 100);
				drawMenu();
				break;
			case SETTINGS :
				background(300, 20, 100);
				textSize(36);
				text("Settings", width / 2, 50);
				textSize(12);
				text("Window size?, game dimensions (both x & Y), max rect dimension, mode, player count, randomise starting player",
						500, 200);
				r.setSeed(10);
				back.draw();
				break;
			case HELP :
				background(300, 20, 100);
				textSize(36);
				text("Help", width / 2, 50);
				textSize(24);
				text(helpDescription, 400, 400);
				r.setSeed(10);
				back.draw();
				break;
			case GAME :
				background(playerColorHues[currentPlayer - 1], 20, 100);
				mouseGridX = constrain(round((mouseX - borderX - divisionWidth / 2) / divisionWidth), 0,
						divisionsX - w);
				mouseGridY = constrain(round((mouseY - borderY - divisionHeight / 2) / divisionHeight), 0,
						divisionsY - h);
				textFont(Luna);
				textAlign(CENTER, CENTER);
				textSize(24);
				text("Out-Spread", width / 2, borderY / 2); // title

				image(gridCache, borderX, borderY);
				rectMode(CENTER);
				back.draw();
				rectMode(CORNER);
				drawPlayerRegions();
				// image(playerRegionCache, 0, 0); // todo
				drawLiveRegion();
				drawScores();
				break;
			case ENDGAME :
				background(255);

				tint(128, 255);
				image(endgameCache, 0, 0);
				noTint();

				textFont(Luna);
				textAlign(CENTER, CENTER);
				textSize(44);
				text("GAME OVER", width / 2, borderY / 2 + 100); // title

				textSize(56);

				if (winners.size() > 1) {
					text("DRAW", width / 2, borderY / 2 + 100);
				} else {
					textSize(56);
					fill(120, 100, 75); // green
					text("PLAYER " + winners.get(0) + " - WIN", width / 2, borderY / 2 + 250);
				}

				rectMode(CENTER);
				back.draw();
				rectMode(CORNER);
				fill(0, 0, 100);
				textSize(40);
				text("Scores", width / 2, borderY / 2 + 450);
				textSize(24);
				for (int i = 0; i < players; i++) {
					text("Player " + (i + 1) + " : " + scores[i], width / 2, borderY / 2 + 550 + i * 50);
				}
				break;
		}
	}

	private void renderGrid(PGraphics p) {
		p.beginDraw();
		p.colorMode(HSB, 360, 100, 100);
		p.noStroke();
		p.fill(0, 0, 100);
		p.rectMode(CORNER);
		p.rect(0, 0, gridWIDTH, gridHEIGHT); // bg

		p.stroke(0, 0, 40);
		p.strokeWeight(2);
		for (int i = 1; i <= divisionsX - 1; i++) {
			p.line(i * divisionWidth, 0, i * divisionWidth, gridHEIGHT); // verical
		}
		for (int i = 1; i <= divisionsY - 1; i++) {
			p.line(0, i * divisionHeight, gridWIDTH, i * divisionHeight); // vertical
		}

		p.noFill();
		p.strokeWeight(6);
		p.stroke(0);
		p.rect(3, 3, gridWIDTH, gridHEIGHT); // border
		p.endDraw();
	}

	private void drawLiveRegion() {
		fill(playerColorHues[currentPlayer - 1], 100, 100, 128); // not blocked but can't place

		check : {
			for (int x = mouseGridX; x < mouseGridX + w; x++) {
				for (int y = mouseGridY; y < mouseGridY + h; y++) {
					if (array[x][y] != 0) {
						fill(0, 0, 50, 128); // gray
						break check;
					}
				}
			}
			for (final Integer[] coord : borderCoords(mouseGridX, mouseGridY, w, h)) {
				if (array[coord[0]][coord[1]] == currentPlayer) {
					fill(playerColorHues[currentPlayer - 1], 80, 100); // can place
				}
			}
		}

		strokeWeight(3);
		r.rect(mouseGridX * divisionWidth + borderX, mouseGridY * divisionHeight + borderY, divisionWidth * w,
				divisionHeight * h);

		if (!firstMove[currentPlayer - 1]) { // if first move, draw ghost
			fill(playerColorHues[currentPlayer - 1], 100, 100, 255 - abs(frameCount * 4 % (2 * 255) - 255));
			switch (currentPlayer) {
				case 1 :
					r.rect(0 * divisionWidth + borderX, 0 * divisionHeight + borderY, divisionWidth * w,
							divisionHeight * h);
					break;
				case 2 :
					r.rect((divisionsX - w) * divisionWidth + borderX, (divisionsY - h) * divisionHeight + borderY,
							divisionWidth * w, divisionHeight * h);
					break;
				case 3 :
					r.rect(0 * divisionWidth + borderX, (divisionsY - h) * divisionHeight + borderY, divisionWidth * w,
							divisionHeight * h);
					break;
				case 4 :
					r.rect((divisionsX - w) * divisionWidth + borderX, 0 * divisionHeight + borderY, divisionWidth * w,
							divisionHeight * h);
					break;
			}
		}
	}

	private void drawPlayerRegions() {
		stroke(0, 0, 0);
		strokeWeight(4);
		noFill();
		textSize(20);
		textAlign(CENTER, CENTER);
		fill(255, 100, 100); // blue
		for (final byte player : outlineData.keySet()) {
			r.setSeed(0);
			for (final Float[] integers : outlineData.get(player)) {
				final float x = integers[0];
				final float y = integers[1];
				final int w = round(integers[2]);
				final int h = round(integers[3]);
				fill(playerColorHues[player - 1], 100, 100);
				r.rect(x + borderX, y + borderY, w * divisionWidth, h * divisionHeight);
				fill(0);
				text(w * h, x + divisionWidth * w * 0.5f + borderX, y + divisionHeight * h * 0.5f + borderY);
			}
		}
		textAlign(LEFT, TOP); // reset to default
	}

	private void drawScores() {
		fill(0);
		textSize(20);
		text("Scores", 10, 5);
		textFont(createFont("Courier", 16, true));
		for (int i = 0; i < players; i++) {
			text("Player " + (i + 1) + " : " + scores[i], 12, 40 + i * 20);
		}
	}

	private void reset() {
		mouseGridX = -1; // prevent
		currentPlayer = (byte) round(random(1, players));
		scores = new int[players];
		firstMove = new boolean[players];
		outlineData.clear();
		for (byte p = 1; p <= players; p++) {
			outlineData.put(p, new ArrayList<>());
		}
		array = new byte[divisionsY][divisionsX];
		w = round(random(1, maxRectDimension));
		h = round(random(1, maxRectDimension));
	}

	protected void changeMode(modes mode) {
		this.mode = mode;
		back.setPosition(width - 100, height - 100);
		switch (mode) {
			case MENU :
				registerMethod("mouseEvent", play);
				registerMethod("mouseEvent", settings);
				registerMethod("mouseEvent", help);
				unregisterMethod("mouseEvent", back);
				menuAnimation.clear();
				break;
			case GAME :
				reset();
				back.setPosition(width - 100, 50);
			case SETTINGS :
			case HELP :
				unregisterMethod("mouseEvent", play);
				unregisterMethod("mouseEvent", settings);
				unregisterMethod("mouseEvent", help);
				registerMethod("mouseEvent", back);
				break;
			case ENDGAME :
				back.setPosition(width - 100, 50);
				endgameCache = copy();
				endgameCache.filter(BLUR, 6);
				break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == LEFT) {
			if (mode == modes.GAME) {
				if (!firstMove[currentPlayer - 1]) { // first-move
					switch (currentPlayer) {
						case 1 :
							if (mouseGridX == 0 & mouseGridY == 0) {
								addRectangle();
							}
							break;
						case 2 :
							if (numberBetween(mouseGridX, divisionsX - w, divisionsX)
									&& numberBetween(mouseGridY, divisionsY - h, divisionsY)) {
								addRectangle();
							}
							break;
						case 3 :
							if (mouseGridX == 0 && numberBetween(mouseGridY, divisionsY - h, divisionsY)) {
								addRectangle();
							}
							break;
						case 4 :
							if (mouseGridY == 0 && numberBetween(mouseGridX, divisionsX - w, divisionsX)) {
								addRectangle();
							}
							break;
					}
				} else {
					// test not on top
					for (int x = mouseGridX; x < min(mouseGridX + w, divisionsX); x++) { // todo merge loops
						for (int y = mouseGridY; y < min(mouseGridY + h, divisionsY); y++) {
							if (array[x][y] != 0) {
								return; // break here if can't set
							}
						}
					}

					for (final Integer[] coord : borderCoords(mouseGridX, mouseGridY, w, h)) { // borders existing player rects?
						if (array[coord[0]][coord[1]] == currentPlayer) { // is touching
							addRectangle();
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (mode == modes.GAME) {
			switch (e.getKey()) {
				case ' ' :
					nextPlayer();
					break;
				default :
					break;
			}
		}
	}

	@Override
	public void mouseWheel(MouseEvent e) {
		// if (e.getCount() == -1) { // up
		// if (w < maxRectDimension && h > 1) {
		// w++;
		// h--;
		// scores[currentPlayer - 1]--;
		// }
		// } else { // down
		// if (h < maxRectDimension && w > 1) {
		// h++;
		// w--;
		// scores[currentPlayer - 1]--;
		// }
		// }
		final int temp = w;
		w = h;
		h = temp;
	}

	/**
	 * Valid position played
	 */
	private void addRectangle() {
		firstMove[currentPlayer - 1] = true;
		for (int x = mouseGridX; x < (mouseGridX) + w; x++) {
			for (int y = mouseGridY; y < (mouseGridY) + h; y++) {
				array[x][y] = currentPlayer;
			}
		}
		outlineData.get(currentPlayer)
				.add(new Float[]{mouseGridX * divisionWidth, mouseGridY * divisionHeight, (float) w, (float) h});
		scores[currentPlayer - 1] += w * h;

		if (!testEndGame()) { // check game-mode specific tests here (ie. is player on top)#
			nextPlayer();
		} else {
			changeMode(modes.ENDGAME);
		}

	}

	private void nextPlayer() {
		currentPlayer += 1;
		currentPlayer %= players + 1;
		if (currentPlayer == 0) {
			currentPlayer++;
		}
		// GFG.maxRectangle(R, C, A) todo
		w = round(random(1, maxRectDimension));
		h = round(random(1, maxRectDimension));
	}

	private boolean testEndGame() {
		switch (gameMode) {
			case FULL :
				for (int x = 0; x < array.length; x++) {
					for (int y = 0; y < array[x].length; y++) {
						if (array[x][y] == 0) {
							return false;
						}
					}
				}
				determineWinners();
				return true;
			default :
				return false;
		}
	}

	private void determineWinners() {
		int max_score = 0;
		final HashSet<Integer> players = new HashSet<>();
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > max_score) {
				players.clear();
				players.add(i + 1);
				max_score = scores[i];
			} else if (scores[i] == max_score) {
				players.add(i + 1);
			}
		}
		winners.clear();
		winners.addAll(players);
	}

	/**
	 * All parameter coordinates should be grid coordinates and not surface coordinates.
	 * @param cornerX x-coordinate of rectangle (top-left corner)
	 * @param cornerY y-coordinate of rectangle (top-left corner)
	 * @param w Width of rectangle
	 * @param h Height of rectangle
	 * @return List of [x,y] grid-coordinate pairs of squares that border the rectangle. 
	 */
	private static ArrayList<Integer[]> borderCoords(int cornerX, int cornerY, int w, int h) {
		final ArrayList<Integer[]> coords = new ArrayList<>();

		if (cornerY > 0) { // row above
			for (int x = cornerX; x < cornerX + w; x++) {
				coords.add(new Integer[]{x, cornerY - 1});
			}
		}
		if (cornerY + h <= divisionsY - 1) { // row below
			for (int x = cornerX; x < cornerX + w; x++) {
				coords.add(new Integer[]{x, cornerY + h});
			}
		}
		if (cornerX > 0) { // column left
			for (int y = cornerY; y < cornerY + h; y++) {
				coords.add(new Integer[]{cornerX - 1, y});
			}
		}
		if (cornerX + w <= divisionsX - 1) { // column right
			for (int y = cornerY; y < cornerY + h; y++) {
				coords.add(new Integer[]{cornerX + w, y});
			}
		}
		return coords;
	}

	private static boolean numberBetween(int n, int a1, int a2) {
		return (n >= Math.min(a1, a2) && n <= Math.max(a1, a2));
	}
}
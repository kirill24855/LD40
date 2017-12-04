package pro.shpin.kirill.ld40.model;

import pro.shpin.kirill.ld40.Sound;
import pro.shpin.kirill.ld40.view.Window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Game {

	private static final boolean DEBUG_MODE = false;

	public static float PORTAL_WIDTH;
	private static final float PORTAL_HEIGHT = 10f;

	public static final float PRESENT_WIDTH = 25f;
	public static final float PRESENT_HEIGHT = 25f;

	public static final float TOTAL_ANIM_TIME = 0.3f;

	private static final float DIFFICULTY = 0.125f;
	private static final float BONUS_CHANCE = 0.05f;
	private static final float BOMB_CHANCE = 0.1f;

	private List<Portal> portals;
	private List<Present> presents;

	private int selectedId;
	private float selectedX;

	private Sound teleportSound;
	private Sound explodeSound;
	private Sound bonusSound;

	private int width;
	private int height;

	private int mouseX;
	private int mouseY;

	private boolean leftButtonPressed = false;
	private boolean lastUpdateLeftButtonState = false;

	private boolean spacePressed = false;
	private boolean lastUpdateSpaceState = false;

	private Random rng;

	private float timeScale = 1f;

	private float presentInterval;
	private float timeUntilNextPresent = presentInterval;

	public boolean alive;

	public int score;

	public Game() {
		selectedId = -1;
		selectedX = 0;
	}

	public void init(Window window) {
		rng = new Random();

		width = window.width;
		height = window.height;

		teleportSound = new Sound("/sounds/8BitTeleport.wav");
		explodeSound = new Sound("/sounds/8BitExplode.wav");
		bonusSound = new Sound("/sounds/8BitBonus.wav");

		glfwSetCursorPosCallback(window.getHandle(), (windowHandle, posX, posY) -> {
				mouseX = (int) posX;
				mouseY = height - (int) posY;
		});
		glfwSetMouseButtonCallback(window.getHandle(), (windowHandle, button, action, mode) ->
				leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS
		);

		reinit(true);
	}

	private void reinit(boolean restartGame) {
		if (restartGame) {
			portals = new ArrayList<>();
			timeUntilNextPresent = presentInterval;
			alive = true;

			score = 0;

			presents = new ArrayList<>();
			presentInterval = 2f;
		}
		portals.add(new Portal(width/2f));

		PORTAL_WIDTH = width/(portals.size()+2);
		//presentInterval = 1f/(portals.size()+3)/DIFFICULTY;
	}

	public List<Portal> getPortals() {
		return portals;
	}

	public List<Present> getPresents() {
		return presents;
	}

	private void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

	public void updateInput(Window window) {
		spacePressed = window.isKeyPressed(GLFW_KEY_SPACE);
	}

	public void update(float interval) {
		processInput();
		if (!alive) return;

		updatePortals(interval);
		updatePresents(interval);
		spawnPresent(interval);

		lastUpdateLeftButtonState = leftButtonPressed;
		lastUpdateSpaceState = spacePressed;
	}

	private void spawnPresent(float interval) {
		timeUntilNextPresent -= interval*timeScale;
		if (timeUntilNextPresent <= 0) {
			timeUntilNextPresent = presentInterval;

			boolean isBomb, isBonus;
			float random = rng.nextFloat();
			if (random < BOMB_CHANCE) {
				isBomb = true;
				isBonus = false;
			} else if (random < BOMB_CHANCE + BONUS_CHANCE) {
				isBonus = true;
				isBomb = false;
			} else {
				isBomb = isBonus = false;
			}

			presents.add(new Present(rng.nextFloat()*(width-PRESENT_WIDTH), height-20f, isBomb, isBonus));
		}
	}

	private void updatePresents(float interval) {
		for (Iterator<Present> iterator = presents.iterator(); iterator.hasNext(); ) {
			Present present = iterator.next();

			// Move through the animation
			present.timeThroughAnim += interval;
			present.fall(timeScale * interval);

			// Die if normal present falls on the ground
			if (present.y < 0 && !present.isBomb && !present.isBonus && !DEBUG_MODE) {
				alive = false;
				selectedId = -1;
				playSound(explodeSound);
			}

			// Collision with portal
			if (present.y < PORTAL_HEIGHT) {
				if (!present.isPortable) continue;
				boolean inPortal = false;

				// Check collision with all present portals
				for (Portal portal : portals) {
					boolean inCurPortal = (present.x + PRESENT_WIDTH > portal.x && present.x < portal.x + PORTAL_WIDTH);
					if (inCurPortal) portal.timeThroughAnim = 0f;
					inPortal = inPortal || inCurPortal;
				}

				// Respective effect of the types of presents when in the portal
				if (inPortal) {
					if (present.isBomb && !DEBUG_MODE) { // If bomb -> death
						alive = false;
						playSound(explodeSound);
						return;
					} else if (present.isBonus) { // If bonus -> just like present but more score and not necessary to catch
						iterator.remove();
						playSound(bonusSound);
						score += 10;
					} else { // If normal present -> add to score, teleport it to the top
						present.x = rng.nextFloat() * (width - PRESENT_WIDTH);
						present.y = height - 20f;
						present.timeThroughAnim = 0f;
						playSound(teleportSound);
						score++;
					}
				} else present.isPortable = false; // Can no longer teleport if below the portal level but not in the portal
			} else if (present.y < -PRESENT_HEIGHT) iterator.remove(); // Remove redundancies in the list of presents
		}
	}

	private void processInput() {
		if (!alive) {
			if (spacePressed && !lastUpdateSpaceState) reinit(true);
		} else {
			if (spacePressed && !lastUpdateSpaceState) reinit(false);

			if (leftButtonPressed && !lastUpdateLeftButtonState) {
				for (Portal portal : portals) {
					if (mouseX > portal.x && mouseX < portal.x + PORTAL_WIDTH) {
						selectedId = portals.indexOf(portal);
						selectedX = mouseX - portal.x;
						break;
					}
				}
			} else if (!leftButtonPressed && lastUpdateLeftButtonState)
				selectedId = -1;
		}
	}

	private void updatePortals(float interval) {
		for (Portal portal : portals) {
			portal.timeThroughAnim += interval;
		}

		if (selectedId >= 0) {
			Portal curPortal = portals.get(selectedId);
			curPortal.x = mouseX - selectedX;
			if (curPortal.x < 0) curPortal.x = 0;
			if (curPortal.x > width - PORTAL_WIDTH) curPortal.x = width - PORTAL_WIDTH;
		}
	}

	public void cleanup() {}
}

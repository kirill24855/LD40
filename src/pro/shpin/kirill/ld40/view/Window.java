package pro.shpin.kirill.ld40.view;

import org.lwjgl.opengl.GL;
import pro.shpin.kirill.ld40.GLUtil;
import pro.shpin.kirill.ld40.model.Game;
import pro.shpin.kirill.ld40.model.Portal;
import pro.shpin.kirill.ld40.model.Present;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Window {

	private static final float SCORE_WIDTH = 50f;
	private static final float SCORE_HEIGHT = 100f;

	private static final float PORTAL_DRAW_HEIGHT = 50f;

	public int width;
	public int height;
	private String title;

	private long windowHandle;

	// Textures
	private int portalBaseTex;
	private int portalAnimTex;
	private int presentTex;
	private int bombTex;
	private int bonusTex;
	private int deathScreenTex;

	private int prevScore = -1;
	private List<Integer> scoreDigits; // Global scope so that there isn't a need to re-calculate this list every time when it stays the same

	public Window(String title, int width, int height) {
		this.width = width;
		this.height = height;
		this.title = title;
	}

	public void init() {
		glfwInit();

		windowHandle = glfwCreateWindow(width, height, title, 0, 0);
		glfwMakeContextCurrent(windowHandle);

		GL.createCapabilities();
		glClearColor(0.3f, 0.3f, 0.3f, 1f); // Background color

		// Texture init
		glEnable(GL_TEXTURE_2D);
		portalBaseTex = GLUtil.loadTexture("/textures/portalBaseWhite.png");
		portalAnimTex = GLUtil.loadTexture("/textures/portalAnimGreen.png");
		presentTex = GLUtil.loadTexture("/textures/presentCyanOrange.png");
		bombTex = GLUtil.loadTexture("/textures/bombDark.png");
		bonusTex = GLUtil.loadTexture("/textures/bonusYellowPurple.png");
		deathScreenTex = GLUtil.loadTexture("/textures/deathScreenBlue.png");

		// Below -- DO NOT TOUCH (will break)
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, 0, height, -1, 1);
		glMatrixMode(GL_MODELVIEW);
	}

	public long getHandle() {
		return windowHandle;
	}

	public void updateInput() {
		glfwPollEvents();
	}

	public void render(Game game) {
		if (game.alive) {
			glClear(GL_COLOR_BUFFER_BIT);

			drawPortals(game);
			drawPresents(game);
			drawScore(game);
		} else drawDeathScreen();

		glfwSwapBuffers(windowHandle);
	}

	private void drawScore(Game game) {
		if (game.score == prevScore) { // No need to split the score into digits again if it doesn't change
			GLUtil.drawNumber(
					scoreDigits,
					width- SCORE_WIDTH * scoreDigits.size(),
					height- SCORE_HEIGHT,
					SCORE_WIDTH,
					SCORE_HEIGHT
			);
			return;
		}

		scoreDigits = new ArrayList<>();
		int number = prevScore = game.score;

		// Obtain individual digits of the score
		if (number == 0) scoreDigits.add(0);
		else {
			while (number > 0) {
				scoreDigits.add(number % 10);
				number /= 10;
			}
		}

		GLUtil.drawNumber(
				scoreDigits,
				width- SCORE_WIDTH * scoreDigits.size(),
				height- SCORE_HEIGHT,
				SCORE_WIDTH,
				SCORE_HEIGHT
		);
	}

	private void drawDeathScreen() {
		GLUtil.texRectCenter(
				width/2f,
				height/2f,
				Math.min(width, height)/4f*3f,
				Math.min(width, height)/4f*3f,
				0,
				deathScreenTex,
				1f,
				1f,
				1f
		);
	}

	private void drawPresents(Game game) {
		for (Present present : game.getPresents()) {
			int curTex;
			if (present.isBomb) curTex = bombTex;
			else if (present.isBonus) curTex = bonusTex;
			else curTex = presentTex;

			float alpha;
			if (present.timeThroughAnim > Game.TOTAL_ANIM_TIME) alpha = 0f;
			else alpha = -4f*present.timeThroughAnim/Game.TOTAL_ANIM_TIME/Game.TOTAL_ANIM_TIME * (present.timeThroughAnim - Game.TOTAL_ANIM_TIME);

			// Draw present
			GLUtil.texRectCorner(
					present.x,
					present.y,
					Game.PRESENT_WIDTH,
					Game.PRESENT_HEIGHT,
					0f,
					curTex,
					1f,
					1f,
					1f
			);

			// Draw animation
			GLUtil.texRectCorner(
					present.x - Game.PRESENT_WIDTH/2f,
					height+12f,
					Game.PRESENT_WIDTH*2f,
					-Game.PRESENT_HEIGHT*3f,
					0f,
					portalAnimTex,
					alpha,
					1f,
					1f
			);
		}
	}

	private void drawPortals(Game game) {
		for (Portal portal : game.getPortals()) {
			float alpha;
			if (portal.timeThroughAnim > Game.TOTAL_ANIM_TIME) alpha = 0;
			else {
				alpha = -4f*portal.timeThroughAnim/Game.TOTAL_ANIM_TIME/Game.TOTAL_ANIM_TIME * (portal.timeThroughAnim - Game.TOTAL_ANIM_TIME);
				/*alpha += -64f*portal.timeThroughAnim/1.58f/Math.pow(Game.TOTAL_ANIM_TIME, 4) * (portal.timeThroughAnim - Game.TOTAL_ANIM_TIME/3f)
																							 * (portal.timeThroughAnim - 2f*Game.TOTAL_ANIM_TIME/3f)
																							 * (portal.timeThroughAnim - Game.TOTAL_ANIM_TIME);*/
			}

			// Draw portal
			GLUtil.texRectCorner(
					portal.x,
					0,
					Game.PORTAL_WIDTH,
					PORTAL_DRAW_HEIGHT,
					0f,
					portalBaseTex,
					1f,
					1f,
					1f
			);

			// Draw animation
			GLUtil.texRectCorner(
					portal.x,
					0,
					Game.PORTAL_WIDTH,
					PORTAL_DRAW_HEIGHT,
					0f,
					portalAnimTex,
					alpha,
					1f,
					1f
			);
		}
	}

	public boolean isKeyPressed(int keyCode) {
		return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(windowHandle);
	}
}

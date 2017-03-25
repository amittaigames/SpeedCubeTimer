package com.amittaigames.cubetimer;

import com.amittaigames.engine.CoreGame;
import com.amittaigames.engine.graphics.Font;
import com.amittaigames.engine.graphics.Render;
import com.amittaigames.engine.graphics.Window;
import com.amittaigames.engine.util.FileIO;
import com.amittaigames.engine.util.Keys;

import java.io.File;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class Main extends CoreGame {

	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	
	private Font timerFont;
	private Font stateFont;
	
	private File saveFile;
	private String saveFileLocation = System.getProperty("user.home") + "/.best_cube_time.txt";
	private float bestTime = 99 * 60 + 59.999f;
	
	private float startTime;
	private float currentTime;
	private String timeFormat = "%02d:%06.3f";
	
	enum State {
		PRIMED,
		RUNNING,
		STOPPED
	}
	
	private State state;
	
	private boolean spaceAfterRun = false;
	
	public static void main(String[] args) {
		Window.enable("anti_alias");
		Window.init("Speed Cube Timer - by Amittai Games", WIDTH, HEIGHT, false, new Main());
	}

	@Override
	public void init() {
		// Load main timer font
		Font.load("/data/fonts/Monospaced", 1, 1);
		timerFont = Font.get("Monospaced 1");
		timerFont.setColor(0, 0, 0);
		
		// Load state/best time font
		Font.load("/data/fonts/Monospaced", 2, 0.25f);
		stateFont = Font.get("Monospaced 2");
		stateFont.setColor(0, 0, 0);
		
		// Initialize state
		state = State.STOPPED;
		
		// Load/Initialize best time
		saveFile = new File(saveFileLocation);
		if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String bestTime = FileIO.readExternalFile(saveFileLocation);
		if (!bestTime.isEmpty())
			this.bestTime = Float.parseFloat(bestTime);
	}

	@Override
	public void render(Render render) {
		// Background
		render.clear(234, 234, 234);
		
		// Timer formatting math
		int min = (int) currentTime/60;
		float sec = currentTime - (min * 60);
		
		// Draw main timer
		render.drawText(String.format(timeFormat, min, sec), ((WIDTH / 2) - (469.0f/2.0f)), 
				((HEIGHT / 2) - (96.0f/2.0f)), timerFont); // width = 469, height = 96
		
		// Output timer state
		switch (state) {
			case STOPPED: {
				render.drawText("STOPPED", 5, 5, stateFont);
				break;
			}
			case PRIMED: {
				render.drawText("PRIMED", 5, 5, stateFont);
				break;
			}
			case RUNNING: {
				render.drawText("RUNNING", 5, 5, stateFont);
				break;
			}
		}
		
		// Output best time
		int b_min = (int) bestTime/60;
		float b_sec = bestTime - (b_min * 60);
		render.drawText(String.format("Best Time: " + timeFormat, b_min, b_sec), 5, 29, stateFont);
	}

	@Override
	public void update(float delta) {
		// Check state
		
		// When stopped, press space to prime the timer.
		// When the space key is then released, the timer
		// starts running. When the space key is again
		// pressed, the timer stops, and a flag is set
		// so the timer doesn't start back up again.
		// When the space key is then released, the
		// flag sets to allow the loop to begin again.
		
		switch (state) {
			case STOPPED: {
				if (!Window.isKeyDown(Keys.KEY_SPACE)) {
					spaceAfterRun = false;
				}
				if (Window.isKeyDown(Keys.KEY_SPACE) && !spaceAfterRun) {
					state = State.PRIMED;
				}
				break;
			}
			case PRIMED: {
				currentTime = 0;
				if (!Window.isKeyDown(Keys.KEY_SPACE)) {
					state = State.RUNNING;
					startTime = (float)glfwGetTime();
				}
				break;
			}
			case RUNNING: {
				currentTime = ((float)glfwGetTime() - startTime);
				if (Window.isKeyDown(Keys.KEY_SPACE)) {
					state = State.STOPPED;
					spaceAfterRun = true;
					if (currentTime < bestTime) {
						FileIO.writeExternalFile(saveFileLocation, currentTime + "");
						bestTime = currentTime;
					}
				}
				break;
			}
		}
	}

	@Override
	public void cleanUp() {
		
	}
	
}

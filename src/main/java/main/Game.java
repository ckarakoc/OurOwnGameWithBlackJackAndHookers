package main;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game implements Runnable {

	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;

	private long window;
	private boolean running;
	private Thread thread;

	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit()) {
			System.err.println("Could not initialize GLFW!");
			return;
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		window = glfwCreateWindow(WIDTH, HEIGHT, "OOG: BlackJack and Hookers", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// TODO: this needs to be in another class (keyhandlers)
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
		});


		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2, (vidmode.height() - HEIGHT) / 2);

		glfwMakeContextCurrent(window);
		GL.createCapabilities();

		// Enable v-sync
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		drawQuad();
		glfwSwapBuffers(window);
	}

	private void tick() {
		glfwPollEvents();
	}

	private void drawQuad() {
		glColor3f(0.0f, 0.0f, 1.0f);
		glBegin(GL_QUADS);
		{
			glVertex3f(-.5f, -.5f, 0.0f);
			glVertex3f(.5f, -.5f, 0.0f);
			glVertex3f(.5f, .5f, 0.0f);
			glVertex3f(-.5f, .5f, 0.0f);
		}
		glEnd();
	}

	private void loop() {
		long lastTime = System.nanoTime();
		double delta = 0.0;
		double ns = 1000000000.0 / 60.0;
		long timer = System.currentTimeMillis();
		int updates = 0;
		int frames = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1.0) {
				tick();
				updates++;
				delta--;
			}
			render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println(updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}
			if (glfwWindowShouldClose(window))
				running = false;
		}
	}

	@Override
	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		try {
			init();
			loop();

			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
		} finally {
			glfwTerminate();
			glfwSetErrorCallback(null).free();
		}

	}

	public void start() {
		running = true;
		thread = new Thread(this, "Game");
		thread.start();
	}

	public static void main(String[] args) {
		new Game().start();
	}
}

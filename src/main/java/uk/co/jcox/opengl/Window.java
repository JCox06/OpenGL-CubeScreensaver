package uk.co.jcox.opengl;

import kotlin.Function;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;

public class Window {

    private final String name;
    private int width;
    private int height;

    private long handle;


    public Window(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public boolean createGlContext() {
        if (! GLFW.glfwInit()) {
            return false;
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        this.handle = GLFW.glfwCreateWindow(this.width, this.height, this.name, 0, 0);


        GLFW.glfwSetFramebufferSizeCallback(this.handle, (win, w, h) -> resized(w, h));

        GLFW.glfwMakeContextCurrent(this.handle);


        if (this.handle == 0) {
            terminate();
            return false;
        }

        return true;
    }


    public void setKeyCallback(Consumer<Integer> keyCallback) {

        GLFW.glfwSetKeyCallback(this.handle, (winId, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                keyCallback.accept(key);
            }
        });
    }

    private void resized(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
    }

    public void runWindowUpdates() {
        GLFW.glfwSwapBuffers(this.handle);
        GLFW.glfwPollEvents();
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(this.handle);
    }


    public void terminate() {
        glfwFreeCallbacks(this.handle);
        GLFW.glfwTerminate();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public double getTimeElapsed() {
        return GLFW.glfwGetTime();
    }

    public boolean isPressed(int keyCode) {
        return GLFW.glfwGetKey(this.handle, keyCode) == GLFW.GLFW_PRESS;
    }


    public void setInfo(String info) {
        GLFW.glfwSetWindowTitle(this.handle, name + info);
    }


    public void exit() {
        GLFW.glfwSetWindowShouldClose(this.handle, true);
    }


    public void setClose() {
        GLFW.glfwSetWindowShouldClose(handle, true);
    }
}

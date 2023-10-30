package uk.co.jcox.opengl

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import java.io.IOException
import java.nio.IntBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

const val DEBUG_MODE = true

//OpenGL Rendering related
private val window: Window = Window("LWJGL Test", 1000, 1000)
private val renderer: Renderer = Renderer()
private val program: ShaderProgram = ShaderProgram()



private var refreshCubePositionsAccumulator: Double = 0.0
private const val refreshCubePositionsFrequency: Double = 0.01
private val cubePositions: MutableMap<Vector3f, ObjRepresentable> = mutableMapOf()

private val registeredCubes: MutableList<ObjRepresentable> = mutableListOf()

private val random: Random = Random()


fun main() {

    window.createGlContext()
    renderer.setup()

    renderer.setColour(0.001f, 0.01f, 0.01f)

    val vertShadSrc = readFile("data/shaders/default.vert")
    val fragShadSrc = readFile("data/shaders/default.frag")

    if (vertShadSrc == null || fragShadSrc == null) {
        throw RuntimeException("Could not open shaders file")
    }

    val vertex: ShaderProgram.ShaderInfo = ShaderProgram.ShaderInfo(ShaderProgram.ShaderType.VERTEX, vertShadSrc)
    val fragment: ShaderProgram.ShaderInfo = ShaderProgram.ShaderInfo(ShaderProgram.ShaderType.FRAGMENT, fragShadSrc)

    program.createProgram(vertex, fragment)

    println("Shaders compiled")


    val textureId = loadTexture("data/textures/texture.png")
    val cubeGeometry = GeometryBuilder2D.rectangle()

    registeredCubes.add(ObjRepresentable(Material(Vector3f(0.8f, 0.5f, 0.5f), textureId), cubeGeometry))
    registeredCubes.add(ObjRepresentable(Material(Vector3f(0.5f, 0.8f, 0.5f), textureId), cubeGeometry))
    registeredCubes.add(ObjRepresentable(Material(Vector3f(0.5f, 0.5f, 0.8f), textureId), cubeGeometry))
    registeredCubes.add(ObjRepresentable(Material(Vector3f(1.0f, 1.0f, 1.0f), textureId), cubeGeometry))
    registeredCubes.add(ObjRepresentable(Material(Vector3f(0.8f, 0.8f, 0.1f), textureId), cubeGeometry))

    repeat(2000) {
        cubePositions[getRandomPosition()] = registeredCubes.random()
    }

    runRenderLoop()
    window.terminate()
}

private fun runRenderLoop() {


    var lastFrameTime = 0.0
    var deltaTime: Double

    while (! window.shouldClose()) {

        val currentFrame: Double = window.timeElapsed
        deltaTime = currentFrame - lastFrameTime
        lastFrameTime = currentFrame

        renderer.clearBuffers()

        render()

        update(deltaTime)

        window.runWindowUpdates()
    }
}


private fun render() {

    //Update proj matrix every render as aspect ratio may change
    val projection: Matrix4f = Matrix4f().setPerspective(Math.toRadians(45.0).toFloat(), window.width.toFloat() / window.height, 0.01f, 1000.0f)

    val camX = sin(window.timeElapsed / 20)
    val camY = 0.0f
    val camZ = cos(window.timeElapsed / 20)

    val camera: Matrix4f = Matrix4f().setLookAt(camX.toFloat(), camY, camZ.toFloat(), 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)


    program.send("projection", projection)
    program.send("camera", camera)

    cubePositions.forEach {
        program.send("model", Matrix4f().translate(it.key))
        renderer.draw(it.value, program, window.width, window.height)
    }
}


private fun update(deltaTime: Double) {

    refreshCubePositionsAccumulator+= deltaTime

    if (refreshCubePositionsAccumulator >= refreshCubePositionsFrequency) {


        //Start removing cubes if the map gets larger than 4000
        if (cubePositions.size >= 4000) {
            cubePositions.remove(cubePositions.keys.random())
        }

        //Add random cube
        cubePositions[getRandomPosition()] = registeredCubes.random()

        refreshCubePositionsAccumulator = 0.0
    }
}


private fun getRandomPosition(): Vector3f {
    val x = random.nextFloat(-100f, 100f)
    val y = random.nextFloat(-100f, 100f)
    val z = random.nextFloat(-100f, 100f)

    return Vector3f(x, y, z)
}



private fun readFile(file: String): String? {
    try {
        val path = Paths.get(file)
        println(path.toAbsolutePath())
        return Files.readString(path)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}


private fun loadTexture(pngPath: String): Int {

    STBImage.stbi_set_flip_vertically_on_load(true)

    println("Loading texture: $pngPath")

    val widthBuff: IntBuffer = BufferUtils.createIntBuffer(1)
    val heightBuff: IntBuffer = BufferUtils.createIntBuffer(1)
    val nrChannels: IntBuffer = BufferUtils.createIntBuffer(1)

    val data = STBImage.stbi_load(pngPath, widthBuff, heightBuff, nrChannels, 4)

    val textureId = GL11.glGenTextures()
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)

    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, widthBuff.get(), heightBuff.get(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data)
    GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
    if (data != null) {
        STBImage.stbi_image_free(data)
    }

    println("Texture loading completed")

    return textureId
}
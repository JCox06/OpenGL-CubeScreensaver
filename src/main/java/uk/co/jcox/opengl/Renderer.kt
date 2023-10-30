package uk.co.jcox.opengl

import org.lwjgl.opengl.*


class Renderer {

    fun setup() {
        GL.createCapabilities()
        GLUtil.setupDebugMessageCallback()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }


    fun setColour(x: Float, y: Float, z: Float) {
        GL11.glClearColor(x, y, z, 1.0f)
    }

    fun clearBuffers() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun draw(objrep: ObjRepresentable, shader: ShaderProgram, screenX: Int, screenY: Int) {
        GL11.glViewport(0, 0, screenX, screenY)
        shader.bind()
        GL30.glBindVertexArray(objrep.geom.vaoId)
        if (DEBUG_MODE) {
            shader.validateProgram()
        }
        shader.send("diffuseColour", objrep.material.diffuseColour)
        GL15.glActiveTexture(GL15.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, objrep.material.textureId)
        shader.send("mainTexture1", 0)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36)
    }
}
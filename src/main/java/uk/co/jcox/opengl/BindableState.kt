package uk.co.jcox.opengl

interface BindableState {
    fun bind()

    fun unbind()

    fun destroy()
}
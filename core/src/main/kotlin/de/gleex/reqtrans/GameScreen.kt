package de.gleex.reqtrans

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.tommyettinger.colorful.ipt.ColorfulBatch
import com.github.tommyettinger.colorful.rgb.Palette
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.graphics.use

class GameScreen : KtxScreen {
    // TODO use AssetManager
    private val image = Texture(
        "spaceIcons/single/Retina/ship_C.png".toInternalFile(),
        true
    ).apply { setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) }
    private val batch = ColorfulBatch()

    private val viewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT)

    override fun render(delta: Float) {
        input()
        logic()
        draw()
    }

    private fun input() {
        // TODO handle input
    }

    private fun logic() {
        // TODO game logic
    }

    private fun draw() {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined
        batch.use {
            it.setColor(Palette.YELLOW)
            it.draw(image, 10f, 15f, 10f, 10f)
        }
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    companion object {
        private const val WORLD_WIDTH = 100f
        private const val WORLD_HEIGHT = 100f
    }
}

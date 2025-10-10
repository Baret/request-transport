package de.gleex.reqtrans

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.tommyettinger.colorful.rgb.ColorTools
import com.github.tommyettinger.colorful.rgb.ColorfulBatch
import com.github.tommyettinger.colorful.rgb.ColorfulSprite
import com.github.tommyettinger.colorful.rgb.Palette
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.graphics.use
import kotlin.random.Random

class GameScreen : KtxScreen {
    // TODO use AssetManager
    private val image = Texture(
        "spaceIcons/single/Retina/ship_C.png".toInternalFile(),
        true
    ).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    private val personSprite = ColorfulSprite(image).apply {
        setSize(5f, 5f)
        setOrigin(0.5f, 0.5f)
        setPosition(Random.nextFloat() * WORLD_WIDTH, Random.nextFloat() * WORLD_WIDTH)
        rotate(Random.nextFloat() * 360)
        color = ColorTools.fromColor(Color.CORAL)
    }
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
            personSprite.draw(it)
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

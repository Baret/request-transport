package de.gleex.reqtrans

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.tommyettinger.colorful.rgb.ColorTools
import com.github.tommyettinger.colorful.rgb.ColorfulBatch
import com.github.tommyettinger.colorful.rgb.ColorfulSprite
import com.github.tommyettinger.colorful.rgb.Palette
import de.gleex.reqtrans.model.Building
import de.gleex.reqtrans.model.House
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.graphics.use
import ktx.math.toImmutable
import space.earlygrey.simplegraphs.UndirectedGraph
import kotlin.random.Random

class GameScreen : KtxScreen {
    // TODO use AssetManager
    private val personImage = Texture(
        "spaceIcons/single/Retina/ship_C.png".toInternalFile(),
        true
    ).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    private val personSprite = ColorfulSprite(personImage).apply {
        setSize(5f, 5f)
        setOrigin(0.5f, 0.5f)
        setPosition(Random.nextFloat() * WORLD_WIDTH, Random.nextFloat() * WORLD_WIDTH)
        rotate(Random.nextFloat() * 360)
        color = ColorTools.fromColor(Color.CORAL)
    }

    private val houseTexture = Texture(
        "spaceIcons/single/Retina/meteor_large.png".toInternalFile(),
        true
    ).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    private val houseSprites: Array<ColorfulSprite> = Array()

    private val batch = ColorfulBatch()

    private val viewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT)

    private val graph = UndirectedGraph<Building>()

    override fun render(delta: Float) {
        input()
        logic()
        draw()
    }

    private fun input() {
        if(Gdx.input.isTouched) {
            val screenX = Gdx.input.x
            val screenY = Gdx.input.y
            val buildingPos = viewport.unproject(Vector2(screenX.toFloat(), screenY.toFloat()))
            val newBuilding = House(buildingPos.toImmutable())
            if (graph.vertices.any { it.distanceTo(newBuilding) < 20f }) {
                return
            }
            println("Adding house at touch pos $screenX|$screenY => $buildingPos")
            graph.addVertex(newBuilding)
            graph.vertices
                .filter { it != newBuilding }
                .forEach { oldBuilding ->
                    graph.addEdge(
                        oldBuilding,
                        newBuilding,
                        -oldBuilding.distanceTo(newBuilding)
                    )
                }
            val newSprite = ColorfulSprite(houseTexture).apply {
                setPosition(buildingPos.x - (Building.WIDTH / 2f), buildingPos.y - (Building.HEIGHT / 2f))
                setSize(Building.WIDTH, Building.HEIGHT)
                color = Palette.COAL_BLACK
            }
            houseSprites.add(newSprite)
        }
    }

    private fun logic() {
        // TODO game logic
    }

    private fun draw() {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined
        batch.use {
            it.packedColor = Palette.YELLOW
            personSprite.draw(it)
            houseSprites.forEach { house ->
                house.draw(it)
            }
        }
    }

    override fun dispose() {
        personImage.disposeSafely()
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

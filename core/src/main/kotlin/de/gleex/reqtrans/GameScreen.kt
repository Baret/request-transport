package de.gleex.reqtrans

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
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
import ktx.math.ImmutableVector2
import ktx.math.toImmutable
import ktx.math.toMutable
import space.earlygrey.simplegraphs.Path
import space.earlygrey.simplegraphs.UndirectedGraph
import space.earlygrey.simplegraphs.algorithms.UndirectedGraphAlgorithms
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
        color = Palette.BRONZE_SKIN_1
    }

    private val houseTexture = Texture(
        "spaceIcons/single/Retina/meteor_large.png".toInternalFile(),
        true
    ).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    private val houseSprites: Array<ColorfulSprite> = Array()

    private val shapeRenderer = ShapeRenderer()
    private val batch = ColorfulBatch()

    private val viewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT)

    private val graph = UndirectedGraph<Building>()

    private var personPath: Path<Building?>? = null

    override fun render(delta: Float) {
        input()
        logic(delta)
        draw()
    }

    private fun input() {
        if (Gdx.input.isTouched) {
            val screenX = Gdx.input.x
            val screenY = Gdx.input.y
            val buildingPos = viewport.unproject(Vector2(screenX.toFloat(), screenY.toFloat()))
            val newBuilding = House(buildingPos.toImmutable())
            if (graph.vertices.any { it.distanceTo(newBuilding) < 20f }) {
                return
            }
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

    private fun logic(delta: Float) {
        if (personPath == null && graph.vertices.size >= 2) {
            val pathSearch = UndirectedGraphAlgorithms(graph).newAstarSeach(
                graph.vertices.random(),
                graph.vertices.random(),
                { a, b -> a.distanceTo(b) },
                {})
            pathSearch.finish()
            personPath = pathSearch.path
        }
        if(personPath != null) {
            if(personAimsAtTarget()) {
                movePersonToTarget(delta)
            } else {
                turnPersonToTarget(delta)
            }
        }
    }

    private fun movePersonToTarget(delta: Float) {
        TODO("Not yet implemented")
    }

    private fun turnPersonToTarget(delta: Float) {
        TODO("Not yet implemented")
    }

    private fun personAimsAtTarget(): Boolean {
        val targetPos = personPath?.first?.position
        if(targetPos == null) {
            return false
        }
        val fromPos = ImmutableVector2(personSprite.x, personSprite.y)
        val angleInDegrees = angleBetween(targetPos, fromPos)

        return angleInDegrees > 2f
    }

    /**
     * Calculates the angle in degrees between the given vertices from the X axis.
     */
    private fun angleBetween(targetPos: ImmutableVector2, fromPos: ImmutableVector2): Float {
        val difference: ImmutableVector2 = (targetPos - fromPos)
        val direction: ImmutableVector2 = difference.nor
        val dotProduct: Float = Vector2.X.dot(direction.toMutable())
        val angleInRadians = MathUtils.acos(dotProduct)
        val angleInDegrees = angleInRadians * MathUtils.radiansToDegrees
        return angleInDegrees
    }

    private fun draw() {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        viewport.apply()

        shapeRenderer.projectionMatrix = viewport.camera.combined
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, viewport.camera) { renderer ->
            renderer.color = Color.BLACK
            graph.edges.forEach { edge ->
                renderer.line(edge.a.position.toMutable(), edge.b.position.toMutable())
            }
        }

        batch.projectionMatrix = viewport.camera.combined
        batch.use { batch ->
            houseSprites.forEach { house ->
                house.draw(batch)
            }
            personSprite.draw(batch)
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

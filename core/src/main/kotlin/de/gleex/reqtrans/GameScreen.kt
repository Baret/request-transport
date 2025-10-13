package de.gleex.reqtrans

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.CatmullRomSpline
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
import kotlin.math.abs
import kotlin.math.min
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
        setOrigin(width / 2f, height / 2f)
        setPosition(Random.nextFloat() * WORLD_WIDTH, Random.nextFloat() * WORLD_WIDTH)
        val degrees = Random.nextFloat() * 360
        rotate(degrees)
        Gdx.app.log(
            "spriteInit",
            "pos = ($x|$y), rotated personSprite initially by $degrees degrees, current = $rotation"
        )
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
    private var splinePath: CatmullRomSpline<Vector2>? = null
    private var turnProgress = 0f
    private var moveProgress = 0f

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
                setOrigin(width / 2f, height / 2f)
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
            val allVertices: List<Vector2> = listOf(Vector2(personSprite.x, personSprite.y)) + pathSearch.path.map { it.position.toMutable() }
            splinePath = CatmullRomSpline(allVertices.toTypedArray(), false)
            turnProgress = 0f
            moveProgress = 0f
        }
        if (personPath != null) {
            if (personAimsAtTarget()) {
                movePersonToTarget(delta)
                if(personReachedTarget()) {
                    personPath?.remove(0)
                    if(personPath?.isEmpty() ?: false) {
                        personPath = null
                        splinePath = null
                    }
                }
            } else {
                turnPersonToTarget(delta)
            }
        }
    }

    private fun movePersonToTarget(delta: Float) {
        val moveAmount = 1f * delta
        moveProgress += moveAmount
        val newPos = Vector2(personSprite.x, personSprite.y)
        splinePath?.valueAt(newPos, moveProgress)
        personSprite.x = newPos.x
        personSprite.y = newPos.y
    }

    private fun personReachedTarget(): Boolean {
        val target = currentPersonTarget()
        return if (target != null) {
            val personPos = Vector2(personSprite.x, personSprite.y)
            Gdx.app.log("reachCalc", "target=$target personPos=$personPos")
            target != personPos
        } else {
            true
        }
    }

    private fun turnPersonToTarget(delta: Float) {
        val turnSpeed = 20f // degrees per second I guess?
        val targetPos = currentPersonTarget() ?: return
        val fromPos = ImmutableVector2(personSprite.x, personSprite.y)
        val angleInDegrees = fromPos.toMutable().angleDeg(targetPos) //angleBetween(fromPos, targetPos)

        val turnAmount = min(turnSpeed * delta, abs(personSprite.rotation - angleInDegrees))

        Gdx.app.log("turning", "Turning by $turnAmount degrees, current rotation ${personSprite.rotation}")
        // TODO check if turning right or left is better
        personSprite.rotation += turnAmount
        if(personSprite.rotation > 360f) {
            personSprite.rotation = 360f - personSprite.rotation
        }
        if(personSprite.rotation < 0f) {
            personSprite.rotation += 360f
        }
    }

    private fun personAimsAtTarget(): Boolean {
        val targetPos = currentPersonTarget()
        if (targetPos == null) {
            return false
        }
        // TODO: use Vector2.angleDeg(v)
        val fromPos = Vector2(personSprite.x, personSprite.y)
        val angleDeg = fromPos.angleDeg(targetPos)
        Gdx.app.log("aimCheck", "angleDeg from $fromPos to $targetPos = $angleDeg")
        return MathUtils.isEqual(
            angleDeg,
            personSprite.rotation,
            1f
        )
//        val angleInDegrees = angleBetween(fromPos, targetPos)
//
//        return abs(angleInDegrees - personSprite.rotation) < 1f
    }

    private fun currentPersonTarget(): Vector2? {
        val path = personPath
        if(path != null && path.isNotEmpty()) {
            return path.first?.position?.toMutable()
        }
        return null
    }

    /**
     * Calculates the angle in degrees between the given vertices from the Y axis, counterclockwise.
     *
     * This is the same angle that Sprite uses in its rotation value.
     *
     * @see ColorfulSprite.setRotation
     */
    private fun angleBetween(fromPos: Vector2, targetPos: Vector2): Float {
        if (fromPos == targetPos) {
            return 0f
        }
        val atan2Deg360 = MathUtils.atan2Deg360(targetPos.x - fromPos.y, targetPos.y - fromPos.x)
        val angleCounterClockwise = if(atan2Deg360 == 0f) {
            0f
        } else {
            360f - atan2Deg360
        }
        Gdx.app.log("angleMath", "$fromPos => $targetPos = $angleCounterClockwise")
        return angleCounterClockwise
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

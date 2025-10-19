package de.gleex.reqtrans

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.tommyettinger.colorful.rgb.ColorfulBatch
import de.gleex.reqtrans.actors.PersonActor
import de.gleex.reqtrans.input.PersonSender
import de.gleex.reqtrans.model.WorldDimensions.WORLD_HEIGHT
import de.gleex.reqtrans.model.WorldDimensions.WORLD_WIDTH
import ktx.app.KtxScreen
import ktx.app.clearScreen
import kotlin.random.Random

class GameScreenWithStage : KtxScreen {
    private val viewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT)
    private val batch = ColorfulBatch()
    private val stage = Stage(viewport, batch)
    private val person = PersonActor()

    init {
        stage.addListener(PersonSender(person))
        stage.addActor(person)

        person.setPosition(20f, 30f)
        person.rotation = Random.nextFloat() * 359

        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }
}

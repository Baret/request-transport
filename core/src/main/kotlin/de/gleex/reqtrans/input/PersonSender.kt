package de.gleex.reqtrans.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import de.gleex.reqtrans.actors.PersonActor

class PersonSender(private val person: PersonActor) : InputListener() {
    override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
        Gdx.app.log("personSender", "touchUp at $x | $y, pointer=$pointer, button=$button")
        if (button == 0) {
            val targetPos = Vector2(x + person.originX, y + person.originY)
            val targetAngle = MathUtils.atan2Deg360(targetPos.x - person.y, targetPos.y - person.x)
            Gdx.app.log("personSender", "Telling person to turn to $targetAngle° and then move to $targetPos")
            person.addAction(
                sequence(
                    rotateTo(targetAngle, 1f),
                    moveTo(targetPos.x, targetPos.y, 5f)
                )
            )
            return true
        }
        return false
    }
}

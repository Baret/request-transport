package de.gleex.reqtrans.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.github.tommyettinger.colorful.rgb.ColorfulBatch
import com.github.tommyettinger.colorful.rgb.ColorfulSprite
import com.github.tommyettinger.colorful.rgb.Palette
import de.gleex.reqtrans.model.WorldDimensions.PERSON_HEIGHT
import de.gleex.reqtrans.model.WorldDimensions.PERSON_WIDTH
import de.gleex.reqtrans.model.WorldDimensions.WORLD_WIDTH
import ktx.assets.toInternalFile
import kotlin.random.Random

class PersonActor: Actor() {
    init {
        setSize(PERSON_WIDTH, PERSON_HEIGHT)
        setOrigin(width / 2f, height / 2f)
    }

    private val personImage = Texture(
        "spaceIcons/single/Retina/ship_C.png".toInternalFile(),
        true
    ).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    private val personSprite = ColorfulSprite(personImage).apply {
        setSize(this@PersonActor.width, this@PersonActor.height)
        color = Palette.BRONZE_SKIN_1
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        if(batch != null && batch is ColorfulBatch) {
            personSprite.draw(batch)
        }
    }
}

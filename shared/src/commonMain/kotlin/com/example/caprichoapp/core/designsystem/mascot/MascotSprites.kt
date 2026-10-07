package com.example.caprichoapp.core.designsystem.mascot

data class SpriteFrame(val rows: List<String>, val durationMs: Long)

/**
 * Sprites de la mascota: grillas de 12x10 donde cada letra es un color de la paleta.
 *  B cuerpo · K ojos/boca · P mejillas · N nariz · H corazón/destello · . vacío
 */
object MascotSprites {
    const val WIDTH = 12
    const val HEIGHT = 10
    const val VALID_CHARS = ".BKPNH"

    // Piezas reutilizables de la cara
    private const val EARS_1 = "..BB....BB.."
    private const val EARS_2 = ".BBBB..BBBB."
    private const val FOREHEAD = "BBBBBBBBBBBB"
    private const val EYES_OPEN = "BBKKBBBBKKBB"
    private const val EYES_RIGHT = "BBBKKBBBBKKB"
    private const val EYES_LEFT = "BKKBBBBKKBBB"
    private const val CHEEKS = "BPPBBNNBBPPB"
    private const val NOSE = "BBBBBNNBBBBB"
    private const val MOUTH_CLOSED = "BBBBBKKBBBBB"
    private const val MOUTH_SMIRK = "BBBBBBKKBBBB"
    private const val MOUTH_OPEN = "BBBBKKKKBBBB"
    private const val CHIN_1 = ".BBBBBBBBBB."
    private const val CHIN_OPEN = ".BBBKKKKBBB."
    private const val CHIN_2 = "..BBBBBBBB.."

    private fun face(
        eyeTop: String,
        eyeBottom: String,
        mouth: String,
        chin: String = CHIN_1,
        ears1: String = EARS_1,
        ears2: String = EARS_2,
    ) = listOf(ears1, ears2, FOREHEAD, eyeTop, eyeBottom, CHEEKS, NOSE, mouth, chin, CHIN_2)

    private val idleOpen = face(EYES_OPEN, EYES_OPEN, MOUTH_CLOSED)
    private val idleBlink = face(FOREHEAD, EYES_OPEN, MOUTH_CLOSED)
    private val lookRight = face(EYES_RIGHT, EYES_RIGHT, MOUTH_CLOSED)
    private val lookLeft = face(EYES_LEFT, EYES_LEFT, MOUTH_CLOSED)

    private val talkOpen = face(EYES_OPEN, EYES_OPEN, MOUTH_OPEN, CHIN_OPEN)

    private val thinkRight = face(EYES_RIGHT, EYES_RIGHT, MOUTH_SMIRK)
    private val thinkLeft = face(EYES_LEFT, EYES_LEFT, MOUTH_SMIRK)

    // Feliz: ojos ^ ^, boca abierta y un destello rosa que late arriba
    private val happy = face("BBBKBBBBKBBB", "BBKBKBBKBKBB", MOUTH_OPEN)
    private val happySpark = face(
        eyeTop = "BBBKBBBBKBBB",
        eyeBottom = "BBKBKBBKBKBB",
        mouth = MOUTH_OPEN,
        ears1 = "..BB.HH.BB..",
        ears2 = ".BBBBHHBBBB.",
    )

    // Preocupado: Ojos angustiados y boca apretada
    private val worried1 = face(
        eyeTop = "BB.KBBBB.KBB",
        eyeBottom = "BKKBBBBKKBBB",
        mouth = "BBBBKKKKBBBB",
    )
    private val worried2 = face(
        eyeTop = "BB.KBBBB.KBB",
        eyeBottom = "BKKBBBBKKBBB",
        mouth = "BBBBBKKBBBBB",
    )

    // Triste: Ojos caídos y boca hacia abajo
    private val sad1 = face(
        eyeTop = "BBKKBBBBKKBB",
        eyeBottom = "BBBKKBBBBKKB",
        mouth = "BBBBBKKBBBBB",
        chin = ".BBBKKKKBBB.",
    )
    private val sad2 = face(
        eyeTop = "BBBKKBBBBKKB",
        eyeBottom = "BBKKBBBBKKBB",
        mouth = "BBBBBKKBBBBB",
        chin = ".BBBKKKKBBB.",
    )

    fun framesFor(mood: MascotMood): List<SpriteFrame> = when (mood) {
        MascotMood.Idle -> listOf(
            SpriteFrame(idleOpen, 2_000),
            SpriteFrame(idleBlink, 140),
            SpriteFrame(idleOpen, 1_200),
            SpriteFrame(lookRight, 500),
            SpriteFrame(lookLeft, 500),
            SpriteFrame(idleOpen, 800),
            SpriteFrame(idleBlink, 140),
        )
        MascotMood.Talking -> listOf(
            SpriteFrame(talkOpen, 140),
            SpriteFrame(idleOpen, 120),
            SpriteFrame(talkOpen, 170),
            SpriteFrame(idleOpen, 110),
        )
        MascotMood.Thinking -> listOf(
            SpriteFrame(thinkRight, 600),
            SpriteFrame(thinkLeft, 600),
        )
        MascotMood.Happy -> listOf(
            SpriteFrame(happySpark, 320),
            SpriteFrame(happy, 320),
        )
        MascotMood.Worried -> listOf(
            SpriteFrame(worried1, 500),
            SpriteFrame(worried2, 500),
        )
        MascotMood.Sad -> listOf(
            SpriteFrame(sad1, 800),
            SpriteFrame(sad2, 400),
        )
    }
}

package com.example.caprichoapp.core.designsystem.mascot

data class SpriteFrame(val rows: List<String>, val durationMs: Long)

object MascotSprites {
    private val idleOpen = listOf(
        "..BB....BB..",
        ".BBBB..BBBB.",
        "BBBBBBBBBBBB",
        "BBKKBBBBKKBB",
        "BBKKBBBBKKBB",
        "BPPBBNNBBPPB",
        "BBBBBNNBBBBB",
        "BBBBBKKBBBBB",
        ".BBBBBBBBBB.",
        "..BBBBBBBB..",
    )

    private val idleBlink = listOf(
        "..BB....BB..",
        ".BBBB..BBBB.",
        "BBBBBBBBBBBB",
        "BBBBBBBBBBBB",
        "BBKKBBBBKKBB",
        "BPPBBNNBBPPB",
        "BBBBBNNBBBBB",
        "BBBBBKKBBBBB",
        ".BBBBBBBBBB.",
        "..BBBBBBBB..",
    )

    fun framesFor(mood: MascotMood): List<SpriteFrame> = when (mood) {
        MascotMood.Idle -> listOf(
            SpriteFrame(idleOpen, 2200),
            SpriteFrame(idleBlink, 150),
        )
        // TODO: Thinking, Happy, Worried, Sad (mismas dimensiones 12x10)
        else -> listOf(SpriteFrame(idleOpen, 1000))
    }
}
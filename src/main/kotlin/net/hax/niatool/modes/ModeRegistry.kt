package net.hax.niatool.modes

import net.hax.niatool.modes.glyph.GlyphHackMode
import net.hax.niatool.modes.quiz.QuizHelperMode

object ModeRegistry {

    private val modes = mapOf<String, OperationMode>(
            "glyph" to GlyphHackMode(),
            "quiz" to QuizHelperMode()
    )

    fun getModeModule(id: String): OperationMode? {
        return modes[id]
    }

    data class Info(val name: String, val description: String)

}

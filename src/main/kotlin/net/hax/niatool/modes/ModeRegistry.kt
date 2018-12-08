package net.hax.niatool.modes

import net.hax.niatool.modes.glyph.GlyphHackMode

object ModeRegistry {

    private val modes = mapOf<String, OperationMode>(
            "glyph" to GlyphHackMode()
    )

    fun getModeModule(id: String): OperationMode? = modes[id]

    fun getModeNames(): Set<String> = modes.keys

    data class Info(val name: String, val description: String)

}

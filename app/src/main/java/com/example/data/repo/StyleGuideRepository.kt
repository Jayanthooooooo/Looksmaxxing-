package com.example.data.repo

import android.content.Context
import com.example.core.AppDispatchers
import com.example.face.shape.BeardOption
import com.example.face.shape.Haircuts
import com.example.face.shape.ShapeStyleGuide
import kotlinx.coroutines.withContext
import org.json.JSONObject

class StyleGuideRepository(
    private val context: Context,
    private val dispatchers: AppDispatchers
) {
    private var cachedShapes: List<ShapeStyleGuide>? = null

    suspend fun getShapes(): List<ShapeStyleGuide> = withContext(dispatchers.io) {
        cachedShapes?.let { return@withContext it }
        try {
            val jsonString = context.assets.open("style_guide.json").bufferedReader().use { it.readText() }
            val parsed = parseStyleGuideJson(jsonString)
            cachedShapes = parsed
            parsed
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getShapeById(id: String): ShapeStyleGuide? = withContext(dispatchers.io) {
        val shapes = getShapes()
        shapes.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
    }

    companion object {
        fun parseStyleGuideJson(jsonString: String): List<ShapeStyleGuide> {
            val root = JSONObject(jsonString)
            val shapesArray = root.getJSONArray("shapes")
            val result = mutableListOf<ShapeStyleGuide>()
            for (i in 0 until shapesArray.length()) {
                val item = shapesArray.getJSONObject(i)
                val haircutsObj = item.getJSONObject("haircuts")
                val haircuts = Haircuts(
                    top = haircutsObj.getString("top"),
                    sides = haircutsObj.getString("sides"),
                    fringe = haircutsObj.getString("fringe"),
                    parting = haircutsObj.getString("parting")
                )

                val beardsArray = item.getJSONArray("beard_options")
                val beards = mutableListOf<BeardOption>()
                for (j in 0 until beardsArray.length()) {
                    val bObj = beardsArray.getJSONObject(j)
                    beards.add(
                        BeardOption(
                            style = bObj.getString("style"),
                            effect = bObj.getString("effect")
                        )
                    )
                }

                val glassesArray = item.getJSONArray("glasses_frames")
                val glasses = mutableListOf<String>()
                for (j in 0 until glassesArray.length()) {
                    glasses.add(glassesArray.getString(j))
                }

                val avoidArray = item.getJSONArray("what_to_avoid")
                val avoid = mutableListOf<String>()
                for (j in 0 until avoidArray.length()) {
                    avoid.add(avoidArray.getString(j))
                }

                result.add(
                    ShapeStyleGuide(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        description = item.getString("description"),
                        barber_summary = item.getString("barber_summary"),
                        haircuts = haircuts,
                        beard_options = beards,
                        eyebrow_tips = item.getString("eyebrow_tips"),
                        glasses_frames = glasses,
                        what_to_avoid = avoid
                    )
                )
            }
            return result
        }
    }
}

package app.aaps.core.objects.extensions

import app.aaps.core.data.model.ICfg
import org.json.JSONObject

fun ICfg.toJson(): JSONObject = JSONObject()
    .put("insulinLabel", insulinLabel)
    .put("insulinEndTime", insulinEndTime)
    .put("peak", peak)

fun ICfg.Companion.fromJson(json: JSONObject): ICfg = ICfg(
    insulinLabel = json.optString("insulinLabel", ""),
    insulinEndTime = json.optLong("insulinEndTime", 6 * 3600 * 1000),
    peak = json.optLong("peak")
)
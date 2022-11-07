package info.nightscout.plugins.sync.nsclient.extensions

import info.nightscout.androidaps.database.embedments.InterfaceIDs
import info.nightscout.androidaps.database.entities.TemporaryBasal
import info.nightscout.androidaps.database.entities.TemporaryBasal.Type.Companion.fromString
import info.nightscout.androidaps.database.entities.TherapyEvent
import info.nightscout.androidaps.extensions.convertedToAbsolute
import info.nightscout.androidaps.interfaces.Profile
import info.nightscout.androidaps.utils.DateUtil
import info.nightscout.androidaps.utils.T
import info.nightscout.interfaces.utils.JsonHelper
import org.json.JSONObject

fun TemporaryBasal.toJson(isAdd: Boolean, profile: Profile, dateUtil: DateUtil): JSONObject =
    JSONObject()
        .put("created_at", dateUtil.toISOString(timestamp))
        .put("enteredBy", "openaps://" + "AndroidAPS")
        .put("eventType", TherapyEvent.Type.TEMPORARY_BASAL.text)
        .put("isValid", isValid)
        .put("duration", T.msecs(duration).mins())
        .put("durationInMilliseconds", duration) // rounded duration leads to different basal IOB
        .put("type", type.name)
        .put("rate", convertedToAbsolute(timestamp, profile)) // generated by OpenAPS, for compatibility
        .also {
            if (isAbsolute) it.put("absolute", rate)
            else it.put("percent", rate - 100)
            if (interfaceIDs.pumpId != null) it.put("pumpId", interfaceIDs.pumpId)
            if (interfaceIDs.endId != null) it.put("endId", interfaceIDs.endId)
            if (interfaceIDs.pumpType != null) it.put("pumpType", interfaceIDs.pumpType!!.name)
            if (interfaceIDs.pumpSerial != null) it.put("pumpSerial", interfaceIDs.pumpSerial)
            if (isAdd && interfaceIDs.nightscoutId != null) it.put("_id", interfaceIDs.nightscoutId)
        }

fun temporaryBasalFromJson(jsonObject: JSONObject): TemporaryBasal? {
    val timestamp = JsonHelper.safeGetLongAllowNull(jsonObject, "mills", null) ?: return null
    val percent = JsonHelper.safeGetDoubleAllowNull(jsonObject, "percent")
    val absolute = JsonHelper.safeGetDoubleAllowNull(jsonObject, "absolute")
    val duration = JsonHelper.safeGetLongAllowNull(jsonObject, "duration") ?: return null
    val durationInMilliseconds = JsonHelper.safeGetLongAllowNull(jsonObject, "durationInMilliseconds")
    val type = fromString(JsonHelper.safeGetString(jsonObject, "type"))
    val isValid = JsonHelper.safeGetBoolean(jsonObject, "isValid", true)
    val id = JsonHelper.safeGetStringAllowNull(jsonObject, "_id", null) ?: return null
    val pumpId = JsonHelper.safeGetLongAllowNull(jsonObject, "pumpId", null)
    val endPumpId = JsonHelper.safeGetLongAllowNull(jsonObject, "endId", null)
    val pumpType = InterfaceIDs.PumpType.fromString(JsonHelper.safeGetStringAllowNull(jsonObject, "pumpType", null))
    val pumpSerial = JsonHelper.safeGetStringAllowNull(jsonObject, "pumpSerial", null)

    val rate: Double
    val isAbsolute: Boolean
    if (absolute != null) {
        rate = absolute
        isAbsolute = true
    } else if (percent != null) {
        rate = percent + 100
        isAbsolute = false
    } else return null
    if (duration == 0L && durationInMilliseconds == null) return null
    if (timestamp == 0L) return null

    return TemporaryBasal(
        timestamp = timestamp,
        rate = rate,
        duration = durationInMilliseconds ?: T.mins(duration).msecs(),
        type = type,
        isAbsolute = isAbsolute,
        isValid = isValid
    ).also {
        it.interfaceIDs.nightscoutId = id
        it.interfaceIDs.pumpId = pumpId
        it.interfaceIDs.endId = endPumpId
        it.interfaceIDs.pumpType = pumpType
        it.interfaceIDs.pumpSerial = pumpSerial
    }
}
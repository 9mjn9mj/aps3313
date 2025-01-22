package app.aaps.pump.equil.keys

import app.aaps.core.keys.BooleanNonPreferenceKey

enum class EquilBooleanKey(
    override val key: String,
    override val defaultValue: Boolean
) : BooleanNonPreferenceKey {

    BasalSet("key_equil_basal_set", false),
    AlarmBattery10("key_equil_alarm_battery_10", false),
    AlarmInsulin10("key_equil_alarm_insulin_10", false),
    AlarmInsulin5("key_equil_alarm_insulin_5", false),
}

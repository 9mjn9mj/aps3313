package info.nightscout.androidaps.plugins.general.overview

import android.graphics.Color
import android.widget.TextView
import androidx.annotation.StringRes
import info.nightscout.androidaps.Config
import info.nightscout.androidaps.MainApp
import info.nightscout.androidaps.R
import info.nightscout.androidaps.db.CareportalEvent
import info.nightscout.androidaps.interfaces.ActivePluginProvider
import info.nightscout.androidaps.plugins.pump.common.defs.PumpType
import info.nightscout.androidaps.plugins.pump.omnipod.driver.definition.OmnipodConstants
import info.nightscout.androidaps.utils.DecimalFormatter
import info.nightscout.androidaps.utils.WarnColors
import info.nightscout.androidaps.utils.resources.ResourceHelper
import info.nightscout.androidaps.utils.sharedPreferences.SP
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusLightHandler @Inject constructor(
    private val resourceHelper: ResourceHelper,
    private val sp: SP,
    private val activePlugin: ActivePluginProvider,
    private val warnColors: WarnColors,
    private val config: Config
) {

    /**
     * applies the extended statusLight subview on the overview fragment
     */
    fun updateStatusLights(careportal_canulaage: TextView?, careportal_insulinage: TextView?, careportal_reservoirlevel: TextView?, careportal_sensorage: TextView?, careportal_sensorbatterylevel: TextView?, careportal_pbage: TextView?, careportal_batterylevel: TextView?) {
        val pump = activePlugin.activePump
        val bgSource = activePlugin.activeBgSource
        handleAge(careportal_canulaage, CareportalEvent.SITECHANGE, R.string.key_statuslights_cage_warning, 48.0, R.string.key_statuslights_cage_critical, 72.0)
        handleAge(careportal_insulinage, CareportalEvent.INSULINCHANGE, R.string.key_statuslights_iage_warning, 72.0, R.string.key_statuslights_iage_critical, 144.0)
        handleAge(careportal_sensorage, CareportalEvent.SENSORCHANGE, R.string.key_statuslights_sage_warning, 216.0, R.string.key_statuslights_sage_critical, 240.0)
        if (pump.pumpDescription.isBatteryReplaceable) {
            handleAge(careportal_pbage, CareportalEvent.PUMPBATTERYCHANGE, R.string.key_statuslights_bage_warning, 216.0, R.string.key_statuslights_bage_critical, 240.0)
        }
        if (!config.NSCLIENT) {
            if (pump.model() == PumpType.Insulet_Omnipod) {
                handleOmnipodReservoirLevel(careportal_reservoirlevel, R.string.key_statuslights_res_critical, 10.0, R.string.key_statuslights_res_warning, 80.0, pump.reservoirLevel, "U")
            } else {
                handleLevel(careportal_reservoirlevel, R.string.key_statuslights_res_critical, 10.0, R.string.key_statuslights_res_warning, 80.0, pump.reservoirLevel, "U")
            }
            if (bgSource.sensorBatteryLevel != -1)
                handleLevel(careportal_sensorbatterylevel, R.string.key_statuslights_sbat_critical, 5.0, R.string.key_statuslights_sbat_warning, 20.0, bgSource.sensorBatteryLevel.toDouble(), "%")
            else
                careportal_sensorbatterylevel?.text = ""
        }
        if (!config.NSCLIENT && pump.model() != PumpType.AccuChekCombo)
            handleLevel(careportal_batterylevel, R.string.key_statuslights_bat_critical, 26.0, R.string.key_statuslights_bat_warning, 51.0, pump.batteryLevel.toDouble(), "%")
    }

    private fun handleAge(view: TextView?, eventName: String, @StringRes warnSettings: Int, defaultWarnThreshold: Double, @StringRes urgentSettings: Int, defaultUrgentThreshold: Double) {
        val warn = sp.getDouble(warnSettings, defaultWarnThreshold)
        val urgent = sp.getDouble(urgentSettings, defaultUrgentThreshold)
        val careportalEvent = MainApp.getDbHelper().getLastCareportalEvent(eventName)
        if (careportalEvent != null) {
            warnColors.setColorByAge(view, careportalEvent, warn, urgent)
            view?.text = careportalEvent.age(resourceHelper.shortTextMode(), resourceHelper)
        } else {
            view?.text = if (resourceHelper.shortTextMode()) "-" else resourceHelper.gs(R.string.notavailable)
        }
    }

    private fun handleLevel(view: TextView?, criticalSetting: Int, criticalDefaultValue: Double, warnSetting: Int, warnDefaultValue: Double, level: Double, units: String) {
        val resUrgent = sp.getDouble(criticalSetting, criticalDefaultValue)
        val resWarn = sp.getDouble(warnSetting, warnDefaultValue)
        @Suppress("SetTextI18n")
        view?.text = " " + DecimalFormatter.to0Decimal(level) + units
        warnColors.setColorInverse(view, level, resWarn, resUrgent)
    }

    // Omnipod only reports reservoir level when it's 50 units or less, so we display "50+U" for any value > 50
    private fun handleOmnipodReservoirLevel(view: TextView?, criticalSetting: Int, criticalDefaultValue: Double, warnSetting: Int, warnDefaultValue: Double, level: Double, units: String) {
        val resUrgent = sp.getDouble(criticalSetting, criticalDefaultValue)
        val resWarn = sp.getDouble(warnSetting, warnDefaultValue)
        if (level > OmnipodConstants.MAX_RESERVOIR_READING) {
            @Suppress("SetTextI18n")
            view?.text = " 50+$units"
            view?.setTextColor(Color.WHITE)
        } else {
            @Suppress("SetTextI18n")
            view?.text = " " + DecimalFormatter.to0Decimal(level) + units
            warnColors.setColorInverse(view, level, resWarn, resUrgent)
        }
    }
}
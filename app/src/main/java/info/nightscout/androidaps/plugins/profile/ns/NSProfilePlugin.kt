package info.nightscout.androidaps.plugins.profile.ns

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.android.HasAndroidInjector
import info.nightscout.androidaps.interfaces.Config
import info.nightscout.androidaps.R
import info.nightscout.androidaps.events.EventProfileStoreChanged
import info.nightscout.androidaps.interfaces.PluginBase
import info.nightscout.androidaps.interfaces.PluginDescription
import info.nightscout.androidaps.interfaces.PluginType
import info.nightscout.androidaps.interfaces.ProfileSource
import info.nightscout.androidaps.interfaces.ProfileStore
import info.nightscout.androidaps.logging.AAPSLogger
import info.nightscout.androidaps.logging.LTag
import info.nightscout.androidaps.plugins.bus.RxBusWrapper
import info.nightscout.androidaps.plugins.general.nsclient.events.EventNSClientRestart
import info.nightscout.androidaps.plugins.profile.ns.events.EventNSProfileUpdateGUI
import info.nightscout.androidaps.receivers.DataWorker
import info.nightscout.androidaps.utils.DateUtil
import info.nightscout.androidaps.utils.resources.ResourceHelper
import info.nightscout.androidaps.utils.sharedPreferences.SP
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NSProfilePlugin @Inject constructor(
    injector: HasAndroidInjector,
    aapsLogger: AAPSLogger,
    private val rxBus: RxBusWrapper,
    resourceHelper: ResourceHelper,
    private val sp: SP,
    private val dateUtil: DateUtil,
    config: Config
) : PluginBase(PluginDescription()
    .mainType(PluginType.PROFILE)
    .fragmentClass(NSProfileFragment::class.java.name)
    .pluginIcon(R.drawable.ic_nightscout_profile)
    .pluginName(R.string.nsprofile)
    .shortName(R.string.profileviewer_shortname)
    .alwaysEnabled(config.NSCLIENT)
    .alwaysVisible(config.NSCLIENT)
    .showInList(!config.NSCLIENT)
    .description(R.string.description_profile_nightscout),
    aapsLogger, resourceHelper, injector
), ProfileSource {

    override var profile: ProfileStore? = null

    override val profileName: String?
        get() = profile?.getDefaultProfileName()

    override fun onStart() {
        super.onStart()
        loadNSProfile()
    }

    private fun storeNSProfile() {
        sp.putString("profile", profile!!.data.toString())
        aapsLogger.debug(LTag.PROFILE, "Storing profile")
    }

    private fun loadNSProfile() {
        aapsLogger.debug(LTag.PROFILE, "Loading stored profile")
        val profileString = sp.getStringOrNull("profile", null)
        if (profileString != null) {
            aapsLogger.debug(LTag.PROFILE, "Loaded profile: $profileString")
            profile = ProfileStore(injector, JSONObject(profileString), dateUtil)
        } else {
            aapsLogger.debug(LTag.PROFILE, "Stored profile not found")
            // force restart of nsclient to fetch profile
            rxBus.send(EventNSClientRestart())
        }
    }


    // cannot be inner class because of needed injection
    class NSProfileWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {

        @Inject lateinit var injector: HasAndroidInjector
        @Inject lateinit var nsProfilePlugin: NSProfilePlugin
        @Inject lateinit var aapsLogger: AAPSLogger
        @Inject lateinit var rxBus: RxBusWrapper
        @Inject lateinit var dateUtil: DateUtil
        @Inject lateinit var dataWorker: DataWorker

        init {
            (context.applicationContext as HasAndroidInjector).androidInjector().inject(this)
        }

        override fun doWork(): Result {
            val profileString = dataWorker.pickupJSONObject(inputData.getLong(DataWorker.STORE_KEY, -1))
                ?: return Result.failure(workDataOf("Error" to "missing input data"))
            nsProfilePlugin.profile = ProfileStore(injector, profileString, dateUtil)
            nsProfilePlugin.storeNSProfile()
            if (nsProfilePlugin.isEnabled()) {
                rxBus.send(EventProfileStoreChanged())
                rxBus.send(EventNSProfileUpdateGUI())
            }
            aapsLogger.debug(LTag.PROFILE, "Received profileStore: ${nsProfilePlugin.profile}")
            return Result.success()
        }
    }
}
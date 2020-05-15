package info.nightscout.androidaps.plugins.general.autotune;

import java.util.List;

import javax.inject.Inject;

import dagger.android.HasAndroidInjector;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.data.Profile;
import info.nightscout.androidaps.db.StaticInjector;
import info.nightscout.androidaps.interfaces.ActivePluginProvider;
import info.nightscout.androidaps.interfaces.InsulinInterface;
import info.nightscout.androidaps.plugins.general.autotune.data.BGDatum;
import info.nightscout.androidaps.plugins.general.autotune.data.CRDatum;
import info.nightscout.androidaps.plugins.general.autotune.data.PreppedGlucose;
import info.nightscout.androidaps.utils.sharedPreferences.SP;

public class AutotuneCore {
    @Inject ActivePluginProvider activePlugin;
    @Inject SP sp;
    @Inject AutotunePlugin autotunePlugin;
    private HasAndroidInjector injector;

    public AutotuneCore () {
        injector = StaticInjector.Companion.getInstance();
        injector.androidInjector().inject(this);
    }

    public Profile tuneAllTheThings (PreppedGlucose preppedGlucose, Profile previousAutotune, Profile pumpProfile) {

        //var pumpBasalProfile = pumpProfile.basalprofile;
        Profile.ProfileValue[] pumpBasalProfile = pumpProfile.getBasalValues();
        //console.error(pumpBasalProfile);
        Profile.ProfileValue[] basalProfile = previousAutotune.getBasalValues();
        //console.error(basalProfile);
        Profile.ProfileValue[]  isfProfile = previousAutotune.getIsfsMgdl();
        //console.error(isfProfile);
        Double ISF = isfProfile[0].value;
        //console.error(ISF);
        Profile.ProfileValue[] carbRatioProfile = previousAutotune.getIcs();
        Double carbRatio = carbRatioProfile[0].value;
        //console.error(carbRatio);
        Double CSF = ISF / carbRatio;
        Double DIA = previousAutotune.getDia();
        InsulinInterface insulinInterface = activePlugin.getActiveInsulin();
        int peak=75;
        if (insulinInterface.getId() == InsulinInterface.OREF_ULTRA_RAPID_ACTING)
            peak=55;
        else if (insulinInterface.getId() == InsulinInterface.OREF_FREE_PEAK)
            peak=sp.getInt(R.string.key_insulin_oref_peak,75);

        List<CRDatum> crData = preppedGlucose.crData;
        List<BGDatum> csfGlucoseData = preppedGlucose.csfGlucoseData;
        List<BGDatum> isfGlucoseData = preppedGlucose.isfGlucoseData;
        List<BGDatum> basalGlucoseData = preppedGlucose.basalGlucoseData;



        // to avoid error
        return previousAutotune;
    }


}

package app.aaps.core.keys

enum class UnitDoubleKey(
    override val key: Int,
    val defaultValue: Double,
    val minMgdl: Int,
    val maxMgdl: Int,
    override val defaultedBySM: Boolean = false,
    val showInApsMode: Boolean = true,
    val showInNsClientMode: Boolean = true,
    val showInPumpControlMode: Boolean = true,
    val hideParentScreenIfHidden: Boolean = false // PreferenceScreen is final so we cannot extend and modify behavior
) : PreferenceKey {

    OverviewEatingSoonTarget(R.string.key_eating_soon_target, 90.0, 72, 160, defaultedBySM = true),
    OverviewActivityTarget(R.string.key_activity_target, 140.0, 108, 180, defaultedBySM = true),
    OverviewHypoTarget(R.string.key_hypo_target, 160.0, 108, 180, defaultedBySM = true),
    OverviewLowMark(R.string.key_low_mark, 72.0, 25, 160, showInNsClientMode = false, hideParentScreenIfHidden = true),
    OverviewHighMark(R.string.key_high_mark, 180.0, 90, 250, showInNsClientMode = false),
    ApsLgsThreshold(R.string.key_dynamic_isf_lgs_threshold, 65.0, 65, 120, defaultedBySM = true),
    ApsAutoIsfHalfBasalExerciseTarget(R.string.key_half_basal_exercise_target, 160.0, 120, 200, defaultedBySM = true),
    ApsAutoIsfSmbDeliveryRatioBgRange(R.string.key_openapsama_smb_delivery_ratio_bg_range, 0.0, 0, 100, defaultedBySM = true)
}
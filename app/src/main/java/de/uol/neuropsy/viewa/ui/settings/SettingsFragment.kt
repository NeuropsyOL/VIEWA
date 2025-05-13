//package de.uol.neuropsy.viewa.ui.settings
//
//import StagingDataStore
//import android.app.Dialog
//import android.content.DialogInterface
//import android.os.Bundle
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewGroup.LayoutParams.MATCH_PARENT
//import android.widget.FrameLayout
//import androidx.appcompat.app.AlertDialog
//import androidx.fragment.app.DialogFragment
//import androidx.preference.PreferenceFragmentCompat
//import androidx.preference.PreferenceManager
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import de.uol.neuropsy.viewa.R
//import de.uol.neuropsy.viewa.dataStore
//
//class SettingsDialog : DialogFragment() {
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        // 1) Inflate your custom view
//        val view = requireActivity().layoutInflater
//            .inflate(R.layout.dialog_settings, null)
//
//        val frag=SettingsFragment()
//        // 2) Build the dialog with that view
//        val dialog = MaterialAlertDialogBuilder(requireContext())
//            .setView(view)
//            .setPositiveButton(android.R.string.ok){ _: DialogInterface, _: Int -> frag.applyChanges()}
//            .create()
//
//        // 3) Once it's shown, embed the PreferenceFragmentCompat
//        dialog.setOnShowListener {
//            childFragmentManager
//                .beginTransaction()
//                .replace(R.id.settings_container, frag)
//                .commitNow()
//        }
//
//        return dialog
//    }
//}
//
//
//class SettingsFragment : PreferenceFragmentCompat() {
//    private lateinit var stagingStore: StagingDataStore
//    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//        val ds = requireContext().dataStore
//        stagingStore = StagingDataStore(ds)
//        preferenceManager.preferenceDataStore = stagingStore
//        setPreferencesFromResource(R.xml.preferences, rootKey)
//    }
//
//    /** Called by your host when the user taps “Apply” */
//    fun applyChanges() = stagingStore.commit()
//}
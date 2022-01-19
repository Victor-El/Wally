package me.codeenzyme.wally.settings.views.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import me.codeenzyme.wally.R
import me.codeenzyme.wally.commons.preferencestore.ORIENTATION_PREF_KEY
import me.codeenzyme.wally.commons.preferencestore.settingsPref
import me.codeenzyme.wally.databinding.FragmentDialogOrientationBinding
import java.util.*

class OrientationDialog : DialogFragment() {

    private val arg by navArgs<OrientationDialogArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewBinding = FragmentDialogOrientationBinding.inflate(layoutInflater)
        var orientation = ""
        viewBinding.orientationRadioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            orientation = viewBinding.root.findViewById<RadioButton>(i).text.toString()
                .toLowerCase(Locale.ROOT)
        }

        when (arg.orientation.toLowerCase(Locale.ROOT)) {
            "all" -> {
                viewBinding.orientationAll.isChecked = true
            }
            "horizontal" -> {
                viewBinding.orientationLandscape.isChecked = true
            }
            "vertical" -> {
                viewBinding.orientationPortrait.isChecked = true
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.orientation)
            .setView(viewBinding.root)
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                if (orientation.trim().isNotEmpty()) {
                    lifecycleScope.launch {
                        requireContext().settingsPref.edit {
                            it[ORIENTATION_PREF_KEY] = orientation
                        }
                        dialogInterface.dismiss()
                    }
                }
            }
            .create()
        // dialog.window?.setBackgroundDrawableResource(android.R.color.background_dark)
        return dialog
    }
}
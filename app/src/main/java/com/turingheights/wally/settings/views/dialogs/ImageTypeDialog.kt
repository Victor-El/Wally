package com.turingheights.wally.settings.views.dialogs

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
import com.turingheights.wally.R
import com.turingheights.wally.commons.preferencestore.IMAGE_TYPE_PREF_KEY
import com.turingheights.wally.commons.preferencestore.settingsPref
import com.turingheights.wally.databinding.FragmentDialogImageTypeBinding
import java.util.*

class ImageTypeDialog : DialogFragment() {

    private lateinit var viewBinding: FragmentDialogImageTypeBinding

    private val arg by navArgs<ImageTypeDialogArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = FragmentDialogImageTypeBinding.inflate(layoutInflater)
        var imageType = ""

        viewBinding.imageTypeRadioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            imageType = viewBinding.root.findViewById<RadioButton>(i).text.toString()
                .toLowerCase(Locale.ROOT)
        }

        when (arg.imageType.toLowerCase(Locale.ROOT)) {
            "all" -> {
                viewBinding.imageTypeAll.isChecked = true
            }
            "photo" -> {
                viewBinding.imageTypePhoto.isChecked = true
            }
            "illustration" -> {
                viewBinding.imageTypeIllustration.isChecked = true
            }
            "vector" -> {
                viewBinding.imageTypeVector.isChecked = true
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.image_type))
            .setView(viewBinding.root)
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                if (imageType.trim().isNotEmpty()) {
                    lifecycleScope.launch {
                        requireContext().settingsPref.edit {
                            it[IMAGE_TYPE_PREF_KEY] = imageType
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
package com.turingheights.wally.settings.views.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import com.turingheights.wally.R
import com.turingheights.wally.commons.preferencestore.IMAGE_TYPE_PREF_KEY
import com.turingheights.wally.commons.preferencestore.settingsPref
import com.turingheights.wally.databinding.FragmentDialogImageTypeBinding

class ImageTypeDialog : BottomSheetDialogFragment() {

    private lateinit var viewBinding: FragmentDialogImageTypeBinding
    private val arg by navArgs<ImageTypeDialogArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentDialogImageTypeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Wally_BottomSheetDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var imageType = arg.imageType.lowercase()

        viewBinding.imageTypeRadioGroup.setOnCheckedChangeListener { _, i ->
            imageType = viewBinding.root.findViewById<RadioButton>(i).text.toString()
                .lowercase()
        }

        when (arg.imageType.lowercase()) {
            "all" -> viewBinding.imageTypeAll.isChecked = true
            "photo" -> viewBinding.imageTypePhoto.isChecked = true
            "illustration" -> viewBinding.imageTypeIllustration.isChecked = true
            "vector" -> viewBinding.imageTypeVector.isChecked = true
        }

        viewBinding.btnOk.setOnClickListener {
            lifecycleScope.launch {
                requireContext().settingsPref.edit {
                    it[IMAGE_TYPE_PREF_KEY] = imageType
                }
                dismiss()
            }
        }
    }
}
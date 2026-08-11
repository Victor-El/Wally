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
import com.turingheights.wally.commons.preferencestore.ORIENTATION_PREF_KEY
import com.turingheights.wally.commons.preferencestore.settingsPref
import com.turingheights.wally.databinding.FragmentDialogOrientationBinding

class OrientationDialog : BottomSheetDialogFragment() {

    private val arg by navArgs<OrientationDialogArgs>()
    private lateinit var viewBinding: FragmentDialogOrientationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentDialogOrientationBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Wally_BottomSheetDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var orientation = arg.orientation.lowercase()

        viewBinding.orientationRadioGroup.setOnCheckedChangeListener { _, i ->
            orientation = viewBinding.root.findViewById<RadioButton>(i).text.toString()
                .lowercase()
        }

        when (arg.orientation.lowercase()) {
            "all" -> viewBinding.orientationAll.isChecked = true
            "horizontal" -> viewBinding.orientationLandscape.isChecked = true
            "vertical" -> viewBinding.orientationPortrait.isChecked = true
        }

        viewBinding.btnOk.setOnClickListener {
            lifecycleScope.launch {
                requireContext().settingsPref.edit {
                    it[ORIENTATION_PREF_KEY] = orientation
                }
                dismiss()
            }
        }
    }
}
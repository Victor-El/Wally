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
import com.turingheights.wally.commons.preferencestore.ORDER_PREF_KEY
import com.turingheights.wally.commons.preferencestore.settingsPref
import com.turingheights.wally.databinding.FragmentDialogOrderBinding

class OrderDialog : BottomSheetDialogFragment() {

    private val arg by navArgs<OrderDialogArgs>()
    private lateinit var viewBinding: FragmentDialogOrderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentDialogOrderBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Wally_BottomSheetDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var order = arg.order.lowercase()

        viewBinding.orientationRadioGroup.setOnCheckedChangeListener { _, i ->
            order = viewBinding.root.findViewById<RadioButton>(i).text.toString()
                .lowercase()
        }

        when (arg.order.lowercase()) {
            "popular" -> viewBinding.orderPopular.isChecked = true
            "latest" -> viewBinding.orderLatest.isChecked = true
        }

        viewBinding.btnOk.setOnClickListener {
            lifecycleScope.launch {
                requireContext().settingsPref.edit {
                    it[ORDER_PREF_KEY] = order
                }
                dismiss()
            }
        }
    }
}
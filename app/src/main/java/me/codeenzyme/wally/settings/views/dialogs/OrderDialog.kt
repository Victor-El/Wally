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
import me.codeenzyme.wally.commons.preferencestore.ORDER_PREF_KEY
import me.codeenzyme.wally.commons.preferencestore.ORIENTATION_PREF_KEY
import me.codeenzyme.wally.commons.preferencestore.settingsPref
import me.codeenzyme.wally.databinding.FragmentDialogOrderBinding
import me.codeenzyme.wally.databinding.FragmentDialogOrientationBinding
import java.util.*

class OrderDialog : DialogFragment() {

    private val arg by navArgs<OrderDialogArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewBinding = FragmentDialogOrderBinding.inflate(layoutInflater)
        var order = ""
        viewBinding.orientationRadioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            order = viewBinding.root.findViewById<RadioButton>(i).text.toString()
                .toLowerCase(Locale.ROOT)
        }

        when (arg.order.toLowerCase(Locale.ROOT)) {
            "popular" -> {
                viewBinding.orderPopular.isChecked = true
            }
            "latest" -> {
                viewBinding.orderLatest.isChecked = true
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.order)
            .setView(viewBinding.root)
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                if (order.trim().isNotEmpty()) {
                    lifecycleScope.launch {
                        requireContext().settingsPref.edit {
                            it[ORDER_PREF_KEY] = order
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
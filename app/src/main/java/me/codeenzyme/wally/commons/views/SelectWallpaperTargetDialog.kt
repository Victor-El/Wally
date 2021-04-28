package me.codeenzyme.wally.commons.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import me.codeenzyme.wally.R
import me.codeenzyme.wally.databinding.DialogFragmentLayoutWallpaperTargetChooserBinding

class SelectWallpaperTargetDialog: DialogFragment(R.layout.dialog_fragment_layout_wallpaper_target_chooser) {

    private lateinit var listener: SelectTargetListener

    fun bindListener(selectTargetListener: SelectTargetListener) {
        listener = selectTargetListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = DialogFragmentLayoutWallpaperTargetChooserBinding.bind(view)


        viewBinding.let {
            it.setHomeScreenTv.setOnClickListener {
                listener.onHomeSelected()
                dismiss()
            }

            it.setLockScreenWallpaper.setOnClickListener {
                listener.onLockSelected()
                dismiss()
            }

            it.setBothScreensWallpaper.setOnClickListener {
                listener.onBothSelected()
                dismiss()
            }
        }
    }

    interface SelectTargetListener {
        fun onHomeSelected()
        fun onLockSelected()
        fun onBothSelected()
    }
}
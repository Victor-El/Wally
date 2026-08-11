package com.turingheights.wally.commons.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.turingheights.wally.R
import com.turingheights.wally.databinding.DialogFragmentLayoutWallpaperTargetChooserBinding

class SelectWallpaperTargetDialog: BottomSheetDialogFragment() {

    private lateinit var listener: SelectTargetListener

    fun bindListener(selectTargetListener: SelectTargetListener) {
        listener = selectTargetListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_layout_wallpaper_target_chooser, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Wally_BottomSheetDialog)
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
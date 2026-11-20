package com.turingheights.wally.commons.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.turingheights.wally.R
import com.turingheights.wally.databinding.DialogFragmentLayoutWallpaperTargetChooserBinding

class SelectWallpaperTargetDialog : BottomSheetDialogFragment() {

    private var _viewBinding: DialogFragmentLayoutWallpaperTargetChooserBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = DialogFragmentLayoutWallpaperTargetChooserBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Wally_BottomSheetDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString(ARG_IMAGE_URL)
        if (imageUrl.isNullOrBlank()) {
            dismissAllowingStateLoss()
            return
        }

        viewBinding.setHomeScreenTv.setOnClickListener {
            deliverSelection(SetCroppedImageFragment.WallpaperTarget.HOME, imageUrl)
        }

        viewBinding.setLockScreenWallpaper.setOnClickListener {
            deliverSelection(SetCroppedImageFragment.WallpaperTarget.LOCK, imageUrl)
        }

        viewBinding.setBothScreensWallpaper.setOnClickListener {
            deliverSelection(SetCroppedImageFragment.WallpaperTarget.BOTH, imageUrl)
        }
    }

    private fun deliverSelection(
        target: SetCroppedImageFragment.WallpaperTarget,
        imageUrl: String
    ) {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_TARGET to target.name,
                RESULT_URL to imageUrl
            )
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    companion object {
        const val TAG = "SelectWallpaperTargetDialog"
        const val REQUEST_KEY = "select_wallpaper_target_request"
        const val RESULT_TARGET = "result_target"
        const val RESULT_URL = "result_url"
        private const val ARG_IMAGE_URL = "arg_image_url"

        fun newInstance(imageUrl: String): SelectWallpaperTargetDialog {
            return SelectWallpaperTargetDialog().apply {
                arguments = bundleOf(ARG_IMAGE_URL to imageUrl)
            }
        }
    }
}

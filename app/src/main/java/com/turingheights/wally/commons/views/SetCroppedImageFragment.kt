package com.turingheights.wally.commons.views

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.turingheights.wally.R
import com.turingheights.wally.commons.utils.WallyWallpaperManager
import com.turingheights.wally.databinding.FragmentSetCroppedImageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SetCroppedImageFragment : Fragment() {

    @Inject
    lateinit var wallpaperManager: WallyWallpaperManager

    private val wallpaperTarget by navArgs<SetCroppedImageFragmentArgs>()

    private var _viewBinding: FragmentSetCroppedImageBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentSetCroppedImageBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.setWallpaperRoot) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top)
            windowInsets
        }

        viewBinding.confirmClickableImage.isEnabled = false
        viewBinding.confirmClickableImage.alpha = 0.4f

        val (screenWidth, screenHeight) = getScreenDimensions()
        Timber.d("Screen dimensions: ${screenWidth}x$screenHeight")

        Glide.with(this)
            .asBitmap()
            .load(wallpaperTarget.target.url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val binding = _viewBinding ?: return
                    binding.cropImageView.setMaxCropResultSize(screenWidth, screenHeight)
                    binding.cropImageView.setImageBitmap(resource)
                    binding.cropImageProgressBar.isVisible = false
                    binding.confirmClickableImage.isEnabled = true
                    binding.confirmClickableImage.alpha = 1f
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    val binding = _viewBinding ?: return
                    binding.cropImageProgressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        R.string.error_loading_wallpapers,
                        Toast.LENGTH_SHORT
                    ).show()
                    goToHome()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    _viewBinding?.cropImageView?.clearImage()
                }
            })

        viewBinding.closeClickableImage.setOnClickListener {
            goToHome()
        }

        viewBinding.confirmClickableImage.setOnClickListener {
            val binding = _viewBinding ?: return@setOnClickListener
            val croppedImage = binding.cropImageView.croppedImage
            if (croppedImage == null) {
                Toast.makeText(
                    requireContext(),
                    R.string.wallpaper_not_set,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            binding.confirmClickableImage.isEnabled = false
            binding.cropImageProgressBar.isVisible = true

            viewLifecycleOwner.lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    when (wallpaperTarget.target.target) {
                        WallpaperTarget.HOME -> wallpaperManager.setHomeScreen(croppedImage, null)
                        WallpaperTarget.LOCK -> wallpaperManager.setLockScreen(croppedImage, null)
                        WallpaperTarget.BOTH -> wallpaperManager.setBothLockScreenAndHomeScreen(croppedImage, null)
                    }
                }

                if (!success && isAdded) {
                    Toast.makeText(
                        requireContext(),
                        R.string.wallpaper_not_set,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                if (isAdded) {
                    goToHome()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getScreenDimensions(): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = requireActivity().windowManager.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            displayMetrics.widthPixels to displayMetrics.heightPixels
        }
    }

    private fun goToHome() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    @Parcelize
    enum class WallpaperTarget : Parcelable {
        HOME, LOCK, BOTH
    }

    @Parcelize
    data class Data(
        val target: WallpaperTarget,
        val url: String
    ) : Parcelable
}

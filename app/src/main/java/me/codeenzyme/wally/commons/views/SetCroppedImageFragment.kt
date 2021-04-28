package me.codeenzyme.wally.commons.views

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import me.codeenzyme.wally.NavGraphDirections
import me.codeenzyme.wally.R
import me.codeenzyme.wally.commons.utils.WallyWallpaperManager
import me.codeenzyme.wally.databinding.FragmentSetCroppedImageBinding
import timber.log.Timber
import java.io.Serializable

@AndroidEntryPoint
class SetCroppedImageFragment : Fragment() {

    private val wallpaperTarget by navArgs<SetCroppedImageFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_cropped_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = FragmentSetCroppedImageBinding.bind(view)

        WallyWallpaperManager.wallpaperPermission(requireContext())

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        Timber.d(displayMetrics.widthPixels.toString())
        Timber.d(displayMetrics.heightPixels.toString())

        Glide.with(requireContext()).asBitmap().load(wallpaperTarget.target.url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    viewBinding.run {
                        cropImageView.setMaxCropResultSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                        cropImageView.setImageBitmap(resource)
                        cropImageProgressBar.isVisible = false
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })

        viewBinding.let {
            it.closeClickableImage.setOnClickListener {
                goToHome()
            }

            it.confirmClickableImage.setOnClickListener {
                viewBinding.cropImageProgressBar.isVisible = true
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                    val wallpaperManager = WallyWallpaperManager.getInstance(requireContext())
                    when (wallpaperTarget.target.target) {
                        WallpaperTarget.HOME -> {
                            if (!wallpaperManager.setHomeScreen(
                                    viewBinding.cropImageView.croppedImage,
                                    null
                                )
                            ) {
                                Snackbar.make(
                                    viewBinding.root,
                                    requireContext().getString(R.string.wallpaper_not_set),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                        WallpaperTarget.LOCK -> if (!wallpaperManager.setLockScreen(
                                viewBinding.cropImageView.croppedImage,
                                null
                            )
                        ) {
                            Snackbar.make(
                                viewBinding.root,
                                requireContext().getString(R.string.wallpaper_not_set),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        WallpaperTarget.BOTH -> if (!wallpaperManager.setBothLockScreenAndHomeScreen(
                                viewBinding.cropImageView.croppedImage,
                                null
                            )
                        ) {
                            Snackbar.make(
                                viewBinding.root,
                                requireContext().getString(R.string.wallpaper_not_set),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        goToHome()
                    }
                }
            }
        }
    }

    private fun goToHome() {
        // findNavController().navigate(NavGraphDirections.actionGlobalHomeFragment())
        // findNavController().navigateUp()
        findNavController().popBackStack()
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
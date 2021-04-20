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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import me.codeenzyme.wally.R
import me.codeenzyme.wally.commons.utils.WallyWallpaperManager
import me.codeenzyme.wally.databinding.FragmentSetCroppedImageBinding
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

        val displayMetrics = DisplayMetrics()

        Glide.with(requireContext()).asBitmap().load(wallpaperTarget.target.url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    viewBinding.run {
                        cropImageView.setImageBitmap(resource)
                        cropImageView.setMaxCropResultSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                        cropImageProgressBar.isVisible = false
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })

        viewBinding.let {
            it.closeClickableImage.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            it.confirmClickableImage.setOnClickListener {
                val wallpaperManager = WallyWallpaperManager.getInstance(requireContext())
                when (wallpaperTarget.target.target) {
                    WallpaperTarget.HOME -> {
                        if (wallpaperManager.setHomeScreen(
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
                    WallpaperTarget.LOCK -> if (wallpaperManager.setLockScreen(
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
                    WallpaperTarget.BOTH -> if (wallpaperManager.setBothLockScreenAndHomeScreen(
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
            }
        }
    }

    companion object {
        const val TARGET_ARG_KEY = "target"
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
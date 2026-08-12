package com.turingheights.wally.commons.views

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import com.turingheights.wally.R
import com.turingheights.wally.databinding.FragmentFullImageBinding
import com.turingheights.wally.home.viewmodels.HomeViewModel

@AndroidEntryPoint
class FullImageFragment : Fragment(R.layout.fragment_full_image) {

    private var viewBinding: FragmentFullImageBinding? = null

    private val arg by navArgs<FullImageFragmentArgs>()

    private val viewModel by viewModels<HomeViewModel>()

    private lateinit var selectWallpaperTargetDialog: SelectWallpaperTargetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectWallpaperTargetDialog = SelectWallpaperTargetDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentFullImageBinding.bind(view)

        viewBinding?.let { binding ->

            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                binding.closeClickableImage.updateLayoutParams<MarginLayoutParams> {
                    topMargin = insets.top + 16.dpToPx()
                }
                binding.actionsContainer.updateLayoutParams<MarginLayoutParams> {
                    bottomMargin = insets.bottom + 32.dpToPx()
                }
                windowInsets
            }

            binding.closeClickableImage.setOnClickListener {
                findNavController().navigateUp()
            }

            Glide.with(requireContext())
                .load(arg.photo.largeImageURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(
                    Glide.with(requireContext())
                        .load(arg.photo.webformatURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(
                            Glide.with(requireContext())
                                .load(arg.photo.previewURL)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                        )
                )
                .error(
                    Glide.with(requireContext())
                        .load(arg.photo.webformatURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(
                            Glide.with(requireContext())
                                .load(arg.photo.previewURL)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                        )
                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.cropImageProgressBar.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.cropImageProgressBar.isVisible = false
                        return false
                    }
                })
                .into(binding.photoView)

            binding.setWallpaper.setOnClickListener {
                selectWallpaperTargetDialog.bindListener(object :
                    SelectWallpaperTargetDialog.SelectTargetListener {
                    override fun onHomeSelected() {
                        findNavController().navigate(
                            FullImageFragmentDirections.actionFullImageFragmentToSetCroppedImageFragment(
                                SetCroppedImageFragment.Data(
                                    SetCroppedImageFragment.WallpaperTarget.HOME,
                                    arg.photo.largeImageURL
                                )
                            )
                        )
                    }

                    override fun onLockSelected() {
                        findNavController().navigate(
                            FullImageFragmentDirections.actionFullImageFragmentToSetCroppedImageFragment(
                                SetCroppedImageFragment.Data(
                                    SetCroppedImageFragment.WallpaperTarget.LOCK,
                                    arg.photo.largeImageURL
                                )
                            )
                        )
                    }

                    override fun onBothSelected() {
                        findNavController().navigate(
                            FullImageFragmentDirections.actionFullImageFragmentToSetCroppedImageFragment(
                                SetCroppedImageFragment.Data(
                                    SetCroppedImageFragment.WallpaperTarget.BOTH,
                                    arg.photo.largeImageURL
                                )
                            )
                        )
                    }
                })
                selectWallpaperTargetDialog.show(
                    requireActivity().supportFragmentManager,
                    "selectDialog"
                )
            }


            binding.favouriteWallpaper.setOnClickListener {
                viewModel.addToFavourites(arg.photo)
                Toast.makeText(
                    requireContext(),
                    "Adding to Favourites ... ",
                    Toast.LENGTH_SHORT
                ).show()
            }


            binding.downloadWallpaper.setOnClickListener {
                viewModel.startDownload(arg.photo.largeImageURL)
                Toast.makeText(
                    requireContext(),
                    "Downloading...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

}
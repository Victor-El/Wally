package me.codeenzyme.wally.commons.views

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import me.codeenzyme.wally.R
import me.codeenzyme.wally.databinding.FragmentFullImageBinding
import me.codeenzyme.wally.home.viewmodels.HomeViewModel
import me.codeenzyme.wally.home.views.HomeFragmentDirections

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

        viewBinding?.let {

            it.closeClickableImage.setOnClickListener {
                findNavController().navigateUp()
            }

            Glide.with(requireContext()).asBitmap().load(arg.photo.largeImageURL)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {

                        it.photoView.setImageBitmap(resource)
                        it.cropImageProgressBar.isVisible = false
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        it.cropImageProgressBar.isVisible = false
                    }
                })

            it.setWallpaper.setOnClickListener {
                selectWallpaperTargetDialog.bindListener(object :
                    SelectWallpaperTargetDialog.SelectTargetListener {
                    override fun onHomeSelected() {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToSetCroppedImageFragment(
                                SetCroppedImageFragment.Data(
                                    SetCroppedImageFragment.WallpaperTarget.HOME,
                                    arg.photo.largeImageURL
                                )
                            )
                        )
                    }

                    override fun onLockSelected() {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToSetCroppedImageFragment(
                                SetCroppedImageFragment.Data(
                                    SetCroppedImageFragment.WallpaperTarget.LOCK,
                                    arg.photo.largeImageURL
                                )
                            )
                        )
                    }

                    override fun onBothSelected() {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToSetCroppedImageFragment(
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


            it.favouriteWallpaper.setOnClickListener {
                viewModel.addToFavourites(arg.photo)
                Snackbar.make(
                    viewBinding!!.root,
                    "Downloading...",
                    Snackbar.LENGTH_LONG
                ).show()
            }


            it.downloadWallpaper.setOnClickListener {
                viewModel.startDownload(arg.photo.largeImageURL)
                Snackbar.make(
                    viewBinding!!.root,
                    "Downloading...",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

}
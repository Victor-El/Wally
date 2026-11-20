package com.turingheights.wally.commons.views

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.turingheights.wally.R
import com.turingheights.wally.commons.viewmodels.FullImageViewModel
import com.turingheights.wally.databinding.FragmentFullImageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullImageFragment : Fragment(R.layout.fragment_full_image) {

    private var viewBinding: FragmentFullImageBinding? = null

    private val arg by navArgs<FullImageFragmentArgs>()

    private val viewModel by viewModels<FullImageViewModel>()

    private var pendingDownloadUrl: String? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val url = pendingDownloadUrl
        pendingDownloadUrl = null
        if (isGranted && url != null) {
            enqueueDownload(url)
        } else if (!isGranted) {
            Toast.makeText(
                requireContext(),
                R.string.storage_permission_required,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Notification permission is optional; download proceeds regardless */ }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentFullImageBinding.bind(view)

        childFragmentManager.setFragmentResultListener(
            SelectWallpaperTargetDialog.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val targetName = bundle.getString(SelectWallpaperTargetDialog.RESULT_TARGET) ?: return@setFragmentResultListener
            val url = bundle.getString(SelectWallpaperTargetDialog.RESULT_URL) ?: return@setFragmentResultListener
            val target = runCatching {
                SetCroppedImageFragment.WallpaperTarget.valueOf(targetName)
            }.getOrNull() ?: return@setFragmentResultListener

            findNavController().navigate(
                FullImageFragmentDirections.actionFullImageFragmentToSetCroppedImageFragment(
                    SetCroppedImageFragment.Data(target, url)
                )
            )
        }

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

            Glide.with(this)
                .load(arg.photo.largeImageURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(
                    Glide.with(this)
                        .load(arg.photo.webformatURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(
                            Glide.with(this)
                                .load(arg.photo.previewURL)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                        )
                )
                .error(
                    Glide.with(this)
                        .load(arg.photo.webformatURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(
                            Glide.with(this)
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
                        viewBinding?.cropImageProgressBar?.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        viewBinding?.cropImageProgressBar?.isVisible = false
                        return false
                    }
                })
                .into(binding.photoView)

            binding.setWallpaper.setOnClickListener {
                if (childFragmentManager.findFragmentByTag(SelectWallpaperTargetDialog.TAG) == null) {
                    SelectWallpaperTargetDialog.newInstance(arg.photo.largeImageURL)
                        .show(childFragmentManager, SelectWallpaperTargetDialog.TAG)
                }
            }

            binding.favouriteWallpaper.setOnClickListener {
                viewModel.addToFavourites(arg.photo)
                Toast.makeText(
                    requireContext(),
                    R.string.added_to_favourites,
                    Toast.LENGTH_SHORT
                ).show()
            }

            binding.downloadWallpaper.setOnClickListener {
                requestDownload(arg.photo.largeImageURL)
            }
        }
    }

    private fun requestDownload(url: String) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val hasPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                pendingDownloadUrl = url
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                return
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotifPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasNotifPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        enqueueDownload(url)
    }

    private fun enqueueDownload(url: String) {
        val started = viewModel.startDownload(url)
        val messageRes = if (started) R.string.downloading else R.string.download_failed
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}

package com.turingheights.wally.settings.views

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.turingheights.wally.BuildConfig
import com.turingheights.wally.R
import com.turingheights.wally.commons.utils.ALL
import com.turingheights.wally.databinding.FragmentSettingsBinding
import com.turingheights.wally.settings.viewsmodels.SettingsViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val settingsViewModel by viewModels<SettingsViewModel>()

    private var _viewBinding: FragmentSettingsBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var orientation: String = ALL
    private var imageType: String = ALL

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _viewBinding = FragmentSettingsBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top, bottom = insets.bottom)
            windowInsets
        }

        viewBinding.run {

            actionSettingsClose.setOnClickListener {
                findNavController().navigateUp()
            }

            orientationCard.setOnClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToOrientationDialog(orientation)
                )
            }

            imageTypeCard.setOnClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToImageTypeDialog(imageType)
                )
            }

            rateUsCard.setOnClickListener {
                val packageName = requireContext().packageName
                val marketIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
                try {
                    startActivity(marketIntent)
                } catch (_: ActivityNotFoundException) {
                    val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                    safeStartActivity(webIntent)
                }
            }

            shareAppCard.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out Wally for amazing wallpapers: https://play.google.com/store/apps/details?id=${requireContext().packageName}"
                    )
                }
                safeStartActivity(Intent.createChooser(shareIntent, "Share Wally via"))
            }

            feedbackCard.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:hello@turingheights.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Wally App Feedback")
                }
                safeStartActivity(Intent.createChooser(emailIntent, "Send Feedback"))
            }

            privacyPolicyCard.setOnClickListener {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://turingheights.com/apps/wally/privacy")
                )
                safeStartActivity(browserIntent)
            }

            versionName.text = BuildConfig.VERSION_NAME
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    settingsViewModel.getOrientation(requireContext()).collect {
                        orientation = it
                        _viewBinding?.orientationState?.text = it
                    }
                }
                launch {
                    settingsViewModel.getImageType(requireContext()).collect {
                        imageType = it
                        _viewBinding?.imageTypeState?.text = it
                    }
                }
            }
        }
    }

    private fun safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_app_available, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        _viewBinding = null
        super.onDestroyView()
    }
}

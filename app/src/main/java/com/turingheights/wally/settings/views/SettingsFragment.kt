package com.turingheights.wally.settings.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.turingheights.wally.databinding.FragmentSettingsBinding
import com.turingheights.wally.settings.viewsmodels.SettingsViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val settingsViewModel by viewModels<SettingsViewModel>()

    private lateinit var orientation: String
    private lateinit var imageType: String
    private lateinit var order: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = FragmentSettingsBinding.bind(view)

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
                findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToOrientationDialog(orientation))
            }

            imageTypeCard.setOnClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToImageTypeDialog(imageType)
                )
            }

            orderCard.setOnClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToOrderDialog(order)
                )
            }

            rateUsCard.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=${requireContext().packageName}")
                }
                startActivity(intent)
            }

            shareAppCard.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Check out Wally for amazing wallpapers: https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                }
                startActivity(Intent.createChooser(shareIntent, "Share Wally via"))
            }

            feedbackCard.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:hello@turingheights.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Wally App Feedback")
                }
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
            }

            privacyPolicyCard.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://turingheights.com/apps/wally/privacy"))
                startActivity(browserIntent)
            }

            versionName.text = BuildConfig.VERSION_NAME
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                settingsViewModel.getOrientation(requireContext()).collect {
                    orientation = it
                    viewBinding.orientationState.text = it
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                settingsViewModel.getImageType(requireContext()).collect {
                    imageType = it
                    viewBinding.imageTypeState.text = it
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                settingsViewModel.getOrder(requireContext()).collect {
                    order = it
                    viewBinding.popularState.text = it
                }
            }
        }
    }
}
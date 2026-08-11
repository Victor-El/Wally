package com.turingheights.wally.settings.views

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.turingheights.wally.R
import com.turingheights.wally.databinding.FragmentSettingsBinding
import com.turingheights.wally.settings.viewsmodels.SettingsViewModel

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
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            settingsViewModel.getOrientation(requireContext()).collect {
                orientation = it
                viewBinding.orientationState.text = it
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            settingsViewModel.getImageType(requireContext()).collect {
                imageType = it
                viewBinding.imageTypeState.text = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            settingsViewModel.getOrder(requireContext()).collect {
                order = it
                viewBinding.popularState.text = it
            }
        }
    }
}
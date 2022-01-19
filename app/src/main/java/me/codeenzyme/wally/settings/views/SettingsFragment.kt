package me.codeenzyme.wally.settings.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect
import me.codeenzyme.wally.R
import me.codeenzyme.wally.databinding.FragmentSettingsBinding
import me.codeenzyme.wally.settings.viewsmodels.SettingsViewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val settingsViewModel by viewModels<SettingsViewModel>()

    private lateinit var orientation: String
    private lateinit var imageType: String
    private lateinit var order: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = FragmentSettingsBinding.bind(view)

        viewBinding.run {

            actionSettingsClose.setOnClickListener {
                findNavController().navigateUp()
            }

            safeSearchSwitch.setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setSafeSearch(requireContext(), isChecked)
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

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            settingsViewModel.getSafeSearch(requireContext()).collect {
                viewBinding.safeSearchSwitch.isChecked = it
                if (it) viewBinding.safeSearchState.text = "ON" else viewBinding.safeSearchState.text = "OFF"
            }
        }
    }
}
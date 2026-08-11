package com.turingheights.wally.home.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.turingheights.wally.R
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.commons.preferencestore.*
import com.turingheights.wally.commons.utils.ALL
import com.turingheights.wally.commons.utils.POPULAR
import com.turingheights.wally.commons.views.SelectWallpaperTargetDialog
import com.turingheights.wally.commons.views.SetCroppedImageFragment
import com.turingheights.wally.databinding.FragmentHomeBinding
import com.turingheights.wally.home.viewmodels.HomeViewModel
import com.turingheights.wally.home.views.adapters.HomePagedWallpaperAdapter

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var homeWallpaperRecyclerAdapter: HomePagedWallpaperAdapter
    private lateinit var concatAdapter: RecyclerView.Adapter<*>

    private lateinit var selectWallpaperTargetDialog: SelectWallpaperTargetDialog

    private val homeViewModel by viewModels<HomeViewModel>()
    private lateinit var viewBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentHomeBinding.bind(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.mainConstraintLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top)
            viewBinding.homeWallpaperRecyclerView.updatePadding(bottom = insets.bottom)
            windowInsets
        }

        viewBinding.run {

            actionFavourite.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToFavouritesFragment()
                )
            }

            homeSwipeRefreshLayout.setColorSchemeResources(
                R.color.golden,
                R.color.black_tint,
                R.color.dark_golden
            )
            homeSwipeRefreshLayout.setOnRefreshListener {
                viewBinding.networkErrorView.isVisible = false
                homeWallpaperRecyclerAdapter.retry()
                homeSwipeRefreshLayout.isEnabled = false
            }

            if (!::homeWallpaperRecyclerAdapter.isInitialized) {
                homeWallpaperRecyclerAdapter = HomePagedWallpaperAdapter({ photo, _, view ->
                    val popupMenu = PopupMenu(requireContext(), view).also {
                        it.inflate(R.menu.menu_home_wallpaper)
                        it.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_home_popup_download -> {
                                    homeViewModel.startDownload(photo.largeImageURL)
                                    Snackbar.make(
                                        view,
                                        "Downloading...",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }

                                R.id.action_home_popup_favourite -> {
                                    homeViewModel.addToFavourites(photo)
                                    Snackbar.make(
                                        view,
                                        "Added to favorite",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }

                                R.id.action_home_popup_set_wallpaper -> {
                                    selectWallpaperTargetDialog.bindListener(object :
                                        SelectWallpaperTargetDialog.SelectTargetListener {
                                        override fun onHomeSelected() {
                                            findNavController().navigate(
                                                HomeFragmentDirections.actionHomeFragmentToSetCroppedImageFragment(
                                                    SetCroppedImageFragment.Data(
                                                        SetCroppedImageFragment.WallpaperTarget.HOME,
                                                        photo.largeImageURL
                                                    )
                                                )
                                            )
                                        }

                                        override fun onLockSelected() {
                                            findNavController().navigate(
                                                HomeFragmentDirections.actionHomeFragmentToSetCroppedImageFragment(
                                                    SetCroppedImageFragment.Data(
                                                        SetCroppedImageFragment.WallpaperTarget.LOCK,
                                                        photo.largeImageURL
                                                    )
                                                )
                                            )
                                        }

                                        override fun onBothSelected() {
                                            findNavController().navigate(
                                                HomeFragmentDirections.actionHomeFragmentToSetCroppedImageFragment(
                                                    SetCroppedImageFragment.Data(
                                                        SetCroppedImageFragment.WallpaperTarget.BOTH,
                                                        photo.largeImageURL
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
                            }
                            true
                        }
                    }
                    popupMenu.show()
                }) {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToFullImageFragment(it)
                    )
                }

                homeWallpaperRecyclerAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

                homeWallpaperRecyclerAdapter.addLoadStateListener {
                    if (viewBinding.homeSwipeRefreshLayout.isRefreshing && it.source.append is LoadState.Loading) {
                        viewBinding.homeSwipeRefreshLayout.isRefreshing = false
                    }
                    viewBinding.noPhotosFoundView.isVisible =
                        it.append is LoadState.Loading && homeWallpaperRecyclerAdapter.itemCount == 0
                }

                val footerAdapter =
                    HomePagedWallpaperAdapter.HomeLoadStateAdapter(homeWallpaperRecyclerAdapter::retry)
                concatAdapter = homeWallpaperRecyclerAdapter.withLoadStateFooter(footerAdapter)
            }

            if (homeWallpaperRecyclerView.layoutManager == null) {
                homeWallpaperRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            homeWallpaperRecyclerView.adapter = concatAdapter

            primaryFilterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    val order = when (checkedIds.first()) {
                        R.id.chip_latest -> "latest"
                        else -> POPULAR
                    }
                    homeViewModel.updateSearchParams(order = order)
                }
            }

            categoryFilterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                val category = if (checkedIds.isNotEmpty()) {
                    val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds.first())
                    chip.text.toString()
                } else {
                    null
                }
                homeViewModel.updateSearchParams(category = category)
            }
        }

        selectWallpaperTargetDialog = SelectWallpaperTargetDialog()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.homeWallpaperFlow.collect { pagingData ->
                    homeWallpaperRecyclerAdapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                requireContext().settingsPref.data.collect {
                    val safeSearch = it[SAFE_SEARCH_PREF_KEY] ?: true
                    val orientation = it[ORIENTATION_PREF_KEY] ?: ALL
                    val imageType = it[IMAGE_TYPE_PREF_KEY] ?: ALL

                    homeViewModel.updateSearchParams(
                        safeSearch = safeSearch,
                        orientation = orientation,
                        imageType = imageType
                    )
                }
            }
        }

        startObservingNetworkState()

        viewBinding.actionSearchView.setOnEditorActionListener { tv: TextView, aID: Int, _ ->
            if (aID == EditorInfo.IME_ACTION_SEARCH) {
                homeViewModel.updateSearchParams(query = viewBinding.actionSearchView.text.toString())
                true
            } else {
                false
            }
        }

        viewBinding.actionSettings.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettingsFragment())
        }
    }

    private fun startObservingNetworkState() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            homeViewModel.getWallPaperNetworkState().collect {
                when (it) {
                    is WallpaperDataNetworkState.Loading -> viewBinding.homeProgressBar.isVisible =
                        true
                    WallpaperDataNetworkState.Success -> {
                        if (viewBinding.homeSwipeRefreshLayout.isRefreshing) {
                            viewBinding.homeSwipeRefreshLayout.isRefreshing = false
                        }
                        viewBinding.homeProgressBar.isVisible = false
                        viewBinding.homeSwipeRefreshLayout.isEnabled = false
                    }
                    WallpaperDataNetworkState.Failure -> {
                        if (viewBinding.homeSwipeRefreshLayout.isRefreshing) {
                            viewBinding.homeSwipeRefreshLayout.isRefreshing = false
                        }
                        viewBinding.homeProgressBar.isVisible = false
                        viewBinding.networkErrorView.isVisible = true
                        viewBinding.homeSwipeRefreshLayout.isEnabled = true
                    }
                }
            }
        }
    }
}

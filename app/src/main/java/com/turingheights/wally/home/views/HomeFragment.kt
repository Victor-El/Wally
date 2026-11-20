package com.turingheights.wally.home.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
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
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.turingheights.wally.R
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.commons.preferencestore.IMAGE_TYPE_PREF_KEY
import com.turingheights.wally.commons.preferencestore.ORIENTATION_PREF_KEY
import com.turingheights.wally.commons.preferencestore.SAFE_SEARCH_PREF_KEY
import com.turingheights.wally.commons.preferencestore.settingsPref
import com.turingheights.wally.commons.utils.ALL
import com.turingheights.wally.commons.utils.LATEST
import com.turingheights.wally.commons.utils.POPULAR
import com.turingheights.wally.commons.views.SelectWallpaperTargetDialog
import com.turingheights.wally.commons.views.SetCroppedImageFragment
import com.turingheights.wally.databinding.FragmentHomeBinding
import com.turingheights.wally.home.viewmodels.HomeViewModel
import com.turingheights.wally.home.views.adapters.HomePagedWallpaperAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var homeWallpaperRecyclerAdapter: HomePagedWallpaperAdapter
    private lateinit var concatAdapter: RecyclerView.Adapter<*>

    private val homeViewModel by viewModels<HomeViewModel>()

    private var _viewBinding: FragmentHomeBinding? = null
    private val viewBinding get() = _viewBinding!!

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

    private val loadStateListener: (CombinedLoadStates) -> Unit = { loadStates ->
        _viewBinding?.let { binding ->
            if (binding.homeSwipeRefreshLayout.isRefreshing && loadStates.source.refresh is LoadState.NotLoading) {
                binding.homeSwipeRefreshLayout.isRefreshing = false
            }
            val isListEmpty = loadStates.source.refresh is LoadState.NotLoading &&
                loadStates.append.endOfPaginationReached &&
                homeWallpaperRecyclerAdapter.itemCount == 0
            binding.noPhotosFoundView.isVisible = isListEmpty
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                HomeFragmentDirections.actionHomeFragmentToSetCroppedImageFragment(
                    SetCroppedImageFragment.Data(target, url)
                )
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.mainConstraintLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top)
            _viewBinding?.homeWallpaperRecyclerView?.updatePadding(bottom = insets.bottom)
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
                networkErrorView.isVisible = false
                homeWallpaperRecyclerAdapter.refresh()
            }

            networkErrorRetryBtn.setOnClickListener {
                networkErrorView.isVisible = false
                homeWallpaperRecyclerAdapter.refresh()
            }

            if (!::homeWallpaperRecyclerAdapter.isInitialized) {
                homeWallpaperRecyclerAdapter = HomePagedWallpaperAdapter({ photo, _, anchorView ->
                    val popupMenu = PopupMenu(requireContext(), anchorView).also {
                        it.inflate(R.menu.menu_home_wallpaper)
                        it.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_home_popup_download -> {
                                    requestDownload(photo.largeImageURL)
                                }

                                R.id.action_home_popup_favourite -> {
                                    homeViewModel.addToFavourites(photo)
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.added_to_favourites,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                R.id.action_home_popup_set_wallpaper -> {
                                    if (childFragmentManager.findFragmentByTag(SelectWallpaperTargetDialog.TAG) == null) {
                                        SelectWallpaperTargetDialog.newInstance(photo.largeImageURL)
                                            .show(childFragmentManager, SelectWallpaperTargetDialog.TAG)
                                    }
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

                homeWallpaperRecyclerAdapter.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

                val footerAdapter =
                    HomePagedWallpaperAdapter.HomeLoadStateAdapter(homeWallpaperRecyclerAdapter::retry)
                concatAdapter = homeWallpaperRecyclerAdapter.withLoadStateFooter(footerAdapter)
            }

            homeWallpaperRecyclerAdapter.addLoadStateListener(loadStateListener)

            if (homeWallpaperRecyclerView.layoutManager == null) {
                homeWallpaperRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            homeWallpaperRecyclerView.adapter = concatAdapter

            primaryFilterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    val order = when (checkedIds.first()) {
                        R.id.chip_latest -> LATEST
                        else -> POPULAR
                    }
                    homeViewModel.updateSearchParams(order = order)
                }
            }

            categoryFilterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                val category = checkedIds.firstOrNull()?.let { categoryApiValueForChip(it) }
                homeViewModel.updateSearchParams(category = category)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.homeWallpaperFlow.collectLatest { pagingData ->
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

        viewBinding.actionSearchView.setOnEditorActionListener { _: TextView, aID: Int, _ ->
            if (aID == EditorInfo.IME_ACTION_SEARCH) {
                val query = viewBinding.actionSearchView.text.toString().trim().ifEmpty { null }
                homeViewModel.updateSearchParams(query = query)
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.getWallPaperNetworkState().collect { state ->
                    val binding = _viewBinding ?: return@collect
                    when (state) {
                        is WallpaperDataNetworkState.Loading -> {
                            binding.networkErrorView.isVisible = false
                            binding.homeProgressBar.isVisible = true
                        }
                        WallpaperDataNetworkState.Success -> {
                            if (binding.homeSwipeRefreshLayout.isRefreshing) {
                                binding.homeSwipeRefreshLayout.isRefreshing = false
                            }
                            binding.homeProgressBar.isVisible = false
                            binding.networkErrorView.isVisible = false
                        }
                        WallpaperDataNetworkState.Failure -> {
                            if (binding.homeSwipeRefreshLayout.isRefreshing) {
                                binding.homeSwipeRefreshLayout.isRefreshing = false
                            }
                            binding.homeProgressBar.isVisible = false
                            binding.noPhotosFoundView.isVisible = false
                            binding.networkErrorView.isVisible = true
                        }
                    }
                }
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
        val started = homeViewModel.startDownload(url)
        val messageRes = if (started) R.string.downloading else R.string.download_failed
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
    }

    private fun categoryApiValueForChip(chipId: Int): String? = when (chipId) {
        R.id.chip_cat_backgrounds -> "backgrounds"
        R.id.chip_cat_fashion -> "fashion"
        R.id.chip_cat_nature -> "nature"
        R.id.chip_cat_science -> "science"
        R.id.chip_cat_education -> "education"
        R.id.chip_cat_feelings -> "feelings"
        R.id.chip_cat_health -> "health"
        R.id.chip_cat_people -> "people"
        R.id.chip_cat_religion -> "religion"
        R.id.chip_cat_places -> "places"
        R.id.chip_cat_animals -> "animals"
        R.id.chip_cat_industry -> "industry"
        R.id.chip_cat_computer -> "computer"
        R.id.chip_cat_food -> "food"
        R.id.chip_cat_sports -> "sports"
        R.id.chip_cat_transportation -> "transportation"
        R.id.chip_cat_travel -> "travel"
        R.id.chip_cat_buildings -> "buildings"
        R.id.chip_cat_business -> "business"
        R.id.chip_cat_music -> "music"
        else -> null
    }

    override fun onDestroyView() {
        if (::homeWallpaperRecyclerAdapter.isInitialized) {
            homeWallpaperRecyclerAdapter.removeLoadStateListener(loadStateListener)
        }
        _viewBinding?.homeWallpaperRecyclerView?.adapter = null
        _viewBinding = null
        super.onDestroyView()
    }
}

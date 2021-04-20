package me.codeenzyme.wally.home.views

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.codeenzyme.wally.R
import me.codeenzyme.wally.commons.models.WallpaperDataNetworkState
import me.codeenzyme.wally.commons.views.SelectWallpaperTargetDialog
import me.codeenzyme.wally.commons.views.SetCroppedImageFragment
import me.codeenzyme.wally.commons.views.SetCroppedImageFragmentArgs
import me.codeenzyme.wally.databinding.FragmentHomeBinding
import me.codeenzyme.wally.home.viewmodels.HomeViewModel
import me.codeenzyme.wally.home.views.adapters.HomePagedWallpaperAdapter
import java.io.File

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var homeWallpaperRecyclerAdapter: HomePagedWallpaperAdapter

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
        viewBinding.run {
            homeSwipeRefreshLayout.setColorSchemeResources(
                R.color.golden,
                R.color.black_tint,
                R.color.dark_golden
            )
            homeSwipeRefreshLayout.setOnRefreshListener {
                viewBinding.networkErrorView.isVisible = false
                // viewBinding.homeProgressBar.isVisible = true
                //startObservingNetworkState()
                //loadData()
                // TODO: Handle refresh while no network error
                homeWallpaperRecyclerAdapter.retry()
                homeSwipeRefreshLayout.isEnabled = false
            }
            homeWallpaperRecyclerAdapter = HomePagedWallpaperAdapter { photo, _, view ->
                val popupMenu = PopupMenu(requireContext(), view).also {
                    it.inflate(R.menu.menu_home_wallpaper)
                    it.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_home_popup_download -> {
                                Snackbar.make(
                                    viewBinding.root,
                                    "Downloading...",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                            R.id.action_home_popup_favourite -> {
                                Snackbar.make(
                                    viewBinding.root,
                                    "Adding to favourites...",
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
                                selectWallpaperTargetDialog.show(requireActivity().supportFragmentManager, "selectDialog")
                            }
                        }
                        true
                    }
                }
                popupMenu.show()
            }
            homeWallpaperRecyclerAdapter.addLoadStateListener {
                if (viewBinding.homeSwipeRefreshLayout.isRefreshing && it.source.append is LoadState.Loading) {
                    viewBinding.homeSwipeRefreshLayout.isRefreshing = false
                }
            }
            homeWallpaperRecyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            homeWallpaperRecyclerView.adapter = homeWallpaperRecyclerAdapter
        }

        selectWallpaperTargetDialog = SelectWallpaperTargetDialog()

        startObservingNetworkState()
        loadData()
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

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.homeWallpaperFlow.collectLatest {
                homeWallpaperRecyclerAdapter.submitData(it)
            }
        }
    }
}
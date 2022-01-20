package me.codeenzyme.wally.commons.views

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import me.codeenzyme.wally.R
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.commons.viewmodels.FavouritesViewModel
import me.codeenzyme.wally.databinding.FragmentFavouritesBinding
import me.codeenzyme.wally.databinding.ItemFavouritePhotoLayoutBinding

@AndroidEntryPoint
class FavouritesFragment : Fragment(R.layout.fragment_favourites) {

    private var viewBinding: FragmentFavouritesBinding? = null

    private val viewModel by viewModels<FavouritesViewModel>()

    private lateinit var adapter: FavouritesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentFavouritesBinding.bind(view)

        adapter = FavouritesAdapter ({
            findNavController().navigate(
                FavouritesFragmentDirections.actionFavouritesFragmentToFullImageFragment(it)
            )
        }) {
            viewModel.removeFavourite(it)
        }
        viewBinding?.let {
            it.favouritesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            it.favouritesRecyclerView.adapter = adapter
        }

        observe()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getFavourites().collectLatest {
                viewBinding?.let { binding ->
                    binding.progressBar.isVisible = false
                }
                adapter.submitList(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    class FavouritesAdapter(private val callback: (Photo) -> Unit, private val deleteCallback: (Photo) -> Unit): ListAdapter<Photo, FavouritesAdapter.FavouriteViewHolder>(PhotoDiffUtilItemCallback) {

        inner class FavouriteViewHolder(private val viewBinding: ItemFavouritePhotoLayoutBinding) :
            RecyclerView.ViewHolder(viewBinding.root) {

                fun bind(photo: Photo) {
                    viewBinding.let {

                        it.root.setOnClickListener {
                            callback(photo)
                        }

                        it.moreActionImageView.setOnClickListener {
                            deleteCallback(photo)
                        }

                        Glide.with(viewBinding.root.context)
                            .load(photo.webformatURL)
                            .thumbnail(Glide.with(viewBinding.root.context).load(photo.previewURL))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    viewBinding.photoShimmer.isVisible = false
                                    viewBinding.photoMain.isVisible = true
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    viewBinding.photoShimmer.isVisible = false
                                    viewBinding.photoMain.isVisible = true
                                    viewBinding.moreActionImageView.isVisible = true
                                    return false
                                }

                            }).into(viewBinding.photoItemView)
                    }
                }

            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
            val binding = ItemFavouritePhotoLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return FavouriteViewHolder(binding)
        }

        override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }

    object PhotoDiffUtilItemCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }

    }

}
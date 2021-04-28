package me.codeenzyme.wally.home.views.adapters

import android.graphics.drawable.Drawable
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import me.codeenzyme.wally.R
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.databinding.ItemPhotoLayoutBinding
import me.codeenzyme.wally.databinding.LayoutHomeLoadStateBinding
import timber.log.Timber

class HomePagedWallpaperAdapter(private val actionMoreListener: (Photo, Int, View) -> Unit) :
    PagingDataAdapter<Photo, HomePagedWallpaperAdapter.PhotoItemViewHolder>(PhotoComparator) {

    inner class PhotoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(photo: Photo, pos: Int) {
            val viewBinding = ItemPhotoLayoutBinding.bind(itemView)
            viewBinding.moreActionImageView.run {
                isVisible = false
                setOnClickListener { actionMoreListener(photo, pos, it) }
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

    object PhotoComparator : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: PhotoItemViewHolder, position: Int) {
        holder.bind(getItem(position)!!, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemViewHolder {
        val viewBinding =
            ItemPhotoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Timber.d("Created")
        return PhotoItemViewHolder(viewBinding.root)
    }

    class HomeLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<HomeLoadStateViewHolder>() {
        override fun onBindViewHolder(holder: HomeLoadStateViewHolder, loadState: LoadState) {
            (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
            holder.bind(loadState, retry)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            loadState: LoadState
        ): HomeLoadStateViewHolder {
            return HomeLoadStateViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_home_load_state, parent, false)
            )
        }

    }

    class HomeLoadStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = LayoutHomeLoadStateBinding.bind(itemView)

        fun bind(loadState: LoadState, retry: () -> Unit) {
            binding.errRetryBtn.setOnClickListener {
                retry()
            }
            /*if (loadState is LoadState.Error) {
                binding.errorMsgView.text = loadState.error.localizedMessage
            }*/

            binding.errorMsgView.isVisible = loadState is LoadState.Error
            binding.errRetryBtn.isVisible = loadState is LoadState.Error
            binding.progressBar.isVisible = loadState is LoadState.Loading
        }
    }
}
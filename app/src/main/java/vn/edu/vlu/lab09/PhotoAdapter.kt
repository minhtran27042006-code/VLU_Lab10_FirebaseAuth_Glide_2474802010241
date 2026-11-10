package vn.edu.vlu.lab09

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import vn.edu.vlu.lab09.databinding.ItemPhotoBinding

class PhotoAdapter(
    private val photoList: List<PhotoItem>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photoList[position]

        // Bind textual metadata
        holder.binding.tvTitle.text = photo.title
        holder.binding.tvAuthor.text = holder.itemView.context.getString(
            R.string.explore_photos
        ).let { "Tác giả: ${photo.author}" }

        // Execute Glide asynchronous loading with caching and cross-fade animation
        Glide.with(holder.itemView.context)
            .load(photo.imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_broken_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.ivPhoto)
    }

    override fun getItemCount(): Int = photoList.size

    /**
     * Cancel pending requests in onViewRecycled() to prevent image flickering
     * and avoid memory overflow when recycling views.
     */
    override fun onViewRecycled(holder: PhotoViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.binding.ivPhoto)
    }
}

package vn.edu.vlu.lab10

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import vn.edu.vlu.lab10.databinding.ItemPhotoBinding

/**
 * PhotoAdapter: bridge between the PhotoItem list and RecyclerView.
 * Each row is built from item_photo.xml via ItemPhotoBinding (View Binding).
 */
class PhotoAdapter(
    private val photos: List<PhotoItem>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    /**
     * ViewHolder holds the binding of a row for reuse,
     * avoiding repeated view lookups on every scroll.
     */
    class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    /** Create a new ViewHolder when RecyclerView needs to add a row. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context), // Context obtained from parent
            parent,
            false // Do not attach immediately to parent, RecyclerView manages this automatically
        )
        return PhotoViewHolder(binding)
    }

    /** Bind data of the element at [position] to the ViewHolder. */
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        // Bind text
        holder.binding.tvTitle.text = photo.title
        holder.binding.tvAuthor.text = "Author: ${photo.author}"

        // Asynchronously load images using Glide (runs on background thread, doesn't block UI)
        Glide.with(holder.itemView.context) // Tie Glide lifecycle to context
            .load(photo.imageUrl) // Target image URL
            .placeholder(R.drawable.ic_placeholder) // Display temporarily while loading
            .error(R.drawable.ic_broken_image) // Display when load fails (invalid URL, no network)
            .transition(DrawableTransitionOptions.withCrossFade()) // Smooth fade-in effect
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and transformed images on disk
            .into(holder.binding.ivPhoto) // Target ImageView
    }

    /** Total number of elements in the list. */
    override fun getItemCount(): Int = photos.size

    /**
     * When a ViewHolder is recycled, cancel any pending image load request to
     * avoid loading the wrong image into a reused row and to save memory.
     */
    override fun onViewRecycled(holder: PhotoViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.binding.ivPhoto)
    }
}

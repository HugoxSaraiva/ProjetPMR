package com.example.galeries.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.galeries.R
import com.example.galeries.data.Repository
import com.example.galeries.data.model.GalleryImage
import kotlinx.android.synthetic.main.image_item.view.*

class GaleryImageAdapter(
    private val actionListener: ActionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val galleryList: MutableList<GalleryImage> = mutableListOf()
    private val idUser: Int? = Repository.user?.idUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
                )
    }

    override fun getItemCount(): Int = galleryList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> {
                holder.bind(galleryList[position])
            }
        }
    }

    fun showData(newDataSet : List<GalleryImage>) {
        galleryList.clear()
        galleryList.addAll(newDataSet)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val deleteImage = itemView.deleteImageButton
        private val likeImage = itemView.likeButton
        private val editableImageText = itemView.editableImageText
        private val imageText = itemView.textView
        private val layout = itemView.imageConstraintLayout
        private val imageView = itemView.imageView
        fun bind(image: GalleryImage) {

            // Load image and text for any user
            imageText.apply {
                text = image.legende
                visibility = View.VISIBLE
            }
            editableImageText.visibility = View.GONE
            actionListener.onImageViewLoad(imageView,image.url)

            // Enable buttons and set listeners only if user is connected
            idUser?.let {
                likeImage.visibility = View.VISIBLE
                likeImage.isChecked = image.isLiked ?: false
                likeImage.setOnCheckedChangeListener { _, isChecked ->
                    Log.d("likeImageButton","State : $isChecked")
                    if (isChecked) {
                        actionListener.onLikeImage(image.id)
                    } else {
                        actionListener.onDislikeImage(image.id)
                    }
                }

                // Bindings if user is the owner of the image
                if (image.idUser == it){
                    // Delete Image
                    deleteImage.visibility = View.VISIBLE
                    deleteImage.setOnClickListener {
                        actionListener.onDeleteImage(image.id)
                        notifyItemRemoved(adapterPosition)
                    }

                    // Edit description
                    imageText.visibility = View.INVISIBLE
                    editableImageText.apply {
                        setText(image.legende)
                        visibility = View.VISIBLE
                        setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (image.legende != editableImageText.text.toString()) {
                                    actionListener.onImageDescriptionChange(image.id, editableImageText.text.toString())
                                    notifyItemChanged(adapterPosition)
                                }
                            }
                            false
                        }
                    }
                }
            }

            // Get appropriate margin for image layout
            val margin = idUser?.run {
                if (this == image.idUser) {
                    R.drawable.owner_border
                } else {
                    R.drawable.foreign_border
                } } ?: R.drawable.foreign_border
            layout.setBackgroundResource(margin)
        }
    }

    interface ActionListener {
        fun onImageViewLoad(imageView: ImageView, url: String)
        fun onImageDescriptionChange(imageId: Int, description: String)
        fun onDeleteImage(imageId: Int)
        fun onLikeImage(imageId: Int)
        fun onDislikeImage(imageId: Int)
    }
}
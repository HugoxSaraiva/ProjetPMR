package com.example.galeries.ui.adapter

import android.content.ClipData
import android.content.ClipDescription
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.galeries.R
import com.example.galeries.data.Repository
import com.example.galeries.data.model.Category
import com.example.galeries.data.model.GalleryImage
import kotlinx.android.synthetic.main.image_item.view.*

class GalleryImageAdapter(
    private val actionListener: ActionListener,
    private val category: Category
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val idUser: Int? = Repository.user?.idUser

    private val dataSet: MutableList<GalleryImage> = mutableListOf()

    override fun getItemCount(): Int = dataSet.size

    fun showData(newDataSet : List<GalleryImage>) {
        dataSet.clear()
        dataSet.addAll(newDataSet)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ImageViewHolder(
            inflater.inflate(R.layout.image_item, parent, false)
                )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("GalleryImageAdapter", "onBindViewHolder $position")
        when (holder) {
            is ImageViewHolder -> {
                holder.bind(dataSet[position])
            }
        }
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

        init {
            // User actions if he is connected
            idUser?.let {
                deleteImage.setOnClickListener {
                    val imagePosition = adapterPosition
                    if (imagePosition != RecyclerView.NO_POSITION) {
                        val clickedImage = dataSet[imagePosition]
                        actionListener.onDeleteImage(clickedImage.id)
                        //notifyItemRemoved(imagePosition)
                    }
                }
                editableImageText.setOnEditorActionListener { _, actionId, _ ->
                    val imagePosition = adapterPosition
                    if (
                        imagePosition != RecyclerView.NO_POSITION
                        &&
                        actionId == EditorInfo.IME_ACTION_DONE
                    ) {
                        val changedImage = dataSet[imagePosition]
                       if (changedImage.legende != editableImageText.text.toString()) {
                           actionListener.onImageDescriptionChange(
                               changedImage.id,
                               editableImageText.text.toString()
                           )
                           //notifyItemChanged(imagePosition)
                       }
                    }
                    false
                }
            }
        }

        fun bind(image: GalleryImage) {

            // Load image and text for any user
            imageText.apply {
                text = image.legende
                visibility = View.VISIBLE
            }
            editableImageText.visibility = View.GONE
            editableImageText.setText(image.legende)
            likeImage.visibility = View.GONE
            deleteImage.visibility = View.GONE

            actionListener.imageLoadRequest(imageView,image.url)

            // Visible buttons only if user is connected
            idUser?.let { userId ->
                likeImage.visibility = View.VISIBLE

                // OnCheckListener is called and could cause a loop
                likeImage.setOnCheckedChangeListener(null)
                likeImage.isChecked = image.isLiked
                likeImage.setOnCheckedChangeListener { _, isChecked ->
                    val imagePosition = adapterPosition
                    if (imagePosition != RecyclerView.NO_POSITION) {
                        val clickedImage = dataSet[imagePosition]
                        Log.d("likeImageButton","State : $isChecked")
                        actionListener.onLikeImage(clickedImage.id, isChecked)
                    }
                }

                // Enable those views if user is the owner of the image
                if (image.idUser == userId){
                    // Delete Image
                    if (!category.estCoupsDeCoeur) {
                        deleteImage.visibility = View.VISIBLE
                    }

                    // Edit description
                    imageText.visibility = View.INVISIBLE
                    editableImageText.visibility = View.VISIBLE

                    // Long click listener to drag functionality
                    imageView.setOnLongClickListener {
                        val imagePosition = adapterPosition
                        val clipText = "${image.id},$imagePosition"
                        val item = ClipData.Item(clipText)
                        val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        val data = ClipData(clipText, mimeTypes, item)
                        val dragShadowBuilder = View.DragShadowBuilder(it)
                        it.startDragAndDrop(data ,dragShadowBuilder, it,0)
                        true
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
        fun imageLoadRequest(imageView: ImageView, url: String)
        fun onImageDescriptionChange(imageId: Int, description: String)
        fun onDeleteImage(imageId: Int)
        fun onLikeImage(imageId: Int, isChecked: Boolean)
    }
}
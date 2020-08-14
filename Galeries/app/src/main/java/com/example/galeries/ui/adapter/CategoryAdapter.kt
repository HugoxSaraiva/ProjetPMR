package com.example.galeries.ui.adapter

import android.content.ClipDescription
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.galeries.R
import com.example.galeries.data.Repository
import com.example.galeries.data.model.Category
import com.example.galeries.utilities.UseMode
import com.example.galeries.utilities.getMarginColor
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.category_item_list.view.*

class CategoryAdapter(
    private val actionListener: ActionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), GalleryImageAdapter.ActionListener {

    private val dataSet: MutableList<Category> = mutableListOf()
    private val idUser: Int? = Repository.user?.idUser
    private fun dragListener(onDrop: (Int, Int) -> Unit) = View.OnDragListener { view, event ->
        when(event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // Checks if the view can receive the item
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> {
                view.invalidate()
                true
            }
            DragEvent.ACTION_DROP -> {
                val item = event.clipData.getItemAt(0)
                val data = item.text.toString().split(",")
                val imageId = data[0].toInt()
                val position = data[1].toInt()
                Log.d("DraggedData", "Drag Contents : $imageId")

                view.invalidate()

//                val v = event.localState as View
//                val owner = v.parent as ViewGroup
//                owner.removeView(v)
//                view as ConstraintLayout

                onDrop(imageId, position)
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                view.invalidate()
                true
            }
            else -> false
        }
    }


    override fun getItemCount(): Int = dataSet.size

    fun showData(newDataSet : List<Category>) {
        dataSet.clear()
        dataSet.addAll(newDataSet)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return dataSet[position].useMode.value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            UseMode.LIST.value -> {
                ShowViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.category_item_show, parent, false)
                )
            }
            UseMode.EDIT.value -> {
                EditViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.category_item_edit, parent, false)
                )
            }
            UseMode.SHOW.value -> {
                ShowViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.category_item_show, parent, false)
                )
            }
            else -> {
                ShowViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_item_show, parent, false)
            )}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("ListAdapter", "onBindViewHolder $position")
        when (holder) {
            is EditViewHolder -> {
                holder.bind(dataSet[position])
            }
            is ShowViewHolder -> {
                holder.bind(dataSet[position])
            }
        }

    }

    inner class EditViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // View only visible on EditMode by clicking on the Edit button
        private val categoryName = itemView.findViewById<TextInputEditText>(R.id.categoryNameText)
        private val deleteCategory = itemView.findViewById<ImageButton>(R.id.deleteCategoryButton)
        private val editLayout = itemView.findViewById<ConstraintLayout>(R.id.editConstraintLayout)

        val category : Category?
            get() {
                val categoryPosition = adapterPosition
                return if (categoryPosition != RecyclerView.NO_POSITION) {
                    dataSet[categoryPosition]
                } else {
                    null
                }
            }

        fun bind(category: Category){
            deleteCategory.setOnClickListener {
                actionListener.onDeleteCategory(category.id)
            }
            categoryName.setText(category.titre)
            categoryName.setOnEditorActionListener{ _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    Log.d("EditViewHolder","Action received for category ${category.titre} with text ${categoryName.text.toString()}")
                    if (category.titre != categoryName.text.toString()) {
                        actionListener.categoryTitleChanged(category.id, categoryName.text.toString())
                    }
                }
                false
            }
            categoryName.setOnClickListener{
                itemView.requestFocus()
            }
            editLayout.setBackgroundResource(getMarginColor(idUser,category.idUser))
        }
    }

    inner class ShowViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // List Mode
        private val listMainLayout = itemView.listConstraintLayout
        private val categoryShowButton: Button = itemView.categoryButton
        private val layout = itemView.listFrameLayout

        // Show Mode buttons
        private val showMainLayout = itemView.findViewById<ConstraintLayout>(R.id.showConstraintLayout)
        private val addImageLayout = itemView.findViewById<ConstraintLayout>(R.id.addImageConstraintLayout)
        private val imageRecyclerView = itemView.findViewById<RecyclerView>(R.id.imageList)
        private val categoryHideButton = itemView.findViewById<Button>(R.id.categoryHideButton)
        private val showAddImageLayoutButton = itemView.findViewById<ImageButton>(R.id.imageButton)
        private val addImageButton = itemView.findViewById<Button>(R.id.addImageButton)
        private val urlText = itemView.findViewById<TextInputEditText>(R.id.urlTextInput)
        private val descriptionText = itemView.findViewById<TextInputEditText>(R.id.descriptionTextInput)
        private val dropLayout = itemView.findViewById<ConstraintLayout>(R.id.dropItemBoundary)

        val category : Category?
            get() {
                val categoryPosition = adapterPosition
                return if (categoryPosition != RecyclerView.NO_POSITION) {
                    dataSet[categoryPosition]
                } else {
                    null
                }
            }

        // List Init
        init {
            categoryShowButton.setOnClickListener {
                val categoryPosition = adapterPosition
                if (categoryPosition != RecyclerView.NO_POSITION) {
                    val clickedCategory = dataSet[categoryPosition]
                    //toggleLayout(UseMode.SHOW)
                    actionListener.onCategoryClick(clickedCategory,UseMode.SHOW)
                    notifyItemChanged(categoryPosition)
                }
            }
            listMainLayout.visibility = View.VISIBLE
            showMainLayout.visibility = View.GONE
        }

        // Show init
        init {
            categoryHideButton.setOnClickListener {
                val categoryItemPosition = adapterPosition
                if (categoryItemPosition != RecyclerView.NO_POSITION) {
                    val clickedCategory = dataSet[categoryItemPosition]
                    actionListener.onCategoryClick(clickedCategory,UseMode.LIST)
                    notifyItemChanged(categoryItemPosition)

                }
            }
            idUser?.let {
                addImageButton.setOnClickListener {
                    val categoryItemPosition = adapterPosition
                    if (categoryItemPosition != RecyclerView.NO_POSITION) {
                        val clickedCategoryItem = dataSet[categoryItemPosition]
                        actionListener.addImage(
                            clickedCategoryItem.id, urlText.text.toString(),
                            descriptionText.text.toString()
                        )
                        urlText.setText("")
                        descriptionText.setText("")
                    }

                }
                showAddImageLayoutButton.setOnClickListener {
                    addImageLayout.visibility =
                        if (addImageLayout.visibility == View.GONE) {View.VISIBLE} else {View.GONE}
                }
            }
        }

        fun bind(category: Category){
            val listVisibility = if (category.useMode == UseMode.LIST) {View.VISIBLE} else {View.GONE}
            val showVisibility = if (category.useMode == UseMode.SHOW) {View.VISIBLE} else {View.GONE}
            Log.d("bindCategory","categoryId : ${category.id} with List visibilitty = $listVisibility and ShowVisibility = $showVisibility")
            listMainLayout.visibility = listVisibility
            showMainLayout.visibility = showVisibility

            if (showMainLayout.visibility == View.VISIBLE) {
                actionListener.bindAdapterToImageList(imageRecyclerView)
            }

            // List mode bindings
            categoryShowButton.text = category.titre
            if (category.estCoupsDeCoeur) {
                layout.setBackgroundResource(R.drawable.coups_de_coeur_border)
            } else {
                layout.setBackgroundResource(getMarginColor(idUser, category.idUser))
            }

            // Button used to toggle between list and show views on the show view
            categoryHideButton.text = category.titre

            // If user is connected the button to show the addImageLayout must be visible
            // Button must not be available for the Coups de Coeur category
            if (!category.estCoupsDeCoeur && idUser != null) {
                showAddImageLayoutButton.visibility = View.VISIBLE

                // Set category layout appropriate margin color
                showMainLayout.setBackgroundResource(getMarginColor(idUser, category.idUser))

                // Set drag listener for non-Coups de coeur category
                dropLayout.setOnDragListener(dragListener{imageId, position ->
                    actionListener.imageDroppedOnCategory(imageId, category.id)
                    val adapter = imageRecyclerView.adapter as GalleryImageAdapter
                    adapter.notifyItemRemoved(position)
                })
            } else if (category.estCoupsDeCoeur) {
                showAddImageLayoutButton.visibility = View.GONE
                // Set category layout appropriate margin color
                showMainLayout.setBackgroundResource(R.drawable.coups_de_coeur_border)
            } else {
                showAddImageLayoutButton.visibility = View.GONE
                // Set category layout appropriate margin color
                showMainLayout.setBackgroundResource(R.drawable.foreign_border)
            }

            // Load recyclerView adapter
            imageRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context,RecyclerView.HORIZONTAL,false)
                adapter = newGalleryImageAdapter(category)
            }
        }

        private fun newGalleryImageAdapter(category: Category): GalleryImageAdapter {
            return GalleryImageAdapter(
                actionListener = this@CategoryAdapter,
                category = category
            )
        }

    }
    override fun imageLoadRequest(imageView: ImageView, url: String) {
        actionListener.onImageViewLoad(imageView,url)
    }

    override fun onImageDescriptionChange(imageId: Int, description: String) {
        actionListener.onImageDescriptionChange(imageId,description)
    }

    override fun onDeleteImage(imageId: Int) {
        actionListener.onDeleteImage(imageId)
    }

    override fun onLikeImage(imageId: Int, isChecked: Boolean) {
        actionListener.onLikeImage(imageId, isChecked)
    }

    interface ActionListener {
        fun onCategoryClick(
            category: Category,
            toMode: UseMode
        )
        fun categoryTitleChanged(categoryId: Int, newTitle: String)
        fun addImage(categoryId: Int, url: String, description: String)
        fun onImageViewLoad(imageView: ImageView, url: String)
        fun onImageDescriptionChange(imageId: Int, description: String)
        fun onDeleteImage(imageId: Int)
        fun onLikeImage(imageId: Int, isChecked: Boolean)
        fun onDeleteCategory(categoryId: Int)
        fun bindAdapterToImageList(recyclerView: RecyclerView)
        fun clearImagesList()
        fun imageDroppedOnCategory(
            imageId: Int,
            categoryId: Int
        )
    }


}
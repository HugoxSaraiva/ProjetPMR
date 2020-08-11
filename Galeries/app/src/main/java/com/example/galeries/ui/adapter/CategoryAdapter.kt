package com.example.galeries.ui.adapter

import android.util.Log
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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), GaleryImageAdapter.ActionListener {

    private val categoryList: MutableList<Category> = mutableListOf()
    private val idUser: Int? = Repository.user?.idUser

    override fun getItemCount(): Int = categoryList.size

    fun showData(newDataSet : List<Category>) {
        categoryList.clear()
        categoryList.addAll(newDataSet)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return categoryList[position].useMode.value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            UseMode.LIST.value -> {
                ListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.category_item_list, parent, false)
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
                ListViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_item_list, parent, false)
            )}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("ListAdapter", "onBindViewHolder $position")
        when (holder) {
            is ListViewHolder -> {
                holder.bind(categoryList[position])
            }
            is EditViewHolder -> {
                holder.bind(categoryList[position])
            }
            is ShowViewHolder -> {
                holder.bind(categoryList[position])
            }
        }

    }

    inner class ListViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val categoryButton: Button = itemView.categoryButton
        private val layout = itemView.listFrameLayout

        fun bind(category: Category) {
            categoryButton.text = category.titre
            categoryButton.setOnClickListener {
                toggleLayout(category)
                actionListener.onItemClicked(category)
            }
            if (category.estCoupsDeCoeur) {
                layout.setBackgroundResource(R.drawable.coups_de_coeur_border)
            } else {
                layout.setBackgroundResource(getMarginColor(idUser, category.idUser))
            }
        }

        private fun toggleLayout(category: Category) {
            actionListener.forceOthersToListMode()
            category.useMode = UseMode.SHOW
            Log.d("ListViewHolder","${category.id} on Toggle to : ${category.useMode}")
        }
    }

    inner class EditViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // View only visible on EditMode by clicking on the Edit button
        private val categoryName = itemView.findViewById<TextInputEditText>(R.id.categoryNameText)
        private val deleteCategory = itemView.findViewById<ImageButton>(R.id.deleteCategoryButton)
        private val editLayout = itemView.findViewById<ConstraintLayout>(R.id.editConstraintLayout)

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
        private val addImageLayout = itemView.findViewById<ConstraintLayout>(R.id.addImageConstraintLayout)
        private val imageRecyclerView = itemView.findViewById<RecyclerView>(R.id.imageList)
        private val categoryHideButton = itemView.findViewById<Button>(R.id.categoryHideButton)
        private val showAddImageLayoutButton = itemView.findViewById<ImageButton>(R.id.imageButton)
        private val addImageButton = itemView.findViewById<Button>(R.id.addImageButton)
        private val urlText = itemView.findViewById<TextInputEditText>(R.id.urlTextInput)
        private val descriptionText = itemView.findViewById<TextInputEditText>(R.id.descriptionTextInput)
        private val showLayout = itemView.findViewById<ConstraintLayout>(R.id.showConstraintLayout)
        private val galleryImageAdapter = newGalleryImageAdapter()

        init {
            // Load recyclerView adapter
            imageRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context,RecyclerView.HORIZONTAL,false)
                adapter = galleryImageAdapter
            }
        }


        fun bind(category: Category){

            // Button used to toggle between list and show views
            categoryHideButton.text = category.titre
            categoryHideButton.setOnClickListener {
                actionListener.onItemClicked(category)
                toggleLayout(category)
            }

            // If user is connected the button to show the addImageLayout must be visible
            idUser?.let {
                showAddImageLayoutButton.visibility = View.VISIBLE
                showAddImageLayoutButton.setOnClickListener {
                    addImageLayout.visibility =
                        if (addImageLayout.visibility == View.GONE) {View.VISIBLE} else {View.GONE}
                }
                addImageButton.setOnClickListener {
                    actionListener.addImage(
                        category.id, urlText.text.toString(),
                        descriptionText.text.toString()
                    )
                    urlText.setText("")
                    descriptionText.setText("")
                }
            }

            // Set category layout appropriate margin color
            if (category.estCoupsDeCoeur) {
                showLayout.setBackgroundResource(R.drawable.coups_de_coeur_border)
            } else {
                showLayout.setBackgroundResource(getMarginColor(idUser, category.idUser))
            }

            // Get image URL for given category
            actionListener.bindAdapterToImageList(
                galleryImageAdapter,
                imageRecyclerView
            )
        }

        private fun toggleLayout(category: Category) {
            actionListener.clearImagesList()
            category.useMode = UseMode.LIST
            Log.d("ShowViewHolder","onToggle to : ${category.useMode}")
        }

        private fun newGalleryImageAdapter(): GaleryImageAdapter {
            return GaleryImageAdapter(
                actionListener = this@CategoryAdapter
            )
        }

    }
    override fun onImageViewLoad(imageView: ImageView, url: String) {
        actionListener.onImageViewLoad(imageView,url)
    }

    override fun onImageDescriptionChange(imageId: Int, description: String) {
        actionListener.onImageDescriptionChange(imageId,description)
    }

    override fun onDeleteImage(imageId: Int) {
        actionListener.onDeleteImage(imageId)
    }

    override fun onLikeImage(imageId: Int) {
        actionListener.onLikeImage(imageId)
    }

    override fun onDislikeImage(imageId: Int) {
        actionListener.onDislikeImage(imageId)
    }

    interface ActionListener {
        fun onItemClicked(category: Category)
        fun categoryTitleChanged(categoryId: Int, newTitle: String)
        fun addImage(categoryId: Int, url: String, description: String)
        fun onImageViewLoad(imageView: ImageView, url: String)
        fun onImageDescriptionChange(imageId: Int, description: String)
        fun onDeleteImage(imageId: Int)
        fun onLikeImage(imageId: Int)
        fun onDislikeImage(imageId: Int)
        fun onDeleteCategory(categoryId: Int)
        fun bindAdapterToImageList(
            adapter: GaleryImageAdapter,
            recyclerView: RecyclerView
        )
        fun forceOthersToListMode()
        fun clearImagesList()
    }


}
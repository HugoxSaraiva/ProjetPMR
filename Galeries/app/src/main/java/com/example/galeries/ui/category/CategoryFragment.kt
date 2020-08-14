package com.example.galeries.ui.category

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.galeries.R
import com.example.galeries.data.Repository
import com.example.galeries.data.model.Category
import com.example.galeries.ui.adapter.CategoryAdapter
import com.example.galeries.ui.adapter.GalleryImageAdapter
import com.example.galeries.utilities.UseMode
import com.example.galeries.utilities.hideKeyboard
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_show_category.*
import kotlinx.coroutines.*

class CategoryFragment : Fragment(), CategoryAdapter.ActionListener {
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var navController: NavController
    private lateinit var categoryLayout : ConstraintLayout
    private lateinit var preferences : SharedPreferences
    private val categoryAdapter = newCategoryAdapter()
    private val activityScope = CoroutineScope(
        SupervisorJob()
                + Dispatchers.Main
                + CoroutineExceptionHandler { _, throwable ->
            Log.e("CategoryFragment", "CoroutineExceptionHandler : ${throwable.message}")
        }
    )
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        categoryLayout = view.findViewById(R.id.addCategoryLayout)
        val addCategoryButton = view.findViewById<Button>(R.id.addCategoryButton)
        val addCategoryText = view.findViewById<TextInputEditText>(R.id.categoryInputText)
        recyclerView = view.findViewById(R.id.list)
        navController = Navigation.findNavController(view)
        preferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        addCategoryButton.setOnClickListener {
            if (!addCategoryText.text.isNullOrBlank()) {
                hideKeyboard()
                categoryViewModel.addCategory(addCategoryText.text.toString())
            }
        }

        categoryViewModel.categoryListResult.observe(viewLifecycleOwner,
            Observer { categoryList ->
                if (categoryList == null) {
                    return@Observer
                }
                loadLists(categoryList)
            }
            )

        initCategoryRecyclerView()

        categoryViewModel.getCategoriesList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar, menu)
        menu.getItem(1).isVisible = true
        categoryViewModel.user?.let { menu.getItem(0).isVisible = true }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> run {
                categoryViewModel.logout()
                preferences.edit().clear().apply()
                navController.navigate(R.id.action_global_loginFragment)
            }
            R.id.modifyAction -> kotlin.run {
                toggleEditMode()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCategoryClick(
        category: Category,
        toMode: UseMode
    ) {
        categoryViewModel.setMode(category.id, toMode)
        if (toMode == UseMode.SHOW) {
            categoryViewModel.getImagesList(category.id)
        }
        updateRecyclerView()
        Log.d("onItemShowClick","Category : ${category.id}, mode : $toMode")

    }

    override fun categoryTitleChanged(categoryId: Int, newTitle: String) {
        hideKeyboard()
        Log.d("CategoryTitleChanged", "Changed to : $newTitle")
        categoryViewModel.categoryTitleChanged(categoryId,newTitle)
    }


    override fun addImage(categoryId: Int, url: String, description: String) {
        hideKeyboard()
        categoryViewModel.addImage(categoryId,url,description)
    }

    private fun initCategoryRecyclerView() {
        list.apply {
            layoutManager = LinearLayoutManager(this@CategoryFragment.context)
            adapter = this@CategoryFragment.categoryAdapter
        }
    }

    private fun newCategoryAdapter(): CategoryAdapter {
        return CategoryAdapter(
            actionListener = this
        )
    }

    private fun loadLists(categoryList: List<Category>) {
        activityScope.launch {
            categoryAdapter.showData(categoryList)
        }
    }

    private fun toggleEditMode() {
        categoryViewModel.toggleEditMode()
        // Show the add category menu
        if (Repository.isLoggedIn && categoryLayout.visibility != View.VISIBLE ) {
            categoryLayout.visibility = View.VISIBLE
        } else {
            categoryLayout.visibility = View.GONE
        }
        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
        categoryAdapter.notifyDataSetChanged()
        recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onImageViewLoad(imageView: ImageView, url: String) {
        Log.d("imageLoad", "url : $url")
        Glide.with(this)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(imageView)
    }

    override fun onImageDescriptionChange(imageId: Int, description: String) {
        categoryViewModel.editImageText(imageId, description)
        Log.d("imageDescriptionChanged","Description changed to $description")
    }

    override fun onDeleteImage(imageId: Int) {
        categoryViewModel.deleteImage(imageId)
        Log.d("imageDeleted","image id : $imageId")
    }

    override fun onLikeImage(imageId: Int, isChecked: Boolean) {
        if (isChecked) {
            categoryViewModel.likeImage(imageId)
            Log.d("imageLiked","image id : $imageId")
        } else {
            categoryViewModel.dislikeImage(imageId)
            Log.d("imageDisliked","image id : $imageId")
        }

    }

    override fun onDeleteCategory(categoryId: Int) {
        categoryViewModel.deleteCategory(categoryId)
        Log.d("categoryDeleted","category id : $categoryId")
    }

    override fun bindAdapterToImageList(
        recyclerView: RecyclerView
    ) {
        // Remove previous observers to the imageList
        if (categoryViewModel.imageListResult.hasObservers()) {
            categoryViewModel.imageListResult.removeObservers(viewLifecycleOwner)
        }
        // Set new observer for current category
        categoryViewModel.imageListResult.observe(viewLifecycleOwner,
            Observer {galleryImageList ->
                if (galleryImageList == null){
                    recyclerView.visibility = View.INVISIBLE
                    return@Observer
                }
                val adapter = recyclerView.adapter as GalleryImageAdapter
                if (recyclerView.visibility != View.VISIBLE) {
                    adapter.showData(galleryImageList)
                    recyclerView.visibility = View.VISIBLE
                } else {
                    adapter.notifyDataSetChanged()
                }
            }
        )
    }

//    override fun forceOthersToListMode() {
//        categoryViewModel.toggleListMode()
//    }

    override fun clearImagesList() {
        categoryViewModel.clearImagesList()
        updateRecyclerView()
    }

    override fun imageDroppedOnCategory(
        imageId: Int,
        categoryId: Int
    ) {
        categoryViewModel.changeImageCategory(imageId, categoryId)
        Log.d("CategoryChange","Changing image $imageId to category $categoryId")

    }

}
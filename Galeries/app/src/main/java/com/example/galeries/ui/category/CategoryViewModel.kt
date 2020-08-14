package com.example.galeries.ui.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.galeries.data.Repository
import com.example.galeries.data.Result
import com.example.galeries.data.model.Category
import com.example.galeries.data.model.GalleryImage
import com.example.galeries.utilities.UseMode

class CategoryViewModel: ViewModel() {
    private val repository = Repository

    private val _categoryListResult = MutableLiveData<List<Category>>()
    val categoryListResult: LiveData<List<Category>> = _categoryListResult

    private val _imageListResult = MutableLiveData<List<GalleryImage>>()
    val imageListResult: LiveData<List<GalleryImage>> = _imageListResult

    val user = repository.user

    fun getCategoriesList() {
        repository.getCategories {
            if (it is Result.Success) {
                Log.d("getCategoriesSuccess","Data : ${it.data}")
                _categoryListResult.value =
                    it.data
            } else {
                Log.d("getCategoriesList","Error during category list request")
            }
        }
    }

    fun getImagesList(categoryId: Int) {
        repository.getImages(categoryId) {
            if (it is Result.Success) {
                _imageListResult.value =
                    it.data
            } else {
                Log.d("getImagesList","Error during category list request")
            }
        }
    }

    fun addCategory(title: String) {
        Repository.addCategory(title){
            if (it is Result.Success){
                getCachedCategoryList()
            }
        }
    }

    fun deleteCategory(categoryId: Int) {
        Repository.deleteCategory(categoryId){
            if (it is Result.Success) {
                getCachedCategoryList()
            } else {
                Log.d("deleteCategory", "Error during delete category request")
            }
        }
    }

    fun categoryTitleChanged(categoryId: Int, newTitle: String) {
        Repository.renameCategory(categoryId,newTitle){
            if (it is Result.Success){
                getCachedCategoryList()
            }
        }
    }

    fun addImage(categoryId: Int, url: String, description: String){
        Repository.addImage(categoryId,url,description){
            if (it is Result.Success) {
                getCachedImageList()
            }
        }
    }

    fun editImageText(imageId: Int, newText: String) {
        repository.editImageText(imageId,newText){
            if (it is Result.Success) {
                getCachedImageList()
            }
        }
    }

    fun likeImage(imageId: Int) {
        repository.likeImage(imageId){
            if (it is Result.Success) {
                getCachedImageList()
            }
        }
    }

    fun dislikeImage(imageId: Int) {
        repository.dislikeImage(imageId){
            if (it is Result.Success) {
                getCachedImageList()
            }
        }
    }

    fun deleteImage(imageId: Int) {
        repository.deleteImage(imageId){
            if (it is Result.Success) {
                getCachedImageList()
            }
        }
    }

    fun changeImageCategory(imageId: Int, categoryId: Int) {
        repository.changeImageCategory(imageId, categoryId){
            if (it is Result.Success) {
                Log.d("ChangeCategory", "Success changing image, getting cached image list")
                getCachedImageList()
            }
        }
    }

    fun toggleEditMode() {
        // Set use mode for the categories
        _categoryListResult.value?.forEach {category ->
            val useMode = user?.idUser?.let {
                if (it == category.idUser && category.useMode != UseMode.EDIT && !category.estCoupsDeCoeur) {
                    UseMode.EDIT
                } else {
                    UseMode.LIST
                }
            }
            category.useMode = useMode ?: UseMode.LIST
        }
    }

    fun setMode(categoryId: Int, mode: UseMode){
        // Sets other categories to List mode and then sets the selected category to showMode
        toggleListMode()
        if (mode == UseMode.SHOW) {
            _categoryListResult.value?.first { it.id == categoryId }.apply {
                this?.useMode = mode
            }
        }
    }

    private fun toggleListMode() {
        _categoryListResult.value?.forEach { category ->
            if (category.useMode == UseMode.SHOW) {
                category.useMode = UseMode.LIST
            }
        }
        clearImagesList()
    }


    fun clearImagesList(){
        _imageListResult.value = null
    }

    fun logout() {
        repository.logout()
    }

    private fun getCachedCategoryList(): List<Category> {
        _categoryListResult.value = null
        _categoryListResult.value = repository.categoryList
        return repository.categoryList
    }

    private fun getCachedImageList(): List<GalleryImage> {
        _imageListResult.value = null
        _imageListResult.value = repository.imageList
        return repository.imageList
    }

}
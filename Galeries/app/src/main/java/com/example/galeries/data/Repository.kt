package com.example.galeries.data

import android.util.Log
import com.example.galeries.data.api.responses.LoggedInUser
import com.example.galeries.data.api.responses.SingleCategoryResponse
import com.example.galeries.data.model.Category
import com.example.galeries.data.model.GalleryImage
import com.example.galeries.utilities.UseMode
import java.io.IOException

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

object Repository {
    private val dataSource = DataSource()
    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set
    var categoryList = mutableListOf<Category>()
        private set

    var imageList = mutableListOf<GalleryImage>()
        private set

    private var coupsDeCoeur : Category? = null

    val isLoggedIn: Boolean
        get() = user != null

    fun logout() {
        user = null
        categoryList.clear()
        imageList.clear()
    }

    fun login(username: String, password: String, onResult: (Result<LoggedInUser>) -> Unit) {
        // handle login
        dataSource.login(username, password){
            if (it is Result.Success) {
                setLoggedInUser(it.data)
            }
            onResult(it)
        }
    }

    fun getCategories(onResult: (Result<List<Category>>) -> Unit) {
        // handle category list request
        dataSource.getCategories(user?.hash) { result ->
            if (result is Result.Success) {
                // Conditioning data to display in list mode and identifying CoupsDeCoeur category
                result.data.forEach {
                    it.useMode = UseMode.LIST
                    it.estCoupsDeCoeur = (it.titre == "Coups de coeur")
                }
                val mutableList = mutableListOf<Category>()
                mutableList.apply {
                    addAll(result.data)
                }
                user?.let {
                    coupsDeCoeur = result.data.first { it.estCoupsDeCoeur }.also {
                        mutableList.remove(it)
                        mutableList.add(0,it)
                    }
                }
                setCategoryList(mutableList)
                onResult(Result.Success(mutableList))
            } else {
                onResult(result)
            }

        }
    }

    fun getImages(categoryId: Int, onResult: (Result<List<GalleryImage>>) -> Unit) {
        dataSource.getImages(categoryId) {
            if (it is Result.Success) {
                setImageList(it.data)
                Log.d("getImages","Function call, result : ${it.data}")
            }
            onResult(it)
        }
    }

    fun addCategory(title: String, onResult: (Result<Category>) -> Unit) {
        dataSource.addCategory(title, user!!.hash){
            if (it is Result.Success){
                val id = it.data.categorie.id
                val currentTitle = it.data.categorie.titre
                val coupdecoeur = it.data.categorie.coupdecoeur
                val categoryItem = Category(
                    id = id,
                    titre = currentTitle,
                    idUser = user!!.idUser,
                    useMode = UseMode.LIST,
                    estCoupsDeCoeur = (coupdecoeur == 1)
                )
                categoryList.add(categoryItem)
                onResult(Result.Success(categoryItem))
            } else {
                onResult(Result.Error(IOException("Couldn't add category")))
            }
        }
    }

    fun deleteCategory(categoryId: Int, onResult: (Result<Boolean>) -> Unit) {
        dataSource.deleteCategory(categoryId, user!!.hash){ result ->
            if (result is Result.Success){
                categoryList.remove(categoryList.first { it.id == categoryId })
            }
            onResult(result)
        }
    }

    fun renameCategory(categoryId: Int, newName: String, onResult: (Result<SingleCategoryResponse>) -> Unit){
        dataSource.renameCategory(categoryId, newName, user!!.hash){ result ->
            if (result is Result.Success) {
                categoryList.first { it.id == categoryId }.titre = newName
            }
            onResult(result)
        }
    }

    fun addImage(categoryId: Int, url: String, description: String, onResult: (Result<GalleryImage>) -> Unit) {
        dataSource.addImage(categoryId, description, url, user?.hash.toString()){
            if (it is Result.Success){
                imageList.add(it.data)
            }
            onResult(it)
        }
    }

    fun editImageText(imageId: Int, newText: String, onResult: (Result<GalleryImage>) -> Unit) {
        dataSource.editImageText(imageId, newText, user?.hash.toString()){ result ->
            if (result is Result.Success){
                imageList.first { it.id == imageId }.legende = newText
            }
            onResult(result)
        }
    }

    fun likeImage(imageId: Int, onResult: (Result<GalleryImage>) -> Unit) {
        dataSource.likeImage(imageId, user?.hash.toString()){ result ->
            if (result is Result.Success) {
                imageList.first { it.id == result.data.id }.isLiked = true
            }
            onResult(result)
        }
    }

    fun dislikeImage(imageId: Int, onResult: (Result<GalleryImage>) -> Unit) {
        dataSource.dislikeImage(imageId, user?.hash.toString()){ result ->
            if (result is Result.Success) {
                imageList.first { it.id == result.data.id }.isLiked = false
            }
            onResult(result)
        }
    }

    fun deleteImage(imageId: Int, onResult: (Result<Boolean>) -> Unit) {
        dataSource.deleteImage(imageId, user?.hash.toString()){ result ->
            if (result is Result.Success) {
                imageList.remove(imageList.first { it.id == imageId })
                Log.d("deleteImage","Current list : $imageList")
            }
            onResult(result)
        }
    }

    fun changeImageCategory(imageId: Int, categoryId: Int, onResult: (Result<GalleryImage>) -> Unit) {
        dataSource.changeImageCategory(imageId,categoryId, user?.hash.toString()){ result ->
            if (result is Result.Success) {
                imageList.remove(imageList.first { it.id == imageId })
            }
            onResult(result)
        }
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    private fun setCategoryList(list: List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(list)
    }

    private fun setImageList(list: List<GalleryImage>) {
        this.imageList.clear()
        this.imageList.addAll(list)
    }
}
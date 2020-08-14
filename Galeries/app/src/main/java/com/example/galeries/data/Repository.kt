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
    private var likedImageList = mutableListOf<GalleryImage>()

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
                // Only a logged in user has a Coups de Coeur category, then put it in front of the list
                user?.let {
                    coupsDeCoeur = result.data.first { it.estCoupsDeCoeur }.also {
                        mutableList.remove(it)
                        mutableList.add(0,it)
                    }
                    loadLikedList()
                }
                setCategoryList(mutableList)
                onResult(Result.Success(mutableList))
            } else {
                onResult(result)
            }

        }
    }

    fun getImages(categoryId: Int, onResult: (Result<List<GalleryImage>>) -> Unit) {
        // Get images for selected category and checks if the current item is in the likedList
        dataSource.getImages(categoryId) {
            if (it is Result.Success) {
                // Repository received the image list, now it checks if images are liked
                it.data.forEach{image ->
                    image.isLiked = false
                }
                if (likedImageList.isNotEmpty()) {
                    it.data.forEach { image ->
                        likedImageList.forEach { likedImage ->
                            if (image.id == likedImage.id) {
                                image.isLiked = true
                            }
                        }
                    }
                }
                setImageList(it.data)
                Log.d("getImages","Function call, result : ${it.data}")
                onResult(it)
            } else {
                Log.d("getImages","Function call, error receiving response for category $categoryId")
                if (categoryId == coupsDeCoeur?.id && likedImageList.isNotEmpty()) {
                    /*
                    * Since the request for the coups de coeur category is currently unavailable
                    * this piece of code loads the current session liked images
                    * */
                    Log.d("getImages","Using cached list for Coups de Coeur")
                    setImageList(likedImageList)
                    onResult(Result.Success(likedImageList))
                } else {
                    onResult(it)
                }
            }

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
                    useMode = UseMode.EDIT,
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

    fun renameCategory(
        categoryId: Int,
        newName: String,
        onResult: (Result<SingleCategoryResponse>) -> Unit
    ){
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
                val image = imageList.first { it.id == result.data.id }.apply {
                    isLiked = true
                }

                // Make sure the item is added only 1 time on the list
                likedImageList.filter { it.id == image.id }.forEach {
                    likedImageList.remove(it)
                }
                likedImageList.add(image)
            }
            onResult(result)
        }
    }

    fun dislikeImage(imageId: Int, onResult: (Result<GalleryImage>) -> Unit) {
        dataSource.dislikeImage(imageId, user?.hash.toString()){ result ->
            if (result is Result.Success) {
                try {
                    imageList.first { it.id == result.data.id }.apply {
                        isLiked = false
                    }
                    likedImageList.filter { it.id == result.data.id }.forEach {
                        likedImageList.remove(it)
                    }
                } catch (e: Throwable) {
                    Log.d("Dislike Error", "Error while trying to dislike image")
                }
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

    fun changeImageCategory(
        imageId: Int,
        categoryId: Int,
        onResult: (Result<GalleryImage>) -> Unit
    ) {
        dataSource.changeImageCategory(imageId,categoryId, user?.hash.toString()){ result ->
            if (result is Result.Success) {
                val image = imageList.first { it.id == result.data.id }
                imageList.remove(image)
                Log.d("RemovingImage","ImageId = ${image.id}, reason : Image changed from category")
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

    private fun loadLikedList() {
        // If the Coups de coeur category exists and the likedImages list is empty, try to get current liked images
        Log.d("loadLikedList","Function called")
        coupsDeCoeur?.let {category ->
            if (likedImageList.isEmpty()) {
                dataSource.getImages(category.id) {
                    if (it is Result.Success){
                        likedImageList.clear()
                        likedImageList.addAll(it.data)
                        Log.d("loadLikedList","Success response for category ${category.id}")
                    } else {
                        Log.d("loadLikedList","Error receiving response for category ${category.id}")
                    }
                }
            }
        }
    }
}
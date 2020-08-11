package com.example.galeries.data

import android.util.Log
import com.example.galeries.data.api.GalleryImageService
import com.example.galeries.data.api.responses.*
import com.example.galeries.data.model.Category
import com.example.galeries.data.model.GalleryImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class DataSource {
    private val galleryImageService : GalleryImageService = Retrofit.Builder()
        .baseUrl("http://tomnab.fr/img-api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GalleryImageService::class.java)

    fun login(
        username: String,
        password: String,
        onResult: (Result<LoggedInUser>) -> Unit
    ) {
        galleryImageService.authenticate(username,password)
            .enqueue(
                object : Callback<LoggedInUser> {
                    override fun onFailure(call: Call<LoggedInUser>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't authenticate")))
                    }

                    override fun onResponse(
                        call: Call<LoggedInUser>,
                        response: Response<LoggedInUser>
                    ) {
                        if (response.isSuccessful){
                            onResult(Result.Success(response.body()!!))
                        } else {
                            onResult(Result.Error(IOException("Couldn't authenticate")))
                        }
                    }

                }
            )
    }

    fun getCategories(
        token: String? = null,
        onResult: (Result<List<Category>>) -> Unit
    ) {
        val service = if (token != null) {
            galleryImageService.getCategories(token)
        } else {
            galleryImageService.getCategories()
        }
        service.enqueue(
            object : Callback<CategoryListResponse> {
                override fun onFailure(call: Call<CategoryListResponse>, t: Throwable) {
                    onResult(Result.Error(IOException("Couldn't get categories")))
                }

                override fun onResponse(
                    call: Call<CategoryListResponse>,
                    response: Response<CategoryListResponse>
                ) {
                    if (response.isSuccessful){
                        onResult(Result.Success(response.body()!!.categories))
                    } else {
                        onResult(Result.Error(IOException("Couldn't get categories")))
                    }
                }

            }
        )
    }

    fun getImages(
        categoryId: Int,
        onResult: (Result<List<GalleryImage>>) -> Unit
    ) {
        galleryImageService.getImagesFrom(categoryId)
            .enqueue(
                object : Callback<ImageListResponse> {
                    override fun onFailure(call: Call<ImageListResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't get images")))

                    }

                    override fun onResponse(
                        call: Call<ImageListResponse>,
                        response: Response<ImageListResponse>
                    ) {
                        if (response.isSuccessful){
                            onResult(Result.Success(response.body()!!.images))
                        } else {
                            onResult(Result.Error(IOException("Couldn't get images")))
                        }
                    }

                }
            )
    }

    fun addCategory(
        title: String,
        token: String,
        onResult: (Result<SingleCategoryResponse>) -> Unit
    ) {
        galleryImageService.addCategory(title,token)
            .enqueue(
                object : Callback<SingleCategoryResponse> {
                    override fun onFailure(call: Call<SingleCategoryResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't create category")))
                    }

                    override fun onResponse(
                        call: Call<SingleCategoryResponse>,
                        response: Response<SingleCategoryResponse>
                    ) {
                        if (response.isSuccessful)
                        {
                            Log.d("AddCetegoryDataSource", "adding category : ${response.body()?.categorie}")
                            onResult(Result.Success(response.body()!!))
                        } else {
                            onResult(Result.Error(IOException("Couldn't create category")))
                        }
                    }

                }
            )
    }

    fun deleteCategory(
        categoryId: Int,
        token: String,
        onResult: (Result<Boolean>) -> Unit
    ) {
        galleryImageService.deleteCategory(categoryId, token)
            .enqueue(
                object : Callback<RequestHeader> {
                    override fun onFailure(call: Call<RequestHeader>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't delete category")))
                    }

                    override fun onResponse(
                        call: Call<RequestHeader>,
                        response: Response<RequestHeader>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("DeleteCetegory", "Deleting category : $categoryId")
                            onResult(Result.Success(true))
                        } else {
                            onResult(Result.Error(IOException("Couldn't create category")))
                        }
                    }

                }
            )
    }

    fun renameCategory(
        id: Int,
        name: String,
        token: String,
        onResult: (Result<SingleCategoryResponse>) -> Unit
    ) {
        galleryImageService.renameCategory(id, name, token)
            .enqueue(
                object : Callback<SingleCategoryResponse> {
                    override fun onFailure(call: Call<SingleCategoryResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't rename category")))
                    }

                    override fun onResponse(
                        call: Call<SingleCategoryResponse>,
                        response: Response<SingleCategoryResponse>
                    ) {
                        if (response.isSuccessful) {
                            onResult(Result.Success(response.body()!!))
                        } else {
                            onResult(Result.Error(IOException("Couldn't rename category")))
                        }
                    }

                }
            )
    }

    fun addImage(
        categoryId: Int,
        description: String,
        url: String,
        token: String,
        onResult: (Result<GalleryImage>) -> Unit
    ) {
        Log.d("AddImageDataSource", "adding image at $categoryId")
        galleryImageService.addImageTo(categoryId, description, url, token)
            .enqueue(
                object : Callback<SingleImageResponse> {
                    override fun onFailure(call: Call<SingleImageResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't add image")))
                    }

                    override fun onResponse(
                        call: Call<SingleImageResponse>,
                        response: Response<SingleImageResponse>
                    ) {
                        if (response.isSuccessful) {
                            onResult(Result.Success(response.body()!!.image))
                        } else {
                            onResult(Result.Error(IOException("Couldn't add image")))
                        }
                    }
                }
            )
    }

    fun editImageText(imageId: Int, newText: String, token: String, onResult: (Result<GalleryImage>) -> Unit) {
        galleryImageService.changeImageDescription(imageId,newText, token)
            .enqueue(
                object : Callback<SingleImageResponse> {
                    override fun onFailure(call: Call<SingleImageResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't edit image")))
                    }

                    override fun onResponse(
                        call: Call<SingleImageResponse>,
                        response: Response<SingleImageResponse>
                    ) {
                        if (response.isSuccessful) {
                            onResult(Result.Success(response.body()!!.image))
                        } else {
                            onResult(Result.Error(IOException("Couldn't edit image")))
                        }
                    }

                }
            )
    }

    fun likeImage(imageId: Int, token: String, onResult: (Result<GalleryImage>) -> Unit) {
        galleryImageService.likeImage(imageId,token)
            .enqueue(
                object : Callback<SingleImageResponse> {
                    override fun onFailure(call: Call<SingleImageResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't like image")))
                    }

                    override fun onResponse(
                        call: Call<SingleImageResponse>,
                        response: Response<SingleImageResponse>
                    ) {
                        if (response.isSuccessful) {
                            onResult(Result.Success(response.body()!!.image))
                        } else {
                            onResult(Result.Error(IOException("Couldn't like image")))
                        }
                    }

                }
            )
    }

    fun dislikeImage(imageId: Int, token: String, onResult: (Result<GalleryImage>) -> Unit) {
        galleryImageService.dislikeImage(imageId, token).
            enqueue(
                object : Callback<SingleImageResponse> {
                    override fun onFailure(call: Call<SingleImageResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't dislike image")))
                    }

                    override fun onResponse(
                        call: Call<SingleImageResponse>,
                        response: Response<SingleImageResponse>
                    ) {
                        if (response.isSuccessful){
                            onResult(Result.Success(response.body()!!.image))
                        } else {
                            onResult(Result.Error(IOException("Couldn't like image")))
                        }
                    }

                }
            )
    }

    fun deleteImage(imageId: Int, token: String, onResult: (Result<Boolean>) -> Unit) {
        galleryImageService.deleteImage(imageId, token)
            .enqueue(
                object : Callback<RequestHeader>{
                    override fun onFailure(call: Call<RequestHeader>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't delete image")))
                    }

                    override fun onResponse(
                        call: Call<RequestHeader>,
                        response: Response<RequestHeader>
                    ) {
                        if (response.isSuccessful){
                            onResult(Result.Success(true))
                        } else {
                            onResult(Result.Error(IOException("Couldn't dislike image")))
                        }

                    }

                }
            )
    }

    fun changeImageCategory(imageId: Int, categoryId: Int, token: String, onResult: (Result<GalleryImage>) -> Unit) {
        galleryImageService.changeImageCategory(imageId, categoryId, token)
            .enqueue(
                object : Callback<SingleImageResponse> {
                    override fun onFailure(call: Call<SingleImageResponse>, t: Throwable) {
                        onResult(Result.Error(IOException("Couldn't delete image")))
                    }

                    override fun onResponse(
                        call: Call<SingleImageResponse>,
                        response: Response<SingleImageResponse>
                    ) {
                        if (response.isSuccessful) {
                            onResult(Result.Success(response.body()!!.image))
                        } else {
                            onResult(Result.Error(IOException("Couldn't delete image")))
                        }
                    }

                }
            )
    }

}
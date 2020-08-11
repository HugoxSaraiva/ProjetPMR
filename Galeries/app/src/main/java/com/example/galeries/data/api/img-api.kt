package com.example.galeries.data.api

import com.example.galeries.data.api.responses.*
import retrofit2.Call
import retrofit2.http.*

interface GalleryImageService {

    @POST("authenticate")
    fun authenticate(
        @Query("user") username: String,
        @Query("password") password: String
    ) : Call<LoggedInUser>

    @GET("categories")
    fun getCategories(
        @Header("hash") token: String
    ) : Call<CategoryListResponse>

    @GET("categories")
    fun getCategories() : Call<CategoryListResponse>

    @POST("categories")
    fun addCategory(
        @Query("titre") title: String,
        @Header("hash") token: String
    ) : Call<SingleCategoryResponse>

    @PUT("categories/{id}")
    fun renameCategory(
        @Path("id") categoryId: Int,
        @Query("titre") newTitle: String,
        @Header("hash") token: String
    ) : Call<SingleCategoryResponse>

    @DELETE("categories/{id}")
    fun deleteCategory(
        @Path("id") categoryId : Int,
        @Query("hash") token: String
    ) : Call<RequestHeader>

    @GET("categories/{id}/images")
    fun getImagesFrom(
        @Path("id") categoryId: Int
    ) : Call<ImageListResponse>

    @POST("categories/{id}/images")
    fun addImageTo(
        @Path("id") categoryId : Int,
        @Query("legende") description : String,
        @Query("url") url : String,
        @Header("hash") token: String
    ) : Call<SingleImageResponse>

    @GET("images/{imageId}")
    fun getImageMetadata(
        @Path("imageId") imageId : Int
    )

    @PUT("images/{imageId}")
    fun changeImageDescription(
        @Path("imageId") imageId: Int,
        @Query("legende") newDescription : String,
        @Header("hash") token: String
    ) : Call<SingleImageResponse>

    @DELETE("images/{imageId}")
    fun deleteImage(
        @Path("imageId") imageId: Int,
        @Header("hash") token: String
    ) : Call<RequestHeader>

    @PUT("images/{imageId}")
    fun changeImageCategory(
        @Path("imageId") imageId: Int,
        @Query("idCategorie") categoryId: Int,
        @Header("hash") token: String
    ) : Call<SingleImageResponse>

    @PUT("images/{imageId}/coupdecoeur")
    fun likeImage(
        @Path("imageId") imageId: Int,
        @Header("hash") token: String
    ) : Call<SingleImageResponse>

    @DELETE("images/{imageId}/coupdecoeur")
    fun dislikeImage(
        @Path("imageId") imageId: Int,
        @Header("hash") token: String
    ) : Call<SingleImageResponse>

    @GET("users")
    fun getUsers(
        @Header("hash") token: String
    )
}
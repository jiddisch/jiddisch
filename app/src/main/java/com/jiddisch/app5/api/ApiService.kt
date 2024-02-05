package com.jiddisch.app5.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MainCategoryApi {
    @GET("main-category")
    fun getMainCategories(): Call<List<MainCategory>>
}

interface CategoryApi {
    @GET("category/")
    fun getSubCategoriesByCategoryId(@Query("main_category") mainCategoryId: Int): Call<List<Category>>

    @GET("category/")
    fun getCategoryById(@Query("category_id") categoryId: Int): Call<List<Category>>
}

interface WordApi {
    @GET("word/")
    fun getWordsByCategoryId(@Query("category") categoryId: Int): Call<List<Word>>
}

interface AlphabetApi {
    @GET("alphabet")
    fun getAlphabetLetters(): Call<List<AlphabetLetter>>
}

interface CustomCallback<T> {
    fun onResponse(call: Call<T>?, response: Response<T>)
    fun onFailure(call: Call<T>?, t: Throwable)
}

class ApiService(context: Context) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://34.31.112.206/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val preferencesHelper = PreferencesHelper(context)
    private val gson = Gson()

    private inline fun <reified T> getData(
        callback: CustomCallback<T>,
        cacheKey: String,
        call: Call<T>
    ) {
        if (preferencesHelper.isCacheValid(cacheKey, 60 * 60 * 1000)) {
            val dataJson = preferencesHelper.get(cacheKey)
            val type = object : TypeToken<T>() {}.type
            val data = gson.fromJson<T>(dataJson, type)

            callback.onResponse(null, Response.success(data))
        } else {
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        val dataJson = gson.toJson(response.body())
                        preferencesHelper.save(cacheKey, dataJson)
                        preferencesHelper.saveTime(cacheKey)
                    }
                    callback.onResponse(call, response)
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    callback.onFailure(call, t)
                }
            })
        }
    }

    fun getMainCategories(callback: CustomCallback<List<MainCategory>>) {
        val api = retrofit.create(MainCategoryApi::class.java)
        getData<List<MainCategory>>(callback, "MainCategories", api.getMainCategories())
    }

    fun getAlphabetLetters(callback: CustomCallback<List<AlphabetLetter>>) {
        val api = retrofit.create(AlphabetApi::class.java)
        getData<List<AlphabetLetter>>(callback, "AlphabetLetters", api.getAlphabetLetters())
    }

    fun getSubCategoriesByCategoryId(
        mainCategoryId: Int,
        callback: CustomCallback<List<Category>>
    ) {
        val api = retrofit.create(CategoryApi::class.java)
        getData<List<Category>>(
            callback,
            "SubCategories_$mainCategoryId",
            api.getSubCategoriesByCategoryId(mainCategoryId)
        )
    }

    fun getWordsByCategoryId(categoryId: Int, callback: CustomCallback<List<Word>>) {
        val api = retrofit.create(WordApi::class.java)
        getData<List<Word>>(callback, "Words_$categoryId", api.getWordsByCategoryId(categoryId))
    }

    fun getCategoryById(categoryId: Int, callback: CustomCallback<List<Category>>) {
        val api = retrofit.create(CategoryApi::class.java)
        getData<List<Category>>(
            callback,
            "Category_$categoryId",
            api.getCategoryById(categoryId)
        )
    }
}

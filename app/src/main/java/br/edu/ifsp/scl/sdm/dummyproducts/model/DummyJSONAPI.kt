package br.edu.ifsp.scl.sdm.dummyproducts.model

import android.content.Context
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_OK

class DummyJSONAPI private constructor(context: Context) {

    companion object {

        const val PRODUCTS_ENDPOINT = "https://dummyjson.com/products/"

        @Volatile
        private var INSTANCE: DummyJSONAPI? = null

        fun getInstance(context: Context): DummyJSONAPI {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DummyJSONAPI(context).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        requestQueue.add(request)
    }

    class ProductListRequest(
        private val responseListener: Response.Listener<ProductList>,
        errorListener: Response.ErrorListener
    ) : Request<ProductList>(Method.GET, PRODUCTS_ENDPOINT, errorListener) {

        override fun parseNetworkResponse(response: NetworkResponse): Response<ProductList> {

            return if (response.statusCode == HTTP_OK || response.statusCode == HTTP_NOT_MODIFIED) {

                val jsonString = String(response.data)

                Response.success(
                    Gson().fromJson(jsonString, ProductList::class.java),
                    HttpHeaderParser.parseCacheHeaders(response)
                )

            } else {
                Response.error(VolleyError("Network error"))
            }
        }

        override fun deliverResponse(response: ProductList) {
            responseListener.onResponse(response)
        }
    }
}
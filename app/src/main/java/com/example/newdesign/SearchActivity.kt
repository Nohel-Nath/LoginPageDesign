package com.example.newdesign

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.newdesign.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://jsonplaceholder.typicode.com/"

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var bankAdapter: SearchAdapter
    private lateinit var dataList: List<DataClassItem>
    private var dataSetSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataList = emptyList()
        setUpRecycleView()
        getMyData()
        setUpSearchView()
    }

    private fun setUpRecycleView() {
        bankAdapter = SearchAdapter()
        //  binding.rvSearch.adapter = bankAdapter

        binding.rvSearch.apply {
            adapter = bankAdapter
            setHasFixedSize(true)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        dataSetSize = dataList.size
    }

    private fun getMyData() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val bankService = retrofit.create(DataInterface::class.java)

        val call = bankService.getEmail()
        call.enqueue(object : Callback<List<DataClassItem>?> {
            override fun onResponse(
                call: Call<List<DataClassItem>?>,
                response: Response<List<DataClassItem>?>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        dataList = it
                        bankAdapter.submitSearchClass(dataList)
                        dataSetSize = dataList.size // Update dataSetSize
                    }
                } else {
                    Log.e("TAG", "Unsuccessful response: ${response.code()}")
                    // Handle the case where the response is not successful
                }
            }

            override fun onFailure(call: Call<List<DataClassItem>?>, t: Throwable) {
                Log.d("MainActivity", "onFailure: " + t.message)
            }
        })
    }

    private fun setUpSearchView() {
        binding.searchBank.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.e("TAG", "onQueryTextChange: code 1 $query")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchQuery ->
                    val trimmedQuery = searchQuery.trim()
                    val filteredList =
                        dataList.filter { it.email.contains(trimmedQuery, true) }
                    binding.rvSearch.scrollToPosition(0)
                    bankAdapter.submitSearchClass(filteredList)
                    if (filteredList.size == dataSetSize) {
                        binding.rvSearch.scrollToPosition(0)
                    }

                    return true
                }
                return true
            }
        })
    }
}

//        val bankList = resources.getStringArray(R.array.bank_name)
//        bankDataList = bankList.map { SearchDataClass(it) }.sortedBy { it.item }
//    private fun setUpRecycleView() {
//        bankAdapter = SearchAdapter()
//        binding.rvSearch.adapter = bankAdapter
////        dataSetSize = bankDataList.size
////        bankAdapter.submitSearchClass(bankDataList)
//
//    }
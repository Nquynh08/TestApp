package com.example.testapp

import CapBacAdapter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var capBacAdapter: CapBacAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        capBacAdapter = CapBacAdapter()
        recyclerView.adapter = capBacAdapter

        // Gọi gRPC
        fetchCapBacList()
    }

    private fun fetchCapBacList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val channel = ManagedChannelBuilder.forAddress("192.168.43.48", 5000)
                    .usePlaintext()
                    .build()

                val stub = CapBacServiceGrpc.newBlockingStub(channel)
                val request = GetListCapBacRequest.newBuilder().build()
                val response = stub.getListCapBac(request)

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        capBacAdapter.setItems(response.itemsList)
                    } else {
                        Log.e("MainActivity", "Error: ${response.message}")
                    }
                }

                channel.shutdown()

            } catch (e: Exception) {
                Log.e("MainActivity", "Error during gRPC call", e)
            }
        }
    }
}
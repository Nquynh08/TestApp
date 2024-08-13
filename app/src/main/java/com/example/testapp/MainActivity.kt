package com.example.testapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle


    private lateinit var recyclerView: RecyclerView
    private lateinit var viewButton: AppCompatButton
    private lateinit var addButton: AppCompatButton
    private lateinit var capBacAdapter: CapBacAdapter
    private val capBacList = mutableListOf<DanhMucCapBac>()

    private lateinit var capBacServiceStub: CapBacServiceGrpc.CapBacServiceBlockingStub

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> Toast.makeText(applicationContext, "Clicked Home", Toast.LENGTH_LONG).show()
                R.id.nav_cb -> Toast.makeText(applicationContext, "Clicked canbo", Toast.LENGTH_LONG).show()
                R.id.nav_cv -> Toast.makeText(applicationContext, "Clicked chucvu", Toast.LENGTH_LONG).show()
                R.id.nav_dd -> Toast.makeText(applicationContext, "Clicked daidoi", Toast.LENGTH_LONG).show()
                R.id.nav_hv -> Toast.makeText(applicationContext, "Clicked hocvien", Toast.LENGTH_LONG).show()

            }
            true
        }

        recyclerView = findViewById(R.id.recyclerViewCapBac)
        viewButton = findViewById(R.id.btnViewList)
        addButton = findViewById(R.id.btnAddNew)

        capBacAdapter = CapBacAdapter(capBacList, ::onEditClick, ::onDeleteClick)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = capBacAdapter

        viewButton.setOnClickListener { fetchCapBacList() }
        addButton.setOnClickListener { showAddCapBacDialog() }

        // Initialize gRPC client
        val channel = ManagedChannelBuilder.forAddress("192.168.43.48", 5000)
            .usePlaintext()
            .build()
        capBacServiceStub = CapBacServiceGrpc.newBlockingStub(channel)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean{
        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    private fun fetchCapBacList() {
        val request = GetListCapBacRequest.newBuilder().build()
        val response = capBacServiceStub.getListCapBac(request)
        if (response.success) {
            capBacList.clear()
            capBacList.addAll(response.itemsList)
            capBacAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showAddCapBacDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cap_bac, null)
        val idEditText = dialogView.findViewById<EditText>(R.id.etId)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val orderingEditText = dialogView.findViewById<EditText>(R.id.etOrdering)

        AlertDialog.Builder(this)
            .setTitle("Thêm cấp bậc mới")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val id = idEditText.text.toString()
                val name = nameEditText.text.toString()
                val ordering = orderingEditText.text.toString().toIntOrNull() ?: 0

                val request = SaveCapBacRequest.newBuilder()
                    .setIsNew(true)
                    .setItem(DanhMucCapBac.newBuilder().setId(id).setName(name).setOrdering(ordering).build())
                    .build()

                // Call gRPC service
                Thread {
                    try {
                        val response = capBacServiceStub.saveCapBac(request)
                        runOnUiThread {
                            if (response.success) {
                                fetchCapBacList() // Refresh list
                            } else {
                                Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun showEditCapBacDialog(capBac: DanhMucCapBac) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cap_bac, null)
        val idEditText = dialogView.findViewById<EditText>(R.id.etId)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val orderingEditText = dialogView.findViewById<EditText>(R.id.etOrdering)

        // Set current values
        idEditText.setText(capBac.id)
        nameEditText.setText(capBac.name)
        orderingEditText.setText(capBac.ordering.toString())

        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa cấp bậc")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val id = idEditText.text.toString()
                val name = nameEditText.text.toString()
                val ordering = orderingEditText.text.toString().toIntOrNull() ?: 0

                val request = SaveCapBacRequest.newBuilder()
                    .setIsNew(false)
                    .setItem(DanhMucCapBac.newBuilder().setId(id).setName(name).setOrdering(ordering).build())
                    .build()

                // Call gRPC service
                Thread {
                    try {
                        val response = capBacServiceStub.saveCapBac(request)
                        runOnUiThread {
                            if (response.success) {
                                fetchCapBacList() // Refresh list
                            } else {
                                Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }



    private fun onEditClick(item: DanhMucCapBac) {
        // Implement editing functionality
        showEditCapBacDialog(item)
    }

    private fun onDeleteClick(item: DanhMucCapBac) {
        AlertDialog.Builder(this)
            .setTitle("Xóa cấp bậc")
            .setMessage("Bạn có chắc chắn muốn xóa cấp bậc này không?")
            .setPositiveButton("Xóa") { _, _ ->
                val request = DeleteCapBacRequest.newBuilder()
                    .setId(item.id)
                    .build()

                // Call gRPC service
                Thread {
                    try {
                        val response = capBacServiceStub.deleteCapBac(request)
                        runOnUiThread {
                            if (response.success) {
                                fetchCapBacList() // Refresh list
                            } else {
                                Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

}

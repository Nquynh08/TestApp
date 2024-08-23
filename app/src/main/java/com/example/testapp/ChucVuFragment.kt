package com.example.testapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ChucVuFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chucVuAdapter: ChucVuAdapter
    private val chucVuList = mutableListOf<DanhMucChucVu>()

    private lateinit var chucVuServiceStub: ChucVuServiceGrpc.ChucVuServiceBlockingStub
    private lateinit var channel: ManagedChannel

    // ExecutorService to manage background threads
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chuc_vu, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewChucVu)
        /*val viewButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewList)*/
        val addButton = view.findViewById<androidx.appcompat.widget.AppCompatImageButton>(R.id.btnAddNew)

        chucVuAdapter = ChucVuAdapter(chucVuList, ::onEditClick, ::onDeleteClick)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = chucVuAdapter

        /*viewButton.setOnClickListener { fetchChucVuList() }*/
        addButton.setOnClickListener { showAddChucVuDialog() }

        val searchView = view.findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                chucVuAdapter.filter.filter(newText)
                return true
            }
        })

        // Initialize gRPC client
        channel = ManagedChannelBuilder.forAddress("192.168.43.48", 5000)
            .usePlaintext()
            .build()
        chucVuServiceStub = ChucVuServiceGrpc.newBlockingStub(channel)

        fetchChucVuList()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Shutdown the channel when the view is destroyed
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        executor.shutdown()
    }

    private fun fetchChucVuList() {
        executor.execute {
            try {
                val request = GetListChucVuRequest.newBuilder().build()
                val response = chucVuServiceStub.getListChucVu(request)
                requireActivity().runOnUiThread {
                    if (response.success) {
                        chucVuList.clear()
                        chucVuList.addAll(response.itemsList)
                        chucVuAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showAddChucVuDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_chuc_vu, null)
        val idEditText = dialogView.findViewById<EditText>(R.id.etId)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val orderingEditText = dialogView.findViewById<EditText>(R.id.etOrdering)

        AlertDialog.Builder(requireContext())
            .setTitle("Thêm chức vụ mới")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val id = idEditText.text.toString()
                val name = nameEditText.text.toString()
                val ordering = orderingEditText.text.toString().toIntOrNull() ?: 0

                val request = SaveChucVuRequest.newBuilder()
                    .setIsNew(true)
                    .setItem(DanhMucChucVu.newBuilder().setId(id).setName(name).setOrdering(ordering).build())
                    .build()

                // Call gRPC service
                executor.execute {
                    try {
                        val response = chucVuServiceStub.saveChucVu(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchChucVuList() // Refresh list
                            } else {
                                Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditChucVuDialog(item: DanhMucChucVu) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_chuc_vu, null)
        val idEditText = dialogView.findViewById<EditText>(R.id.etId)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val orderingEditText = dialogView.findViewById<EditText>(R.id.etOrdering)

        // Set the current values of the item in the dialog fields
        idEditText.setText(item.id)
        nameEditText.setText(item.name)
        orderingEditText.setText(item.ordering.toString())

        // ID không được chỉnh sửa
        idEditText.isEnabled = false

        AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa chức vụ")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = nameEditText.text.toString()
                val ordering = orderingEditText.text.toString().toIntOrNull() ?: 0

                val request = SaveChucVuRequest.newBuilder()
                    .setIsNew(false) // Set to false because we are editing
                    .setItem(DanhMucChucVu.newBuilder().setId(item.id).setName(name).setOrdering(ordering).build())
                    .build()

                // Call gRPC service
                executor.execute {
                    try {
                        val response = chucVuServiceStub.saveChucVu(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchChucVuList() // Refresh list
                            } else {
                                Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun onEditClick(item: DanhMucChucVu) {
        showEditChucVuDialog(item)
    }

    private fun onDeleteClick(item: DanhMucChucVu) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa chức vụ")
            .setMessage("Bạn có chắc chắn muốn xóa chức vụ này không?")
            .setPositiveButton("Xóa") { _, _ ->
                val request = DeleteChucVuRequest.newBuilder()
                    .setId(item.id)
                    .build()

                // Call gRPC service
                executor.execute {
                    try {
                        val response = chucVuServiceStub.deleteChucVu(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchChucVuList() // Refresh list
                            } else {
                                Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}

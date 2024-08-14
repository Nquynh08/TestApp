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

class CapBacFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var capBacAdapter: CapBacAdapter
    private val capBacList = mutableListOf<DanhMucCapBac>()

    private lateinit var capBacServiceStub: CapBacServiceGrpc.CapBacServiceBlockingStub
    private lateinit var channel: ManagedChannel

    // ExecutorService to manage background threads
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cap_bac, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCapBac)
        /*val viewButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewList)*/
        val addButton = view.findViewById<androidx.appcompat.widget.AppCompatImageButton>(R.id.btnAddNew)

        capBacAdapter = CapBacAdapter(capBacList, ::onEditClick, ::onDeleteClick)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = capBacAdapter



        /*viewButton.setOnClickListener { fetchCapBacList() }*/
        addButton.setOnClickListener { showAddCapBacDialog() }

        val searchView = view.findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                capBacAdapter.filter.filter(newText)
                return true
            }
        })


        // Initialize gRPC client
        channel = ManagedChannelBuilder.forAddress("192.168.43.48", 5000)
            .usePlaintext()
            .build()
        capBacServiceStub = CapBacServiceGrpc.newBlockingStub(channel)

        fetchCapBacList()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Shutdown the channel when the view is destroyed
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        executor.shutdown()
    }

    private fun fetchCapBacList() {
        executor.execute {
            try {
                val request = GetListCapBacRequest.newBuilder().build()
                val response = capBacServiceStub.getListCapBac(request)
                requireActivity().runOnUiThread {
                    if (response.success) {
                        capBacList.clear()
                        capBacList.addAll(response.itemsList)
                        capBacAdapter.notifyDataSetChanged()
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

    private fun showAddCapBacDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_cap_bac, null)
        val idEditText = dialogView.findViewById<EditText>(R.id.etId)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val orderingEditText = dialogView.findViewById<EditText>(R.id.etOrdering)

        AlertDialog.Builder(requireContext())
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
                executor.execute {
                    try {
                        val response = capBacServiceStub.saveCapBac(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchCapBacList() // Refresh list
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

    private fun showEditCapBacDialog(item: DanhMucCapBac) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_cap_bac, null)
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
            .setTitle("Chỉnh sửa cấp bậc")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = nameEditText.text.toString()
                val ordering = orderingEditText.text.toString().toIntOrNull() ?: 0

                val request = SaveCapBacRequest.newBuilder()
                    .setIsNew(false) // Set to false because we are editing
                    .setItem(DanhMucCapBac.newBuilder().setId(item.id).setName(name).setOrdering(ordering).build())
                    .build()

                // Call gRPC service
                executor.execute {
                    try {
                        val response = capBacServiceStub.saveCapBac(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchCapBacList() // Refresh list
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

    private fun onEditClick(item: DanhMucCapBac) {
        showEditCapBacDialog(item)
    }

    private fun onDeleteClick(item: DanhMucCapBac) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa cấp bậc")
            .setMessage("Bạn có chắc chắn muốn xóa cấp bậc này không?")
            .setPositiveButton("Xóa") { _, _ ->
                val request = DeleteCapBacRequest.newBuilder()
                    .setId(item.id)
                    .build()

                // Call gRPC service
                executor.execute {
                    try {
                        val response = capBacServiceStub.deleteCapBac(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchCapBacList() // Refresh list
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

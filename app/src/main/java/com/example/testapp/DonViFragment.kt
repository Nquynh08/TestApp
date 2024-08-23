package com.example.testapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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

class DonViFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var donViAdapter: DonViAdapter
    private val donViList = mutableListOf<DanhMucDonVi>()

    private lateinit var donViServiceStub: DonViServiceGrpc.DonViServiceBlockingStub
    private lateinit var channel: ManagedChannel

    // ExecutorService to manage background threads
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_don_vi, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewDonVi)
        val addButton = view.findViewById<androidx.appcompat.widget.AppCompatImageButton>(R.id.btnAddNew)

        donViAdapter = DonViAdapter(donViList, ::onEditClick, ::onDeleteClick)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = donViAdapter

        addButton.setOnClickListener { showAddDonViDialog() }

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                donViAdapter.filter.filter(newText)
                return true
            }
        })

        // Initialize gRPC client
        channel = ManagedChannelBuilder.forAddress("192.168.43.48", 5000)
            .usePlaintext()
            .build()
        donViServiceStub = DonViServiceGrpc.newBlockingStub(channel)

        fetchDonViList()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Shutdown the channel when the view is destroyed
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        executor.shutdown()
    }

    private fun fetchDonViList() {
        executor.execute {
            try {
                val request = GetListDonViRequest.newBuilder().build()
                val response = donViServiceStub.getListDonVi(request)
                requireActivity().runOnUiThread {
                    if (response.success) {
                        donViList.clear()
                        donViList.addAll(response.itemsList)
                        donViAdapter.notifyDataSetChanged()
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

    private fun showAddDonViDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_don_vi, null)
        val idEditText = dialogView.findViewById<EditText>(R.id.etId)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerPID = dialogView.findViewById<Spinner>(R.id.spinnerPID)

        // Tạo danh sách các đơn vị cấp trên từ danh sách hiện tại
        val pidList = donViList.map { it.id }.toMutableList()
        pidList.add(0, "None")  // Thêm tùy chọn "None" nếu đơn vị không có cấp trên

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pidList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPID.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("Thêm đơn vị mới")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val id = idEditText.text.toString()
                val name = nameEditText.text.toString()
                val selectedPID = if (spinnerPID.selectedItem == "None") "" else spinnerPID.selectedItem.toString()

                val request = SaveDonViRequest.newBuilder()
                    .setIsNew(true)
                    .setItem(DanhMucDonVi.newBuilder().setId(id).setName(name).setPId(selectedPID).build())
                    .build()

                // Gọi gRPC service để lưu đơn vị mới
                executor.execute {
                    try {
                        val response = donViServiceStub.saveDonVi(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchDonViList() // Refresh list
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

    private fun showEditDonViDialog(item: DanhMucDonVi) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_don_vi, null)
        val idEditText = dialogView.findViewById<EditText>(R.id.etId)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerPID = dialogView.findViewById<Spinner>(R.id.spinnerPID)

        // Set the current values of the item in the dialog fields
        idEditText.setText(item.id)
        nameEditText.setText(item.name)

        // ID không được chỉnh sửa
        idEditText.isEnabled = false

        // Tạo danh sách các đơn vị cấp trên từ danh sách hiện tại
        val pidList = donViList.map { it.id }.toMutableList()
        pidList.add(0, "None")  // Thêm tùy chọn "None" nếu đơn vị không có cấp trên

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pidList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPID.adapter = adapter

        // Đặt giá trị hiện tại cho Spinner
        val position = adapter.getPosition(item.pId)
        spinnerPID.setSelection(position)

        AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa đơn vị")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = nameEditText.text.toString()
                val selectedPID = if (spinnerPID.selectedItem == "None") "" else spinnerPID.selectedItem.toString()

                val request = SaveDonViRequest.newBuilder()
                    .setIsNew(false) // Set to false because we are editing
                    .setItem(DanhMucDonVi.newBuilder().setId(item.id).setName(name).setPId(selectedPID).build())
                    .build()

                // Call gRPC service
                executor.execute {
                    try {
                        val response = donViServiceStub.saveDonVi(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchDonViList() // Refresh list
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

    private fun onEditClick(item: DanhMucDonVi) {
        showEditDonViDialog(item)
    }

    private fun onDeleteClick(item: DanhMucDonVi) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa đơn vị")
            .setMessage("Bạn có chắc chắn muốn xóa đơn vị này không?")
            .setPositiveButton("Xóa") { _, _ ->
                val request = DeleteDonViRequest.newBuilder()
                    .setId(item.id)
                    .build()

                // Call gRPC service
                executor.execute {
                    try {
                        val response = donViServiceStub.deleteDonVi(request)
                        requireActivity().runOnUiThread {
                            if (response.success) {
                                fetchDonViList() // Refresh list
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

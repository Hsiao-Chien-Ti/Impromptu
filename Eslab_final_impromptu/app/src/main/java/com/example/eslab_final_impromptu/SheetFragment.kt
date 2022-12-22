package com.example.eslab_final_impromptu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.eslab_final_impromptu.adapter.ItemAdapter
import com.example.eslab_final_impromptu.data.Datasource
import com.example.eslab_final_impromptu.databinding.FragmentSheetBinding

class SheetFragment : Fragment() {
    private var _binding: FragmentSheetBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myDataset = Datasource().loadSheets()
        val recyclerView=binding.recyclerView
        recyclerView.adapter = ItemAdapter(requireContext(), myDataset)
        recyclerView.setHasFixedSize(true)
    }
}
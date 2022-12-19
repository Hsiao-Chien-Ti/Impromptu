package com.example.eslab_final_impromptu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eslab_final_impromptu.databinding.FragmentHomeBinding
class HomeFragment : Fragment() {
    private val viewModel: PianoViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.connect.setOnClickListener {
            SocketHandler.acceptSocket()
//            SocketHandler.establishConnection()
            val action =
                HomeFragmentDirections.actionHomeFragmentToDetailFragment()
            this.findNavController().navigate(action)
        }
    }
}
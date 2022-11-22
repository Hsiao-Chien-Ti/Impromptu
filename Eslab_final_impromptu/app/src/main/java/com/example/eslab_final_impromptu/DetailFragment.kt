package com.example.eslab_final_impromptu

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.eslab_final_impromptu.databinding.FragmentDetailBinding
class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sheet.setOnClickListener {
            val action =
                DetailFragmentDirections.actionDetailFragmentToSheetFragment()
            this.findNavController().navigate(action)
        }
        binding.settings.setOnClickListener {
//            val action =
//                DetailFragmentDirections.actionDetailFragmentToSettingFragment()
//            this.findNavController().navigate(action)
            val mSocket = SocketHandler.getSocket()
            mSocket.emit("message","adiadiadi")

//            mSocket.on("eventName") { args ->
//                if (args[0] != null) {
//                    val counter = args[0] as Int
//                    Log.i("I",counter.toString())
//                }
//            }
        }
        binding.disconnect.setOnClickListener {
            val action =
                DetailFragmentDirections.actionDetailFragmentToHomeFragment()
            this.findNavController().navigate(action)
        }
    }
}
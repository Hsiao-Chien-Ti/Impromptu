package com.example.eslab_final_impromptu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.media.SoundPool
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.eslab_final_impromptu.SettingVariables.note
import com.example.eslab_final_impromptu.SettingVariables.row
import com.example.eslab_final_impromptu.databinding.FragmentDetailBinding
import java.io.IOException
import java.security.spec.MGF1ParameterSpec
import java.time.LocalDateTime


class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private var soundPool = SoundPool.Builder()
        .setMaxStreams(100)
        .build()
    private var recorder: MediaRecorder? = null
    private var recording=false
    private var isRecordable=true
    @Volatile
    private var socketConnect=true
    @Volatile
    private lateinit var sound0:Array<Int>
    private lateinit var sound1:Array<Int>
    private lateinit var sound2:Array<Int>
    private lateinit var sound3:Array<Int>
    private lateinit var sound4:Array<Int>
    private lateinit var sound:Array<Array<Int>>
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {

                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    isRecordable=false
                    Log.d("ddddd","permission")
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO )!= PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("ddddd","audio")
            requestPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
        sound0=arrayOf(soundPool.load(requireContext(), R.raw.c0, 1),soundPool.load(requireContext(), R.raw.d0, 1),soundPool.load(requireContext(), R.raw.e0, 1),soundPool.load(requireContext(), R.raw.f0, 1),soundPool.load(requireContext(), R.raw.g0, 1))
        sound1=arrayOf(soundPool.load(requireContext(), R.raw.a0, 1),soundPool.load(requireContext(), R.raw.b0, 1),soundPool.load(requireContext(), R.raw.c1, 1),soundPool.load(requireContext(), R.raw.d1, 1),soundPool.load(requireContext(), R.raw.e1, 1))
        sound2=arrayOf(soundPool.load(requireContext(), R.raw.f1, 1),soundPool.load(requireContext(), R.raw.g1, 1),soundPool.load(requireContext(), R.raw.a1, 1),soundPool.load(requireContext(), R.raw.b1, 1),soundPool.load(requireContext(), R.raw.c2, 1))
        sound3=arrayOf(soundPool.load(requireContext(), R.raw.e_1, 1),soundPool.load(requireContext(), R.raw.f_1, 1),soundPool.load(requireContext(), R.raw.g_1, 1),soundPool.load(requireContext(), R.raw.a_1, 1),soundPool.load(requireContext(), R.raw.b_1, 1))
        sound4=arrayOf(soundPool.load(requireContext(), R.raw.g_2, 1),soundPool.load(requireContext(), R.raw.a_2, 1),soundPool.load(requireContext(), R.raw.b_2, 1),soundPool.load(requireContext(), R.raw.c_1, 1),soundPool.load(requireContext(), R.raw.d_1, 1))
        sound= arrayOf(sound4,sound3,sound0,sound1,sound2)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.nowPlaying.text="Now Playing\n"+note[row]
        if(!SettingVariables.instanceExist)
        {
            SettingVariables.instanceExist=true
            Thread {
                row=2
//
                var prevData="0 0 0 0 0 0 0"
//                runOnUiThread{
//                    binding.nowPlaying.text="Now Playing\n"+note[row]
//                }

                while(true)
                {
                    if(!socketConnect)
                    {
                        break
                    }
                    val data=SocketHandler.readData()
                    if(data==prevData)
                        continue
                    if(data.length<=10)
                    {
                        runOnUiThread {
                            binding.nowPlaying.text="Not playing"
                            binding.np1.visibility=INVISIBLE
                            binding.np2.visibility=INVISIBLE
                            binding.np3.visibility=INVISIBLE
                            binding.np4.visibility=INVISIBLE
                            binding.np5.visibility=INVISIBLE
                        }
                        continue
                    }
                    else{
                        runOnUiThread {
                            if(binding.nowPlaying.text!="Now playing")
                            {
                                binding.nowPlaying.text="Now playing"
                                binding.np1.visibility=VISIBLE
                                binding.np2.visibility=VISIBLE
                                binding.np3.visibility=VISIBLE
                                binding.np4.visibility=VISIBLE
                                binding.np5.visibility=VISIBLE
                            }
                        }
                    }


                    for(i in 0..8){
                        if(data[i]=='1'&&data[i]!=prevData[i])
                        {
                            soundPool.play(sound[row][i/2], 1.0f, 1.0f, 0, 0, 1.0f)
                        }
                        else if(data[i]=='0')
                        {
                            soundPool.stop(sound[row][i/2])
                        }
                    }
                    if(data[0]=='1')
                        runOnUiThread { binding.np1.setTextColor(resources.getColor(R.color.blue))}
                    else
                        runOnUiThread { binding.np1.setTextColor(resources.getColor(R.color.vblack)) }
                    if(data[2]=='1')
                        runOnUiThread { binding.np2.setTextColor(resources.getColor(R.color.blue))}
                    else
                        runOnUiThread { binding.np2.setTextColor(resources.getColor(R.color.vblack)) }
                    if(data[4]=='1')
                        runOnUiThread { binding.np3.setTextColor(resources.getColor(R.color.blue))}
                    else
                        runOnUiThread { binding.np3.setTextColor(resources.getColor(R.color.vblack)) }
                    if(data[6]=='1')
                        runOnUiThread { binding.np4.setTextColor(resources.getColor(R.color.blue))}
                    else
                        runOnUiThread { binding.np4.setTextColor(resources.getColor(R.color.vblack)) }
                    if(data[8]=='1')
                        runOnUiThread { binding.np5.setTextColor(resources.getColor(R.color.blue))}
                    else
                        runOnUiThread { binding.np5.setTextColor(resources.getColor(R.color.vblack)) }
                    if(data.length>1&&SettingVariables.pitchSwitching) {
                        if(data[10]=='1'&&row<4)
                        {
                            row += 1
                            runOnUiThread{
                                binding.np1.text=note[row][0]
                                binding.np2.text=note[row][1]
                                binding.np3.text=note[row][2]
                                binding.np4.text=note[row][3]
                                binding.np5.text=note[row][4]
                            }
                        }
                        else if(data[10]=='2'&&row>0)
                        {
                            row -=1
                            runOnUiThread{
                                binding.np1.text=note[row][0]
                                binding.np2.text=note[row][1]
                                binding.np3.text=note[row][2]
                                binding.np4.text=note[row][3]
                                binding.np5.text=note[row][4]
                            }
                        }
                    }
                    Thread.sleep(160)
                    prevData=data
//

                }}.start()
        }

        binding.settings.setOnClickListener {
            val action =
                DetailFragmentDirections.actionDetailFragmentToSettingFragment()
            this.findNavController().navigate(action)
        }
        binding.disconnect.setOnClickListener {
            socketConnect=false
            SettingVariables.instanceExist=false
            if(recording)
                stopRecord()
            SocketHandler.closeConnection()
            val action =
                DetailFragmentDirections.actionDetailFragmentToHomeFragment()
            this.findNavController().navigate(action)
        }
        binding.record.setOnClickListener{
            if(!recording)
            {
                record()
                binding.record.text="Stop Record"
            }
            else
            {
                stopRecord()
                binding.record.text="Record"
            }
        }
        binding.sheet.setOnClickListener{
            val action =
                DetailFragmentDirections.actionDetailFragmentToSheetFragment()
            this.findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

    }
    fun record(){
        Log.d("ddddd",LocalDateTime.now().toString())
        if(!isRecordable)
            return

//        var mFileName:String = requireContext().externalMediaDirs[0].absolutePath + LocalDateTime.now().toString()+"test.3gp"
        var mFileName= "${requireContext().externalCacheDir?.absolutePath+ LocalDateTime.now().toString()}.3gp"
        Log.d("ddddd",mFileName)
        recorder = MediaRecorder().apply {
            setAudioSource( MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(mFileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                println(e)
            }
            start()
        }
        recording=true
    }
    fun stopRecord(){
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        recording=false
    }
}
fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return // Fragment not attached to an Activity
    activity?.runOnUiThread(action)
}
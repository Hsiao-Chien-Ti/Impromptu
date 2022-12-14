package com.example.eslab_final_impromptu

import android.util.Log
import io.socket.client.IO
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.net.Socket
import java.net.URISyntaxException
import java.util.concurrent.atomic.AtomicBoolean

object SocketHandler {
    @Volatile
    lateinit var mSocket: ServerSocket
    @Volatile
    lateinit var output: PrintWriter
    @Volatile
    lateinit var input: BufferedReader
    @Volatile
    var rcvData=""
    @Volatile
    lateinit var client:Socket
    @Volatile
    private var connectionState=AtomicBoolean(false)
    @Synchronized
    fun setSocket() {
        try {
            mSocket = ServerSocket(8080)
        }
        catch (e: URISyntaxException) {
            Log.w("e","error")
        }
    }
    @Synchronized
    fun acceptSocket(){
        Thread{
            try {
                Log.w("e","create socket")
                client = mSocket.accept()
                SettingVariables.connected=true
                Log.w("e","accept")
                connectionState.getAndSet(true)
                output = PrintWriter(client.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(client.inputStream))
                println(input)
                var cnt=0
                while(true)
                {
//                    Log.w("w", "reading")
                    if(!connectionState.get())
                        break
                    rcvData=input.readLine()+" "+cnt.toString()
                    println(rcvData)
                    sleep(160)
                    cnt++
                }

            } catch (e: URISyntaxException) {
                Log.w("e","error")
            }
        }.start()
    }
    @Synchronized
    fun getCState(): Boolean {
        return connectionState.get()
    }

    fun readData():String {
        return rcvData
    }

    @Synchronized
    fun closeConnection() {
        Log.d("ddddd","disconnect")
//        mSocket.disconnect()
        if(connectionState.get())
        {
            connectionState.getAndSet(false)
            client.close()
            rcvData=""
        }
    }
}
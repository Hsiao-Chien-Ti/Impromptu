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
    private var connectionState=false
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
                Log.w("e","accept")
                connectionState=true
                output = PrintWriter(client.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(client.inputStream))
                println(input)
                var cnt=0
                while(true)
                {
//                    Log.w("w", "reading")
                    if(!connectionState)
                        break
                    rcvData=input.readLine()+" "+cnt.toString()
                    println(rcvData)
                    sleep(100)
                    cnt++
                }

            } catch (e: URISyntaxException) {
                Log.w("e","error")
            }
        }.start()
    }
    @Synchronized
    fun getCState(): Boolean {
        return connectionState
    }

    fun readData():String {
        return rcvData
    }

    @Synchronized
    fun closeConnection() {
        Log.d("ddddd","disconnect")
//        mSocket.disconnect()
        if(connectionState)
        {
            connectionState=false
            client.close()
        }

    }
}
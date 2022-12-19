package com.example.eslab_final_impromptu

import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

class ServerThread : Thread() {
    private lateinit var handler: Handler

    override fun run() {
        super.run()

        val serverSocket = ServerSocket(1234)
        println("Server is listening on port 8080")
        while (true) {
            val socket = serverSocket.accept()
            println("New client connected")

            //multi thread to handle multi clients
            Thread {
                val input = socket?.getInputStream()
                val reader = BufferedReader(InputStreamReader(input))

//                val output = socket?.getOutputStream()
//                val writer = PrintWriter(output!!, true)

                var text: String
                try {
                    do {
                        text = reader.readLine()
                        Log.i("I",text)
//                        handler.sendMessage(handler.obtainMessage(0, text))
//                        writer.println(text.reversed())
                    } while (text != "bye")
                } catch (e: IllegalStateException) {
//                    writer.close()
                }
            }.start()
        }
    }

    fun setHandler(handler: Handler){
        this.handler = handler
    }
}
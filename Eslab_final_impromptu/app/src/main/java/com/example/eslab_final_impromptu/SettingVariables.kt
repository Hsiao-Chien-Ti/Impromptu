package com.example.eslab_final_impromptu

import android.text.TextUtils.split

object SettingVariables {
    @Volatile
    var pitchSwitching=true
    @Volatile
    var instanceExist=false
    @Volatile
    var row=2
    val note= arrayOf(split("G1 A1 B1 C2 D2"," "),split("E2 F2 G2 A3 B3"," "),split("C3 D3 E3 F3 G3"," "),split("A3 B3 C4 D4 E4", " "),split("F4 G4 A4 B4 C5"," "))
    @Volatile
    var connected=false
}
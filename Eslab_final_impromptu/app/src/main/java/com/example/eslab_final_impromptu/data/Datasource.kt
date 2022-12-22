package com.example.eslab_final_impromptu.data

import com.example.eslab_final_impromptu.R
import com.example.eslab_final_impromptu.model.Sheet

class Datasource {
    fun loadSheets():List<Sheet>{
        return listOf<Sheet>(
            Sheet(R.string.star,R.drawable.star),
            Sheet(R.string.jinglebell,R.drawable.jinglebell),
            Sheet(R.string.birthday,R.drawable.birthday),
            Sheet(R.string.christmas,R.drawable.christmas)
            )
    }
}
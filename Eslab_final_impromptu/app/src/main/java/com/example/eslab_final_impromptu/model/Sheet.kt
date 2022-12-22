package com.example.eslab_final_impromptu.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Sheet(
    @StringRes val stringResourceId: Int,
    @DrawableRes val imageResourceId: Int
)
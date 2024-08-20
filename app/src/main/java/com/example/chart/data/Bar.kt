package com.example.chart.data

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.Calendar
import java.util.Date


@kotlinx.parcelize.Parcelize
@Immutable
data class Bar(
    @SerializedName("o") val open: Float,
    @SerializedName("c") val close: Float,
    @SerializedName("l") val low: Float,
    @SerializedName("h") val high: Float,
    @SerializedName("t") val time: Long,
) :Parcelable{
    val calendar: Calendar
        get(){
            return Calendar.getInstance().apply {
                time = Date(this@Bar.time)
            }
        }
}


package com.niluogege.databindingdebug

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.niluogege.databindingdebug.Action

class KotlinVm(val activity: MainActivity) {

    val editText = ObservableField<String>("")
    val text = ObservableField<String>("")
    val visiable = ObservableBoolean(false)


    val btnClick = object : Action {
        override fun invoke(view: View) {
            Log.e("Tag", view.toString())
            Toast.makeText(activity, editText.get(), Toast.LENGTH_LONG).show()
        }

    }

    val includeBtnClick = object : Action {
        override fun invoke(view: View) {
            text.set("include 点击了")
        }

    }
}
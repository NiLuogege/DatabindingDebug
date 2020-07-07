package com.niluogege.databindingdebug

import android.view.View
import androidx.databinding.BindingAdapter

object BindingAdapter {

    @BindingAdapter("onClickBtn")
    @JvmStatic
    fun onClickBtn(view: View, action: Action) {
        view.setOnClickListener {
            action.invoke(view)
        }
    }

}
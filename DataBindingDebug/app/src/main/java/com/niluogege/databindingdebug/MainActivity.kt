package com.niluogege.databindingdebug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.niluogege.databindingdebug.databinding.ActivityMainBinding
import com.niluogege.databindingdebug.databinding.ActivityMergeBinding
import com.niluogege.databindingdebug.databinding.ActivityMergeBindingImpl

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val vm = KotlinVm(this)
        binding.viewModel = vm
        vm.text.set("平常心")
    }
}

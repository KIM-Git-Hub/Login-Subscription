package com.jaeyoung.studuyapp03

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jaeyoung.studuyapp03.databinding.ActivityMainBinding
import com.jaeyoung.studuyapp03.databinding.CreateAccountBinding
import com.jaeyoung.studuyapp03.databinding.MainPageBinding

class MainPageActivity : AppCompatActivity() {

    private var mBinding: MainPageBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = MainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}
package com.sola.anime.ai.generator.feature.space

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.sola.anime.ai.generator.databinding.ActivityManagerSpaceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManagerSpaceActivity : LsActivity<ActivityManagerSpaceBinding>(ActivityManagerSpaceBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        finish()
    }


}
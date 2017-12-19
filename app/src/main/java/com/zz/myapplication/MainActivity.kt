package com.zz.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity() {
    lateinit var helloView: HelloView1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        helloView = findViewById(R.id.helloView)
    }


    fun click(v: View) {
        when (v.id) {
            R.id.btn_line -> helloView.setType(HelloView1.TYPE_LINE)
            R.id.btn_rec -> helloView.setType(HelloView1.TYPE_REC)
            R.id.btn_rec -> helloView.setType(HelloView1.TYPE_CIRCLE)
            R.id.btn_start -> helloView.start()
            R.id.btn_reset -> helloView.reSet()
            R.id.btn_switch -> helloView.switchMusic()

        }
    }
}

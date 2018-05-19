package com.zz.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var helloView: HelloView1
    lateinit var listView: ListView
    var client = OkHttpClient.Builder()
            // 添加通用的Header
            .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.i("www", message); }).setLevel(HttpLoggingInterceptor.Level.BASIC))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

    internal fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl("http://192.168.2.213:3000/")
                // 添加Gson转换器
                                .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(ScalarsConverterFactory.create())
                // 添加Retrofit到RxJava的转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        helloView = findViewById(R.id.helloView)
        listView = findViewById(R.id.listView)
        initView()
        initData()
    }

    private fun initData() {
        val provideRetrofit = provideRetrofit(client)
        val create = provideRetrofit.create(MusicService::class.java)
        create.getMusicList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<String>> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onNext(t: List<String>) {
                        Log.i("www", "" + t.size)
                    }

                    override fun onComplete() {

                    }
                })
    }

    private fun initView() {
        listView.adapter = MyAdapter()
    }

    fun click(v: View) {
        when (v.id) {
            R.id.btn_line -> helloView.setType(HelloView1.TYPE_LINE)
            R.id.btn_rec -> helloView.setType(HelloView1.TYPE_REC)
            R.id.btn_circle -> helloView.setType(HelloView1.TYPE_CIRCLE)
            R.id.btn_circle_line -> helloView.setType(HelloView1.TYPE_CIRCLE_LINE)
            R.id.btn_circle_one -> helloView.setType(HelloView1.TYPE_CIRCLE_ONE)
            R.id.btn_start -> helloView.start()
            R.id.btn_reset -> helloView.reSet()
            R.id.btn_switch -> helloView.switchMusic("http://sc1.111ttt.cn:8282/2018/1/03m/13/396131210487.m4a?tflag=1519095601&pin=6cd414115fdb9a950d827487b16b5f97#.mp3")

        }
    }

    interface MusicService {
        @POST("getMusic")
        fun getMusicList(): Observable<List<String>>
    }

    class MyAdapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            return null
        }

        override fun getItem(position: Int): Any {
            return ""
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return 0
        }

    }


}

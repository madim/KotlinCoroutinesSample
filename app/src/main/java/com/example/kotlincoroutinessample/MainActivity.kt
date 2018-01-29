package com.example.kotlincoroutinessample


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

const val PAYMENT_REQUEST_CODE = 12
const val PAYMENT_RESULT_EXTRA = "payment_result"

class MainActivity : AppCompatActivity() {

    private lateinit var hello: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hello = findViewById<TextView>(R.id.hello)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        val protocol = Protocol("950903301272", "Madi", "Myrzabek", "Ulanovich",
                "123456789123456", "03.09.2017", "kbk", "kno", "knp", 12000)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, Gson().toJson(protocol))
        intent.setType("text/plain");

        fab.setOnClickListener {
            startActivityForResult(Intent.createChooser(intent, "Share"), PAYMENT_REQUEST_CODE)
        }
//        setup(hello, fab)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PAYMENT_REQUEST_CODE -> {
                val result = data?.getIntExtra(PAYMENT_RESULT_EXTRA, -1)
                hello.text = "resultCode: $resultCode \n result: $result"
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun setup(hello: TextView, fab: FloatingActionButton) {
        var result = "none"

        launch(UI) {
            var counter = 0
            while (true) {
                hello.text = "${++counter}: $result"
                delay(100)
            }
        }

        var x = 1
        fab.onClick {
            result = "fib($x) = ${fib(x)}"
            x++
        }
    }

    fun View.onClick(action: suspend () -> Unit) {
        val eventActor = actor<Unit>(UI, Channel.CONFLATED) {
            for (event in channel) action()
        }
        setOnClickListener {
            eventActor.offer(Unit)
        }
    }

    suspend fun fib(x: Int): Int = withContext(CommonPool) {
        fibBlocking(x)
    }

    fun fibBlocking(x: Int): Int =
            if (x <= 1) 1 else fibBlocking(x - 1) + fibBlocking(x - 2)
}

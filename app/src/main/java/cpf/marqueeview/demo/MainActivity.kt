package cpf.marqueeview.demo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        marquee_view_gone.setOnClickListener { marquee_view.visibility = View.GONE }
        marquee_view_hide.setOnClickListener { marquee_view.visibility = View.INVISIBLE }
        marquee_view_show.setOnClickListener { marquee_view.visibility = View.VISIBLE }
        marquee_view_start.setOnClickListener { marquee_view.start() }
        marquee_view_stop.setOnClickListener { marquee_view.stop() }
        marquee_view.setOnItemClickListener { position ->
            Toast.makeText(this@MainActivity, position.toString() + "", Toast.LENGTH_SHORT)
                .show()
        }
        marquee.bindToLifeCycle(lifecycle)
    }
}
package app.com.simpalmimagecapture

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import app.com.camera_lib.CameraGalley
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        button.setOnClickListener {
            CameraGalley.CallCamera=true
            startActivityForResult(Intent(this@Main2Activity,CameraGalley::class.java),120)
        }
        button2.setOnClickListener {
            CameraGalley.CallCamera=false
            startActivityForResult(Intent(this@Main2Activity,CameraGalley::class.java),120)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==120){
            if(resultCode== Activity.RESULT_OK){
                imageView2.setImageURI(data?.data as Uri)
            }
        }
    }
}

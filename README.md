# camera_gallery_image
This lib will help user to get image from gallary and camera all permission will take care by lib.
# Start Add Mainfest
under application please deff.

# <activity android:name="app.com.camera_lib.CameraGalley"/>
 # <provider
                android:name="app.com.camera_lib.GenericFileProvider"
                android:authorities="${applicationId}.my.package.name.provider"
                android:exported="false"
                android:grantUriPermissions="true">
            <meta-data
                    android:name="android.support.FILE_PROVIDER_PATHS"
                    android:resource="@xml/provider_paths"/>
        </provider>
        
        
   # MainActivity code:
   
     button.setOnClickListener {
            CameraGalley.CallCamera=true
            startActivityForResult(Intent(this@Main2Activity,CameraGalley::class.java),120)
        }
        button2.setOnClickListener {
            CameraGalley.CallCamera=false
            startActivityForResult(Intent(this@Main2Activity,CameraGalley::class.java),120)
        }
        
         override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==120){
            if(resultCode== Activity.RESULT_OK){
                imageView2.setImageURI(data?.data as Uri)
            }
        }
    }
        
        

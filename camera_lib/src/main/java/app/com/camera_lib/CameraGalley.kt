package app.com.camera_lib

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeFile
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class CameraGalley : AppCompatActivity() {
    private val PERMISSIONS_MULTIPLE_REQUEST = 1
    private var mImageFile: File? = null
    private var mImageUri: Uri? = null
    private var activity: Activity? = null


    var isFromCamera = false
    var isFromGallery = false

    override fun onStart() {
        super.onStart()
        if(CallCamera){
            callCamera()
        }else{
            callGallery()
        }
    }

    companion object{
        var CallCamera=true
    }

    fun callCamera() {
        isFromCamera = true
        isFromGallery = false
        this.activity = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        } else {
            cameraImageCall(activity!!)
        }
    }

    fun callGallery() {
        isFromCamera = false
        isFromGallery = true
        this.activity = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        } else {
            galleryImageCall(activity!!)
        }
    }

    private fun cameraImageCall(context: Context) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT > 23) {
            mImageFile = GalleryPickerUtil.createTempFile()
            mImageUri = FileProvider.getUriForFile(
                context,
                applicationContext.packageName + ".my.package.name.provider",
                mImageFile!!
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(
                android.provider.MediaStore.EXTRA_OUTPUT,
                mImageUri!!
            )
            startActivityForResult(intent, GalleryPickerUtil.CAPTURE_PHOTO)
        } else {
            startActivityForResult(intent, GalleryPickerUtil.CAPTURE_PHOTO)
        }
    }

    private fun galleryImageCall(context: Activity) {
        activity = context
        GalleryPickerUtil.launchGallery(context)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GalleryPickerUtil.CAPTURE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                try {
                    if (Build.VERSION.SDK_INT > 23) {

                        val ei = ExifInterface(mImageFile?.getAbsolutePath())
                        val orientation = ei.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED
                        )

                        var rotatedBitmap: Bitmap? = null
                        when (orientation) {

                            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap =
                                rotateImage(decodeFile(mImageFile?.getAbsolutePath()), 90f)

                            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
                                rotateImage(decodeFile(mImageFile?.getAbsolutePath()), 180f)

                            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
                                rotateImage(decodeFile(mImageFile?.getAbsolutePath()), 270f)
                        }

                        if (rotatedBitmap != null) {
                            crop(getImageUri(this, rotatedBitmap))
                        } else {
                            crop(getImageUri(this, decodeFile(mImageFile?.getAbsolutePath())))
                        }

                    } else {

                        val mImageBitmapUri = data!!.extras!!.get("data") as Bitmap
                        crop(getImageUri(this, mImageBitmapUri))

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            GalleryPickerUtil.SELECT_FILE -> try {
                if (resultCode == Activity.RESULT_OK) {

                    val mImageCaptureUri = data!!.data
                    crop(mImageCaptureUri!!)


                }
            } catch (e: Exception) {
                Toast.makeText(this, "Image Uri not come gallery", Toast.LENGTH_SHORT).show()

            }

            100 -> if (resultCode == Activity.RESULT_OK) {
                try {
                    if (Build.VERSION.SDK_INT > 23) {
                        val mImageBitmapUri = data?.data as Uri
                        mImageFile = File(getPath(data?.getData()))
                        setResult(Activity.RESULT_OK,data)
                        finish()

                    } else {
                        mImageFile = File(getPath(data?.getData())!!)
                        setResult(Activity.RESULT_OK,data)
                        finish()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Image Uri not come", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null) ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun decodeFile(imgPath: String): Bitmap? {
        var b: Bitmap? = null
        val max_size = 1000
        val f = File(imgPath)
        try {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            var fis = FileInputStream(f)
            BitmapFactory.decodeStream(fis, null, o)
            fis.close()
            var scale = 1
            if (o.outHeight > max_size || o.outWidth > max_size) {
                scale = Math.pow(
                    2.0,
                    Math.ceil(
                        Math.log(
                            max_size / Math.max(
                                o.outHeight,
                                o.outWidth
                            ).toDouble()
                        ) / Math.log(0.5)
                    ).toInt().toDouble()
                ).toInt()
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            fis = FileInputStream(f)
            b = BitmapFactory.decodeStream(fis, null, o2)
            fis.close()
        } catch (e: Exception) {
        }

        return b
    }

    fun crop(uri: Uri) {
        // Image Crop Code
        try {
            val cropIntent = Intent("com.android.camera.action.CROP")
            //indicate image type and Uri
            cropIntent.setDataAndType(uri, "image/*")
            //set crop properties
            cropIntent.putExtra("crop", "true")
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256)
            cropIntent.putExtra("outputY", 256)
            //retrieve data on return
            cropIntent.putExtra("return-data", true)
            //start the activity - we handle returning in onActivityResult
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            startActivityForResult(cropIntent, 100)

        } catch (e: Exception) {
            Toast.makeText(this, "error : $e", Toast.LENGTH_SHORT).show()
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) +
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) +
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                Snackbar.make(
                    this.findViewById(android.R.id.content),
                    "Please Grant Permissions to  Application for using camera and gallery",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("ENABLE",
                    View.OnClickListener {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission
                                    .READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            PERMISSIONS_MULTIPLE_REQUEST
                        )
                    }).show()
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSIONS_MULTIPLE_REQUEST
                )
            }
        } else {
            if (isFromCamera) {
                cameraImageCall(activity!!)
            } else if (isFromGallery) {
                galleryImageCall(activity!!)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {

        when (requestCode) {
            1 -> if (grantResults.size > 0) {
                val cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeExternalFile = grantResults[2] == PackageManager.PERMISSION_GRANTED

                if (cameraPermission && readExternalFile && writeExternalFile) {
                    if (isFromCamera) {
                        cameraImageCall(activity!!)
                    } else if (isFromGallery) {
                        galleryImageCall(activity!!)
                    }
                } else {
                    Snackbar.make(
                        window.decorView,
                        "Please Grant Permissions to work with camera and gallery.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("ENABLE",
                        View.OnClickListener {
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission
                                        .READ_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ),
                                PERMISSIONS_MULTIPLE_REQUEST
                            )
                        }).show()
                }
            }
        }
    }

}

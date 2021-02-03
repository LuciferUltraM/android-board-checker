package com.codemobi.boardchecker

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.Toast
import com.codemobi.boardchecker.adapter.PhotoAdapter
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_worksheet.*
import kotlinx.android.synthetic.main.content_worksheet.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import org.apache.sanselan.Sanselan
import java.io.FileOutputStream
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.common.IImageMetadata
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter
import org.apache.sanselan.formats.tiff.TiffImageMetadata
import org.apache.sanselan.formats.tiff.write.TiffOutputSet
import java.io.ByteArrayOutputStream
import java.nio.file.Files


class WorksheetActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ID = "EXTRA_ID"
    }

    private val LOG_TAG = "WorksheetActivity"
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_GALLERY = 2
    val REQUEST_SEND_PICTURE = 200

    var mWorksheetID: String = ""
    var mCurrentPhotoPath: String = ""

    var model: Model? = null
    var photoListItems: ArrayList<Photo>? = null
    var dateEmbed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worksheet)

        mWorksheetID = intent.getStringExtra(EXTRA_ID)
        getWorksheetInfo(mWorksheetID)

        fab.setOnClickListener {
            dispatchTakePictureIntent()
        }
        fabGallery.setOnClickListener {
            choosePhotoFromGallary()
        }
        val sharedPref = this.getSharedPreferences("com.codemobi.boardchecker", Context.MODE_PRIVATE) ?: return
        dateEmbed = sharedPref.getBoolean(LoginActivity.DATE_EMBED, true)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("ID", mWorksheetID)
        outState?.putString("PHOTO_PATH", mCurrentPhotoPath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        mWorksheetID = savedInstanceState!!.getString("ID")
        mCurrentPhotoPath = savedInstanceState!!.getString("PHOTO_PATH")
        getWorksheetInfo(mWorksheetID)
    }

    private fun getWorksheetInfo(id: String) {
        Fuel.get("/api/worksheet/$id").responseObject(Model.Deserializer()) { request, response, result ->
            when (result) {
                is Result.Success -> {
                    val (modelResult, _) = result
                    modelResult?.photos?.forEach { file ->
                        Log.d("qdp success", "${file.runningNumber} - ${file.fileURL}, - ${file.created}")
                    }
                    photoListItems = modelResult?.photos
                    model = modelResult
                    setWorksheet()
                }
                is Result.Failure -> {
                    Toast.makeText(this@WorksheetActivity, "Worksheet Not Found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setWorksheet() {
        val worksheet = model?.worksheet
        if (model?.worksheet == null) {
            finish()
        }

        supportActionBar?.setTitle("No. ${worksheet?.number} - ${worksheet?.name}")

        val adpater = PhotoAdapter(baseContext, model?.photos!!)
        photoListView.adapter = adpater
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.codemobi.boardchecker.fileprovider",
                            it
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic()
            MediaStore.Images.Media.insertImage(
                    applicationContext.contentResolver,
                    mCurrentPhotoPath,
                    "Board Checker",
                    "Test"
            )
            val intent = Intent(this, NewPhotoActivity::class.java).apply {
                putExtra(EXTRA_ID, mWorksheetID)
                if (dateEmbed) {
                    val timeStamp: String = SimpleDateFormat("yyyy/MM/dd HH:mm").format(Date())
                    val processedBitmap = drawTextToBitmap(mCurrentPhotoPath, timeStamp)
                    saveImage(processedBitmap, mCurrentPhotoPath)
                }else {
                    var bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
                    saveImage(bitmap, mCurrentPhotoPath)
                }
                putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath)
            }
            startActivityForResult(intent, REQUEST_SEND_PICTURE)
        } else if (requestCode == REQUEST_SEND_PICTURE && resultCode == RESULT_OK) {
            Snackbar.make(fab, "Send Successfully", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            getWorksheetInfo(mWorksheetID)
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            data?.data?.let {
                val contentURI = getPathFromURI(it)
                contentURI?.let {
                    MediaStore.Images.Media.insertImage(
                            applicationContext.contentResolver,
                            it,
                            "",
                            ""
                    )

                    val intent = Intent(this, NewPhotoActivity::class.java).apply {
                        putExtra(EXTRA_ID, mWorksheetID)
                        if (dateEmbed) {
                            val timeStamp: String = SimpleDateFormat("yyyy/MM/dd HH:mm").format(Date())
                            val processedBitmap = drawTextToBitmap(contentURI, timeStamp)
                            saveImage(processedBitmap, it)
                        } else {
                            var bitmap = BitmapFactory.decodeFile(contentURI)
                            saveImage(bitmap, it)
                        }
                        putExtra(MediaStore.EXTRA_OUTPUT, it)
                    }

                    startActivityForResult(intent, REQUEST_SEND_PICTURE)
                }
            }
        }
    }

    fun getPathFromURI(contentUri: Uri): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = applicationContext.contentResolver.query(contentUri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor.close()
        return res
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(mCurrentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    fun drawTextToBitmap(imagePath: String,
                         gText: String): Bitmap {
        val scale = resources.getDisplayMetrics().density
        var bitmap = BitmapFactory.decodeFile(imagePath)

        var bitmapConfig: android.graphics.Bitmap.Config? = bitmap.config
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true)

        val canvas = Canvas(bitmap)
        // new antialised Paint
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 255, 255))
        // text size in pixels
        paint.setTextSize(42 * scale)
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        // draw text to the Canvas center
        val bounds = Rect()
        paint.getTextBounds(gText, 0, gText.length, bounds)


        canvas.drawText(gText, (bitmap.width - bounds.width() - (50 * scale)), (bitmap.height - bounds.height()).toFloat(), paint)

        return bitmap
    }

    private fun saveImage(finalBitmap: Bitmap, toPath: String) {
        var bos = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        var data = bos.toByteArray()

        var outputSet: TiffOutputSet? = null
        val metadata = Sanselan.getMetadata(File(toPath))
        val jpegMetadata = metadata as JpegImageMetadata
        if (null != jpegMetadata) {
            val exif = jpegMetadata.exif
            if (null != exif) {
                outputSet = exif.outputSet
            }
        }

        if (outputSet != null) {
            bos.flush()
            bos.close()
            bos = ByteArrayOutputStream()
            val er = ExifRewriter()
            er.updateExifMetadataLossless(data, bos, outputSet)
            data = bos.toByteArray()
        }

        val file = File(toPath)
        if (file.exists()) file.delete()
        try {
            val stream = FileOutputStream(file)
            stream.write(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}


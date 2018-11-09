package com.codemobi.boardchecker

import android.app.ProgressDialog
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.result.Result

import kotlinx.android.synthetic.main.activity_new_photo.*
import java.io.File

class NewPhotoActivity : AppCompatActivity() {

    val LOG_TAG = "NewPhotoActivity"

    var mWorksheetID: String = ""
    var mCurrentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_photo)
        setSupportActionBar(toolbar)

        mWorksheetID = intent.getStringExtra(WorksheetActivity.EXTRA_ID)
        mCurrentPhotoPath = intent.getStringExtra(MediaStore.EXTRA_OUTPUT)

        Log.d(LOG_TAG, "photoPath : $mCurrentPhotoPath")
        Glide.with(this).load(mCurrentPhotoPath).thumbnail(0.1f).into(imageView)

        fab.setOnClickListener {
            sendPhoto()
        }
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
    }

    private fun sendPhoto() {
        val loadingDialog = ProgressDialog.show(this, "Fetch photos", "Loading...", true, false)
        Fuel.upload("/api/worksheet/$mWorksheetID/photo/new", Method.POST)
                .source { request, url ->
                    File(mCurrentPhotoPath)
                }.name {
                    "uploadFile"
//                }.progress { readBytes, totalBytes ->
//                    val progress = readBytes.toFloat() / totalBytes.toFloat()
                }.responseString { request, response, result ->
                    loadingDialog.dismiss()
                    when(result) {
                        is Result.Success -> {
                            Log.d(LOG_TAG, result.get())
                            setResult(RESULT_OK)
                            finish()
                        }
                        is Result.Failure -> {
                            Log.e(LOG_TAG, result.get())
                            runOnUiThread {
                                Snackbar.make(fab, "Error!!!", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show()
                            }
                        }
                    }

                }

    }

}

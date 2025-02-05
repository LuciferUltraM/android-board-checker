package com.codemobi.boardchecker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.codemobi.boardchecker.adapter.WorksheetAdapter
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val LOG_TAG = "MainActivity"
    private val TAG = "PermissionCamera"
    private val RECORD_REQUEST_CODE = 101
    private val BASE_PATH = "http://ps.code-mobi.com"

    var worksheetListItems: ArrayList<Worksheet>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FuelManager.instance.basePath = BASE_PATH

        fab.setOnClickListener {
            val integrator = IntentIntegrator(this@MainActivity)
            integrator.setPrompt("Scan Ticket")
            integrator.setBeepEnabled(true)
            integrator.initiateScan()
        }

        setupPermissions()
        getWorksheets()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to camera denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                }
            }
        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
//    ﻿override fun onRequestPermissionsResult(requestCode: Int,
//                                             permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            RECORD_REQUEST_CODE -> {
//
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//
//                    Log.i(TAG, "Permission has been denied by user")
//                } else {
//                    Log.i(TAG, "Permission has been granted by user")
//                }
//            }
//        }
//    }﻿

    fun toast(text: String) {
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_login -> {
                goLogin()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                toast(result.contents)
                openWorksheet(result.contents)
            }
        } else {
            if (requestCode == LoginActivity.REQUEST_CODE) {
                getWorksheets()
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun goLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {  }
        startActivityForResult(intent, LoginActivity.REQUEST_CODE)
    }

    private fun getWorksheets() {
        val sharedPref = this.getSharedPreferences("com.codemobi.boardchecker", Context.MODE_PRIVATE) ?: return
        val teamID = sharedPref.getString(LoginActivity.TEAM_ID, "")
        if (teamID == "") {
            goLogin()
            return
        }

        setTitle("Board Checker - Team ID : " + teamID)

        Fuel.get("/api/team/$teamID/worksheets").responseObject(Worksheets.Deserializer()) { request, response, result ->
            when (result) {
                is Result.Success -> {
                    val (model, _) = result
                    worksheetListItems = model?.worksheets
                    setWorksheets()
                }
                is Result.Failure -> {
                    Toast.makeText(this@MainActivity, "Data Not Found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setWorksheets() {
        val adapter = WorksheetAdapter(baseContext, worksheetListItems!!)
        projectListView.adapter = adapter
        projectListView.setOnItemClickListener { parent, view, position, id ->
            val selectedWorksheetID = worksheetListItems?.get(position)?.id
            openWorksheet("$selectedWorksheetID")
        }
    }

    fun openWorksheet(id: String) {
        val intent = Intent(this, WorksheetActivity::class.java).apply {
            putExtra(WorksheetActivity.EXTRA_ID, id)
        }
        startActivity(intent)
    }
}

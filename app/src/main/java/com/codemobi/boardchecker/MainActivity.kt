package com.codemobi.boardchecker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.codemobi.boardchecker.adapter.ProjectAdapter
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val LOG_TAG = "MainActivity"

    private val BASE_PATH = "http://192.168.1.120:4000"

    var projectListItems: ArrayList<Project>? = null

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

        getProjects()
    }

    fun toast(text: String) {
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                toast(result.contents)
                openProject(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getProjects() {
        Fuel.get("/api/projects").responseObject(Projects.Deserializer()) { request, response, result ->
            when (result) {
                is Result.Success -> {
                    val (model, _) = result
                    projectListItems = model?.projects
                    setProjects()
                }
                is Result.Failure -> {
                    Toast.makeText(this@MainActivity, "Data Not Found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setProjects() {
        val adapter = ProjectAdapter(baseContext, projectListItems!!)
        projectListView.adapter = adapter
        projectListView.setOnItemClickListener { parent, view, position, id ->
            val selectedProjectID = projectListItems?.get(position)?.id
            openProject("$selectedProjectID")
        }
    }

    fun openProject(id: String) {
        val intent = Intent(this, ProjectActivity::class.java).apply {
            putExtra(ProjectActivity.EXTRA_ID, id)
        }
        startActivity(intent)
    }
}

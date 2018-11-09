package com.codemobi.boardchecker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.content.Context

import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    companion object {
        const val REQUEST_CODE = 1001
        const val TEAM_ID = "TEAM_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        val sharedPref = this.getSharedPreferences("com.codemobi.boardchecker", Context.MODE_PRIVATE) ?: return
        val teamID = sharedPref.getString(TEAM_ID, "")
        textViewTeamID.setText(teamID)

        buttonSignIn.setOnClickListener { attemptLogin() }
    }

    fun attemptLogin() {
        val sharedPref = this.getSharedPreferences("com.codemobi.boardchecker", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(TEAM_ID, textViewTeamID.text.toString())
            commit()
        }
        finish()
    }
}

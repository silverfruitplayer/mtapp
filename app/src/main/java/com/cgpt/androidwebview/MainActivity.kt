package com.cgpt.androidwebview


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.cgpt.androidwebview.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var myWebView: WebView

    private fun getLogcatInfo(): String {
        val process = Runtime.getRuntime().exec("logcat -d")
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        val log = StringBuilder()
        var line: String?

        while (bufferedReader.readLine().also { line = it } != null) {
            log.append(line).append("\n")
        }

        return log.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "App Crashed!? please send logs to get fixed ASAP!", Snackbar.LENGTH_LONG)
                .setAction("Send Log", View.OnClickListener {
                    val emailIntent = Intent(Intent.ACTION_SENDTO)
                    emailIntent.data = Uri.parse("mailto:ajeynagar846@gmail.com")
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Logcat Information")

                    // Attach log information to the email body
                    val logcatInfo = getLogcatInfo()
                    emailIntent.putExtra(Intent.EXTRA_TEXT, logcatInfo)

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send email..."))
                    } catch (ex: ActivityNotFoundException) {
                        Snackbar.make(view, "No email app installed", Snackbar.LENGTH_SHORT).show()
                    }
                }).show()
        }


        // Initialize WebView
        myWebView = findViewById(R.id.webview)

        // Call the loadWebView function
        loadWebView()
    }

    private fun loadWebView() {
        // Enable JavaScript in the WebView
        myWebView.settings.javaScriptEnabled = true

        // Enable WebView to open popups (important for JavaScript prompts)
        myWebView.settings.javaScriptCanOpenWindowsAutomatically = true

        // Set a WebViewClient to handle redirects within the WebView
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Allow navigation to any URL
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Handle redirection or additional actions after the page has finished loading
                Log.d("WebView", "Page finished loading: $url")
            }
        }

        // Set a WebChromeClient to handle JavaScript alert dialogs
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                // Handle JavaScript alert dialogs
                // You can implement your own custom alert or log the message
                Log.d("WebViewAlert", message ?: "")
                result?.confirm()
                return true
            }
        }

        // Load the initial URL
        myWebView.loadUrl("https://mt-fva7.vercel.app/movie%20site/Index.html")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        // Check if WebView can go back
        if (myWebView.canGoBack()) {
            // If there is a previous page, go back
            myWebView.goBack()
        } else {
            // If there is no previous page, perform default back button behavior
            super.onBackPressed()
        }
    }
}

package com.example.childrencompetition

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import core.Api
import core.NetworkLiveData
import core.TAG
import core.WebSocketNotifications
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_participant_edit.*
import todo.data.local.OfflineWorker
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkLiveData: NetworkLiveData

    private val REQUEST_PERMISSION = 10
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2

    lateinit var currentPhotoPath: String


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        connectivityManager = getSystemService(android.net.ConnectivityManager::class.java)
        networkLiveData = NetworkLiveData(connectivityManager)
        networkLiveData.observe(this, Observer{
            Log.v(TAG, "recieved notification from live data : $it")
            NetworkLiveData.networkInfo = it
            //onlineSwitch.isChecked = it;
            //Log.i("MainActivity","Online switch is checked: "+onlineSwitch.isChecked.toString())
            if(it==true) {
                title = getString(R.string.app_name_online);
                toolbar.setBackgroundColor(Color.GREEN)
                //onlineSwitch.setBackgroundColor(Color.GREEN)
                backOnline()
            }
            else{
                title = getString(R.string.app_name_offline)
                toolbar.setBackgroundColor(Color.RED)
                //onlineSwitch.setBackgroundColor(Color.RED)
            }
        })

    }

    @SuppressLint("RestrictedApi")
    fun backOnline(){
        Api.tokenInterceptor.token?.let { WebSocketNotifications.initializeWebSocket(it) }
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        /*val inputData = androidx.work.Data.Builder()
            .build()

         */

        val myWork = OneTimeWorkRequest.Builder(OfflineWorker::class.java)
            .setConstraints(constraints)
            .build()


        val workId = myWork.id
        WorkManager.getInstance(this).apply {
            // enqueue Work
            enqueue(myWork)
            // observe work status
            getWorkInfoByIdLiveData(workId)
                .observe(this@MainActivity) { status ->
                    val isFinished = status?.state?.isFinished
                    Log.d(TAG, "Job $workId; finished: $isFinished")
                }
        }
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

    /*
    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION)
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createCapturedPhoto()
                } catch (ex: IOException) {
                    null
                }
                Log.d("MainActivity", "photofile $photoFile");
                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "ro.ubbcluj.cs.ilazar.myphoto.fileprovider",
                        it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                val uri = Uri.parse(currentPhotoPath)
                img_view_photo.setImageURI(uri)
            }
            else if (requestCode == REQUEST_PICK_IMAGE) {
                val uri = data?.getData()
                img_view_photo.setImageURI(uri)
            }
        }
    }

    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}",".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }*/ */



}
package app.todo.participant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.childrencompetition.R
import core.NetworkLiveData
import core.TAG
import kotlinx.android.synthetic.main.fragment_participant_edit.*
import todo.data.local.Participant
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ParticipantEditFragment : Fragment() {
    companion object {
        const val ITEM_ID = "ITEM_ID"
    }



    private val REQUEST_PERMISSION = 10
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2

    lateinit var currentPhotoPath: String




    private lateinit var viewModel: ParticipantEditViewModel
    private var itemId: String? = null
    private var item: Participant? = null

    private lateinit var connectivityManager: ConnectivityManager
    lateinit var networkLiveData: NetworkLiveData

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(ITEM_ID)) {
                itemId = it.getString(ITEM_ID).toString()
            }
        }
        connectivityManager =
            activity?.getSystemService(android.net.ConnectivityManager::class.java) as ConnectivityManager
        networkLiveData = NetworkLiveData(connectivityManager)
        networkLiveData.observe(this, Observer {
            Log.v(TAG, "recieved notification from live data : $it")
            NetworkLiveData.networkInfo = it
            viewModel.connected = it
            //onlineSwitch.isChecked = it;
            //Log.i("MainActivity","Online switch is checked: "+onlineSwitch.isChecked.toString())
        })


    }

    /*
    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_participant_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated")
//        item_text.setText(itemId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fab_save.setOnClickListener {
            Log.v(TAG, "save item")
            val i = item
            if (i != null) {
                i.age = participant_age.text.toString()
                i.name = participant_name.text.toString()
                i.participationsNo = "0"
                viewModel.saveOrUpdateItem(i)
            }
        }
        img_btn_camera.setOnClickListener { openCamera() }
        img_btn_gallery.setOnClickListener { openGallery() }


    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ParticipantEditViewModel::class.java)
        viewModel.fetching.observe(viewLifecycleOwner, { fetching ->
            Log.v(TAG, "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        })
        viewModel.fetchingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.completed.observe(viewLifecycleOwner, { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                //findNavController().popBackStack()
                findNavController().navigate(R.id.action_ItemEditFragment_to_ItemListFragment)
            }
        })
        val id = itemId
        if (id == null) {
            item = Participant(0, "", "", "")
        } else {
            viewModel.getItemById(id).observe(viewLifecycleOwner, {
                Log.v(TAG, "update items")
                if (it != null) {
                    item = it
                    participant_name.setText(it.name)
                    participant_age.setText(it.age)
                }
            })
        }
    }




    private fun checkCameraPermission() {
        if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
            != PackageManager.PERMISSION_GRANTED) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_PERMISSION)
            }
        }
    }



    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.let {
                intent.resolveActivity(it.packageManager)?.also {
                    val photoFile: File? = try {
                        createCapturedPhoto()
                    } catch (ex: IOException) {
                        null
                    }
                    Log.d("MainActivity", "photofile $photoFile");
                    photoFile?.also {
                        val photoURI = FileProvider.getUriForFile(
                            requireActivity(),
                            "com.example.childrencompetition.fileprovider",
                            it
                        )
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
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




    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }



    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}",".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }


}


/*
class ParticipantEditFragment : Fragment() {
    companion object {
        const val ITEM_ID = "ITEM_ID"
    }

    private lateinit var viewModel: ParticipantEditViewModel
    private var itemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(ITEM_ID)) {
                itemId = it.getString(ITEM_ID).toString()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_participant_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated")
//        item_text.setText(itemId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fab.setOnClickListener {
            Log.v(TAG, "save item")
            viewModel.saveOrUpdateItem(participant_name.text.toString(),participant_age.text.toString())
        }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ParticipantEditViewModel::class.java)
        viewModel.item.observe(viewLifecycleOwner, { item ->
            Log.v(TAG, "update items")
            participant_name.setText(item.name)
            participant_age.setText(item.age)
        })
        viewModel.fetching.observe(viewLifecycleOwner, { fetching ->
            Log.v(TAG, "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        })
        viewModel.fetchingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.completed.observe(viewLifecycleOwner, Observer { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                findNavController().navigateUp()
            }
        })
        val id = itemId
        if (id != null) {
            viewModel.loadItem(id)
        }
    }
}
*/
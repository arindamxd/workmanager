package com.example.background

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import android.net.Uri
import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker
import com.example.background.workers.makeStatusNotification

class BlurViewModel : ViewModel() {

    private val TAG by lazy { BlurViewModel::class.java.simpleName }

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null

    private val workManager = WorkManager.getInstance()

    // New instance variable for the WorkInfo
    internal val outputWorkInfos: LiveData<List<WorkInfo>>


    // In the BlurViewModel constructor
    init {
        // This transformation makes sure that whenever the current work Id changes the WorkInfo
        // the UI is listening to changes
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }


    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Setters
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    /*internal fun setLifecycleOwnerRef(lifecycleOwner: LifecycleOwner?) {
        lifecycleOwner?.let {
            this.lifecycleOwner = it
        }
    }*/

    internal fun applyBlur(blurLevel: Int) {

        //workManager.enqueue(OneTimeWorkRequest.from(BlurWorker::class.java))

        /*val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
                .setInputData(createInputDataForUri())
                .build()

        workManager.enqueue(blurRequest)*/

        // -------



        // Add WorkRequest to Cleanup temporary images
        //var continuation = workManager.beginWith(OneTimeWorkRequest.from(CleanupWorker::class.java))

        var continuation = workManager.beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
        )


        // Add WorkRequest to blur the image
        /*val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
                .setInputData(createInputDataForUri())
                .build()

        continuation = continuation.then(blurRequest)*/

        // Add WorkRequests to blur the image the number of times requested
        for (i in 0 until blurLevel) {

            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()

            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if (i == 0) {
                blurBuilder.setInputData(createInputDataForUri())
            }

            continuation = continuation.then(blurBuilder.build())
        }

        // Add WorkRequest to save the image to the filesystem
        //val save = OneTimeWorkRequest.Builder(SaveImageToFileWorker::class.java).build()
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
                .addTag(TAG_OUTPUT)
                .build()

        continuation = continuation.then(save)

        // Actually start the work
        continuation.enqueue()

        //observeSave(save)
    }

    /*private fun observeSave(save: OneTimeWorkRequest) {
        workManager.getWorkInfoByIdLiveData(save.id).observe(lifecycleOwner as LifecycleOwner, Observer { info ->
            if (info != null && info.state.isFinished) {
                val result = info.outputData.getString(KEY_IMAGE_URI)

                // ... do something with the result ...

                Log.e(TAG, result)
            }
        })
    }*/

    /**
     * Creates the input data bundle which includes the Uri to operate on
     * @return Data which contains the Image Uri as a String
     */
    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        return builder.build()
    }
}

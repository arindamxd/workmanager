package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import java.lang.IllegalArgumentException

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val TAG by lazy { BlurWorker::class.java.simpleName }


    override fun doWork(): Result {

        val appContext = applicationContext

        makeStatusNotification("Blurring image", appContext)
        //sleep()

        val resourceUri = inputData.getString(KEY_IMAGE_URI)


        return try {

            // Create a Bitmap from the test image
            //val picture = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test)

            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input Uri")
                throw IllegalArgumentException("Invalid input Uri")
            }

            var resolver = appContext.contentResolver
            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val output = blurBitmap(picture, appContext)

            // Write bitmap to a temp file
            val outputUri = writeBitmapToFile(appContext, output)

            makeStatusNotification("Output is $outputUri", appContext)

            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            /*val outputData: Data = mapOf(
                    "KEY_X_ARG" to 42,
                    "KEY_Y_ARG" to 421,
                    "KEY_Z_ARG" to 8675309).toWorkData()*/

            Result.success(outputData)

        } catch (throwable: Throwable) {

            Log.e(TAG, "Error applying blur", throwable)

            makeStatusNotification("Blurring image failed", appContext)

            Result.failure()
        }
    }
}
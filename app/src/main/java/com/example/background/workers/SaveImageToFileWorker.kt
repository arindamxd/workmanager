package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val TAG by lazy { SaveImageToFileWorker::class.java.simpleName }

    private val Title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

    override fun doWork(): Result {

        val appContext = applicationContext

        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Saving image", appContext)
        sleep()

        val resolver = appContext.contentResolver

        return try {

            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val imageUrl = MediaStore.Images.Media.insertImage(resolver, bitmap, Title, dateFormatter.format(Date()))

            if (!imageUrl.isNullOrEmpty()) {

                val outputData = workDataOf(KEY_IMAGE_URI to imageUrl)

                makeStatusNotification("Saved: $imageUrl", appContext)

                Result.success(outputData)

            } else {

                Log.e(TAG, "Writing to MediaStore failed")

                makeStatusNotification("Writing to MediaStore failed", appContext)

                Result.failure()
            }

        } catch (e: Exception) {

            Log.e(TAG, "Unable to save image to Gallery", e)

            makeStatusNotification("Unable to save image to Gallery", appContext)

            Result.failure()
        }
    }
}
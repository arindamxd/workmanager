package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.OUTPUT_PATH
import java.io.File
import java.lang.Exception

class CleanupWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val TAG by lazy { CleanupWorker::class.java.simpleName }


    override fun doWork(): Result {

        val appContext = applicationContext

        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Cleaning up old temporary files", appContext)
        sleep()

        return try {

            val outputDirectory = File(appContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {

                val entries = outputDirectory.listFiles()
                entries?.let {
                    for (entry in it) {

                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {

                            val deleted = entry.delete()
                            Log.i(TAG, String.format("Deleted %s - %s", name, deleted))
                        }
                    }
                }
            }

            Result.success()

        } catch (exception: Exception) {

            Log.e(TAG, "Error cleaning up", exception)

            makeStatusNotification("Error cleaning up", appContext)

            Result.failure()
        }
    }
}
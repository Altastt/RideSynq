package com.example.ridesynq.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object FileUtils {

    // Copies a file from a source Uri to the app's internal files directory
    suspend fun copyUriToInternalStorage(context: Context, sourceUri: Uri, userId: Int): File? = withContext(Dispatchers.IO) {
        var inputStream: java.io.InputStream? = null
        var outputStream: FileOutputStream? = null
        var newFile: File? = null
        try {
            inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                Log.e("FileUtils", "Failed to open input stream for $sourceUri")
                return@withContext null
            }

            // Create a destination file in app's private storage
            // Use a unique name, e.g., user_{userId}_avatar.jpg
            val extension = getExtensionFromUri(context, sourceUri) ?: "jpg" // Get extension or default
            val fileName = "user_${userId}_avatar_${UUID.randomUUID()}.$extension"
            val outputDir = File(context.filesDir, "avatars") // Subdirectory for organization
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            newFile = File(outputDir, fileName)

            // Delete previous avatar for this user if it exists (optional but recommended)
            deletePreviousAvatar(outputDir, userId)


            outputStream = FileOutputStream(newFile)
            inputStream.copyTo(outputStream)

            Log.d("FileUtils", "Successfully copied $sourceUri to ${newFile.absolutePath}")
            return@withContext newFile

        } catch (e: IOException) {
            Log.e("FileUtils", "IOException during copy: ${e.message}", e)
            newFile?.delete() // Clean up partially created file on error
            return@withContext null
        } catch (e: SecurityException) {
            Log.e("FileUtils", "SecurityException accessing $sourceUri: ${e.message}", e)
            return@withContext null
        } catch (e: Exception) {
            Log.e("FileUtils", "Generic exception during copy: ${e.message}", e)
            newFile?.delete()
            return@withContext null
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                // Ignore close exceptions or log them
            }
        }
    }

    // Helper to delete previous avatars for a user
    private fun deletePreviousAvatar(outputDir: File, userId: Int) {
        outputDir.listFiles { file ->
            file.name.startsWith("user_${userId}_avatar_")
        }?.forEach { oldFile ->
            if (oldFile.delete()) {
                Log.d("FileUtils", "Deleted previous avatar: ${oldFile.name}")
            } else {
                Log.w("FileUtils", "Failed to delete previous avatar: ${oldFile.name}")
            }
        }
    }


    // Helper to try and get file extension
    private fun getExtensionFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.getType(uri)?.substringAfterLast('/')
        } catch (e: Exception) {
            null
        }
    }
}
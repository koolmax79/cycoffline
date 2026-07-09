package ru.koolmax.cycoffline.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object MediaLibrary {

    fun getPhotosUri(startDate: Long, endDate: Long, context: Context): ArrayList<Uri> {
        val photoUris = ArrayList<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val selection = MediaStore.Images.Media.DATE_TAKEN + " BETWEEN ? AND ?"
        val selectionArgs = arrayOf(startDate.toString(), endDate.toString())
        val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC"
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idx = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                if(idx>0) {
                    val id = cursor.getLong(idx)
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    photoUris.add(uri)
                }
            }
            cursor.close()
        }
        return photoUris
    }
}
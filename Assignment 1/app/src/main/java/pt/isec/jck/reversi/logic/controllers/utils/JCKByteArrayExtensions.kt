package pt.isec.jck.reversi.logic.controllers.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

fun ByteArray.toBase64(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

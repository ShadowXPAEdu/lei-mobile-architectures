package pt.isec.jck.reversi.logic.controllers.utils

import android.graphics.Bitmap
import android.util.Base64

fun String.toBitmap(): Bitmap {
    return Base64.decode(this, Base64.NO_WRAP).toBitmap()
}

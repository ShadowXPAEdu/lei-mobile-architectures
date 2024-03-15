package pt.isec.jck.reversi.logic.controllers.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun Bitmap.toBase64(): String {
    val bos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, bos)
    return bos.toByteArray().toBase64()
}

fun Bitmap.scale(maxSize: Int = 120): Bitmap {
    return Bitmap.createScaledBitmap(
        this, maxSize, maxSize, false
    )
}

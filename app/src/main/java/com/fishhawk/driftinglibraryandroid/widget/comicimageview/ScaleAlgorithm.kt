package com.fishhawk.driftinglibraryandroid.widget.comicimageview

//import android.graphics.Bitmap
//import org.opencv.android.OpenCVLoader
//import org.opencv.android.Utils
//import org.opencv.core.Mat
//import org.opencv.core.Size
//import org.opencv.imgproc.Imgproc

object ScaleAlgorithm {
//
//    init {
//        OpenCVLoader.initDebug()
//    }
//
//    fun scale(bitmap: Bitmap, scale: Float): Bitmap {
//        val mat = Mat()
//        val scaledMat = Mat()
//
//        Utils.bitmapToMat(bitmap, mat)
//
//        Imgproc.resize(
//            mat, scaledMat,
//            Size(0.0, 0.0),
//            scale.toDouble(), scale.toDouble(),
//            Imgproc.INTER_AREA
//        )
//
//        val scaledBitmap = Bitmap.createBitmap(scaledMat.width(), scaledMat.height(), bitmap.config)
//        Utils.matToBitmap(scaledMat, scaledBitmap)
//        return scaledBitmap
//    }
}
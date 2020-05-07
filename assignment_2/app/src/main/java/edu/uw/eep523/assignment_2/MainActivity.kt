package edu.uw.eep523.assignment_2

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.ArrayList
import com.divyanshu.draw.widget.DrawView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark

class MainActivity : AppCompatActivity() {

    private var isLandScape: Boolean = false
    private var imageUri: Uri? = null
    private var imageUri2: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getRuntimePermissions()
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }
        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        savedInstanceState?.let {
            imageUri = it.getParcelable(KEY_IMAGE_URI)
            imageUri2 = it.getParcelable(KEY_IMAGE_URI1)
        }
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }
    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        with(outState) {
            putParcelable(KEY_IMAGE_URI, imageUri)
            putParcelable(KEY_IMAGE_URI1, imageUri2)
        }
    }

    fun startCameraIntentForResult_L(view: View) {
        // Clean up last time's image
        imageUri = null
        imageUri2 = null
//        imageViewLeft?.setImageBitmap(null)
//        imageViewLeft?.background = BitmapDrawable(resources, null)

        // Take picture and add to gallery
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_L)
        }
    }

    fun startCameraIntentForResult_R(view: View) {
        // Clean up last time's image
        imageUri = null
        imageUri2 = null
//        imageViewLeft?.setImageBitmap(null)
//        imageViewRight?.setImageBitmap(null)

        // Take picture and add to gallery
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri2 = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri2)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_R)
        }
    }

    fun startChooseImageIntentForResult_L(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE_L)
    }

    fun startChooseImageIntentForResult_R(view: View) {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE_R)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE_L && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage(imageUri,0)
        } else if (requestCode == REQUEST_CHOOSE_IMAGE_L && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imageViewLeft?.background= BitmapDrawable(resources,imageBitmap)

//            tryReloadAndDetectInImage(0)
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_R && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage(imageUri2,1)
        } else if (requestCode == REQUEST_CHOOSE_IMAGE_R && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri2 = data!!.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
            imageViewRight?.background= BitmapDrawable(resources,imageBitmap)
//            tryReloadAndDetectInImage(1)
        }
    }

    private fun tryReloadAndDetectInImage(uri:Uri?, status:Int) {
        try {
            if (uri == null) {
                return
            }
            val imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri!!)
                ImageDecoder.decodeBitmap(source)
            }
//                imageViewLeft?.setImageBitmap(imageBitmap) //previewPane is the ImageView from the layout
            if(status == 0)
                imageViewLeft?.background= BitmapDrawable(resources,imageBitmap)
            if(status == 1)
                imageViewRight?.background= BitmapDrawable(resources,imageBitmap)

        } catch (e: IOException) {
        }
    }

    fun blurImageL(view:View){
        try {
            if (imageUri == null) {
                return
            }
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imageViewLeft?.background = BitmapDrawable(resources, bitmapBlur(imageBitmap, 0.3f, 50))
//            Log.d("aaaaaaaaaaa","bbbbbbbbbbbb")
        }
        catch(e: IOException) {}
    }

    fun blurImageR(view:View){
        try {
            if (imageUri2 == null) {
                return
            }
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
            imageViewRight?.background = BitmapDrawable(resources, bitmapBlur(imageBitmap, 0.3f, 50))
        }
        catch(e: IOException) {}
    }

    fun clearDrawL(view:View){
        imageViewLeft.clearCanvas()
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        imageViewLeft?.background = BitmapDrawable(resources, imageBitmap)
    }

    fun clearDrawR(view:View){
        imageViewRight.clearCanvas()
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)
        imageViewRight?.background = BitmapDrawable(resources, imageBitmap)
    }
/*
    fun swapFace(view:View){
        val contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS)
            for(point in contour.points ){
                val px = point.x
                val py = point.y
            }

        if(face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY)
            val smileProb = face.smilingProbability
        if(face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY)
            val rightEyeOpenProb = face.rightEyeOpenProbability

    }

    private fun faceDetection () {
        val mSelectedImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val image = FirebaseVisionImage.fromBitmap(mSelectedImage)
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
//          .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
            detector.detectInImage(image)
                addOnSuccessListener { faces ->
                    processFaces(faces)
                }.addOnFailureListener { e -> // Task failed with an exception
                    e.printStackTrace()
                }
    }

    private fun processFaces(faces: List<FirebaseVisionFace>){
        if (faces.size == 0){
            return
        }

        val face = faces[0]
        val center_x = (face.boundingBox.centerX().toInt())
        val center_y = (face.boundingBox.centerY().toInt())

        var leftEyePosX = 0
        var leftEyePosY = 0

        val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
        leftEye?.let{
            leftEyePosX = leftEye.position.x.toInt()
            leftEyePosY = leftEye.position.y.toInt()
        }
    }
*/

    fun bitmapBlur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap? {
        var sentBitmap = sentBitmap
        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)
        val bitmap = sentBitmap.copy(sentBitmap.config, true)

        if (radius < 1) { return null }

        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))

        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yi = 0
        yw = yi

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }

    companion object {
        private const val KEY_IMAGE_URI = "edu.uw.eep523.takepicture.KEY_IMAGE_URI"
        private const val KEY_IMAGE_URI1 = "edu.uw.eep523.takepicture.KEY_IMAGE_URI1"
        private const val REQUEST_IMAGE_CAPTURE_L = 1001
        private const val REQUEST_IMAGE_CAPTURE_R = 1004
        private const val REQUEST_CHOOSE_IMAGE_L = 1002
        private const val REQUEST_CHOOSE_IMAGE_R = 1003
        private const val PERMISSION_REQUESTS = 1
    }
}


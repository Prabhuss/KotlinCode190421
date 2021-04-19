package com.getpy.dikshasshop.ui.Products

import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.load
import com.getpy.dikshasshop.R
import com.github.chrisbanes.photoview.OnMatrixChangedListener
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.OnSingleFlingListener
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class ProductImageViewerActivity : AppCompatActivity() {


    var mPhotoView: PhotoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url=intent.extras?.getString("ImgURL")

        setContentView(R.layout.fullscreen_image_view)
        val mCloseButton = findViewById<FloatingActionButton>(R.id.floatingCloseButton)
        mCloseButton.setOnClickListener{
            super.onBackPressed()
        }
        mPhotoView = findViewById<PhotoView>(R.id.fullscreen_content)
        mPhotoView!!.load(url){
            placeholder(R.drawable.ic_no_image_found)
            error(R.drawable.ic_no_image_found)
        }

    }

}

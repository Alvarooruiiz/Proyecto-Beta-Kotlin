package com.example.proyectobetakotlin.Carousel

import android.annotation.SuppressLint
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.proyectobetakotlin.BBDD.UserProvider
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.databinding.CarouselStyleLayoutBinding

class CarouselActivity : AppCompatActivity() {

    private lateinit var binding: CarouselStyleLayoutBinding
    private lateinit var viewPager: ViewPager
    private lateinit var sliderDotspanel: LinearLayout
    private var dotscount: Int = 0
    private lateinit var dots: Array<ImageView?>
    private lateinit var imageBitmaps: ArrayList<Bitmap>
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CarouselStyleLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("userId", -1)
        imageBitmaps = obtenerListaDeBitmapsDeImagenes(userId)

        viewPager = binding.viewPager
        sliderDotspanel = binding.sliderDots

        val viewPagerAdapter = CarouselViewPagerAdapter(this, imageBitmaps)
        viewPager.adapter = viewPagerAdapter

        dotscount = viewPagerAdapter.count
        dots = arrayOfNulls(dotscount)

        for (i in 0 until dotscount) {
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.nonactive_dot))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)

            sliderDotspanel.addView(dots[i], params)
        }

        dots[0]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.active_dot))

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                for (i in 0 until dotscount) {
                    dots[i]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.nonactive_dot))
                }
                dots[position]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.active_dot))
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    @SuppressLint("Range")
    private fun obtenerListaDeBitmapsDeImagenes(userId: Int): ArrayList<Bitmap> {
        val bitmaps = ArrayList<Bitmap>()
        val cursor: Cursor? = contentResolver.query(
            UserProvider.CONTENT_URI_IMAGES,
            arrayOf(UserProvider.Images.COL_IMAGE_URL),
            UserProvider.Images.COL_USER_ID + " = ?",
            arrayOf(userId.toString()),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val imageData = cursor.getBlob(cursor.getColumnIndex(UserProvider.Images.COL_IMAGE_URL))
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                bitmaps.add(bitmap)
            } while (cursor.moveToNext())
            cursor.close()
        }

        return bitmaps
    }
}

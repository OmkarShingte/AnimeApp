package com.sportsintercative.contentapp.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.sportsintercative.contentapp.R
import com.sportsintercative.contentapp.models.ImageItem
import kotlinx.android.synthetic.main.item_image.view.*

class ImagePagerAdapter(private val images: List<ImageItem>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_image, container, false)

        view.imageView.setImageResource(images[position].imageResId)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return images.size
    }
}

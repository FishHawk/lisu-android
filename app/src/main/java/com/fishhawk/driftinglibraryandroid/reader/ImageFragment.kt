package com.fishhawk.driftinglibraryandroid.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.fishhawk.driftinglibraryandroid.R
import com.github.chrisbanes.photoview.PhotoView


class ImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoView: PhotoView = view.findViewById(R.id.content)
        photoView.setZoomable(true)
        Glide.with(context!!)
            .asBitmap()
            .load(arguments?.getString(IMAGE))
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .into(photoView)
    }

    companion object {
        private const val IMAGE = "image"

        fun newInstance(image: String) = ImageFragment().apply {
            arguments = Bundle(1).apply {
                putString(IMAGE, image)
            }
        }
    }
}
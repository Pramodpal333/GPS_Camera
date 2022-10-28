package com.ctech.mapkotlin

import android.graphics.Bitmap

class ImageModel(image: Bitmap, rotate: Int, index: Int) {
    private var image: Bitmap
    private var rotate: Int
    private var index:Int

    init {
        this.image = image
        this.rotate = rotate
        this.index = index
    }

    fun getImage(): Bitmap {
        return image
    }

    fun setImage(image: Bitmap) {
        this.image = image
    }

    fun getRotate(): Int {
        return rotate
    }

    fun setRotate(rotate: Int) {
        this.rotate = rotate
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
    }
}
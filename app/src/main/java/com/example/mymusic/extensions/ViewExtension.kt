package com.example.mymusic.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.widgets.RecyclerItemClickListener
import com.example.mymusic.widgets.RecyclerViewItemClickListener

fun RecyclerView.addOnItemClick(listener: RecyclerViewItemClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, listener, null))
}

@Suppress("UNCHECKED_CAST")
fun <T : View> ViewGroup.inflate(@LayoutRes layout: Int): T {
    return LayoutInflater.from(context).inflate(layout, this, false) as T
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.showOrHide(show: Boolean) = if (show) show() else hide()

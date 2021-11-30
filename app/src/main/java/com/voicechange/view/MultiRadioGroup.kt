package com.voicechange.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup


class MultiRadioGroup : RadioGroup {
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        mOnCheckedChangeListener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is LinearLayout) {
            val childCount = child.childCount
            for (i in 0 until childCount) {
                val view = child.getChildAt(i)
                if (view is RadioButton) {
                    view.setOnTouchListener { _, _ ->
                        view.isChecked = true
                        checkRadioButton(view)
                        if (mOnCheckedChangeListener != null) {
                            mOnCheckedChangeListener!!.onCheckedChanged(
                                this@MultiRadioGroup,
                                view.id
                            )
                        }
                        true
                    }
                }
            }
        }
        super.addView(child, index, params)
    }

    private fun checkRadioButton(radioButton: RadioButton) {
        var child: View
        val radioCount = childCount
        for (i in 0 until radioCount) {
            child = getChildAt(i)
            if (child is RadioButton) {
                if (child === radioButton) {
                    // do nothing
                } else {
                    child.isChecked = false
                }
            } else if (child is LinearLayout) {
                val childCount = child.childCount
                for (j in 0 until childCount) {
                    val view = child.getChildAt(j)
                    if (view is RadioButton) {
                        val button = view
                        if (button === radioButton) {
                            // do nothing
                        } else {
                            button.isChecked = false
                        }
                    }
                }
            }
        }
    }
}
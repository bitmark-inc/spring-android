/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.feature.whatsnew

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.bitmark.fbm.BuildConfig
import com.bitmark.fbm.R
import com.bitmark.fbm.feature.BaseAppCompatActivity
import com.bitmark.fbm.feature.BaseViewModel
import com.bitmark.fbm.feature.Navigator
import com.bitmark.fbm.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.fbm.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.fbm.util.DateTimeUtil
import com.bitmark.fbm.util.ext.*
import kotlinx.android.synthetic.main.activity_whats_new.*
import kotlinx.android.synthetic.main.layout_list_item.view.*
import java.util.*
import javax.inject.Inject


class WhatsNewActivity : BaseAppCompatActivity() {

    companion object {

        private const val RE_ENTER = "re_enter"

        fun getBundle(reEnter: Boolean): Bundle {
            val bundle = Bundle()
            bundle.putBoolean(RE_ENTER, reEnter)
            return bundle
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    private var reEnter = false

    override fun layoutRes(): Int = R.layout.activity_whats_new

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        reEnter = intent?.extras?.getBoolean(RE_ENTER) ?: error("missing RE_ENTER")

        if (reEnter) {
            layoutToolbar.visible()
            btnContinue.invisible()
        } else {
            layoutToolbar.invisible()
            btnContinue.visible()
        }

        tvVersion.text = getString(R.string.version_format).format(BuildConfig.VERSION_NAME)

        val newFeatures = resources?.getStringArray(R.array.release_note_features)
        if (newFeatures?.isNotEmpty() == true) {
            val tvFeature = getContentTv()
            tvFeature.text = getString(R.string.highlights_of_new_features)
            layout_content.addView(tvFeature)
            newFeatures.forEach { feature ->
                val layoutItem = getItemLayout(feature)
                layout_content.addView(layoutItem)
            }
        }

        val enhancements = resources?.getStringArray(R.array.release_note_enhancements)
        if (enhancements?.isNotEmpty() == true) {
            val tvEnhancement = getContentTv()
            tvEnhancement.text = getString(R.string.enhancements)
            layout_content.addView(tvEnhancement)
            enhancements.forEach { enhancement ->
                val layoutItem = getItemLayout(enhancement)
                layout_content.addView(layoutItem)
            }
        }

        val contactInfo = getString(R.string.we_value_your_feedback)
        val spannable = SpannableString(contactInfo)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigator.openIntercom()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

        }

        val linkLabel = getString(R.string.let_us_know)
        val startPos = contactInfo.indexOf(linkLabel)
        spannable.setSpan(
            clickableSpan,
            startPos,
            startPos + linkLabel.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            UnderlineSpan(), startPos,
            startPos + linkLabel.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )

        val tvContactInfo = getContentTv()
        tvContactInfo.text = spannable
        tvContactInfo.movementMethod = LinkMovementMethod.getInstance()
        tvContactInfo.setLinkTextColor(getColor(R.color.tundora))
        tvContactInfo.highlightColor = Color.TRANSPARENT

        layout_content.addView(tvContactInfo)

        tvDate.text = getString(R.string.day_ago_format).format(
            DateTimeUtil.dayCountFrom(
                DateTimeUtil.stringToDate(getString(R.string.release_note_date)) ?: Date()
            )
        )

        ivBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        btnContinue.setOnClickListener {
            navigator.anim(BOTTOM_UP).finishActivityForResult()
        }
    }

    private fun getContentTv(): AppCompatTextView {
        val tv = AppCompatTextView(this)
        tv.setTextColor(getColor(R.color.tundora))
        tv.setTextSize(22)
        tv.typeface = ResourcesCompat.getFont(this, R.font.grotesk_light_font_family)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, getDimensionPixelSize(R.dimen.dp_24), 0, 0)
        tv.layoutParams = params
        return tv
    }

    private fun getItemLayout(text: String): View {
        val layoutItem = layoutInflater.inflate(R.layout.layout_list_item, null)
        with(layoutItem) {
            tvContent.text = text
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, getDimensionPixelSize(R.dimen.dp_4), 0, 0)
        layoutItem.layoutParams = params
        return layoutItem
    }

    override fun onBackPressed() {
        if (reEnter) {
            navigator.anim(RIGHT_LEFT).finishActivity()
        } else {
            navigator.anim(BOTTOM_UP).finishActivityForResult()
        }
        super.onBackPressed()
    }
}
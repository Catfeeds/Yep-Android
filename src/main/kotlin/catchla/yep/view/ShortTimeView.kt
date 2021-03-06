/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package catchla.yep.view

import android.content.Context
import android.os.SystemClock
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.util.AttributeSet
import catchla.yep.R
import catchla.yep.util.Utils

class ShortTimeView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyle) {


    private val ticker: Runnable
    private val timeTemplate: String?
    private var timeTemplateValid = false

    var time: Long = 0
        set(value) {
            field = value
            invalidateTime()
        }

    init {
        ticker = TickerRunnable(this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShortTimeView)
        timeTemplate = a.getString(R.styleable.ShortTimeView_timeTemplate)
        a.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(ticker)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    private fun invalidateTime() {
        if (TextUtils.isEmpty(timeTemplate)) {
            text = Utils.formatSameDayTime(context, time)
        } else if (!timeTemplateValid) {
            text = Utils.formatSameDayTime(context, time)
        } else try {
            text = String.format(timeTemplate!!, Utils.formatSameDayTime(context, time))
        } catch (e: Exception) {
            timeTemplateValid = false
            text = Utils.formatSameDayTime(context, time)
        }
    }

    private class TickerRunnable constructor(val textView: ShortTimeView) : Runnable {

        private val TICKER_DURATION = 5000L

        override fun run() {
            val handler = textView.handler ?: return
            textView.invalidateTime()
            val now = SystemClock.uptimeMillis()
            val next = now + TICKER_DURATION - now % TICKER_DURATION
            handler.postAtTime(this, next)
        }
    }

}

package com.rainbow.priceperuse

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.*

/** Native selectable/scrollable text inside the same stepped panels as the canvas UI. */
object PixelInfoDialog {
    fun show(activity:Activity,palette:AppPalette,title:String,body:String,close:String) {
        val density=activity.resources.displayMetrics.density
        val scale=UiSizing.compactScale(activity.resources.displayMetrics.widthPixels/density)
        fun dp(value:Int)=(value*density).toInt()
        val ink=Color.parseColor("#28103F")
        val dialog=Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val panel=LinearLayout(activity).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(17),dp(17),dp(17),dp(17))
            background=PixelPanel(density,Color.parseColor("#FFF0CF"),Color.parseColor("#FFD28F"))
        }
        val heading=TextView(activity).apply {
            text=title;textSize=26f*scale;typeface=Typeface.create("serif",Typeface.BOLD)
            setTextColor(ink);gravity=Gravity.CENTER;setPadding(dp(3),dp(2),dp(3),dp(12))
            if(android.os.Build.VERSION.SDK_INT>=28)isAccessibilityHeading=true
        }
        panel.addView(heading,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT))
        val cleanBody=if(body.lineSequence().first().trim().equals(title,true))body.substringAfter('\n').trimStart()else body
        val formatted=SpannableString(cleanBody)
        var offset=0
        val lines=cleanBody.lines()
        lines.forEachIndexed {i,line ->
            if(line.isNotBlank() && line.length<70 && (i==0 || lines[i-1].isBlank()) && i<lines.lastIndex && lines[i+1].isNotBlank()) {
                formatted.setSpan(StyleSpan(Typeface.BOLD),offset,offset+line.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            offset+=line.length+1
        }
        Linkify.addLinks(formatted,Linkify.EMAIL_ADDRESSES)
        Regex("""https://\S+""").findAll(cleanBody).forEach { match ->
            formatted.setSpan(URLSpan(match.value),match.range.first,match.range.last+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val text=TextView(activity).apply {
            this.text=formatted;textSize=16f*scale;setTextColor(ink)
            setPadding(dp(4),dp(5),dp(8),dp(15));setLineSpacing(dp(4).toFloat(),1f)
            setTextIsSelectable(true)
            movementMethod=LinkMovementMethod.getInstance()
            setLinkTextColor(ink)
        }
        val scroll=LimitedScrollView(activity,kotlin.math.min((activity.resources.displayMetrics.heightPixels*.56f).toInt(),activity.resources.displayMetrics.heightPixels-dp(210)).coerceAtLeast(dp(64))).apply {
            isFillViewport=false;isVerticalScrollBarEnabled=true
            addView(text)
        }
        panel.addView(scroll,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT))
        val button=Button(activity).apply {
            this.text=close;textSize=21f*scale;typeface=Typeface.DEFAULT_BOLD;isAllCaps=false;setTextColor(ink)
            minimumHeight=0;minHeight=0;setPadding(dp(8),0,dp(8),dp(3))
            stateListAnimator=null
            background=StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed),PixelPanel(density,palette.selected,palette.accent))
                addState(intArrayOf(),PixelPanel(density,palette.accent,Color.rgb((Color.red(palette.accent)*.75).toInt(),(Color.green(palette.accent)*.75).toInt(),(Color.blue(palette.accent)*.75).toInt())))
            }
            setOnClickListener {dialog.dismiss()}
        }
        panel.addView(button,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(53)).apply {topMargin=dp(11)})
        dialog.setContentView(panel)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(.58f)
        }
        dialog.show()
        dialog.window?.setLayout((activity.resources.displayMetrics.widthPixels-dp(28)).coerceAtMost(dp(450)),ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private class LimitedScrollView(context:Context,val limit:Int):ScrollView(context) {
        override fun onMeasure(widthMeasureSpec:Int,heightMeasureSpec:Int) {
            val available=if(MeasureSpec.getMode(heightMeasureSpec)==MeasureSpec.UNSPECIFIED)limit else kotlin.math.min(limit,MeasureSpec.getSize(heightMeasureSpec))
            super.onMeasure(widthMeasureSpec,MeasureSpec.makeMeasureSpec(available,MeasureSpec.AT_MOST))
        }
    }
    private class PixelPanel(val density:Float,val color:Int,val shadow:Int):Drawable() {
        val paint=Paint().apply {isAntiAlias=false}
        private fun shape(l:Float,t:Float,r:Float,b:Float):Path {
            val unit=3*density;val step=kotlin.math.min(unit,(b-t)/8)
            return Path().apply {
                moveTo(l+step*3,t);lineTo(r-step*3,t);lineTo(r-step*3,t+step);lineTo(r-step,t+step);lineTo(r-step,t+step*3);lineTo(r,t+step*3)
                lineTo(r,b-step*3);lineTo(r-step,b-step*3);lineTo(r-step,b-step);lineTo(r-step*3,b-step);lineTo(r-step*3,b)
                lineTo(l+step*3,b);lineTo(l+step*3,b-step);lineTo(l+step,b-step);lineTo(l+step,b-step*3);lineTo(l,b-step*3)
                lineTo(l,t+step*3);lineTo(l+step,t+step*3);lineTo(l+step,t+step);lineTo(l+step*3,t+step);close()
            }
        }
        override fun draw(canvas:Canvas) {
            val l=bounds.left.toFloat();val t=bounds.top.toFloat();val r=bounds.right.toFloat();val b=bounds.bottom.toFloat()
            paint.color=Color.parseColor("#28103F");canvas.drawPath(shape(l,t,r,b),paint)
            val inset=2*density
            val inside=shape(l+inset,t+inset,r-inset,b-4*density)
            paint.color=color;canvas.drawPath(inside,paint)
            canvas.save();canvas.clipPath(inside)
            paint.color=Color.argb(170,255,255,255);canvas.drawRect(l,t,r,t+5*density,paint)
            paint.color=shadow;canvas.drawRect(l,b-9*density,r,b,paint)
            canvas.restore()
        }
        override fun setAlpha(alpha:Int) {paint.alpha=alpha}
        override fun setColorFilter(filter:ColorFilter?) {paint.colorFilter=filter}
        @Deprecated("Deprecated in Java") override fun getOpacity()=PixelFormat.TRANSLUCENT
    }
}

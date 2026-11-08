package com.rainbow.priceperuse

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/** Small code-native sprite; the six-band exhaust and paws animate independently. */
object RainbowCat {
    private val paint=Paint().apply {isAntiAlias=false}
    private val colors=intArrayOf(0xFFF75B9E.toInt(),0xFFFFAA85.toInt(),0xFFFFDE68.toInt(),0xFF63E6AF.toInt(),0xFF6BCDEB.toInt(),0xFFAC92EC.toInt())
    fun draw(canvas:Canvas,y:Float,elapsed:Long) {
        val phase=elapsed/1000.0
        val x=209f+(kotlin.math.sin(phase*.85)*49).toFloat()
        val bob=(kotlin.math.sin(phase*2.7)*3).toFloat()
        val frame=(elapsed/140%4).toInt()
        fun rect(a:Float,b:Float,w:Float,h:Float,col:Int) {paint.color=col;canvas.drawRect(a,b,a+w,b+h,paint)}
        val ink=Color.parseColor("#28103F")
        // Jet exhaust trails behind the cat, with a travelling square wave.
        for(segment in 0..17) {
            val wave=if((segment+frame)%6<3)0f else 2f
            val sx=x-107+segment*6
            colors.forEachIndexed {row,color -> rect(sx,y+6+row*3+wave,7f,3f,color)}
        }
        for(i in 0..3) {
            val sx=x-112-i*12+frame*3
            if(sx>16){rect(sx,y+5+(i%3)*8f,3f,3f,colors[(i+frame)%6])}
        }
        canvas.save();canvas.translate(x,y+bob)
        fun r(a:Float,b:Float,w:Float,h:Float,color:String)=rect(a,b,w,h,Color.parseColor(color))
        fun block(a:Float,b:Float,w:Float,h:Float,color:String) {rect(a-1,b-1,w+2,h+2,ink);r(a,b,w,h,color)}
        val paw=if(frame%2==0)0f else 3f
        block(-6f,9f,10f,5f,"#E8D9FA");block(-10f,4f-paw,5f,8f,"#E8D9FA")
        block(2f,5f,27f,18f,"#E8D9FA");r(5f,8f,16f,11f,"#FFF4DB")
        block(5f,21f+paw,8f,4f,"#F9BAD2");block(23f,21f-paw,8f,4f,"#F9BAD2")
        block(22f,2f,20f,19f,"#FFF4DB")
        block(23f,-3f,6f,7f,"#FFF4DB");block(35f,-3f,6f,7f,"#FFF4DB")
        r(25f,-1f,2f,4f,"#F6A3C2");r(37f,-1f,2f,4f,"#F6A3C2")
        r(28f,8f,3f,4f,"#28103F");r(37f,8f,3f,4f,"#28103F")
        r(30f,14f,2f,2f,"#28103F");r(32f,16f,4f,2f,"#28103F");r(36f,14f,2f,2f,"#28103F")
        r(24f,13f,4f,3f,"#FFB4CE");r(39f,13f,4f,3f,"#FFB4CE")
        r(30f,5f,2f,2f,"#FFFFFF")
        canvas.restore()
    }
}

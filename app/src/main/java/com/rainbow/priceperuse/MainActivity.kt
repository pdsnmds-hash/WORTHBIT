package com.rainbow.priceperuse

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var store: ItemStore
    private lateinit var world: RainbowView
    private lateinit var root: android.widget.FrameLayout
    private var nameField: EditText? = null
    private var priceField: EditText? = null
    private var items = mutableListOf<Item>()
    private var page = "home"
    private var onboardingStep = 0
    private var onboardingReturn = "home"
    private var splashDeadline = 0L
    private val finishSplash = Runnable {if(page=="splash" && !isFinishing)afterSplash()}
    private fun afterSplash() {world.removeCallbacks(finishSplash);if(store.onboardingSeen)navigate("home")else openOnboarding(false)}
    private fun openOnboarding(replay:Boolean) {onboardingStep=0;onboardingReturn=if(replay)"settings"else"home";navigate("onboarding")}
    private fun finishOnboarding() {store.onboardingSeen=true;navigate(onboardingReturn)}

    private var selected: Item? = null
    private var selectedIcon = 2
    private var menu = false
    private var deleting = false
    private var undo: Pair<String, Int>? = null
    private var scroll = 0f
    private var language = "en"
    private var themeId = 0
    private val palette get() = AppPalette.all[themeId]
    private var settingsReturn = "home"
    private var detailReturn = "home"
    private var chartMode = 0
    private var chartSelection: String? = null
    private fun openItem(item: Item, from: String = "home") { selected=item;undo=null;detailReturn=from;navigate("detail") }
    private fun openStatistics() { chartSelection=null;chartMode=0;navigate("statistics");world.animateChart() }
    private val appVersion: String get() = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
    private fun nativeTextSize(base:Float)=base*UiSizing.compactScale(resources.displayMetrics.widthPixels/resources.displayMetrics.density)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun applyPalette() {
        root.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(palette.top,palette.bottom))
        window.decorView.setBackgroundColor(palette.top)
        @Suppress("DEPRECATION")
        window.statusBarColor = if(android.os.Build.VERSION.SDK_INT>=23)palette.top else palette.edge
        @Suppress("DEPRECATION")
        window.navigationBarColor = if(android.os.Build.VERSION.SDK_INT>=26)palette.bottom else Color.parseColor("#28103F")
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        if(android.os.Build.VERSION.SDK_INT in 23..29) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                (if(android.os.Build.VERSION.SDK_INT>=26)View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0)
        }
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
        }
        world.invalidate()
    }
    private fun openSettings() { settingsReturn=page; navigate("settings") }
    private fun showInformation(title:String,body:String) {
        PixelInfoDialog.show(this,palette,title,body,t("Close","Chiudi"))
    }
    private fun chooseIcon() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root.windowToken,0)
        val grid=GridView(this).apply { numColumns=3;horizontalSpacing=dp(8);verticalSpacing=dp(8);setPadding(dp(12),dp(12),dp(12),dp(12));clipToPadding=false }
        val dialog=AlertDialog.Builder(this).setTitle(t("Choose an icon · 30","Scegli un’icona · 30"))
            .setView(grid).setNegativeButton(t("Cancel","Annulla"),null).create()
        grid.adapter=object:BaseAdapter() {
            override fun getCount()=ItemIcons.count
            override fun getItem(position:Int)=position
            override fun getItemId(position:Int)=position.toLong()
            override fun getView(position:Int,convertView:View?,parent:ViewGroup?):View {
                val cell=LinearLayout(this@MainActivity).apply {
                    orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(3),dp(8),dp(3),dp(5))
                    layoutParams=AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(133))
                    background=GradientDrawable().apply {setColor(if(position==selectedIcon)palette.selected else Color.parseColor("#FFF0CF"));setStroke(dp(2),Color.parseColor("#28103F"));cornerRadius=dp(5).toFloat()}
                    contentDescription=ItemIcons.name(position,language=="it")+if(position==selectedIcon)t(", selected",", selezionata")else ""
                }
                cell.addView(object:View(this@MainActivity) {
                    val bitmap=Bitmap.createBitmap(76,80,Bitmap.Config.ARGB_8888).also {ItemIcons.draw(Canvas(it),position,0f,0f,1f)}
                    val paint=Paint().apply {isFilterBitmap=false}
                    override fun onDraw(canvas:Canvas) {val side=kotlin.math.min(width,height);val left=(width-side)/2;canvas.drawBitmap(bitmap,null,Rect(left,0,left+side,side),paint)}
                },LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(83)))
                cell.addView(TextView(this@MainActivity).apply {text=(if(position==selectedIcon)"✓ "else "")+ItemIcons.name(position,language=="it");textSize=nativeTextSize(12f);gravity=Gravity.CENTER;maxLines=2;setTextColor(Color.parseColor("#28103F"));typeface=Typeface.DEFAULT_BOLD},LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(34)))
                return cell
            }
        }
        grid.setOnItemClickListener { _,_,position,_ -> selectedIcon=position;world.invalidate();dialog.dismiss() }
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(palette.middle))
            grid.layoutParams=grid.layoutParams.apply {height=(resources.displayMetrics.heightPixels*.65f).toInt()}
            grid.setSelection(selectedIcon)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#28103F"))
        }
        dialog.show()
    }
    private fun t(en: String, it: String) = if (language == "it") it else en
    private fun money(value: java.math.BigDecimal): String = NumberFormat.getCurrencyInstance(if(language == "it") Locale.ITALY else Locale.UK).apply { currency = java.util.Currency.getInstance("EUR"); minimumFractionDigits = if(value.stripTrailingZeros().scale() <= 0) 0 else 2 }.format(value)
    private fun price(item: Item) = money(java.math.BigDecimal.valueOf(item.cents, 2))
    override fun onCreate(state: Bundle?) {
        setTheme(resources.getIdentifier("AppTheme","style",packageName))
        super.onCreate(state)
        if(android.os.Build.VERSION.SDK_INT>=33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT) {handleBack()}
        }
        store = ItemStore(this); items = store.load();themeId=store.theme
        language = store.language.ifEmpty { if(Locale.getDefault().language == "it") "it" else "en" }
        root = FrameLayout(this); world = RainbowView(); root.addView(world); setContentView(root);applyPalette()
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
            }
            view.post { positionFields() }
            insets
        }
        root.requestApplyInsets()
        if(state==null) {page="splash";splashDeadline=0L}
        if(state != null) {
            onboardingStep=state.getInt("onboardingStep",0).coerceIn(0,2);onboardingReturn=state.getString("onboardingReturn","home")
            splashDeadline=if(state.getBoolean("splashStarted",false))android.os.SystemClock.uptimeMillis()+state.getLong("splashRemaining",0L)else 0L
            selected = items.find { it.id == state.getString("selected") }
            page = state.getString("page", "home");settingsReturn=state.getString("settingsReturn","home")
            detailReturn=state.getString("detailReturn","home");chartMode=state.getInt("chartMode",0).coerceIn(0,1);chartSelection=state.getString("chartSelection")
            if(page == "detail" && selected == null) page = "home"
            if(page == "form") root.post { form(selected, state.getString("draftName", ""), state.getString("draftPrice", ""), state.getInt("draftIcon",2)) }
            val undoId=state.getString("undoId"); if(undoId != null) undo = undoId to state.getInt("undoCount")
        }
    }
    override fun onResume() {
        super.onResume()
        if(page=="splash" && splashDeadline>0L)world.postDelayed(finishSplash,(splashDeadline-android.os.SystemClock.uptimeMillis()).coerceAtLeast(0L))
    }
    override fun onPause() {if(::world.isInitialized)world.removeCallbacks(finishSplash);super.onPause()}
    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putBoolean("splashStarted",splashDeadline>0L);out.putInt("onboardingStep",onboardingStep);out.putString("onboardingReturn",onboardingReturn);out.putLong("splashRemaining",(splashDeadline-android.os.SystemClock.uptimeMillis()).coerceAtLeast(0L))
        out.putString("detailReturn",detailReturn);out.putInt("chartMode",chartMode);out.putString("chartSelection",chartSelection);out.putString("settingsReturn",settingsReturn);out.putString("page",page); out.putString("selected",selected?.id)
        out.putString("draftName", nameField?.text?.toString()); out.putString("draftPrice",priceField?.text?.toString());out.putInt("draftIcon",selectedIcon)
        undo?.let { out.putString("undoId",it.first);out.putInt("undoCount",it.second) }
    }
    private fun persist() { store.save(items); world.invalidate() }
    private fun clearFields() { nameField?.let(root::removeView);priceField?.let(root::removeView);nameField=null;priceField=null }
    private fun navigate(destination: String) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root.windowToken,0)
        clearFields();page=destination;menu=false;deleting=false;scroll=scroll.coerceIn(0f,kotlin.math.max(0f,items.size*122f-445f));world.invalidate()
    }
    // API 33+ uses the registered platform callback; this override serves API 24–32.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Deprecated("Activity back handling") override fun onBackPressed() {handleBack()}
    private fun handleBack() {
        when {page=="splash" -> afterSplash();page=="onboarding" && onboardingStep>0 -> {onboardingStep--;world.invalidate()};page=="onboarding" && onboardingReturn=="settings" -> navigate("settings"); deleting -> {deleting=false;world.invalidate()}; menu -> {menu=false;world.invalidate()}; page=="form" -> leaveForm(); page=="detail" -> navigate(detailReturn);page=="statistics" -> navigate("home");page=="inventory" -> navigate("home");page=="settings" -> navigate(settingsReturn); else -> finish() }
    }
    private fun leaveForm() {
        val changed = nameField?.text.toString() != (selected?.name ?: "") || priceField?.text.toString() != (selected?.let { java.math.BigDecimal.valueOf(it.cents,2).stripTrailingZeros().toPlainString() } ?: "") || selectedIcon != (selected?.icon ?: 2)
        if(changed) AlertDialog.Builder(this).setTitle(t("Discard changes?","Scartare le modifiche?"))
            .setNegativeButton(t("Keep editing","Continua"),null).setPositiveButton(t("Discard","Scarta")) { _,_ -> navigate(if(selected==null) "home" else "detail") }.show()
        else navigate(if(selected==null) "home" else "detail")
    }
    private fun form(item: Item?, draftName: String? = null, draftPrice: String? = null, draftIcon: Int? = null) {
        selected=item;navigate("form");selectedIcon=draftIcon ?: item?.icon ?: 2
        fun field(value: String, numeric: Boolean): EditText = EditText(this).apply {
            setSingleLine(true);setText(value);textSize=23f;setTextColor(Color.rgb(41,18,65));typeface=Typeface.create("sans-serif",Typeface.BOLD)
            setPadding(12,0,12,0); background=GradientDrawable().apply { setColor(Color.parseColor("#FFF9EC"));cornerRadius=9f;setStroke(2,Color.parseColor("#80588A")) }
            inputType=if(numeric) InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            filters=arrayOf(android.text.InputFilter.LengthFilter(if(numeric)12 else 48))
            hint=if(numeric) "0.00" else t("e.g. Coffee machine","Es. Macchina del caffè")
            contentDescription=if(numeric)t("Purchase price in euros","Prezzo di acquisto in euro") else t("Item name","Nome della cosa")
        }
        nameField=field(draftName ?: item?.name ?: "",false)
        priceField=field(draftPrice ?: item?.let { java.math.BigDecimal.valueOf(it.cents,2).stripTrailingZeros().toPlainString() } ?: "",true)
        root.addView(nameField);root.addView(priceField);positionFields();world.invalidate()
    }
    private fun positionFields() {
        if(page!="form" || root.width==0) return
        val sx=world.width/360f;val sy=world.height/760f
        listOf(nameField to 190,priceField to 290).forEach { (field,y) ->
            field?.layoutParams=FrameLayout.LayoutParams((308*sx).toInt(),(45*sy).toInt()).apply {leftMargin=(26*sx).toInt();topMargin=(y*sy).toInt()}
            field?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,23f*kotlin.math.min(sx,sy))
            field?.setPadding((10*sx).toInt(),0,(10*sx).toInt(),0)
        }
    }
    private fun saveForm() {
        val name=nameField?.text.toString().trim();val cents=PriceInput.cents(priceField?.text.toString())
        if(name.isBlank()) {nameField?.error=t("Enter a name","Inserisci un nome");nameField?.requestFocus();return}
        if(cents==null) {priceField?.error=t("Enter a price greater than zero (up to 2 decimals)","Inserisci un prezzo positivo (massimo 2 decimali)");priceField?.requestFocus();return}
        val existing=selected
        if(existing==null) {detailReturn="home";selected=Item(UUID.randomUUID().toString(),name,cents,selectedIcon);items.add(selected!!)}
        else {existing.name=name;existing.cents=cents;existing.icon=selectedIcon}
        undo=null;persist();navigate("detail")
    }
    inner class RainbowView : View(this@MainActivity) {
        private val p=Paint(Paint.ANTI_ALIAS_FLAG)
        private val ink=Color.parseColor("#28103F")
        private val cream=Color.parseColor("#FFF0C9")
        private var c=Canvas()
        private val artwork=Bitmap.createBitmap(360,760,Bitmap.Config.ARGB_8888)
        private val pixelPaint=Paint().apply { isFilterBitmap=false;isAntiAlias=false }
        private val hits=mutableListOf<Pair<RectF,()->Unit>>()
        private var downY=0f
        private val catStart=android.os.SystemClock.uptimeMillis()
        override fun onWindowFocusChanged(hasWindowFocus:Boolean) {super.onWindowFocusChanged(hasWindowFocus);if(hasWindowFocus && (page=="home" || page=="splash"))invalidate()}
        private fun flyingCat(y:Float) {
            val animated=if(android.os.Build.VERSION.SDK_INT>=26)android.animation.ValueAnimator.areAnimatorsEnabled()
                else android.provider.Settings.Global.getFloat(contentResolver,android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0f
            RainbowCat.draw(c,y,if(animated)android.os.SystemClock.uptimeMillis()-catStart else 0L)
            if(animated && hasWindowFocus() && isShown)postInvalidateDelayed(80L)
        }
        private var splashFramePending=false
        private var chartProgress=1f
        private var chartAnimator: android.animation.ValueAnimator? = null
        fun animateChart() {
            chartAnimator?.cancel()
            chartAnimator=android.animation.ValueAnimator.ofFloat(0f,1f).apply {
                duration=550;interpolator=android.view.animation.DecelerateInterpolator()
                addUpdateListener {chartProgress=it.animatedValue as Float;invalidate()}
                start()
            }
        }
        override fun onDetachedFromWindow() {chartAnimator?.cancel();super.onDetachedFromWindow()}
        private val bold=Typeface.create("sans-serif-black",Typeface.BOLD)
        private val title=Typeface.create("serif",Typeface.BOLD)
        init { isFocusable=true;contentDescription=t("WorthBit inventory","Inventario WorthBit") }
        override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int) { super.onSizeChanged(w,h,oldw,oldh);post {positionFields()} }
        private fun fill(color:Int) {p.color=color;p.style=Paint.Style.FILL;p.shader=null;p.strokeWidth=1f}
        private fun rect(x:Float,y:Float,w:Float,h:Float,color:Int) {fill(color);c.drawRect(x,y,x+w,y+h,p)}
        private fun pathBox(x:Float,y:Float,w:Float,h:Float):Path = Path().apply {
            moveTo(x+12,y);lineTo(x+w-12,y);lineTo(x+w-12,y+3);lineTo(x+w-5,y+3);lineTo(x+w-5,y+8);lineTo(x+w,y+8);lineTo(x+w,y+h-10);lineTo(x+w-4,y+h-10);lineTo(x+w-4,y+h-4);lineTo(x+w-12,y+h-4);lineTo(x+w-12,y+h);lineTo(x+12,y+h);lineTo(x+12,y+h-3);lineTo(x+5,y+h-3);lineTo(x+5,y+h-8);lineTo(x,y+h-8);lineTo(x,y+10);lineTo(x+4,y+10);lineTo(x+4,y+4);lineTo(x+12,y+4);close()
        }
        private fun panel(x:Float,y:Float,w:Float,h:Float,color:Int=cream) {
            fill(ink);c.drawPath(pathBox(x-3,y-3,w+6,h+8),p)
            val shadow=if(color==cream)Color.parseColor("#FFD28F")else Color.rgb((Color.red(color)*.73f).toInt(),(Color.green(color)*.73f).toInt(),(Color.blue(color)*.73f).toInt())
            fill(color);p.shader=LinearGradient(0f,y,0f,y+h,intArrayOf(Color.WHITE,color,shadow),floatArrayOf(0f,.16f,1f),Shader.TileMode.CLAMP)
            c.drawPath(pathBox(x,y,w,h),p);p.shader=null
            val light=if(color==cream)Color.parseColor("#FFF0CF")else Color.rgb((Color.red(color)*.85f+255*.15f).toInt(),(Color.green(color)*.85f+255*.15f).toInt(),(Color.blue(color)*.85f+255*.15f).toInt())
            fill(light);c.drawPath(pathBox(x+3,y+4,w-6,h-11),p)
        }
        private fun text(s:String,x:Float,y:Float,size:Float,color:Int=ink,center:Boolean=false,outline:Boolean=false,max:Float=330f,serif:Boolean=false) {
            p.shader=null;p.typeface=if(serif)title else bold;p.textSize=size
            if(p.measureText(s)>max)p.textSize=size*max/p.measureText(s)
            p.textAlign=if(center)Paint.Align.CENTER else Paint.Align.LEFT
            if(outline){p.style=Paint.Style.STROKE;p.strokeJoin=Paint.Join.ROUND;p.strokeWidth=7f;p.color=ink;c.drawText(s,x+1,y+3,p);c.drawText(s,x,y,p)}
            p.style=Paint.Style.FILL;p.color=color;c.drawText(s,x,y,p)
        }
        private fun hit(x:Float,y:Float,w:Float,h:Float,action:()->Unit) {hits.add(RectF(x,y,x+w,y+h) to action)}
        private fun button(label:String,x:Float,y:Float,w:Float,h:Float=57f,red:Boolean=false,action:()->Unit) {
            panel(x,y,w,h,if(red)Color.parseColor("#FF6A8E")else palette.accent);text(label,x+w/2,y+h/2+10,28f,if(red)Color.WHITE else ink,true,red,max=w-18);hit(x,y,w,h,action)
        }
        private fun star(x:Float,y:Float,r:Float=10f,color:Int=Color.parseColor("#FFE75D")) {
            val path=Path(); for(i in 0..7){val a=Math.PI*i/4-Math.PI/2;val rad=if(i%2==0)r else r*.32f;val px=x+(Math.cos(a)*rad).toFloat();val py=y+(Math.sin(a)*rad).toFloat();if(i==0)path.moveTo(px,py) else path.lineTo(px,py)};path.close()
            fill(Color.parseColor("#EE9C00"));p.style=Paint.Style.STROKE;p.strokeWidth=3f;c.drawPath(path,p);fill(color);c.drawPath(path,p);rect(x-1,y-r*.55f,2f,r*.5f,Color.WHITE)
        }
        private fun cloud(x:Float,y:Float,s:Float) {
            val points=arrayOf(floatArrayOf(0f,18f),floatArrayOf(8f,18f),floatArrayOf(8f,10f),floatArrayOf(16f,10f),floatArrayOf(16f,3f),floatArrayOf(30f,3f),floatArrayOf(30f,8f),floatArrayOf(38f,8f),floatArrayOf(38f,15f),floatArrayOf(46f,15f),floatArrayOf(46f,24f),floatArrayOf(0f,24f))
            c.save();c.translate(x,y);c.scale(s,s);val path=Path();points.forEachIndexed { i,v->if(i==0)path.moveTo(v[0],v[1])else path.lineTo(v[0],v[1]) };path.close();fill(Color.parseColor("#9F69ED"));p.style=Paint.Style.STROKE;p.strokeWidth=5f;c.drawPath(path,p);fill(Color.WHITE);c.drawPath(path,p);rect(4f,19f,38f,4f,Color.parseColor("#D7BAFF"));c.restore()
        }
        private fun rainbow(cx:Float,bottom:Float,r:Float) {
            val colors=intArrayOf(0xFFF45B9F.toInt(),0xFFFFAD8D.toInt(),0xFFFFE76C.toInt(),0xFF4EEBC2.toInt(),0xFF68BBF8.toInt(),0xFFA48AF0.toInt())
            // Square cells retain the stepped edges of the reference artwork.
            val step=3f
            var x=-r
            while(x<=r){for(i in colors.indices){val ro=r-i*7;val ri=ro-7;if(kotlin.math.abs(x)<=ro){val outer=kotlin.math.sqrt(ro*ro-x*x);val inner=if(kotlin.math.abs(x)<ri)kotlin.math.sqrt(ri*ri-x*x)else 0f;rect(cx+x,bottom-(outer/step).toInt()*step,step,kotlin.math.max(step,outer-inner+step),colors[i])}};x+=step}
        }
        private fun scenery(y:Float) {
            rainbow(180f,y+35,67f);cloud(55f,y+1,.95f);cloud(254f,y+3,1f)
            for(i in 0..17){val x=i*22f;val h=if(i<5 || i>13) (10+(i*13)%22).toFloat() else 7f;rect(x,y+39-h,24f,h,Color.parseColor("#268E42"));rect(x+4,y+35-h,16f,h,Color.parseColor("#80E742"));rect(x+8,y+39-h,7f,8f,Color.parseColor("#B6F356"))}
        }
        private fun header(label:String,back:Boolean) {
            val compact=page=="statistics"
            rainbow(187f,if(compact)103f else 127f,if(compact)90f else 116f);cloud(7f,if(compact)63f else 84f,.72f);cloud(304f,if(compact)57f else 75f,.94f);star(104f,25f,9f);star(291f,33f,6f)
            if(back){panel(16f,15f,40f,36f);text("‹",36f,45f,40f,center=true);hit(10f,8f,56f,49f){if(page=="form")leaveForm()else if(page=="settings")navigate(settingsReturn)else if(page=="detail")navigate(detailReturn)else navigate("home")}}
            if(page=="home" || page=="inventory" || page=="detail" || page=="statistics"){panel(309f,16f,31f,36f);text("⋮",325f,44f,29f,center=true);hit(301f,9f,49f,50f){if(page!="detail")openSettings()else {menu=!menu;invalidate()}}}
            text(label,180f,if(compact)85f else 106f,if(compact)32f else 43f,Color.parseColor("#FFFCEB"),true,true,max=330f,serif=true)
        }
        private fun face(x:Float,y:Float) {rect(x,y,3f,4f,ink);rect(x+19,y,3f,4f,ink);rect(x+8,y+4,6f,2f,ink);rect(x+6,y+2,2f,2f,ink);rect(x+14,y+2,2f,2f,ink);rect(x-4,y+5,5f,3f,Color.parseColor("#FF9FA8"));rect(x+23,y+5,5f,3f,Color.parseColor("#FF9FA8"))}
        private fun icon(type:Int,x:Float,y:Float,s:Float) { ItemIcons.draw(c,type,x,y,s) }
        private fun chest(x:Float,y:Float) {
            c.save();c.translate(x,y);c.scale(1.3f,1.3f)
            panel(0f,18f,83f,60f,Color.parseColor("#FA7B8E"));rect(0f,36f,83f,6f,ink);rect(7f,21f,9f,50f,Color.parseColor("#FFC800"));rect(67f,21f,9f,50f,Color.parseColor("#FFC800"));panel(31f,36f,23f,21f,Color.parseColor("#FFE344"));rect(41f,42f,5f,10f,ink);star(-17f,35f,10f);star(101f,35f,10f);c.restore()
        }
        override fun onDraw(canvas:Canvas) {
            super.onDraw(canvas);c=Canvas(artwork);hits.clear();c.save()
            fill(palette.top);p.shader=LinearGradient(0f,0f,360f,760f,intArrayOf(palette.top,palette.middle,palette.bottom),null,Shader.TileMode.CLAMP);c.drawRect(0f,0f,360f,760f,p);p.shader=null
            rect(1f,0f,4f,760f,palette.edge);rect(355f,0f,4f,760f,palette.edge)
            if(page!="statistics")contentDescription=when(page){"splash"->"WorthBit";"onboarding"->t("Welcome to WorthBit, step ${onboardingStep+1} of 3","Benvenuto in WorthBit, passo ${onboardingStep+1} di 3");"home"->t("WorthBit inventory","Inventario WorthBit");"inventory"->t("All things","Tutte le cose");"settings"->t("Settings","Impostazioni");"form"->t("Item form","Modulo cosa");else->selected?.name ?: ""}
            when(page){"splash"->splash();"onboarding"->onboarding();"home"->home();"inventory"->inventory();"form"->drawForm();"detail"->detail();"settings"->settings();"statistics"->statistics()}
            if(menu)drawMenu()
            if(deleting)deleteDialog()
            c.restore()
            canvas.drawBitmap(artwork,null,Rect(0,0,width,height),pixelPaint)
        }
        private fun splash() {
            // Count the brief interval from the committed frame, not slow startup work.
            if(splashDeadline==0L && !splashFramePending && hasWindowFocus()) {
                splashFramePending=true
                val visible=Runnable {
                    splashFramePending=false
                    if(page=="splash" && !isFinishing && !isDestroyed && hasWindowFocus()) {
                        splashDeadline=android.os.SystemClock.uptimeMillis()+950L
                        postDelayed(finishSplash,950L)
                    }
                }
                if(android.os.Build.VERSION.SDK_INT>=29 && isHardwareAccelerated)
                    viewTreeObserver.registerFrameCommitCallback {post(visible)}
                else post(visible)
                postInvalidateOnAnimation()
            }
            rainbow(180f,350f,135f);cloud(17f,285f,.85f);cloud(301f,294f,.8f)
            star(58f,203f,10f);star(296f,220f,8f)
            text("WorthBit",180f,329f,58f,Color.parseColor("#FFFCEB"),true,true,max=320f,serif=true)
            text(t("Every use counts","Ogni utilizzo conta"),180f,390f,25f,center=true,max=310f)
            flyingCat(450f)
            text(t("Tap to continue","Tocca per continuare"),180f,597f,16f,Color.parseColor("#896185"),true,max=310f)
            scenery(686f)
            hit(0f,0f,360f,760f){afterSplash()}
        }
        private fun onboarding() {
            scenery(705f)
            header("WorthBit",false)
            panel(17f,16f,81f,34f)
            text("EN",38f,40f,18f,if(language=="en")ink else Color.parseColor("#896185"),true)
            text("IT",77f,40f,18f,if(language=="it")ink else Color.parseColor("#896185"),true)
            if(language=="en")rect(26f,44f,24f,2f,palette.edge)else rect(65f,44f,24f,2f,palette.edge)
            hit(13f,10f,44f,47f){language="en";store.language=language;invalidate()}
            hit(57f,10f,45f,47f){language="it";store.language=language;invalidate()}
            panel(267f,16f,76f,34f)
            text(if(onboardingReturn=="settings")t("Close","Chiudi")else t("Skip","Salta"),305f,40f,18f,center=true,max=64f)
            hit(261f,10f,86f,47f){finishOnboarding()}
            panel(22f,151f,316f,317f)
            panel(128f,167f,104f,26f,palette.selected)
            text("${onboardingStep+1} / 3",180f,187f,18f,center=true)
            when(onboardingStep) {
                0 -> {
                    icon(0,45f,249f,.95f);icon(1,123f,218f,1.55f);icon(2,242f,270f,.93f)
                    star(79f,227f,9f);star(286f,229f,8f)
                    panel(109f,399f,142f,40f,palette.selected)
                    text(money(java.math.BigDecimal("120")),180f,428f,27f,center=true,max=126f)
                }
                1 -> {
                    icon(0,128f,216f,1.4f);star(94f,260f,11f)
                    panel(238f,245f,58f,35f,palette.selected);text("+1",267f,270f,22f,center=true)
                    text(money(java.math.BigDecimal("12")),180f,382f,55f,palette.accent,true,true,max=270f)
                    text(t("per use","per utilizzo"),180f,412f,23f,center=true)
                    text(t("Example: €120 ÷ 10 uses","Esempio: 120 € ÷ 10 utilizzi"),180f,447f,16f,Color.parseColor("#896185"),true,max=286f)
                }
                else -> {
                    val colors=intArrayOf(0xFFF68EB8.toInt(),0xFFF9CD6A.toInt(),palette.accent,0xFF79CAEB.toInt(),0xFFB398EE.toInt())
                    for(i in 0..4) {
                        val center=65f+i*57f;val height=45f+i*24f
                        rect(center-15,385f-height,30f,height,ink)
                        rect(center-12,388f-height,24f,height-3,colors[i])
                        for(row in 0 until (height/9).toInt())rect(center-10,380f-row*9,20f,1f,Color.argb(120,255,255,255))
                        text(((i+1)*20).toString(),center,377f-height,16f,center=true,max=48f)
                        icon(intArrayOf(10,2,1,5,11)[i],center-16,397f,.43f)
                    }
                    text(t("Illustrative example","Esempio illustrativo"),180f,450f,15f,Color.parseColor("#896185"),true,max=286f)
                }
            }
            val titles=if(language=="it")listOf("Ogni cosa ha una storia","Usala. Scopri il valore.","Guarda la tua collezione")else listOf("Every thing has a story","Use it. See its worth.","See the bigger picture")
            val first=if(language=="it")listOf("Aggiungi una cosa e il suo prezzo.","Premi «Utilizzato» quando la usi.","Esplora le tue statistiche in pixel.")else listOf("Add a thing and its purchase price.","Tap ‘Used it’ after each use.","Explore your pixel statistics.")
            val second=if(language=="it")listOf("Scegli un’icona e falla tua.","Puoi annullare l’ultimo tocco.","Scopri le cose che apprezzi di più.")else listOf("Pick a pixel icon. Make it yours.","Undo the last tap whenever you need.","Find the things you use and value most.")
            text(titles[onboardingStep],180f,514f,30f,center=true,max=318f)
            text(first[onboardingStep],180f,554f,19f,Color.parseColor("#78557F"),true,max=316f)
            text(second[onboardingStep],180f,583f,19f,Color.parseColor("#78557F"),true,max=316f)
            for(i in 0..2) {
                val x=156f+i*24f
                rect(x-2,615f,12f,12f,ink)
                rect(x,617f,8f,8f,if(i==onboardingStep)palette.accent else Color.parseColor("#FFF0CF"))
                hit(x-7,604f,24f,34f){onboardingStep=i;invalidate()}
            }
            if(onboardingStep>0){text("‹",30f,633f,29f,center=true);hit(12f,597f,43f,44f){onboardingStep--;invalidate()}}
            button(if(onboardingStep==2)t("Let’s start","Iniziamo")else t("Next","Avanti"),18f,659f,324f,60f) {
                if(onboardingStep<2){onboardingStep++;invalidate()}else finishOnboarding()
            }
        }
        private fun home() {
            header("WorthBit",false)
            panel(17f,126f,159f,28f);text(t("My things","Le mie cose"),96f,147f,21f,center=true)
            panel(201f,126f,142f,28f,palette.selected)
            rect(212f,139f,4f,8f,ink);rect(219f,134f,4f,13f,ink);rect(226f,130f,4f,17f,ink)
            text(t("Statistics","Statistiche"),283f,147f,19f,center=true,max=99f)
            hit(196f,119f,151f,44f){openStatistics()}
            if(items.isEmpty()) {
                panel(17f,174f,326f,300f);chest(126f,219f)
                text(t("Nothing here yet","Ancora nessuna cosa"),180f,407f,30f,center=true,max=302f)
                text(t("Add your first item","Aggiungi la tua prima cosa"),180f,441f,21f,Color.parseColor("#896185"),true,max=300f)

            } else {
                items.take(3).forEachIndexed {i,item -> itemCard(item,180f+i*106f,93f,"home")}
                if(items.size>3) {
                    panel(65f,505f,230f,34f,palette.selected)
                    text(t("All things (${items.size}) ›","Tutte le cose (${items.size}) ›"),180f,529f,20f,center=true,max=212f)
                    hit(59f,499f,242f,46f){scroll=0f;navigate("inventory")}
                }
            }
            // Keep the flight in the free space, clear of the three cards and landscape.
            val catY=when {items.isEmpty()->530f;items.size>3->558f;else->(180f+(items.size-1)*106f+93f+591f)/2-14f}
            flyingCat(catY)
            scenery(623f)
            button(t("+ Add item","+ Aggiungi cosa"),18f,674f,324f,60f){form(null)}
        }
        private fun itemCard(item:Item,y:Float,height:Float,from:String) {
            panel(17f,y,326f,height)
            icon(item.icon,27f,y+12f,.8f)
            text(item.name,100f,y+27f,22f,max=227f)
            text(item.costAt()?.let(::money) ?: "—",100f,y+59f,28f,Color.parseColor("#136D42"),max=220f)
            text(if(item.uses==0)t("No uses yet","Nessun utilizzo")else t("${item.uses} uses · per use","${item.uses} utilizzi · per uso"),100f,y+82f,15f,max=224f)
            val top=kotlin.math.max(174f,y);val bottom=if(from=="inventory")kotlin.math.min(647f,y+height)else y+height
            if(bottom>top)hit(17f,top,326f,bottom-top){openItem(item,from)}
        }
        private fun inventory() {
            header(t("All things","Tutte le cose"),true)
            panel(17f,126f,326f,29f)
            text(t("${items.size} things · swipe to browse","${items.size} cose · scorri per sfogliare"),180f,148f,18f,center=true,max=305f)
            scenery(623f)
            c.save();c.clipRect(12f,174f,349f,647f)
            items.forEachIndexed {i,item ->val y=180f+i*122-scroll;if(y+105>=174 && y<=647)itemCard(item,y,105f,"inventory")}
            c.restore()
            if(items.isEmpty())text(t("Nothing here yet","Ancora nessuna cosa"),180f,365f,28f,center=true,max=300f)
            button(t("+ Add item","+ Aggiungi cosa"),18f,674f,324f,60f){form(null)}
        }
        private fun drawForm() {
            header(if(selected==null)t("New item","Nuova cosa")else t("Edit item","Modifica"),true)
            panel(17f,157f,326f,89f);text(t("Name","Nome"),29f,181f,22f)
            panel(17f,257f,326f,89f);text(t("Purchase price · €","Prezzo di acquisto · €"),29f,281f,22f,max=302f)
            panel(17f,360f,326f,156f);text(t("Icon","Icona"),29f,386f,22f)
            panel(30f,400f,88f,99f,palette.selected);icon(selectedIcon,38f,410f,1f)
            text(ItemIcons.name(selectedIcon,language=="it"),133f,427f,20f,max=191f)
            panel(132f,446f,194f,44f,palette.accent)
            text(t("Choose · 30 icons","Scegli · 30 icone"),229f,475f,20f,center=true,max=178f)
            hit(26f,395f,306f,113f){chooseIcon()}
            scenery(564f);star(180f,589f,18f);face(169f,587f)
            button(t("Save","Salva"),18f,674f,324f,60f){saveForm()}
        }
        private fun detail() {
            val item=selected ?: return
            header(item.name,true)
            for(i in 0..9){val x=if(i%2==0)35f+(i*11)%43 else 290f+(i*7)%35;val y=175f+(i*19)%130;fill(intArrayOf(0xFFFFC631.toInt(),0xFFFF70B0.toInt(),0xFF3EE3E6.toInt())[i%3]);c.save();c.rotate(if(i%2==0)-35f else 35f,x,y);c.drawRect(x,y,x+7,y+15,p);c.restore()}
            star(300f,178f,12f)
            if(item.uses==0) {
                panel(138f,226f,84f,17f,Color.parseColor("#65F7AF"));text(t("No uses yet","Nessun utilizzo"),180f,315f,23f,center=true)
            } else {
                text(money(item.costAt()!!),180f,266f,73f,Color.parseColor("#5AF4AA"),true,true,max=285f)
                text(t("per use","per utilizzo"),180f,302f,25f,center=true)
                if(item.uses>1){panel(105f,321f,150f,30f,Color.parseColor("#89F5BC"));text(t("Was ","Prima ")+money(item.costAt(item.uses-1)!!),180f,343f,19f,Color.parseColor("#155535"),true,max=138f)}
            }
            panel(17f,373f,326f,100f);text(t("Purchase price","Prezzo di acquisto"),29f,404f,21f,max=162f);text(price(item),29f,449f,32f,max=161f)
            rect(181f,390f,2f,65f,Color.parseColor("#B887A4"));text(t("Uses","Utilizzi"),201f,404f,21f,max=125f);text(item.uses.toString(),201f,449f,33f,max=122f)
            button(t("+ Used it","+ Utilizzato"),18f,502f,324f,66f){if(item.uses<Int.MAX_VALUE){undo=item.id to item.uses;item.uses++;persist()}}
            if(undo?.first==item.id){panel(17f,601f,326f,43f,Color.parseColor("#DFF7CE"));text("✓",29f,631f,23f,Color.parseColor("#127339"));text(t("Use added","Utilizzo aggiunto"),52f,629f,16f,max=176f);text(t("Undo","Annulla"),255f,630f,19f,Color.parseColor("#166037"),max=72f);hit(250f,598f,91f,51f){undo?.let { item.uses=it.second };undo=null;persist()}}
            scenery(687f)
        }
        private fun compactNumber(value:Long):String {
            val locale=if(language=="it")Locale.ITALY else Locale.UK
            return when {
                value>=1_000_000_000 -> String.format(locale,"%.1fB",value/1_000_000_000.0)
                value>=1_000_000 -> String.format(locale,"%.1fM",value/1_000_000.0)
                value>=1_000 -> String.format(locale,"%.1fk",value/1_000.0)
                else -> value.toString()
            }
        }
        private fun fittedName(value:String,maxWidth:Float,size:Float):String {
            p.typeface=bold;p.textSize=size
            if(p.measureText(value)<=maxWidth)return value
            val count=p.breakText(value,true,(maxWidth-p.measureText("…")).coerceAtLeast(0f),null)
            return value.take(count)+"…"
        }
        private fun statistics() {
            val stats=CollectionStats.from(items)
            contentDescription=t("Collection statistics: ${stats.itemCount} items, ${stats.totalUses} uses, ${stats.unusedCount} unused.","Statistiche della collezione: ${stats.itemCount} cose, ${stats.totalUses} utilizzi, ${stats.unusedCount} mai usate.")
            scenery(727f)
            header(t("Collection stats","Statistiche"),true)
            fun metric(label:String,value:String,x:Float,y:Float) {
                panel(x,y,156f,45f);text(label,x+78f,y+15f,12f,Color.parseColor("#896185"),center=true,max=134f)
                text(value,x+78f,y+38f,23f,center=true,max=134f)
            }
            metric(t("Items","Cose"),stats.itemCount.toString(),17f,112f)
            metric(t("Collection value","Valore della collezione"),money(stats.totalPurchasePrice),187f,112f)
            metric(t("Total uses","Utilizzi totali"),NumberFormat.getIntegerInstance(if(language=="it")Locale.ITALY else Locale.UK).format(stats.totalUses),17f,169f)
            metric(t("Never used","Mai usate"),stats.unusedCount.toString(),187f,169f)

            panel(17f,232f,326f,346f)
            text(t("Your collection, in pixels","La collezione, in pixel"),30f,258f,20f,max=279f)
            text("i",326f,258f,19f,center=true)
            hit(306f,236f,34f,35f) {showInformation(t("Reading your statistics","Come leggere le statistiche"),t(
                "Collection value is the sum of your current items’ purchase prices. Total uses includes all recorded uses. Never used counts items with zero uses.\n\nThe graph shows up to five items, ranked by use count or purchase price. Each bar represents one item. Tap a bar to see its name and exact value; tap the caption to open the item.\n\nBest value ranks up to three used items by their precise purchase price divided by uses. Unused items have no cost per use yet. Statistics update when items are used, edited or deleted, including Undo. These are lifetime totals, not a timeline.",
                "Il valore della collezione è la somma dei prezzi di acquisto delle cose attuali. Gli utilizzi totali comprendono tutti gli utilizzi registrati. Mai usate indica le cose con zero utilizzi.\n\nIl grafico mostra fino a cinque cose, ordinate per utilizzi o prezzo di acquisto. Ogni barra rappresenta una cosa. Tocca una barra per vedere il nome e il valore esatto; tocca la didascalia per aprire la cosa.\n\nIl miglior valore classifica fino a tre cose usate in base al prezzo diviso per gli utilizzi, senza arrotondare per il confronto. Le cose mai usate non hanno ancora un costo per utilizzo. Le statistiche si aggiornano dopo utilizzi, modifiche, eliminazioni e annullamenti. Sono totali complessivi, non una cronologia."))}
            for(i in 0..1) {
                val x=31f+i*153f
                panel(x,272f,144f,29f,if(chartMode==i)palette.selected else cream)
                text(if(i==0)t("Top uses","Più utilizzate")else t("Top value · €","Più costose · €"),x+72f,292f,16f,center=true,max=129f)
                hit(x,269f,144f,35f){if(chartMode!=i){chartMode=i;chartSelection=null;animateChart()}}
            }
            val graphItems=if(chartMode==0)stats.mostUsed else stats.highestPurchasePrice
            val chosen=graphItems.firstOrNull {it.id==chartSelection} ?: graphItems.firstOrNull()
            if(graphItems.isEmpty()) {
                icon(28,148f,347f,.85f)
                text(if(items.isEmpty())t("Your story starts here","La tua storia inizia qui")else t("Ready for the first use?","Pronto per il primo utilizzo?"),180f,480f,22f,center=true,max=285f)
                text(if(items.isEmpty())t("Add a thing to see its statistics","Aggiungi una cosa per vedere i dati")else t("Use a thing and watch the bars grow","Usa una cosa e guarda crescere le barre"),180f,510f,16f,Color.parseColor("#896185"),true,max=285f)
                panel(88f,533f,184f,28f,palette.selected)
                text(if(items.isEmpty())t("+ Add item","+ Aggiungi cosa")else t("View your things ›","Vedi le tue cose ›"),180f,553f,16f,center=true,max=168f)
                hit(83f,527f,194f,40f){if(items.isEmpty())form(null)else navigate("home")}
            } else {
                val maxValue=if(chartMode==0)graphItems.first().uses.toDouble()else graphItems.first().cents.toDouble()
                val baseline=465f
                for(tick in 0..2) {
                    val y=baseline-tick*66f
                    for(dx in 57..329 step 8)rect(dx.toFloat(),y,3f,1f,Color.parseColor("#DABCC5"))
                    val tickValue=maxValue*tick/2
                    val axisNumber=if(chartMode==0)tickValue else tickValue/100
                    val axisLabel=if(axisNumber<1000)NumberFormat.getNumberInstance(if(language=="it")Locale.ITALY else Locale.UK).apply {maximumFractionDigits=if(chartMode==0)1 else 2}.format(axisNumber)else compactNumber(axisNumber.toLong())
                    text(axisLabel,28f,y+4f,10f,Color.parseColor("#896185"),max=26f)
                }
                val colors=intArrayOf(0xFFFA88B3.toInt(),0xFFF9CA60.toInt(),palette.accent,0xFF79CCEF.toInt(),0xFFB69AF0.toInt())
                graphItems.forEachIndexed {i,item ->
                    val center=if(graphItems.size==1)190f else 83f+i*212f/(graphItems.size-1)
                    val metric=if(chartMode==0)item.uses.toDouble()else item.cents.toDouble()
                    val height=if(maxValue<=0)0f else (kotlin.math.round(metric/maxValue*132*chartProgress/4)*4).toFloat().coerceAtLeast(if(chartProgress>0f)4f else 0f)
                    val x=center-16;val top=baseline-height
                    if(height>0) {
                        rect(x+3,top+3,34f,height,Color.parseColor("#CDAEC1"))
                        rect(x-2,top,36f,height,ink)
                        rect(x,top+2,32f,(height-2).coerceAtLeast(1f),colors[i])
                        for(yy in 0 until (height/6).toInt()) {
                            val row=baseline-4-yy*6
                            rect(x+3,row,26f,1f,Color.argb(75,255,255,255))
                            if(yy%2==0){rect(x+4,row-3,4f,3f,Color.argb(50,255,255,255));rect(x+20,row-3,4f,3f,Color.argb(50,255,255,255))}
                        }
                        rect(x,top,32f,3f,if(item.id==chosen?.id)Color.parseColor("#FFE46B")else Color.parseColor("#FFF7E3"))
                        rect(x+28,top+3,4f,(height-3).coerceAtLeast(1f),Color.argb(70,40,16,63))
                    }
                    val value=if(chartMode==0)compactNumber(item.uses.toLong())else if(item.cents>=100000L)"€"+compactNumber(item.cents/100)else price(item)
                    text(value,center,top-7,13f,center=true,max=49f)
                    icon(item.icon,center-23.5f,476f,.62f)
                    val shortName=fittedName(item.name,49f,12f)
                    text(shortName,center,544f,12f,center=true,max=49f)
                    if(item.id==chosen?.id)rect(center-11,548f,22f,2f,palette.edge)
                    hit(center-25,312f,51f,240f){chartSelection=item.id;invalidate()}
                }
                chosen?.let { item ->
                    val value=if(chartMode==0)t("${item.uses} uses","${item.uses} utilizzi")else price(item)
                    text(fittedName(item.name,170f,15f)+" · "+value+"  ›",180f,568f,15f,center=true,max=300f)
                    hit(28f,553f,304f,24f){openItem(item,"statistics")}
                }
            }
            panel(17f,592f,326f,137f)
            star(36f,611f,7f)
            text(t("Best cost per use","Miglior costo per uso"),52f,617f,20f,max=275f)
            if(stats.bestValue.isEmpty()) {
                text(t("Your best finds will appear here","Qui troverai le cose più convenienti"),180f,662f,17f,center=true,max=289f)
                text(t("Start with one use ✦","Inizia con un utilizzo ✦"),180f,693f,16f,Color.parseColor("#896185"),true,max=289f)
            } else {
                stats.bestValue.forEachIndexed {i,item ->
                    val y=626f+i*33f
                    if(i>0)rect(31f,y-3f,297f,1f,Color.parseColor("#E7C8BE"))
                    text((i+1).toString(),31f,y+22f,15f,Color.parseColor("#896185"))
                    icon(item.icon,46f,y-1f,.42f)
                    text(fittedName(item.name,134f,17f),85f,y+20f,17f,max=140f)
                    text(money(item.costAt()!!),233f,y+20f,19f,Color.parseColor("#236D52"),max=82f)
                    text("›",326f,y+21f,21f,center=true)
                    hit(24f,y-1f,313f,33f){openItem(item,"statistics")}
                }
            }
        }
        private fun settings() {
            scenery(720f)
            header(t("Settings","Impostazioni"),true)
            panel(17f,150f,326f,112f)
            text(t("Language","Lingua"),30f,179f,22f)
            for(i in 0..1) {
                val code=if(i==0)"en"else"it";val x=30f+i*157f
                panel(x,196f,143f,45f,if(language==code)palette.selected else cream)
                text((if(language==code)"✓ "else "")+if(i==0)"English"else"Italiano",x+71f,226f,22f,center=true,max=129f)
                hit(x,192f,143f,55f){language=code;store.language=code;invalidate()}
            }
            panel(17f,282f,326f,131f);text(t("Color theme","Tema colore"),30f,312f,22f)
            AppPalette.all.forEachIndexed {i,theme ->
                val x=30f+i*104f
                panel(x,330f,92f,60f,theme.middle)
                rect(x+10f,340f,72f,11f,theme.accent)
                text((if(themeId==i)"✓ "else "")+if(language=="it")theme.nameIt else theme.nameEn,x+46f,376f,17f,center=true,max=82f)
                hit(x,327f,94f,70f){themeId=i;store.theme=i;applyPalette()}
            }
            fun row(label:String,y:Float,action:()->Unit) {
                panel(17f,y,326f,57f);text(label,31f,y+37f,23f,max=277f);text("›",324f,y+37f,28f,center=true);hit(17f,y,326f,60f,action)
            }
            row(t("About the app","Informazioni sull’app"),436f){showInformation(t("About WorthBit","Informazioni su WorthBit"),AppInformation.about(language=="it"))}
            row(t("Privacy policy","Informativa sulla privacy"),513f){showInformation(t("Privacy policy","Informativa sulla privacy"),AppInformation.privacy(language=="it"))}
            row(t("Version ","Versione ")+appVersion,590f){showInformation(t("App version","Versione dell’app"),"WorthBit"+"\n\n"+t("Version ","Versione ")+appVersion+"\nAndroid 7.0+\n\n"+t("Works offline · 30 icons · 3 themes","Funziona offline · 30 icone · 3 temi"))}
            panel(60f,668f,240f,32f,palette.selected)
            text(t("Replay introduction","Rivedi l’introduzione"),180f,691f,19f,center=true,max=224f)
            hit(55f,662f,250f,44f){openOnboarding(true)}
        }
        private fun drawMenu() {
            hits.clear();hit(0f,0f,360f,760f){menu=false;invalidate()}
            panel(156f,58f,187f,151f)
            text(t("✎  Edit","✎  Modifica"),170f,94f,23f,max=159f)
            rect(169f,105f,160f,1f,Color.parseColor("#BC879F"))
            text(t("×  Delete","×  Elimina"),170f,139f,23f,max=159f)
            rect(169f,155f,160f,1f,Color.parseColor("#BC879F"))
            text(t("Settings","Impostazioni"),170f,189f,23f,max=159f)
            hit(156f,58f,187f,49f){menu=false;form(selected)}
            hit(156f,107f,187f,48f){menu=false;deleting=true;invalidate()}
            hit(156f,157f,187f,52f){openSettings()}
        }
        private fun deleteDialog() {
            hits.clear();rect(0f,0f,360f,760f,Color.argb(160,29,12,55));panel(22f,240f,316f,287f)
            panel(159f,258f,42f,44f,Color.parseColor("#FF7D9F"));rect(154f,254f,51f,7f,ink);for(i in 0..2)rect(168f+i*10,268f,3f,25f,ink)
            text(t("Delete item?","Eliminare la cosa?"),180f,352f,34f,Color.parseColor("#FFFBEA"),true,true,max=289f,serif=true)
            text(selected?.name ?: "",180f,390f,23f,Color.parseColor("#85587D"),true,max=285f)
            text(t("and its uses will be deleted","e i suoi utilizzi verranno eliminati"),180f,420f,18f,Color.parseColor("#85587D"),true,max=285f)
            panel(36f,455f,132f,52f);text(t("Cancel","Annulla"),102f,490f,24f,center=true,max=116f);hit(36f,455f,132f,52f){deleting=false;invalidate()}
            button(t("Delete","Elimina"),183f,455f,140f,52f,true){selected?.let {items.remove(it)};selected=null;undo=null;persist();navigate("home")}
        }
        override fun onTouchEvent(event:android.view.MotionEvent):Boolean {
            val x=event.x*360/width;val y=event.y*760/height
            when(event.action){MotionEvent.ACTION_DOWN->{downY=y;return true};MotionEvent.ACTION_UP->{
                if(page=="inventory" && !menu && kotlin.math.abs(y-downY)>25){scroll=(scroll+downY-y).coerceIn(0f,kotlin.math.max(0f,items.size*122f-445f));invalidate()}
                else {hits.lastOrNull { it.first.contains(x,y) }?.second?.invoke();performClick()};return true}}
            return true
        }
        override fun performClick():Boolean {super.performClick();return true}
    }
}

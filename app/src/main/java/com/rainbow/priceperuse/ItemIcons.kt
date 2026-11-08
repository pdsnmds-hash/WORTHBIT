package com.rainbow.priceperuse

import android.graphics.*

/** Stable catalog IDs: adding icons must never reorder existing entries. */
object ItemIcons {
    val namesEn = listOf("Coffee machine","Backpack","Box","Laptop","Phone","Headphones","Camera","Television","Watch","Game console","Book","Bicycle","Trainers","T-shirt","Jacket","Handbag","Suitcase","Chair","Lamp","Vacuum cleaner","Washing machine","Blender","Kettle","Frying pan","Drill","Guitar","Tent","Dumbbells","Plant","Glasses")
    val namesIt = listOf("Macchina del caffè","Zaino","Scatola","Portatile","Telefono","Cuffie","Fotocamera","Televisore","Orologio","Console","Libro","Bicicletta","Scarpe sportive","Maglietta","Giacca","Borsa","Valigia","Sedia","Lampada","Aspirapolvere","Lavatrice","Frullatore","Bollitore","Padella","Trapano","Chitarra","Tenda","Manubri","Pianta","Occhiali")
    val count get() = namesEn.size
    fun name(id: Int, italian: Boolean) = (if(italian) namesIt else namesEn)[id.coerceIn(0,count-1)]
    fun draw(canvas: Canvas, id: Int, x: Float, y: Float, scale: Float) {
        canvas.save();canvas.translate(x,y);canvas.scale(scale,scale)
        Painter(canvas).draw(id)
        canvas.restore()
    }
    private class Painter(val c: Canvas) {
        val ink = Color.parseColor("#28103F")
        val p = Paint().apply { isAntiAlias=false;strokeJoin=Paint.Join.MITER }
        fun r(x:Float,y:Float,w:Float,h:Float,col:String) {p.style=Paint.Style.FILL;p.color=Color.parseColor(col);c.drawRect(x,y,x+w,y+h,p)}
        fun b(x:Float,y:Float,w:Float,h:Float,col:String) {r(x-2,y-2,w+4,h+4,"#28103F");r(x,y,w,h,col)}
        fun line(x:Float,y:Float,xx:Float,yy:Float,col:String,width:Float=3f) {p.style=Paint.Style.STROKE;p.color=Color.parseColor(col);p.strokeWidth=width;c.drawLine(x,y,xx,yy,p);p.style=Paint.Style.FILL}
        fun circle(x:Float,y:Float,r:Float,col:String) {p.color=ink;p.style=Paint.Style.FILL;c.drawCircle(x,y,r+2,p);p.color=Color.parseColor(col);c.drawCircle(x,y,r,p)}
        fun poly(col:String,vararg coords:Float) {
            val path=Path().apply {moveTo(coords[0],coords[1]);for(i in 2 until coords.size step 2)lineTo(coords[i],coords[i+1]);close()}
            p.color=ink;p.style=Paint.Style.STROKE;p.strokeWidth=4f;c.drawPath(path,p);p.style=Paint.Style.FILL;p.color=Color.parseColor(col);c.drawPath(path,p)
        }
        fun face(x:Float,y:Float) {r(x,y,3f,4f,"#28103F");r(x+19,y,3f,4f,"#28103F");r(x+8,y+4,6f,2f,"#28103F");r(x+6,y+2,2f,2f,"#28103F");r(x+14,y+2,2f,2f,"#28103F");r(x-4,y+5,5f,3f,"#FF9FA8");r(x+23,y+5,5f,3f,"#FF9FA8")}
        fun draw(id:Int) {
            when(id) {
                0 -> {b(8f,8f,51f,18f,"#F985A0");r(12f,10f,43f,4f,"#FFB5C0");b(12f,29f,11f,36f,"#EF657C");b(47f,27f,8f,38f,"#E2516D");b(8f,65f,53f,7f,"#A6AFD8");b(29f,28f,14f,7f,"#A8B6C9");b(49f,46f,8f,10f,"#FFFFFF");b(29f,45f,19f,16f,"#FFFFFF");face(23f,17f);r(34f,36f,3f,7f,"#B4E5FA");line(60f,42f,66f,38f,"#FFC400");line(61f,48f,69f,48f,"#FFC400")}
                1 -> {b(26f,3f,23f,12f,"#FFE46A");r(32f,5f,11f,9f,"#FFF0C9");b(11f,28f,10f,38f,"#35C4CB");b(20f,16f,42f,53f,"#5DE9DF");r(26f,20f,29f,7f,"#B1FFF0");b(18f,48f,46f,22f,"#20B7CA");b(31f,48f,21f,9f,"#FFCE4D");r(40f,50f,4f,6f,"#FFF0C9");face(28f,32f)}
                2 -> {b(10f,23f,51f,44f,"#ECA55D");poly("#FFD18B",10f,23f,29f,10f,65f,10f,61f,23f);r(32f,23f,9f,13f,"#FFDB91");line(33f,22f,48f,11f,"#D68A42",4f);face(22f,49f)}
                3 -> {b(10f,13f,53f,39f,"#9F87E3");b(14f,17f,45f,31f,"#9AECE7");r(18f,20f,17f,3f,"#D9FFF3");face(26f,32f);poly("#C7BBF0",10f,54f,63f,54f,71f,66f,3f,66f);r(17f,57f,39f,3f,"#7868AD");r(29f,62f,15f,3f,"#ECE6FF")}
                4 -> {b(19f,4f,35f,67f,"#A995E9");b(23f,10f,27f,50f,"#8FE4F0");r(30f,7f,13f,2f,"#28103F");r(26f,15f,19f,4f,"#D6FFFF");face(26f,34f);circle(36f,65f,2f,"#F9F1FF")}
                5 -> {poly("#A886E0",8f,48f,8f,27f,17f,12f,28f,7f,46f,7f,58f,14f,66f,28f,66f,48f,58f,48f,58f,29f,50f,20f,24f,20f,16f,30f,16f,48f);b(5f,36f,15f,26f,"#F99FBC");b(55f,36f,15f,26f,"#F99FBC");r(8f,39f,4f,19f,"#FFD0E0");r(58f,39f,4f,19f,"#FFD0E0");line(28f,50f,33f,55f,"#28103F",2f);line(33f,55f,39f,55f,"#28103F",2f);line(39f,55f,44f,50f,"#28103F",2f)}
                6 -> {b(16f,12f,22f,10f,"#8C83BC");b(7f,23f,61f,41f,"#F8A0B3");r(11f,26f,53f,7f,"#FFD0DB");circle(37f,46f,14f,"#716A9D");circle(37f,46f,9f,"#6CDBE1");r(31f,40f,6f,5f,"#C9FFFF");b(54f,30f,8f,6f,"#FFE674");r(12f,44f,4f,12f,"#D8688C")}
                7 -> {b(4f,12f,65f,43f,"#9384C4");b(9f,17f,55f,33f,"#A5E8F1");r(13f,20f,17f,4f,"#DCFFFF");face(26f,32f);b(33f,57f,8f,9f,"#8476B0");b(20f,67f,34f,4f,"#B6A7E2")}
                8 -> {b(27f,3f,20f,69f,"#F8ACB9");r(32f,5f,10f,10f,"#FFD6DD");b(18f,21f,38f,34f,"#FFDD72");b(22f,25f,30f,26f,"#B0F1DE");line(37f,29f,37f,39f,"#28103F");line(37f,39f,45f,43f,"#28103F");r(34f,60f,6f,3f,"#BE647D");r(34f,66f,6f,3f,"#BE647D")}
                9 -> {b(5f,16f,64f,42f,"#B59BE9");b(15f,20f,44f,32f,"#91E3DB");face(27f,33f);r(7f,31f,6f,3f,"#28103F");r(9f,29f,2f,7f,"#28103F");r(62f,28f,3f,3f,"#F5749A");r(62f,36f,3f,3f,"#FFE271");r(24f,62f,27f,4f,"#D1B6F2")}
                10 -> {poly("#90DCCF",9f,12f,20f,8f,65f,8f,65f,66f,18f,66f,9f,61f);b(18f,13f,47f,53f,"#A6F1DC");r(22f,17f,5f,45f,"#6AC8BB");b(34f,23f,22f,25f,"#FFF0BB");face(35f,32f);r(46f,56f,8f,15f,"#F17B9B")}
                11 -> {circle(17f,54f,13f,"#FFF0CD");circle(59f,54f,13f,"#FFF0CD");line(17f,54f,31f,32f,"#28103F",6f);line(31f,32f,44f,54f,"#28103F",6f);line(17f,54f,44f,54f,"#28103F",6f);line(44f,54f,55f,30f,"#28103F",6f);line(31f,32f,55f,30f,"#28103F",6f);line(59f,54f,52f,20f,"#28103F",6f);line(17f,54f,31f,32f,"#F17B9B");line(31f,32f,44f,54f,"#F17B9B");line(17f,54f,44f,54f,"#F17B9B");line(44f,54f,55f,30f,"#F17B9B");line(31f,32f,55f,30f,"#F17B9B");line(59f,54f,52f,20f,"#6DCACD");b(24f,25f,14f,4f,"#B5A0DA");line(52f,20f,63f,20f,"#28103F",4f)}
                12 -> {poly("#B69BE9",9f,26f,27f,30f,34f,20f,45f,38f,62f,43f,68f,49f,68f,62f,6f,62f,6f,42f);b(6f,57f,62f,7f,"#FFEFCE");r(11f,34f,9f,16f,"#D9C8FF");line(34f,32f,43f,30f,"#FFF7DC",3f);line(38f,38f,47f,36f,"#FFF7DC",3f);line(42f,44f,51f,42f,"#FFF7DC",3f);r(15f,52f,39f,3f,"#8970B7")}
                13 -> {poly("#FA9EBB",22f,12f,29f,17f,44f,17f,51f,12f,69f,27f,58f,40f,53f,34f,53f,67f,20f,67f,20f,34f,14f,40f,3f,27f);line(29f,17f,33f,23f,"#CF6F99");line(33f,23f,40f,23f,"#CF6F99");line(40f,23f,44f,17f,"#CF6F99");face(26f,43f);r(24f,61f,25f,3f,"#FFD0DE")}
                14 -> {poly("#89DCD6",24f,11f,48f,11f,63f,21f,69f,59f,58f,62f,53f,36f,53f,68f,19f,68f,19f,36f,14f,62f,3f,59f,10f,21f);poly("#CAFFF0",24f,11f,36f,25f,48f,11f,43f,32f,36f,25f,29f,32f);line(36f,27f,36f,65f,"#346C79",3f);b(23f,46f,8f,11f,"#65BBB9");b(42f,46f,8f,11f,"#65BBB9");r(37f,36f,3f,4f,"#FFE284")}
                15 -> {b(23f,12f,27f,24f,"#FFD273");r(28f,17f,17f,17f,"#FFF0CD");poly("#F398B7",12f,28f,61f,28f,68f,65f,6f,65f);r(15f,32f,7f,26f,"#FFC4D8");b(31f,35f,12f,10f,"#FFE18E");face(26f,49f)}
                16 -> {b(25f,4f,23f,12f,"#FFE397");r(30f,8f,13f,7f,"#FFF0CD");b(12f,18f,49f,48f,"#9ACDED");r(17f,23f,5f,37f,"#D0F4FF");r(26f,24f,3f,34f,"#67A7D4");r(47f,24f,3f,34f,"#67A7D4");face(27f,39f);b(17f,68f,8f,5f,"#9584B5");b(49f,68f,8f,5f,"#9584B5")}
                17 -> {b(18f,8f,39f,33f,"#F5A9B2");r(23f,12f,29f,6f,"#FFCFD4");face(26f,25f);b(13f,45f,49f,12f,"#E38D9D");b(17f,59f,7f,14f,"#C89468");b(51f,59f,7f,14f,"#C89468");line(19f,41f,19f,46f,"#28103F",4f);line(55f,41f,55f,46f,"#28103F",4f)}
                18 -> {b(33f,36f,8f,28f,"#B79CE4");poly("#FFE48D",22f,11f,52f,11f,63f,40f,11f,40f);r(24f,14f,6f,21f,"#FFF4CA");face(26f,27f);b(22f,66f,30f,6f,"#A48BD2");line(65f,11f,69f,7f,"#F1B62B");line(8f,14f,3f,11f,"#F1B62B")}
                19 -> {b(12f,47f,37f,20f,"#F794B4");circle(20f,66f,6f,"#9385B9");circle(43f,66f,6f,"#9385B9");face(20f,52f);line(44f,47f,55f,33f,"#28103F",8f);line(55f,33f,55f,13f,"#28103F",8f);line(44f,47f,55f,33f,"#A7D9DC",4f);line(55f,33f,55f,13f,"#A7D9DC",4f);line(57f,11f,67f,51f,"#28103F",5f);b(58f,52f,15f,6f,"#B3A2D5")}
                20 -> {b(11f,8f,52f,61f,"#BBDBF4");r(15f,12f,44f,13f,"#E2F4FF");b(16f,15f,16f,6f,"#A593C8");circle(49f,18f,3f,"#F69DBB");circle(37f,46f,17f,"#9486BE");circle(37f,46f,12f,"#9AEDE5");r(28f,47f,18f,7f,"#70C6DD");r(33f,36f,5f,5f,"#DFFFF7");b(15f,71f,7f,3f,"#9486BE");b(51f,71f,7f,3f,"#9486BE")}
                21 -> {b(22f,10f,29f,5f,"#A6A1CB");b(51f,20f,11f,23f,"#BAF0EA");r(52f,25f,6f,12f,"#FFF0CD");poly("#B9F2EB",19f,17f,53f,17f,48f,47f,26f,47f);poly("#F6ACBA",24f,32f,50f,32f,48f,45f,27f,45f);r(27f,20f,5f,8f,"#E5FFFA");b(24f,49f,26f,18f,"#A99BDD");b(19f,67f,36f,5f,"#8D7FBC");circle(37f,58f,4f,"#FFE17C")}
                22 -> {b(49f,23f,16f,30f,"#B09AD8");r(52f,28f,8f,20f,"#FFF0CD");poly("#88DED4",21f,22f,48f,22f,55f,57f,49f,65f,16f,65f,10f,57f,15f,36f,4f,28f,17f,28f);b(22f,15f,22f,5f,"#B7A3DB");b(29f,10f,8f,4f,"#B7A3DB");r(24f,28f,5f,13f,"#C5FFF1");face(23f,47f)}
                23 -> {line(48f,38f,67f,14f,"#28103F",10f);line(48f,38f,67f,14f,"#B6A0DC",6f);circle(29f,47f,23f,"#9B87BC");circle(29f,47f,18f,"#CBBADA");circle(28f,46f,12f,"#FFF7D8");circle(29f,46f,6f,"#FFD264");r(22f,37f,3f,3f,"#FFFFFF")}
                24 -> {b(49f,25f,12f,12f,"#A89DBE");b(62f,28f,10f,5f,"#C9CEDD");poly("#7ADDD1",12f,17f,46f,17f,52f,23f,52f,40f,33f,40f,29f,62f,13f,62f,17f,40f,7f,36f,7f,23f);b(11f,63f,23f,8f,"#A695C7");r(14f,22f,27f,5f,"#B3FFF0");r(34f,43f,6f,7f,"#F8B74F");face(18f,29f)}
                25 -> {poly("#C29A76",43f,5f,54f,9f,47f,25f,40f,21f);poly("#D7B18D",39f,20f,47f,24f,33f,54f,25f,50f);circle(29f,47f,13f,"#F7BA78");circle(23f,60f,16f,"#F7BA78");circle(30f,46f,6f,"#7D597A");line(47f,13f,22f,63f,"#FFF0CE",2f);line(43f,12f,18f,62f,"#A77B6A",1f);b(13f,63f,17f,4f,"#A97474");r(17f,46f,4f,6f,"#FFDBA7")}
                26 -> {poly("#89DDD1",4f,65f,34f,13f,71f,65f);poly("#F8B1C2",34f,13f,58f,65f,10f,65f);poly("#FFF0BD",34f,31f,47f,65f,21f,65f);line(34f,31f,34f,65f,"#A78898",2f);line(34f,13f,34f,6f,"#28103F",3f);line(4f,65f,1f,71f,"#28103F",3f);line(71f,65f,74f,71f,"#28103F",3f);r(12f,60f,8f,3f,"#F9D4DE")}
                27 -> {b(18f,23f,37f,8f,"#BFB0DF");b(7f,12f,10f,30f,"#F291B2");b(19f,17f,7f,20f,"#F8B8CF");b(56f,12f,10f,30f,"#F291B2");b(47f,17f,7f,20f,"#F8B8CF");b(18f,57f,37f,7f,"#BFB0DF");b(7f,47f,10f,28f,"#8CDCD7");b(19f,51f,7f,20f,"#B6F7E5");b(56f,47f,10f,28f,"#8CDCD7");b(47f,51f,7f,20f,"#B6F7E5")}
                28 -> {line(36f,48f,36f,14f,"#3B8C59",4f);poly("#92E393",36f,34f,18f,31f,11f,18f,28f,21f);poly("#73CFA1",37f,27f,44f,12f,62f,9f,57f,23f);poly("#B2ED8B",36f,16f,27f,9f,29f,2f,40f,9f);poly("#F7A5B9",17f,46f,57f,46f,52f,71f,22f,71f);b(15f,42f,44f,7f,"#F8BACC");face(26f,55f)}
                29 -> {b(4f,29f,27f,23f,"#BC9DDF");b(43f,29f,27f,23f,"#BC9DDF");r(8f,33f,19f,15f,"#A8E3ED");r(47f,33f,19f,15f,"#A8E3ED");line(33f,36f,41f,36f,"#28103F",4f);line(6f,29f,14f,18f,"#28103F",4f);line(68f,29f,61f,18f,"#28103F",4f);r(10f,35f,6f,3f,"#E5FFFF");r(49f,35f,6f,3f,"#E5FFFF");line(30f,61f,34f,65f,"#28103F",2f);line(34f,65f,40f,65f,"#28103F",2f);line(40f,65f,44f,61f,"#28103F",2f)}
            }
        }
    }
}

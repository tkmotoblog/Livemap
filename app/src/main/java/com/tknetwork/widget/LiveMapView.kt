package com.tknetwork.widget

import android.content.Context
import android.graphics.*
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.tknetwork.livemap.R
import java.util.Locale
import kotlin.math.sin


class LiveMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Runnable {

    data class Node(val name: String, val lat: Double, val lng: Double)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3BDE32")
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        setShadowLayer(15f, 0f, 0f, Color.parseColor("#3BDE32"))
    }
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val nodes = ArrayList<Node>()
    private var running = false
    private var thread: Thread? = null
    var isBlinking: Boolean = false
    private var activeNode = -1
    private var homeNodeIndex = 0
    private var selectedNodeIndex = -1
    private var pulse = 0f
    private var linePhase = 0f
    private var lineProgress = 0f
    private var worldMap: Bitmap? = null
    private var activeFlagBitmap: Bitmap? = null
    private var homeFlagBitmap: Bitmap? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        val original = BitmapFactory.decodeResource(resources, R.drawable.world_map)
        if (original != null) {
            worldMap = processMapBitmap(original)
        }
        initNodes()
        val deviceCountry = Locale.getDefault().displayCountry
        setHomeCountry(deviceCountry)
    }

    private fun updateHomeFlag() {
        if (homeNodeIndex in nodes.indices) {
            var name = nodes[homeNodeIndex].name.lowercase(Locale.ROOT).replace(" ", "_")
            if (name.isEmpty()) {
                name = "philippines"
            }
            try {
                val inputStream = context.assets.open("flags/flag_$name.png")
                val original = BitmapFactory.decodeStream(inputStream)
                homeFlagBitmap = getCircularBitmap(original)
            } catch (e: Exception) {
                try {
                    val inputStream = context.assets.open("flags/flag_philippines.png")
                    val original = BitmapFactory.decodeStream(inputStream)
                    homeFlagBitmap = getCircularBitmap(original)
                } catch (ex: Exception) {
                    homeFlagBitmap = null
                }
            }
        }
    }

    fun setActiveFlag(flagName: String) {
        val formattedName = flagName.lowercase(Locale.ROOT).replace(" ", "_")
        
        try {
            val inputStream = try {
                context.assets.open("flags/flag_$formattedName.png")
            } catch (e: Exception) {
                if (activeNode in nodes.indices) {
                    val nodeName = nodes[activeNode].name.lowercase(Locale.ROOT).replace(" ", "_")
                    context.assets.open("flags/flag_$nodeName.png")
                } else {
                    throw e
                }
            }
            
            val original = BitmapFactory.decodeStream(inputStream)
            if (original != null) {
                activeFlagBitmap = getCircularBitmap(original)
            }
            inputStream.close()
            postInvalidate()
        } catch (e: Exception) {
            try {
                val inputStream = context.assets.open("flags/flag_philippines.png")
                val original = BitmapFactory.decodeStream(inputStream)
                activeFlagBitmap = getCircularBitmap(original)
                inputStream.close()
            } catch (ex: Exception) {
                activeFlagBitmap = null
            }
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, size, size)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun setActiveNode(index: Int) {
        if (activeNode != index) {
            activeNode = index
            lineProgress = 0f
        }
        postInvalidate()
    }

    private fun processMapBitmap(source: Bitmap): Bitmap {
        val bmp = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // 1. Gawing Grayscale ang image para mawala ang original colors
        val matrix = ColorMatrix().apply {
            setSaturation(0f)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        val tintColor = context.getColor(R.color.colorAccent)
        paint.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(bmp, 0f, 0f, paint)

        return bmp
    }
    private fun initNodes() {
        nodes.clear()
        addNode("Philippines", 12.8797, 121.7740)
        
        // ASIA
        addNode("Singapore", 1.3521, 103.8198)
        addNode("Japan", 36.2048, 138.2529)
        addNode("Hong Kong", 22.3193, 114.1694)
        addNode("South Korea", 35.9078, 127.7669)
        addNode("Taiwan", 23.6978, 120.9605)
        addNode("Vietnam", 14.0583, 108.2772)
        addNode("Thailand", 15.8700, 100.9925)
        addNode("Malaysia", 4.2105, 101.9758)
        addNode("Indonesia", -0.7893, 113.9213)
        addNode("India", 20.5937, 78.9629)
        
        // OCEANIA
        addNode("Australia", -25.2744, 133.7751)
        addNode("New Zealand", -40.9006, 174.8860)
        
        // NORTH AMERICA
        addNode("USA", 37.0902, -95.7129)
        addNode("Canada", 56.1304, -106.3468)
        addNode("Mexico", 23.6345, -102.5528)
        
        // SOUTH AMERICA
        addNode("Brazil", -14.2350, -51.9253)
        addNode("Argentina", -38.4161, -63.6167)
        addNode("Chile", -35.6751, -71.5430)
        addNode("Colombia", 4.5709, -74.2973)
        
        // EUROPE
        addNode("Finland", 61.9241, 25.7482)
        addNode("United Kingdom", 55.3781, -3.4360)
        addNode("Germany", 51.1657, 10.4515)
        addNode("France", 46.2276, 2.2137)
        addNode("Netherlands", 52.1326, 5.2913)
        addNode("Italy", 41.8719, 12.5674)
        addNode("Spain", 40.4637, -3.7492)
        addNode("Russia", 61.5240, 105.3188)
        addNode("Sweden", 60.1282, 18.6435)
        addNode("Norway", 60.4720, 8.4689)
        addNode("Switzerland", 46.8182, 8.2275)
        addNode("Turkey", 38.9637, 35.2433)
        
        // MIDDLE EAST & AFRICA
        addNode("UAE", 23.4241, 53.8478)
        addNode("Saudi Arabia", 23.8859, 45.0792)
        addNode("Israel", 31.0461, 34.8516)
        addNode("South Africa", -30.5595, 22.9375)
        addNode("Egypt", 26.8206, 30.8025)
        addNode("Nigeria", 9.0820, 8.6753)
    }

    private fun addNode(name: String, lat: Double, lng: Double) {
        nodes.add(Node(name, lat, lng))
    }

    fun setHomeCountry(countryName: String) {
        homeNodeIndex = 0
        updateHomeFlag()
        postInvalidate()
    }

    fun setCountry(countryName: String) {
        Log.d("LiveMapView", "setCountry called with: $countryName")
        if (countryName.isEmpty()) return
        
        val name = countryName.uppercase(Locale.ROOT)
        var newIndex = -1

        // 1. Enhanced Matching Logic
        newIndex = nodes.indexOfFirst {
            val nodeName = it.name.uppercase(Locale.ROOT)
            val isMatch = name.contains(nodeName) || nodeName.contains(name) ||
            (nodeName == "USA" && (name.contains("UNITED STATES") || name.contains("US ") || name.startsWith("US-") || name.contains(".US"))) ||
            (nodeName == "UNITED KINGDOM" && (name.contains("UK ") || name.contains("GREAT BRITAIN") || name.startsWith("UK-") || name.contains(".UK"))) ||
            (nodeName == "SINGAPORE" && (name.contains("SG") || name.contains("SINGA") || name.contains(".SG"))) ||
            (nodeName == "JAPAN" && (name.contains("JP") || name.contains("JAPAN") || name.contains(".JP"))) ||
            (nodeName == "HONG KONG" && (name.contains("HK") || name.contains("HONGKONG") || name.contains(".HK"))) ||
            (nodeName == "SOUTH KOREA" && (name.contains("KOREA") || name.contains("KR"))) ||
            (nodeName == "GERMANY" && (name.contains("DE") || name.contains(".DE"))) ||
            (nodeName == "FRANCE" && (name.contains("FR") || name.contains(".FR"))) ||
            (nodeName == "CANADA" && (name.contains("CA") || name.contains(".CA")))
            isMatch
        }

        if (newIndex == -1) {
            // Fallback 2: Try splitting by common delimiters
            val parts = name.split(Regex("[^A-Z]"))
            for (part in parts) {
                if (part.length >= 2) {
                    newIndex = nodes.indexOfFirst { it.name.uppercase(Locale.ROOT).contains(part) }
                    if (newIndex != -1) break
                }
            }
        }

        if (newIndex != -1) {
            if (activeNode != newIndex) {
                activeNode = newIndex
                lineProgress = 0f
                Log.d("LiveMapView", "Match found! Node: ${nodes[newIndex].name} (Index: $newIndex)")
            }
        } else {
            Log.d("LiveMapView", "No match found for: $countryName. Using first ASIA node as fallback for debugging.")
            // Debug fallback to Singapore if no match found
            val sgIndex = nodes.indexOfFirst { it.name == "Singapore" }
            if (sgIndex != -1) {
                activeNode = sgIndex
                lineProgress = 0f
            }
        }
        postInvalidate()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val mapRect = RectF(width * 0.05f, height * 0.15f, width * 0.95f, height * 0.85f)
            var clickedOnNode = false
            for (i in nodes.indices) {
                val coords = getScreenCoords(nodes[i], mapRect)
                val dx = event.x - coords.x
                val dy = event.y - coords.y
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                if (distance < 45f) {
                    selectedNodeIndex = i
                    clickedOnNode = true
                    break
                }
            }
            if (!clickedOnNode) selectedNodeIndex = -1
            performClick()
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(context.getColor(R.color.colorPrimary))
        val mapRect = RectF(width * 0.05f, height * 0.15f, width * 0.95f, height * 0.85f)

        if (width == 0 || height == 0) return

        worldMap?.let {
            canvas.drawBitmap(it, null, mapRect, Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 180 })
        }

        pulse += 0.35f
        if (pulse > 1000f) pulse = 0f

        // 1. Draw Connection Line (From Own Country to VPN Country)
        if (activeNode != -1 && activeNode != homeNodeIndex) {
            val homeCoords = getScreenCoords(nodes[homeNodeIndex], mapRect)
            val activeCoords = getScreenCoords(nodes[activeNode], mapRect)

            linePhase -= 1.8f
            // Dash effect ay lilitaw lang kapag tapos na ang animation (progress == 1.0)
            if (lineProgress >= 1.0f) {
                linePaint.pathEffect = DashPathEffect(floatArrayOf(30f, 20f), linePhase)
            } else {
                linePaint.pathEffect = null
            }
            drawCurveLine(canvas, homeCoords.x, homeCoords.y, activeCoords.x, activeCoords.y, lineProgress)
        }

        // 2. Draw Nodes
        for (i in nodes.indices) {
            val coords = getScreenCoords(nodes[i], mapRect)

            when (i) {
                activeNode -> {
                    // --- VPN COUNTRY (Neon Green Radar Pulse + Flag) ---
                    val activePulse = (pulse * 2.5f % 100f)
                    val radarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#3BDE32")
                        style = Paint.Style.STROKE
                        strokeWidth = 4f
                        alpha = (200 * (1 - activePulse / 100f)).toInt()
                    }
                    canvas.drawCircle(coords.x, coords.y, 15f + (activePulse * 0.7f), radarPaint)

                    activeFlagBitmap?.let {
                        val flagSize = 25f // Bahagyang pinalaki
                        val destRect = RectF(
                            coords.x - flagSize,
                            coords.y - flagSize,
                            coords.x + flagSize,
                            coords.y + flagSize
                        )
                        canvas.drawBitmap(it, null, destRect, Paint(Paint.ANTI_ALIAS_FLAG))
                    } ?: run {
                        nodePaint.color = Color.parseColor("#3BDE32")
                        nodePaint.alpha = 255
                        canvas.drawCircle(coords.x, coords.y, 10f, nodePaint)
                    }

                    textPaint.color = Color.parseColor("#3BDE32")
                    canvas.drawText(nodes[i].name, coords.x, coords.y - 50f, textPaint)
                }
                homeNodeIndex -> {
                    // --- OWN COUNTRY (Professional Green Radar Pulse + Local Flag/Logo) ---
                    val homePulse = (pulse * 2.0f % 100f)
                    val radarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#3BDE32")
                        style = Paint.Style.STROKE
                        strokeWidth = 3.5f
                        alpha = (180 * (1 - homePulse / 100f)).toInt()
                    }
                    canvas.drawCircle(coords.x, coords.y, 15f + (homePulse * 0.6f), radarPaint)

                    homeFlagBitmap?.let {
                        val flagSize = 22f
                        val destRect = RectF(
                            coords.x - flagSize,
                            coords.y - flagSize,
                            coords.x + flagSize,
                            coords.y + flagSize
                        )
                        canvas.drawBitmap(it, null, destRect, Paint(Paint.ANTI_ALIAS_FLAG))
                    } ?: run {
                        nodePaint.color = Color.parseColor("#3BDE32")
                        nodePaint.alpha = 255
                        canvas.drawCircle(coords.x, coords.y, 9f, nodePaint)
                    }
                }
                else -> {
                    // --- INACTIVE NODES (Twinkling Stars) ---
                    val twinkle = (sin((pulse * 0.15f + i * 0.8f).toDouble()).toFloat() + 1f) / 2f
                    nodePaint.color = Color.parseColor("#94A3B8")
                    nodePaint.alpha = (100 + (twinkle * 130)).toInt()

                    val radius = 5.5f + (twinkle * 2.5f)
                    canvas.drawCircle(coords.x, coords.y, radius, nodePaint)

                    if (i == selectedNodeIndex) {
                        textPaint.color = Color.parseColor("#94A3B8")
                        canvas.drawText(nodes[i].name, coords.x, coords.y - 35f, textPaint)
                    }
                }
            }
        }
    }

    private fun getScreenCoords(node: Node, rect: RectF): PointF {
        val x = rect.left + (node.lng + 180) * (rect.width() / 360f)
        val y = rect.top + (90 - node.lat) * (rect.height() / 180f)
        return PointF(x.toFloat(), y.toFloat())
    }

    private fun drawCurveLine(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, progress: Float) {
        if (progress <= 0f) return
        
        val path = Path()
        path.moveTo(x1, y1)
        
        // Dynamic Curve Height: Adjust curve based on distance to keep it within view
        val dist = Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat()
        val midX = (x1 + x2) / 2
        val curveHeight = Math.min(dist / 2f, 100f)
        val midY = (y1 + y2) / 2 - curveHeight
        
        path.quadTo(midX, midY, x2, y2)

        val pathMeasure = PathMeasure(path, false)
        val partialPath = Path()
        pathMeasure.getSegment(0f, pathMeasure.length * progress, partialPath, true)
        
        canvas.drawPath(partialPath, linePaint)
        
        // Draw a leading particle (Meteor/Glow effect)
        if (progress > 0f) {
            val pos = FloatArray(2)
            pathMeasure.getPosTan(pathMeasure.length * progress, pos, null)
            val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFFFFF") // White center
                style = Paint.Style.FILL
                setShadowLayer(20f, 0f, 0f, Color.parseColor("#3BDE32")) // Green glow
            }
            canvas.drawCircle(pos[0], pos[1], 10f, particlePaint)
            
            // Outer glow circle
            particlePaint.color = Color.parseColor("#3BDE32")
            particlePaint.alpha = 100
            canvas.drawCircle(pos[0], pos[1], 18f, particlePaint)
        }
    }

    override fun run() {
        while (running) {
            if (activeNode != -1 && lineProgress < 1.0f) {
                lineProgress += 0.02f
                if (lineProgress > 1.0f) lineProgress = 1.0f
            }
            postInvalidate()
            try { Thread.sleep(16) } catch (e: Exception) {}
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        running = true
        thread = Thread(this).apply { start() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        running = false
    }
}

//package com.example.enddoomscroll.service
//
//import android.accessibilityservice.AccessibilityService
//import android.accessibilityservice.GestureDescription
//import android.content.Intent
//import android.graphics.Path
//import android.os.Handler
//import android.os.Looper
//import android.view.accessibility.AccessibilityEvent
//import android.view.accessibility.AccessibilityNodeInfo
//import android.widget.Toast
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//
//class MindfulScrollAccessibilityService : AccessibilityService() {
//
//    companion object {
//        const val ACTION_REELS_BLOCKED = "com.mindfulscroll.app.REELS_BLOCKED"
//        const val EXTRA_PACKAGE_NAME = "package_name"
//
//        // Known package names for social media apps
//        const val PACKAGE_INSTAGRAM = "com.instagram.android"
//        const val PACKAGE_YOUTUBE = "com.google.android.youtube"
//        const val PACKAGE_TIKTOK = "com.zhiliaoapp.musically" // TikTok package name varies by region
//    }
//
//    // UI element identifiers for detection (cannot be const val with listOf)
//    private val REELS_INDICATOR_IDS = listOf(
//        "com.instagram.android:id/reel_viewer_container",
//        "reels_feed",
//        "video_container")
//
//
//    private val handler = Handler(Looper.getMainLooper())
//    private var lastBlockTime = 0L
//    private val blockCooldown = 1000L // 1 second cooldown between blocks
//
//    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        event ?: return
//
//        when (event.eventType) {
//            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
//            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
//                checkAndBlockReelsShorts(event)
//            }
//        }
//    }
//
//    override fun onInterrupt() {
//        // Service interrupted
//    }
//
//    private fun checkAndBlockReelsShorts(event: AccessibilityEvent) {
//        val packageName = event.packageName?.toString() ?: return
//        val rootNode = rootInActiveWindow ?: return
//
//        // Check if this is a monitored app
//        if (!isMonitoredApp(packageName)) return
//
//        // Check if Reels/Shorts screen is detected
//        if (isReelsShortsScreen(rootNode, packageName)) {
//            val currentTime = System.currentTimeMillis()
//            if (currentTime - lastBlockTime > blockCooldown) {
//                blockReelsShorts(packageName)
//                lastBlockTime = currentTime
//            }
//        }
//    }
//
//    private fun isMonitoredApp(packageName: String): Boolean {
//        // In a real implementation, check against user's monitored apps from database
//        return packageName == PACKAGE_INSTAGRAM ||
//               packageName == PACKAGE_YOUTUBE ||
//               packageName == PACKAGE_TIKTOK
//    }
//
//    private fun isReelsShortsScreen(rootNode: AccessibilityNodeInfo?, packageName: String): Boolean {
//        if (rootNode == null) return false
//
//        // Strategy 1: Check for specific UI elements
//        when (packageName) {
//            PACKAGE_INSTAGRAM -> {
//                // Look for Reels-specific identifiers
//                val reelsIndicator = findNodeByText(rootNode, "Reels")
//                    ?: findNodeByContentDescription(rootNode, "Reels")
//                    ?: findNodeByResourceId(rootNode, "reel")
//
//                if (reelsIndicator != null) {
//                    return true
//                }
//
//                // Check for video player indicators
//                val videoPlayers = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reel_viewer_container")
//                if (videoPlayers.isNotEmpty()) {
//                    return true
//                }
//            }
//            PACKAGE_YOUTUBE -> {
//                // Look for Shorts indicators
//                val shortsIndicator = findNodeByText(rootNode, "Shorts")
//                    ?: findNodeByContentDescription(rootNode, "Shorts")
//
//                if (shortsIndicator != null) {
//                    // Verify we're actually in Shorts, not just seeing the label
//                    val parent = shortsIndicator.parent
//                    if (parent != null && parent.isClickable) {
//                        return true
//                    }
//                }
//
//                // Check for Shorts-specific UI elements
//                val shortsPlayers = rootNode.findAccessibilityNodeInfosByText("Shorts")
//                if (shortsPlayers.isNotEmpty()) {
//                    // Additional check: look for vertical video indicators
//                    val verticalVideos = rootNode.findAccessibilityNodeInfosByText("Subscribe")
//                    // In Shorts, Subscribe buttons appear frequently
//                    if (verticalVideos.size > 2) {
//                        return true
//                    }
//                }
//            }
//            PACKAGE_TIKTOK -> {
//                // TikTok is primarily a short-form app, so we block the main feed
//                val forYouTab = findNodeByText(rootNode, "For You")
//                    ?: findNodeByText(rootNode, "Following")
//
//                if (forYouTab != null) {
//                    return true
//                }
//            }
//        }
//
//        // Strategy 2: Check for video playback indicators
////        val playButtons = rootNode.findAccessibilityNodeInfosByText("Play")
////            .union(rootNode.findAccessibilityNodeInfosByContentDescription("Play"))
////
//        val playButtonsByText =
//            rootNode.findAccessibilityNodeInfosByText("Play")
//
//        val playButtonsByDescription =
//            findNodeByContentDescription(rootNode, "Play")
//
//        val playButtons = playButtonsByText + playButtonsByDescription
//        // If multiple play buttons visible, likely a video feed
//        if (playButtons.size > 3) {
//            return packageName == PACKAGE_TIKTOK || packageName == PACKAGE_INSTAGRAM
//        }
//
//        return false
//    }
//
//    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
//        val nodes = root.findAccessibilityNodeInfosByText(text)
//        return nodes.firstOrNull()
//    }
//
////    private fun findNodeByContentDescription(root: AccessibilityNodeInfo, description: String): AccessibilityNodeInfo? {
////        val nodes = root.findAccessibilityNodeInfosByContentDescription(description)
////        return nodes.firstOrNull()
////    }
//
//    private fun findNodeByContentDescription(
//        root: AccessibilityNodeInfo,
//        description: String
//    ): AccessibilityNodeInfo? {
//        if (root.contentDescription?.toString()
//                ?.contains(description, ignoreCase = true) == true
//        ) {
//            return root
//        }
//
//        for (i in 0 until root.childCount) {
//            val child = root.getChild(i) ?: continue
//            val result = findNodeByContentDescription(child, description)
//            if (result != null) return result
//        }
//
//        return null
//    }
//
//    private fun findNodeByResourceId(root: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
//        if (root.viewIdResourceName?.contains(resourceId, ignoreCase = true) == true) {
//            return root
//        }
//
//        for (i in 0 until root.childCount) {
//            val child = root.getChild(i) ?: continue
//            val found = findNodeByResourceId(child, resourceId)
//            if (found != null) {
//                return found
//            }
//            child.recycle()
//        }
//
//        return null
//    }
//
//    private fun blockReelsShorts(packageName: String) {
//        // Trigger back button gesture
//        performBackGesture()
//
//        // Show toast notification
//        handler.post {
//            Toast.makeText(this, "Reels blocked by MindfulScroll", Toast.LENGTH_SHORT).show()
//        }
//
//        // Broadcast event for tracking
//        val intent = Intent(ACTION_REELS_BLOCKED).apply {
//            putExtra(EXTRA_PACKAGE_NAME, packageName)
//        }
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//    }
//
//    private fun performBackGesture() {
//        // Create a simple tap gesture at the back button area (left edge, middle of screen)
//        val displayMetrics = resources.displayMetrics
//        val screenWidth = displayMetrics.widthPixels
//        val screenHeight = displayMetrics.heightPixels
//
//        // Back button is typically at left edge, vertically centered
//        val backX = 50f // 50 pixels from left edge
//        val backY = screenHeight / 2f
//
//        val path = Path().apply {
//            moveTo(backX, backY)
//            lineTo(backX + 10f, backY) // Small movement to register as gesture
//        }
//
//        val gesture = GestureDescription.Builder()
//            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
//            .build()
//
//        dispatchGesture(gesture, object : GestureResultCallback() {
//            override fun onCompleted(gestureDescription: GestureDescription?) {
//                super.onCompleted(gestureDescription)
//            }
//
//            override fun onCancelled(gestureDescription: GestureDescription?) {
//                super.onCancelled(gestureDescription)
//                // Fallback: try global back action
//                performGlobalAction(GLOBAL_ACTION_BACK)
//            }
//        }, null)
//
//        // Fallback after delay
//        handler.postDelayed({
//            performGlobalAction(GLOBAL_ACTION_BACK)
//        }, 200)
//    }
//}


package com.example.enddoomscroll.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MindfulScrollAccessibilityService : AccessibilityService() {

    private var lastBlockTime = 0L
    private val BLOCK_COOLDOWN = 1500L  // 1.5 seconds

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName != "com.instagram.android") return

        // We ONLY care about content changes, not app launch
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val now = System.currentTimeMillis()
        if (now - lastBlockTime < BLOCK_COOLDOWN) return

        val rootNode = rootInActiveWindow ?: return

        if (isInstagramReels(rootNode)) {
            lastBlockTime = now

            // ✅ Exit ONLY reels (single press)
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    /**
     * Detects Instagram Reels screen.
     * This is intentionally conservative to avoid false positives.
     */
    private fun isInstagramReels(root: AccessibilityNodeInfo): Boolean {
        // Common reels indicators
        val reelsTexts = listOf("Reels", "reel", "Watch again")

        for (text in reelsTexts) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            if (nodes.isNotEmpty()) {
                return true
            }
        }

        // Look for vertical video player containers
        if (containsVerticalVideo(root)) {
            return true
        }

        return false
    }

    private fun containsVerticalVideo(node: AccessibilityNodeInfo): Boolean {
        if (node.className?.toString()?.contains("VideoView", ignoreCase = true) == true) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (containsVerticalVideo(child)) return true
        }
        return false
    }
}



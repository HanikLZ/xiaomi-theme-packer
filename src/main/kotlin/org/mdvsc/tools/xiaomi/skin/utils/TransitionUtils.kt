package org.mdvsc.tools.xiaomi.skin.utils

import javafx.animation.Animation
import javafx.animation.FadeTransition
import javafx.scene.Node
import javafx.util.Duration

/**
 * @author HanikLZ
 * @since 2017/5/10
 */
object TransitionUtils {
    fun fade(durationMillis: Long) = FadeTransition(Duration.millis(durationMillis.toDouble()))
}

private val nodeAnimationMap = mutableMapOf<Node, Animation>()

fun Node.fadeOut(durationMillis: Long = 300) {
    TransitionUtils.fade(durationMillis).apply {
        fromValue = opacity
        toValue = 0.0
    }.startAnimation(this) { isVisible = false }
}

fun Node.fadeIn(durationMillis: Long = 200) {
    isVisible = true
    TransitionUtils.fade(durationMillis).apply {
        fromValue = 0.0
        toValue = opacity
    }.startAnimation(this)
}

fun Animation.finish() {
    stop()
    onFinished?.handle(null)
}

private fun FadeTransition.startAnimation(node: Node, finishCallback: ((Animation) -> Unit)? = null) {
    nodeAnimationMap[node]?.finish()
    nodeAnimationMap[node] = this
    this.node = node
    val originOpacity = node.opacity
    cycleCount = 1
    isAutoReverse = false
    setOnFinished {
        node.opacity = originOpacity
        nodeAnimationMap.remove(node)
        finishCallback?.invoke(this)
    }
    playFromStart()
}


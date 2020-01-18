package nl.c2dev.dnd

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.RectEvaluator
import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.animation.AnimationUtils


fun Activity.animateClipIn(content: View, background: View) {
    val boundsTo = Rect(content.left, content.top, content.right, content.bottom)
    val boundsFrom = Rect(content.left, content.top, content.right, 0)

    val clipAnim = ObjectAnimator.ofObject(
            content,
            "clipBounds",
            RectEvaluator(),
            boundsFrom, boundsTo)

    clipAnim.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    clipAnim.interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in)
    clipAnim.start()

    val colorTo = Color.argb(0.5F, 0F, 0F, 0F)
    ObjectAnimator.ofArgb(background, "backgroundColor", colorTo)
            .setDuration(clipAnim.duration)
            .start()
}

fun Activity.animateClipOut(content: View, background: View, onComplete: () -> Unit) {
    val boundsTo = Rect(content.left, content.top, content.right, 0)
    val boundsFrom = Rect(content.left, content.top, content.right, content.bottom)

    val clipAnim = ObjectAnimator.ofObject(
            content,
            "clipBounds",
            RectEvaluator(),
            boundsFrom, boundsTo)

    clipAnim.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    clipAnim.addListener(object : Animator.AnimatorListener {
        override fun onAnimationEnd(animation: Animator?) {
            onComplete()
        }

        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
    })
    clipAnim.interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in)
    clipAnim.start()

    ObjectAnimator.ofArgb(background, "backgroundColor", Color.argb(0.5F, 0F, 0F, 0F), Color.TRANSPARENT)
            .setDuration(clipAnim.duration)
            .start()
}
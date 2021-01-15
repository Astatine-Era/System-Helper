package com.astatineera.systemhelper

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.astatineera.systemhelper.Size

class SystemHelper(var activity: Activity) {

    var hideSystemBarIsCalled: Boolean = false
    var systemBarDuration: Long = 3000

    init {
        hideSystemBarIsCalled = false
    }

    fun hideTitleBar() {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.hide()
    }

    fun showTitleBar() {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun hideSystemBar() {

        when {
            //  [ SDK >= 30  ]
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val window = activity.window

                //  HIDE SYSTEM BAR ( NAVIGATION AND STATUS BAR )
                val windowInsetsController =
                    activity.window.insetsController!!
                windowInsetsController.hide(
                        WindowInsets.Type.statusBars()
                                or WindowInsets.Type.navigationBars()
                )
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE


                //  PREVENT RE-APPEARING OF SYSTEM BAR ( NAVIGATION AND STATUS BAR )
                //  WHILE TOUCHING ANYWHERE ON THE DISPLAY
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.fitInsetsTypes = (
                        WindowInsets.Type.navigationBars()
                                or WindowInsets.Type.statusBars()
                        )
                window.decorView.layoutParams = layoutParams

                //  MAKE STABLE FULLSCREEN
                val windowInsets = activity.window.windowManager.currentWindowMetrics.windowInsets
                windowInsets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.displayCutout()
                                or WindowInsets.Type.mandatorySystemGestures()
                                or WindowInsets.Type.systemGestures()
                )
                window.setDecorFitsSystemWindows(false)
            }
            //  [ 16 < SDK < 30  ]
            else -> {
                val decorView = activity.window.decorView
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun autoHideSystemBar() {

        //  HIDE SYSTEM BAR ( NAVIGATION AND STATUS BAR ) WHEN AN ACTIVITY STARTS
        if (!hideSystemBarIsCalled) {
            hideSystemBar()
            hideSystemBarIsCalled = true
        }
        val looper =
            Looper.myLooper()!!
        val handler = Handler(looper)
        val decorView = activity.window.decorView


        //  AUTOMATICALLY HIDE THE SYSTEM BAR ( NAVIGATION AND STATUS BAR )
        //  AFTER SYSTEM-BAR-DURATION IS OVER

        //  [   SDK >= 30   ]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.setOnApplyWindowInsetsListener { _, windowInsets ->
                if (windowInsets.isVisible(
                                WindowInsets.Type.statusBars()
                                        or WindowInsets.Type.navigationBars()
                        )
                ) {
                    handler.postDelayed({ hideSystemBar() }, systemBarDuration)
                }
                windowInsets
            }
        } else {
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    handler.postDelayed({ hideSystemBar() }, systemBarDuration)
                }
            }
        }

        //  DETECT A SINGLE TAP ON THE DEVICE SCREEN
        val gestureDetector = GestureDetector(activity, object : GestureDetector.OnGestureListener {
            override fun onDown(motionEvent: MotionEvent): Boolean = false

            override fun onShowPress(motionEvent: MotionEvent) = Unit
            override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                handler.removeCallbacksAndMessages(null)
                hideSystemBar()
                return false
            }

            override fun onScroll(
                    motionEvent: MotionEvent,
                    motionEvent1: MotionEvent,
                    v: Float,
                    v1: Float
            ): Boolean = false

            override fun onLongPress(motionEvent: MotionEvent) = Unit
            override fun onFling(
                    motionEvent: MotionEvent,
                    motionEvent1: MotionEvent,
                    v: Float,
                    v1: Float
            ): Boolean = false
        })

        //  HIDE THE SYSTEM BAR ( NAVIGATION AND STATUS BAR ) WHEN A TAP ON THE
        //  SCREEN IS DETECTED
        decorView.setOnTouchListener { view, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
            view.performClick()
        }
    }

    fun setShowSystemBarDuration(systemBarDuration: Long) {
        this.systemBarDuration = systemBarDuration
    }

    fun keepScreenOn(b: Boolean) {
        when {
            b -> activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else -> activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun showWhenLocked(b: Boolean) {
        when {
            //  [   SDK >= 27   ]
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> activity.setShowWhenLocked(b)
            //  [   SDK < 27   ]
            else -> {
                when {
                    b -> activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                    else -> activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                }
            }
        }
    }

    fun isDeviceLocked(): Boolean {
        return (activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun setSystemBarTransparent() {
        val window = activity.window

        //  [ SDK >= 21  ]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            )
        }
        //  [ 19 <= SDK < 21  ]
        else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun setSystemBarTransparent(flag_s: Int) {
        val window = activity.window

        //  [ SDK >= 21  ]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    or flag_s)
        }
        //  [ 19 < SDK < 21  ]
        else {
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                            or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                            or flag_s
            )
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun setSystemBarColor(colorID: Int) {
        val window = activity.window

        when {
            //  [ SDK >= 23  ]
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                window.statusBarColor = activity.resources.getColor(colorID, null)
                window.navigationBarColor = activity.resources.getColor(colorID, null)
            }
            //  [ 21 < SDK < 23  ]
            else -> {
                window.statusBarColor = activity.resources.getColor(colorID)
                window.navigationBarColor = activity.resources.getColor(colorID)
            }
        }

        window.addFlags(
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
    }

    companion object{
        fun setDarkMode() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        fun setLightMode() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        fun setDefaultMode() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun getDeviceResolution(): Size {
        val displayMetrics = DisplayMetrics()

        //  [   SDK >= 30   ]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) activity.display?.getRealMetrics(displayMetrics)
        //  [   17 < SDK < 30   ]
        else activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        return Size(displayMetrics.heightPixels, displayMetrics.widthPixels)
    }
}
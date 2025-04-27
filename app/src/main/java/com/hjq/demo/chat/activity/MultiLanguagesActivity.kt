package com.hjq.demo.chat.activity

import android.content.Intent
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.SPUtils
import com.hjq.demo.R
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.manager.ActivityManager
import com.hjq.demo.utils.Trace
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import com.hjq.widget.layout.SettingBar


/**
 * @author r
 * @date 2024/10/17
 * @description Brief description of the file content.
 */
class MultiLanguagesActivity : ChatBaseActivity() {
    private var restart: Boolean = false
    private lateinit var sbAuto: SettingBar
    private lateinit var sbRCh: SettingBar
    private lateinit var sbEn: SettingBar
    private lateinit var tvRight: TextView
    val currentLanguage = SPUtils.getInstance().getString(Constant.LANGUAGE, Constant.LANGUAGE_AUTO)
    var tempLanguage: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_multi_languages
    }

    override fun initView() {
        sbAuto = findViewById(R.id.sb_auto)
        sbRCh = findViewById(R.id.sb_simplified_chinese)
        sbEn = findViewById(R.id.sb_english)
        tvRight = findViewById(R.id.tv_right)
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.multi_languages)
        tvRight.visibility = View.VISIBLE
        tvRight.isEnabled = false
        checkVisible()
        setOnClickListener(R.id.tv_right, R.id.sb_auto, R.id.sb_simplified_chinese, R.id.sb_english)
    }

    private fun checkVisible(languate: String = currentLanguage) {
        sbAuto.rightView.visibility =
            if (languate.equals(Constant.LANGUAGE_AUTO)) View.VISIBLE else View.INVISIBLE
        sbRCh.rightView.visibility =
            if (languate.equals(Constant.LANGUAGE_ZH_CN)) View.VISIBLE else View.INVISIBLE
        sbEn.rightView.visibility =
            if (languate.equals(Constant.LANGUAGE_EN)) View.VISIBLE else View.INVISIBLE
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun onClick(view: View?) {
        // 是否需要重启
        if (view?.id == R.id.sb_auto) {
            tempLanguage = Constant.LANGUAGE_AUTO
            tvRight.isEnabled = !tempLanguage.equals(currentLanguage)
            checkVisible(Constant.LANGUAGE_AUTO)
            // 跟随系统
            restart = MultiLanguages.clearAppLanguage(this)
        } else if (view?.id == R.id.sb_simplified_chinese) {
            tempLanguage = Constant.LANGUAGE_ZH_CN
            tvRight.isEnabled = !tempLanguage.equals(currentLanguage)
            checkVisible(Constant.LANGUAGE_ZH_CN)
            // 简体中文
            restart =
                MultiLanguages.setAppLanguage(this, LocaleContract.getSimplifiedChineseLocale())
        } else if (view?.id == R.id.sb_english) {
            tempLanguage = Constant.LANGUAGE_EN
            tvRight.isEnabled = !tempLanguage.equals(currentLanguage)
            checkVisible(Constant.LANGUAGE_EN)
            // 简体中文
            restart = MultiLanguages.setAppLanguage(this, LocaleContract.getEnglishLocale())
        } else if (view?.id == R.id.tv_right) {
            SPUtils.getInstance().put(Constant.LANGUAGE, tempLanguage)
            if (restart) {
               // 我们可以充分运用 Activity 跳转动画，在跳转的时候设置一个渐变的效果
                Trace.d("startActivity:MainActivity ")
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(Constant.LANGUAGE, tempLanguage)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                ActivityManager.getInstance().finishAllActivities(MainActivity::class.java)
            }
        }
    }
}
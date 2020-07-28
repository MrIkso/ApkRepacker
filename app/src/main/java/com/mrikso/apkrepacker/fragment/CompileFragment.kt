package com.mrikso.apkrepacker.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.ybq.android.spinkit.style.CubeGrid
import com.google.android.material.button.MaterialButton
import com.mrikso.apkrepacker.R
import com.mrikso.apkrepacker.adapter.LogAdapter
import com.mrikso.apkrepacker.task.BuildTask
import com.mrikso.apkrepacker.utils.*
import java.io.File
import java.util.*

class CompileFragment : Fragment() {
    private var listView: ListView? = null
    private var adapter: LogAdapter? = null
    var textArray: ArrayList<String>? = null
        private set
    private var mContext: Context? = null
    private var projectDir: String? = null
    private var layoutApkCompiling: LinearLayout? = null
    private var layoutApkCompiled: LinearLayout? = null
    private var uninstallApp: MaterialButton? = null
    private var installApp: MaterialButton? = null
    private var closeFragment: MaterialButton? = null
    private var copyLog: MaterialButton? = null
    private var progressBar: ProgressBar? = null
    private var progressTip: TextView? = null
    private var savedFileMsg: AppCompatTextView? = null
    private var imageError: AppCompatImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectDir = if (arguments != null) requireArguments().getString("project") else null
        retainInstance = true
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        //savedInstanceState.putParcelableArrayList("logArr", logarray);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_compile, container, false)
        mContext = view.context
        listView = view.findViewById(R.id.log)
        layoutApkCompiling = view.findViewById(R.id.layout_apk_compiling)
        layoutApkCompiled = view.findViewById(R.id.layout_apk_compiled)
        uninstallApp = view.findViewById(R.id.btn_remove)
        installApp = view.findViewById(R.id.btn_install)
        closeFragment = view.findViewById(R.id.btn_close)
        copyLog = view.findViewById(R.id.btn_copy)
        progressBar = view.findViewById(R.id.progressBar)
        progressTip = view.findViewById(R.id.progress_tip)
        imageError = view.findViewById(R.id.image_error)
        savedFileMsg = view.findViewById(R.id.message_build_file_saved)
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)
        val cubeGrid = CubeGrid()
        cubeGrid.setBounds(0, 0, 100, 100)
        cubeGrid.color = ViewUtils.getThemeColor(mContext!!, R.attr.colorAccent)
        cubeGrid.alpha = 0
        progressBar!!.indeterminateDrawable = cubeGrid
        textArray = ArrayList()
        adapter = LogAdapter(mContext, R.id.logitemText, textArray, 12)
        listView!!.adapter = adapter
        listView!!.divider = null
        listView!!.dividerHeight = 0
        val build = Runnable {
            SignUtil.loadKey(mContext) { signTool: SignUtil? ->
                BuildTask(mContext, signTool, this).execute(File(projectDir!!))
            }
        }
        build.run()
    }

    fun append(s: CharSequence) {
        textArray!!.add(s.toString())
        listView!!.setSelection(adapter!!.count - 1)
    }

    fun append(list: ArrayList<String?>?) {
        adapter = LogAdapter(mContext, R.id.logitemText, list, 12)
        listView!!.adapter = adapter
    }

    val text: CharSequence
        get() = listToString(textArray)

    private fun listToString(list: ArrayList<String>?): String {
        val listString = StringBuilder()
        for (s in list!!) {
            listString.append(s).append("\n")
        }
        return listString.toString()
    }

    fun builded(result: File?) {
        if (result != null) {
            val animation = AnimationUtils.loadAnimation(mContext, R.anim.about_card_show)
            layoutApkCompiling!!.visibility = View.GONE
            layoutApkCompiled!!.visibility = View.VISIBLE
            layoutApkCompiled!!.startAnimation(animation)
            val pkg = AppUtils.getApkPackage(mContext, result.absolutePath)
            if (AppUtils.checkAppInstalled(mContext, pkg)) {
                uninstallApp!!.visibility = View.VISIBLE
                uninstallApp!!.setOnClickListener { v: View? ->
                    AppUtils.uninstallApp(mContext, pkg)
                }
            }
            savedFileMsg!!.text = mContext!!.resources
                .getString(R.string.build_apk_saved_to, result.absolutePath)
            installApp!!.setOnClickListener {
                AppUtils.installApk(mContext, result)
            }
        } else {
            copyLog!!.visibility = View.VISIBLE
            copyLog!!.setOnClickListener { v: View? ->
                StringUtils.setClipboard(mContext, text.toString())
            }
            progressBar!!.visibility = View.GONE
            imageError!!.visibility = View.VISIBLE
            progressTip!!.setText(R.string.error_build_failed)
            progressTip!!.setTextColor(ContextCompat.getColor(mContext!!,R.color.google_red))
        }
        closeFragment!!.setOnClickListener {
            FragmentUtils.remove(this)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?): CompileFragment {
            val fragment = CompileFragment()
            val args = Bundle()
            args.putString("project", param1)
            fragment.arguments = args
            return fragment
        }
    }
}
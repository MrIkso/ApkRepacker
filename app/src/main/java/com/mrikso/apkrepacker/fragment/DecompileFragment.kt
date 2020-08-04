package com.mrikso.apkrepacker.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.ybq.android.spinkit.style.CubeGrid
import com.google.android.material.button.MaterialButton
import com.mrikso.apkrepacker.R
import com.mrikso.apkrepacker.activity.AppEditorActivity
import com.mrikso.apkrepacker.adapter.LogAdapter
import com.mrikso.apkrepacker.task.DecodeTask
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper
import com.mrikso.apkrepacker.utils.*
import java.io.File
import java.util.*

class DecompileFragment : Fragment() {

    private var listView: ListView? = null
    private var adapter: LogAdapter? = null
    var textArray: ArrayList<String>? = null
        private set
    private var mContext: Context? = null
    private var selectedApk: File? = null
    private var nameApk: String? = null
    private var apkMode = false
    private var mClose: MaterialButton? = null
    private var mOpen: MaterialButton? = null
    private var mCopylog: MaterialButton? = null
    private var mProgress: ProgressBar? = null
    private var mTextProgress: TextView? = null
    private var mImageResult: AppCompatImageView? = null
    private var mMode = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = requireArguments().getString("selected")
        selectedApk = File(path!!)
        nameApk = requireArguments().getString("name")
        apkMode = requireArguments().getBoolean("mode")
        mMode = requireArguments().getInt("decMode")
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_decompile, container, false)
        mContext = view.context
        listView = view.findViewById(R.id.log)
        mProgress = view.findViewById(R.id.progressBar)
        mTextProgress = view.findViewById(R.id.progress_tip)
        mImageResult = view.findViewById(R.id.image_error)
        mOpen = view.findViewById(R.id.btn_open_project)
        mCopylog = view.findViewById(R.id.btn_copy)
        mClose = view.findViewById(R.id.btn_close)
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)
        val cubeGrid = CubeGrid()
        cubeGrid.setBounds(0, 0, 100, 100)
        cubeGrid.color = ViewUtils.getThemeColor(mContext!!, R.attr.colorAccent)
        cubeGrid.alpha = 0
        mProgress!!.indeterminateDrawable = cubeGrid
        textArray = ArrayList()
        adapter = LogAdapter(mContext, R.id.logitemText, textArray, 12)
        listView!!.adapter = adapter
        listView!!.divider = null
        listView!!.dividerHeight = 0
        val preferenceHelper = PreferenceHelper.getInstance(mContext)
        val decodeAppName = AppUtils.getApkName(mContext, selectedApk!!.absolutePath).replace("/", "_")
        if (!apkMode) {
            if (mMode != -1) {
                when (mMode) {
                    1 -> DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk)
                    2 -> DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk)
                    3 -> DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk)
                }
            } else {
                when (preferenceHelper.decodingMode) {
                    0 -> DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk)
                    1 -> DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk)
                    2 -> DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk)
                }
            }
        } else {
            if (mMode != -1) {
                when (mMode) {
                    1 -> DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk)
                    2 -> DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk)
                    3 -> DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk)
                }
            } else {
                when (preferenceHelper.decodingMode) {
                    0 -> DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk)
                    1 -> DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk)
                    2 -> DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk)
                }
            }
        }
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

    /* show decompile result */
    fun decompileResult(result: File?) {
        if (result != null) {
            mProgress!!.visibility = View.GONE
            mImageResult!!.visibility = View.VISIBLE
            mOpen!!.visibility = View.VISIBLE
            mImageResult!!.setImageResource(R.drawable.ic_done)
            mTextProgress!!.setText(R.string.decompile_finished)
            mOpen!!.setOnClickListener {
                val dataFile = File(result.absolutePath, "apktool.json")
                val apkFileName = ProjectUtils.readJson(dataFile, "apkFileName")
                val intent = Intent(mContext, AppEditorActivity::class.java)
                intent.putExtra("projectPatch", result.absolutePath)
                intent.putExtra("apkFileIcon", ProjectUtils.readJson(dataFile, "apkFileIcon"))
                intent.putExtra(
                    "apkFileName",
                    apkFileName ?: result.name
                )
                intent.putExtra(
                    "apkFilePackageName",
                    ProjectUtils.readJson(dataFile, "apkFilePackageName")
                )
                mContext!!.startActivity(intent)
            }
        } else {
            mProgress!!.visibility = View.GONE
            mOpen!!.visibility = View.GONE
            mImageResult!!.visibility = View.VISIBLE
            mCopylog!!.visibility = View.VISIBLE
            mCopylog!!.setOnClickListener {
                StringUtils.setClipboard(mContext, text.toString())
            }
            mTextProgress!!.setText(R.string.error_decompilation_failed)
            mTextProgress!!.setTextColor(ContextCompat.getColor(mContext!!,R.color.google_red))
        }
        mClose!!.setOnClickListener {
            FragmentUtils.remove(this)
        }
    }

    /* new instance functions */
    companion object {

        const val TAG = "DecopmileFragment"

        @JvmStatic fun newInstance(name: String?, selected: String?, f: Boolean): DecompileFragment {
            val fragment = DecompileFragment()
            val args = Bundle()
            args.putString("name", name)
            args.putString("selected", selected)
            args.putBoolean("mode", f)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic fun newInstance(name: String?, selected: String?, f: Boolean, mode: Int): DecompileFragment {
            val fragment = DecompileFragment()
            val args = Bundle()
            args.putString("name", name)
            args.putString("selected", selected)
            args.putBoolean("mode", f)
            args.putInt("decMode", mode)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic fun newInstance(selected: String?, mode: Int): DecompileFragment {
            val fragment = DecompileFragment()
            val args = Bundle()
            args.putString("selected", selected)
            args.putInt("decMode", mode)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic fun newInstance(selected: String?): DecompileFragment {
            val fragment = DecompileFragment()
            val args = Bundle()
            args.putString("selected", selected)
            fragment.arguments = args
            return fragment
        }
    }
}
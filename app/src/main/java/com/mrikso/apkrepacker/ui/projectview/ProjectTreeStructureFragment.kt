package com.mrikso.apkrepacker.ui.projectview

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jecelyin.common.utils.IOUtils
import com.jecelyin.common.utils.UIUtils
import com.jecelyin.common.utils.UIUtils.OnShowInputCallback
import com.mrikso.apkrepacker.App
import com.mrikso.apkrepacker.R
import com.mrikso.apkrepacker.filepicker.FilePickerDialog
import com.mrikso.apkrepacker.filepicker.FilePickerDialog.FileDialogListener
import com.mrikso.apkrepacker.fragment.dialogs.CreateNewClass
import com.mrikso.apkrepacker.fragment.dialogs.CreateNewClass.OnFileCreatedListener
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.ProjectFileOptionDialog
import com.mrikso.apkrepacker.ui.projectview.treeview.adapter.RecyclerAdapter
import com.mrikso.apkrepacker.ui.projectview.treeview.interfaces.FileChangeListener
import com.mrikso.apkrepacker.ui.projectview.treeview.interfaces.ItemFileClickListener
import com.mrikso.apkrepacker.ui.projectview.treeview.model.ItemData
import com.mrikso.apkrepacker.utils.FileUtil
import com.mrikso.apkrepacker.utils.ProjectUtils
import com.mrikso.apkrepacker.utils.common.DLog
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File


class ProjectTreeStructureFragment : Fragment(), ItemFileClickListener, ProjectFileOptionDialog.ItemClickListener {

    private var mRecyclerView: RecyclerView? = null
    private var mLastSelectedDir: File? = null

    @Nullable
    private var mParentListener: FileChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_folder_structure, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabHeaderTitle: AppCompatTextView = view.findViewById(R.id.title_name)
        tabHeaderTitle.text = getString(R.string.project)

        val tabSubTitle: AppCompatTextView = view.findViewById(R.id.project_name)
        tabSubTitle.text = ProjectUtils.getProjectName()
        mRecyclerView = view.findViewById(R.id.tree_view)

        mRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator?.addDuration = 100
            itemAnimator?.removeDuration = 100
            itemAnimator?.moveDuration = 200
            itemAnimator?.changeDuration = 100
        }

        mRecyclerView?.let { FastScrollerBuilder(it).useMd2Style().build() }
        loadFileSystem()
    }

    private fun loadFileSystem() {

        val fileTreeAdapter = RecyclerAdapter(requireContext())
        val list: List<ItemData> = fileTreeAdapter.getChildrenByPath(ProjectUtils.getProjectPath(), 0)
        fileTreeAdapter.addAll(list, 0)

        mRecyclerView?.adapter = fileTreeAdapter

        fileTreeAdapter.setItemFileClickListener(this)
        fileTreeAdapter.setOnScrollToListener { position -> mRecyclerView?.scrollToPosition(position) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mParentListener = activity as FileChangeListener?
        } catch (ignored: ClassCastException) {
        }
    }

    override fun onFileClick(path: String) {
        mParentListener?.apply {
            doOpenFile(path)
        }
    }

    override fun onFileLongClick(path: String) {
        val file = File(path)
        if (file.isFile) {
            showFileInfo(file)
        } else if (file.isDirectory) {
            showDialogNew(file)
        }
    }

    private fun showFileInfo(file: File) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(file.name)
        val message = """Path: ${file.path}
Size: ${file.length()} byte"""
        builder.setMessage(message)
        builder.create().show()
    }

    private fun showDialogNew(parent: File) {
        mLastSelectedDir = parent
        val fragment = ProjectFileOptionDialog.newInstance()
        fragment.show(childFragmentManager, ProjectFileOptionDialog.TAG)
    }

    override fun onFileItemClick(item: Int?) {
        when (item) {
            R.id.create_class_file -> {
                val createNewClass = CreateNewClass(requireContext(), mLastSelectedDir,
                    object : OnFileCreatedListener {
                        override fun onCreateSuccess(file: File) {
                            mParentListener!!.onFileCreated(file)
                        }

                        override fun onCreateFailed(file: File, e: Exception) {
                            //callback.onFailed(e);
                        }
                    })
                createNewClass.show()
            }
            R.id.create_xml_file -> UIUtils.showInputDialog(requireContext(),
                R.string.action_create_xml,
                0,
                null,
                EditorInfo.TYPE_CLASS_TEXT,
                object : OnShowInputCallback() {
                    override fun onConfirm(input: CharSequence) {
                        try {
                            mLastSelectedDir?.let { createNewFile("$input.xml", it) }
                        } catch (e: Exception) {
                            DLog.e(e)
                        }
                    }
                })
            R.id.add_new_folder -> UIUtils.showInputDialog(requireContext(),
                R.string.action_create_new_folder,
                0,
                null,
                EditorInfo.TYPE_CLASS_TEXT,
                object : OnShowInputCallback() {
                    override fun onConfirm(input: CharSequence) {
                        try {
                            FileUtil.createDirectory(mLastSelectedDir, input.toString())
                        } catch (e: Exception) {
                            UIUtils.toast(App.getContext(), R.string.toast_error_on_add_folder)
                            DLog.e(e)
                        }
                    }
                })
            R.id.select_file -> selectNewFile()
        }
    }

    private fun selectNewFile() {
        FilePickerDialog(requireContext())
            .setTitleText(getString(R.string.select_directory))
            .setSelectMode(FilePickerDialog.MODE_MULTI)
            .setSelectType(FilePickerDialog.TYPE_ALL)
            .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
            .setBackCancelable(true)
            .setOutsideCancelable(true)
            .setDialogListener(
                getString(R.string.choose_button_label),
                getString(R.string.cancel_button_label),
                object : FileDialogListener {
                    override fun onSelectedFilePaths(filePaths: Array<String>) {
                        for (file in filePaths) {
                            try {
                                FileUtil.copyFile(File(file), mLastSelectedDir)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onCanceled() {}
                })
            .show()
    }

    private fun createNewFile(fileName: String, currentFolder: File) {
        var fileName = fileName
        try {
            if (!fileName.endsWith(".xml")) {
                fileName += ".xml"
            }
            val xmlFile = File(currentFolder, fileName)
            xmlFile.parentFile.mkdirs()
            var content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            when {
                currentFolder.name.matches(Regex("^color(-v[0-9]+)?")) -> {
                    content += "<selector>\n</selector>"
                }
                currentFolder.name.matches(Regex("^menu(-v[0-9]+)?")) -> {
                    content += """
                        <menu xmlns:android="http://schemas.android.com/apk/res/android">
                        
                        </menu>
                        """.trimIndent()
                }
                currentFolder.name.matches(Regex("^values(-v[0-9]+)?")) -> {
                    content += "<resources>\n</resources>"
                }
                currentFolder.name.matches(Regex("^layout(-v[0-9]+)?")) -> {
                    content += """<LinearLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
        </LinearLayout>"""
                }
            }
            IOUtils.writeFile(xmlFile, content)
            mParentListener!!.onFileCreated(xmlFile)
        } catch (e: Exception) {
            UIUtils.toast(requireContext(), "Can not create new file")
        }
    }

    companion object {
        const val TAG = "ProjectTreeStructureFragment"
    }

}

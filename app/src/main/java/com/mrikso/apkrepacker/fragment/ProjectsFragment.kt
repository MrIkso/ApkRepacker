package com.mrikso.apkrepacker.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jecelyin.common.utils.UIUtils
import com.jecelyin.common.utils.UIUtils.OnClickCallback
import com.mrikso.apkrepacker.R
import com.mrikso.apkrepacker.activity.AppEditorActivity
import com.mrikso.apkrepacker.fragment.CompileFragment.Companion.newInstance
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper
import com.mrikso.apkrepacker.ui.projectlist.ProjectItem
import com.mrikso.apkrepacker.ui.projectlist.ProjectViewAdapter
import com.mrikso.apkrepacker.ui.projectlist.ProjectViewHolder
import com.mrikso.apkrepacker.utils.FragmentUtils
import com.mrikso.apkrepacker.utils.PermissionsUtils
import com.mrikso.apkrepacker.utils.ViewDeviceUtils
import com.mrikso.apkrepacker.utils.ViewUtils
import com.mrikso.apkrepacker.viewmodel.ProjectsFragmentViewModel
import com.mrikso.apkrepacker.viewmodel.projects.ProjectLoader

class ProjectsFragment : Fragment(),
    ProjectViewHolder.OnItemClickListener {
    private var mViewModel: ProjectsFragmentViewModel? = null
    private var mProjectsList: RecyclerView? = null
    private var mAdapter: ProjectViewAdapter? = null
    private var mToolBar: Toolbar? = null
    private var mRefresh: SwipeRefreshLayout? = null
    private var mEmpty: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(this).get(
            ProjectsFragmentViewModel::class.java
        )
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val addAppFab: ExtendedFloatingActionButton =
            view.findViewById(R.id.fab_add_app)
        addAppFab.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(android.R.id.content, MyFilesFragment())
                .commit()
        }
        mEmpty = view.findViewById(R.id.empty_view)
        mRefresh = view.findViewById(R.id.swipe_refresh_layout_recycler_view)
        mProjectsList = view.findViewById(R.id.project_list)
        mToolBar = view.findViewById(R.id.toolbar)
        mRefresh!!.setColorSchemeResources(
            R.color.google_blue,
            R.color.google_green,
            R.color.google_red,
            R.color.google_yellow
        )
        mRefresh!!.setOnRefreshListener { ProjectLoader.getInstance(requireContext()).loadProjects() }
        mAdapter = ProjectViewAdapter(requireContext())
        mAdapter!!.setOnItemClickListener(this)
        mProjectsList!!.adapter = mAdapter
        mToolBar!!.title = getString(R.string.projects_count, mAdapter!!.itemCount)

        initData()
    }

    private fun initData() {
        if (!PermissionsUtils.checkAndRequestStoragePermissions(this)) return
        when {
            ViewDeviceUtils.getScreenWidthDp(requireContext()) >= 1200 -> {
                mProjectsList!!.layoutManager = StaggeredGridLayoutManager(
                    3,
                    StaggeredGridLayoutManager.VERTICAL
                )
            }
            ViewDeviceUtils.getScreenWidthDp(requireContext()) >= 800 -> {
                mProjectsList!!.layoutManager = StaggeredGridLayoutManager(
                    2,
                    StaggeredGridLayoutManager.VERTICAL
                )
            }
            else -> {
                mProjectsList!!.layoutManager = LinearLayoutManager(requireContext())
            }
        }
        mRefresh!!.isRefreshing = true
        mViewModel!!.projects.observe(
            viewLifecycleOwner,
            Observer { projectItems: List<ProjectItem?>? ->
                mAdapter!!.clear()
                mAdapter!!.setData(projectItems)
                val count = mAdapter!!.itemCount
                mToolBar!!.title = getString(R.string.projects_count, count)
                mRefresh!!.isRefreshing = false
                visiblySwitcher(count)
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionsUtils.REQUEST_CODE_STORAGE_PERMISSIONS) {
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                UIUtils.alert(
                    requireContext(),
                    getString(R.string.error),
                    getString(R.string.need_to_enable_read_storage_permissions),
                    object : OnClickCallback() {
                        override fun onOkClick() {
                            activity!!.finish()
                        }
                    })
            } else {
                initData()
            }
        }
    }

    override fun onProjectClick(item: ProjectItem, position: Int) {
        val intent = Intent(requireContext(), AppEditorActivity::class.java)
        intent.putExtra("apkFileIcon", item.appIcon)
        intent.putExtra("apkFileName", item.appName)
        intent.putExtra("apkFilePackageName", item.appPackage)
        intent.putExtra("projectPatch", item.appProjectPath)
        startActivity(intent)
    }

    override fun onProjectMenuClick(
        view: View,
        item: ProjectItem,
        position: Int
    ) {
        when (view.id) {
            R.id.action_build -> if (PreferenceHelper.getInstance(requireContext())
                    .isConfirmBuild
            ) {
                UIUtils.showConfirmDialog(
                    requireContext(),
                    getString(R.string.confirm_build_title),
                    object : OnClickCallback() {
                        override fun onOkClick() {
                            showCompileFragment(item)
                        }
                    })
            } else {
                showCompileFragment(item)
            }
            R.id.action_about_project -> {
                val about = AboutProjectFragment.newInstance(item)
                FragmentUtils.add(about, parentFragmentManager, android.R.id.content)
            }
            R.id.action_delete -> {
                mAdapter!!.onItemDismiss(position)
                mViewModel!!.deleteProject(position)
            }
        }
    }

    override fun onResume() {
       // initData()
        super.onResume()
    }

    private fun visiblySwitcher(visible: Int) {
        if (visible > 0) {
            ViewUtils.setVisibleOrGone(mEmpty!!, false)
            ViewUtils.setVisibleOrGone(mProjectsList!!, true)
        }
    }

    override fun onProjectLongClick(item: ProjectItem, position: Int) {
        mAdapter!!.notifyItemChanged(position)
    }

    private fun showCompileFragment(projectItem: ProjectItem) {
        val compileFragment: Fragment =
            newInstance(projectItem.appProjectPath)
        FragmentUtils.replace(compileFragment, parentFragmentManager, android.R.id.content)
    }

    companion object {
        const val TAG = "ProjectsFragment"
    }
}
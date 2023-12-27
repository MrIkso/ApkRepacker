package com.mrikso.apkrepacker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import brut.util.Logger
import com.jecelyin.common.utils.DLog
import com.jecelyin.common.utils.IOUtils
import com.mrikso.apkrepacker.R
import com.mrikso.apkrepacker.task.BuildTask
import com.mrikso.apkrepacker.ui.apkbuilder.IBuilderCallback
import com.mrikso.apkrepacker.ui.apkbuilder.TaskStepInfo
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper
import com.mrikso.apkrepacker.utils.SignUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.logging.Level
import javax.annotation.Nullable

class BuildService : Service(), IBuilderCallback, Logger {

    companion object {
        const val CHANNEL_ID = "buildApkService"
        const val CHANNEL_NAME = "Build Apk"
        const val NOTIFICATION_ID = 1
    }

    private val LINE_SEPARATOR_WIN = "\r\n"
    private val binder = LocalBinder()
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.IO + job)
    private var mProjectDir: String? = ""

    private val mCompileLogMutable = StringBuilder()
    var compileLog: String? = ""

    private val mStepMutable = MutableLiveData<String>()
    val stepInfo: LiveData<String> = mStepMutable

    private val mTimeMutable = MutableLiveData<Long>(0)
    val time: LiveData<Long> = mTimeMutable

    private val mFaliedMutable = MutableLiveData<String>()
    val falied: LiveData<String> = mFaliedMutable

    private val mSuccessMutable = MutableLiveData<File>()
    val success: LiveData<File> = mSuccessMutable

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        job.start()
        mProjectDir = intent.getStringExtra("projectDir")
        uiScope.launch {
            val build = Runnable {
                SignUtil.loadKey(baseContext) { signTool: SignUtil? ->
                    BuildTask(baseContext, signTool, this@BuildService, this@BuildService).execute(
                        File(mProjectDir!!)
                    )
                }
            }
            build.run()
           // addNotification(/*intent*/ "building...")

        }
        return START_NOT_STICKY
    }

    private fun addNotification(/*intent: Intent,*/ message: String) {
       /* val intent = Intent(baseContext, AppEditorActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("showCompileFragment", true)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // val notificationIntent = Intent(this, CompileFragment::class.java)
        //  val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
*/
        val notificationManager = NotificationManagerCompat.from(this)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.title_build_apk))
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setWhen(System.currentTimeMillis())
            .setOnlyAlertOnce(true)
          //  .setContentIntent(pendingIntent)

        val notification: Notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.setSound(null, null)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): BuildService = this@BuildService
    }

    override fun info(@StringRes id: Int, vararg args: Any?) {
        val text = "I: " + getString(id, *args)
        mCompileLogMutable.append(text)
        mCompileLogMutable.append(LINE_SEPARATOR_WIN)
        DLog.i("BuildService", text)
    }

    override fun warning(@StringRes id: Int, vararg args: Any?) {
        mCompileLogMutable.append(String.format("W: %s", getString(id, *args)))
        mCompileLogMutable.append(LINE_SEPARATOR_WIN)
        DLog.w("BuildService", getString(id, *args))
    }

    override fun fine(@StringRes id: Int, vararg args: Any?) {
        // publishProgress(String.format("F:%s\n",getText(id, args)));
    }

    override fun text(@StringRes id: Int, vararg args: Any?) {
        mCompileLogMutable.append(getString(id, *args))
        mCompileLogMutable.append(LINE_SEPARATOR_WIN)
    }

    override fun error(@StringRes id: Int, vararg args: Any?) {
        compileLog = getString(id, *args)
        mCompileLogMutable.append(String.format("E: %s", compileLog))
        mCompileLogMutable.append(LINE_SEPARATOR_WIN)
        DLog.e("BuildService", compileLog)
        taskFailed(getString(id, *args))
    }

    override fun log(level: Level, format: String?, ex: Throwable?) {
        val ch = level.name[0]
        val fmt = "%c: %s"
        mCompileLogMutable.append(String.format(fmt, ch, format))
        mCompileLogMutable.append(LINE_SEPARATOR_WIN)
        log(fmt, ch, ex)
    }

    private fun log(fmt: String, ch: Char, ex: Throwable?) {
        if (ex == null) return
        mCompileLogMutable.append(String.format(fmt, ch, ex.message))
        mCompileLogMutable.append(LINE_SEPARATOR_WIN)
        for (ste in ex.stackTrace) {
            mCompileLogMutable.append(String.format(fmt, ch, ste))
            mCompileLogMutable.append(LINE_SEPARATOR_WIN)
        }
        log(fmt, ch, ex.cause)
    }

    override fun taskTime(time: Long) {
        mTimeMutable.postValue(time)
    }

    override fun setTaskStepInfo(taskStepInfo: TaskStepInfo?) {
        val desc = String.format(
            getString(R.string.step),
            taskStepInfo?.stepIndex?.let { Integer.valueOf(it) },
            taskStepInfo?.stepTotal?.let { Integer.valueOf(it) },
            taskStepInfo?.stepDescription
        )

        addNotification(desc)
        mStepMutable.postValue(desc)
    }

    override fun taskSucceed(file: File?) {
        mSuccessMutable.postValue(file)
        IOUtils.writeFile(
            File(PreferenceHelper.getInstance(baseContext).decodingPath + "/" + "compile_log.txt"),
            mCompileLogMutable.toString()
        )
        stopForeground(true)
    }

    override fun taskFailed(str: String?) {
        mFaliedMutable.postValue(str)
        mSuccessMutable.postValue(null)
        IOUtils.writeFile(
            File(PreferenceHelper.getInstance(baseContext).decodingPath + "/" + "compile_log.txt"),
            mCompileLogMutable.toString()
        )
        stopForeground(true)
    }

}
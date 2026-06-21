package com.swordfish.lemuroid.app.shared.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CheatZipPickerLauncher : RetrogradeActivity() {
    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/zip"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
            startActivityForResult(intent, REQUEST_CODE_PICK_ZIP)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_CODE_PICK_ZIP && resultCode == Activity.RESULT_OK) {
            val uri = resultData?.data
            if (uri != null) {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                GlobalScope.launch {
                    settingsManager.addCheatZipFile(uri.toString())
                }
            }
        }
        finish()
    }

    companion object {
        private const val REQUEST_CODE_PICK_ZIP = 1

        fun pickZip(context: Context) {
            context.startActivity(Intent(context, CheatZipPickerLauncher::class.java))
        }
    }
}

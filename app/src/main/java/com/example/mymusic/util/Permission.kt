import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permission {

    companion object {

        const val REQUEST_CODE_PERMISSION = 1000
        private fun checkPermission(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ContextCompat.checkSelfPermission(
                    context,
                    READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
            return false
        }

        fun checkPermission(context: Context, requestCode: Int) {
            // Check if permission is not granted
            if (!checkPermission(context)) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    requestCode
                )
            } else {
                Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
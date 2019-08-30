package org.takuma_isec.airmark.domain.overwrap


import android.Manifest
import androidx.fragment.app.Fragment
import com.google.ar.sceneform.ux.ArFragment

/**
 * A simple [Fragment] subclass.
 */
class MyArFragment : ArFragment() {

    override public fun getAdditionalPermissions(): Array<String> {
        var additionalPermissions = super.getAdditionalPermissions()
        var permissionLength: Int = if (additionalPermissions is Array<String>) {
            additionalPermissions.size
        } else {
            0
        }

        var permissions = Array<String>(permissionLength) { _ -> "" }
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if (permissionLength > 0) {
            System.arraycopy(
                additionalPermissions,
                0,
                permissions,
                1,
                additionalPermissions.size - 1
            )
        }
        return permissions;
    }
}
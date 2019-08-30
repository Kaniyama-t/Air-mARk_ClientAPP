package org.takuma_isec.airmark_reader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.os.Handler
import android.util.Log
import android.util.Size
import com.google.ar.core.Session
import com.google.ar.core.SharedCamera
import java.util.*

@SuppressLint("MissingPermission")
abstract class ARShareCameraLifecycle(
    activity: Activity,
    listener: OnCodeReadListener,
    uithreadHandler: Handler?,
    backgroundHandler: Handler?,
    duration: Long
) {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    public lateinit var sharedSession: Session
    public lateinit var cameraManager: CameraManager
    public lateinit var sharedCamera: SharedCamera
    public lateinit var cameraId: String
    public lateinit var cameraSize: Size

    public var activity = activity
    public var listener = listener
    public var uithireadHandler = uithreadHandler
    public var backgroundHandler = backgroundHandler
    public var duration = duration

    public var ARCoreActive = false

    public var timer: Timer? = null

    init {
        initialize()
    }

    fun initialize() {
        Log.i("AR_ShareCameraLifecycle_LOG", "Called Constructor")
        getSharedCamera(activity)
        val wrappedCallback =
            sharedCamera.createARDeviceStateCallback(object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.i("AR_ShareCameraLifecycle_LOG", "Camera2 ... Device(Camera) Opened.")
                    this@ARShareCameraLifecycle.camera = camera
                    createCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
//                    this@ARShareCameraLifecycle.camera = camera
//                    if(camera != null) {
//                        this@ARShareCameraLifecycle.camera.close()
//                    }
                    Log.e("AR_ShareCameraLifecycle_LOG", "Camera2 ... Device(Camera) Disconnected.")
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(
                        "AR_ShareCameraLifecycle_LOG",
                        "Camera2 ... Device(Camera) Open ERROR HAPPENED."
                    )
                }
            }, backgroundHandler)
        cameraManager.openCamera(cameraId, wrappedCallback, backgroundHandler)
    }

    /**
     * ARCoreからSession,SharedCamera,カメラのプロパティを取得します
     * @param activity コンテキスト
     */
    fun getSharedCamera(activity: Activity) {
        sharedSession = Session(activity, EnumSet.of(Session.Feature.SHARED_CAMERA))
        cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        sharedCamera = sharedSession.sharedCamera
        cameraId = sharedSession.cameraConfig.cameraId
        cameraSize = getSupportedPreviewSizes(
            activity,
            sharedSession.cameraConfig.cameraId
        )[0]
        Log.i(
            "AR_ShareCameraLifecycle_LOG",
            "Acquired objects.\n- Shared... camera(Device), Manager, Session\n- Camera2... cameraID, cameraSize."
        )
    }

    private lateinit var camera: CameraDevice
    private lateinit var previewCaptureBuilder: CaptureRequest.Builder
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private var sessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            Log.i("AR_ShareCameraLifecycle_LOG", "VIDEO_RECORD Session Configuring...")
            configuredProcess(session)
        }

        fun configuredProcess(session: CameraCaptureSession){
            cameraCaptureSession = session

            previewCaptureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            for (s in sharedCamera.arCoreSurfaces)
                previewCaptureBuilder.addTarget(s)

            setRepeatingCaptureRequest()

            Log.i(
                "AR_ShareCameraLifecycle_LOG",
                "VIDEO_RECORD Session Configuring COMPLETED"
            )
        }

        override fun onActive(session: CameraCaptureSession) {
            configuredProcess(session)
            super.onActive(session)
            if (ARCoreActive == false) {
                //sharedSession.resume2()
                ARCoreActive = true

                sharedCamera.setCaptureCallback(object :
                    CameraCaptureSession.CaptureCallback() {

                }, backgroundHandler)
                setTimer(camera, duration)
                Log.i("AR_ShareCameraLifecycle_LOG", "SATED TIMER FOR DECODE QR-CODE")
            }
            Log.i(
                "AR_ShareCameraLifecycle_LOG",
                "VIDEO_RECORD Session Activation COMPLETED!"
            )
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(
                "AR_ShareCameraLifecycle_LOG",
                "VIDEO_RECORD Session Activation FAILED!!!"
            )
        }
    }
    private fun createCameraPreviewSession() {
        val wrappedSessionCallback = sharedCamera.createARSessionStateCallback(
            sessionCallback, backgroundHandler
        )
        Log.i(
            "AR_ShareCameraLifecycle_LOG",
            "-----------------------------------------------Created WrappedCallback"
        )

        camera.createCaptureSession(
            sharedCamera.arCoreSurfaces,
            wrappedSessionCallback,
            backgroundHandler
        )
        Log.i("AR_ShareCameraLifecycle_LOG", "Creating session of VIDEO_RECORD...")
    }

    fun setTimer(cameraDevice: CameraDevice, duration: Long) {
        timer = Timer()
        timer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    sharedSession.pause()
                    process(cameraDevice)
                    backgroundHandler?.post {
                        initialize()
                    }
                }
            },
            1000,
            duration
        )
    }

    private fun setRepeatingCaptureRequest() {
        cameraCaptureSession.setRepeatingRequest(
            previewCaptureBuilder.build(),
            null,
            uithireadHandler
        )
    }

    abstract fun process(cameraDevice: CameraDevice)


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities

    private fun getSupportedPreviewSizes(context: Context, cameraId: String): List<Size> {
        var previewSizes: List<Size> = ArrayList()
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?: return previewSizes
            previewSizes = Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)[0])
            Collections.sort(previewSizes, SizeComparator)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        return previewSizes
    }

    private val SizeComparator = object : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            var result = rhs.width - lhs.width
            if (result == 0) {
                result = rhs.height - lhs.height
            }
            return result
        }
    }
}
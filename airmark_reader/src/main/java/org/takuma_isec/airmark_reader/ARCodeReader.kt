package org.takuma_isec.airmark_reader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.InputConfiguration
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import android.util.Size
import com.google.ar.core.Session
import com.google.ar.core.SharedCamera
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import org.takuma_isec.airmark_reader.datas.IncludingCodeData
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList


/**
 * QRCodeReader on ARCore[com.google.ar.core]
 * [WARNING!] This system is NOT Completely.
 *
 * @author Kaniyama_t
 */
class ARCodeReader(
    var cameraManager: CameraManager,
    var sharedCamera: SharedCamera,
    val cameraId: String,
    val cameraSize: Size,
    val listener: OnCodeReadListener,
    var UIHandler: Handler
) : TimerTask() {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CompanionFunctions 》Create And Timer Set Functions
    companion object {
        fun create(activity: Activity, listener: OnCodeReadListener, duration: Long): Timer {
            Log.i("AirmARk_Reader", "Called #create")

            var timer = Timer()
            val UIHandler = Handler()

            // --- [Get] Getting Camera's static data --------------------------------------------------------------------------
            val sharedSession = Session(activity, EnumSet.of(Session.Feature.SHARED_CAMERA))
            var cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            sharedCamera.setAppSurfaces(this.cameraId, Arrays.asList(cpuImageReader.getSurface()));

            // --- [Schedule] Set This To Read Image To QRData.

            timer.scheduleAtFixedRate(
                ARCodeReader(
                    cameraManager = cameraManager,
                    sharedCamera = sharedSession.sharedCamera,
                    cameraId = sharedSession.cameraConfig.cameraId,
                    cameraSize = getSupportedPreviewSizes(
                        activity,
                        sharedSession.cameraConfig.cameraId
                    )[0],
                    listener = listener,
                    UIHandler = UIHandler
                ),
                1000,
                duration
            )
            return timer
        }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BodyFunctions》AR's QRCode System

    /**
     * 定期実行する関数
     * 本クラスは処理を関数ごとに分けているため、ここでは概略のみを確認できます。
     */
    override fun run() {
        Log.i("AirmARk_Reader", "QRCodeCheck START")

        //handler = Handler()

        // --- [SETUP] SharedCamera(ARCore's Object) -----------------------------------------------------------------
        setupSharedCamera(object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.i("AirmARk_Reader#onOpend", "[SUCCEED] Opened Camera.")
                // --- [Capture] Setup CameraDevice(Camera2API 's Object) And Capture --------------------------------
                captureImage(
                    cameraDevice = camera,
                    onImageAvailableListener = object : ImageReader.OnImageAvailableListener {
                        override fun onImageAvailable(readData: ImageReader?) {
                            // [DECODE] Decode bitmap to QRCode(ZXing's Object) ------------------------------------------
                            val resultData = decodeImageToQRCode(readData!!)

                            camera.close()
                            if (resultData is Array<Result>)
                            // resultData is NOT null
                                UIHandler.post { listener.read(convertResult(resultData)) }
                            else
                            // resultData is NULL
                                UIHandler.post { listener.read(ArrayList()) }
                        }
                    })
            }

            override fun onError(camera: CameraDevice, error: Int) {

            }

            override fun onClosed(camera: CameraDevice) {}
            override fun onDisconnected(camera: CameraDevice) {}
        })
    }

    /**
     * SharedCamera(ARCore's Object)のカメラをOpenする
     * @param cameraCallback カメラがOpenされたときの処理
     */
    @SuppressLint("MissingPermission")
    private fun setupSharedCamera(cameraCallback: CameraDevice.StateCallback) {
        // --- [SET-READER] お手製カメラリーダーを生成して設定 -----------------------------------------------------------
        val wrappedCallback = sharedCamera.createARDeviceStateCallback(cameraCallback, UIHandler)
        // ※ 本来ならカメラPermission取得が必要(AR Coreにより取得済み)
        cameraManager.openCamera(cameraId, wrappedCallback, UIHandler)
        Log.i("AirmARk_Reader#setupSharedCamera", "[QUERY] Requested Camera.")
    }

    /***
     * CaptureSessionを生成・capture()で撮影する．
     * @param cameraDevice 撮影するカメラ
     * @param onImageAvailableListener 撮影後の処理
     */
    private fun captureImage(
        cameraDevice: CameraDevice,
        onImageAvailableListener: ImageReader.OnImageAvailableListener
    ) {
        // --- [CREATE] Process when Read Image ----------------------------------------------------------------
        var mImageReader =
            ImageReader.newInstance(cameraSize.width, cameraSize.height, ImageFormat.JPEG, 2)
        mImageReader.setOnImageAvailableListener(onImageAvailableListener, UIHandler)
        Log.i("AirmARk_Reader#setupSharedCamera", "[SUCCEED] Created ImageReader.")

        var readerSurface = mImageReader.surface

        // --- [CREATE SESSION] ----------------------------------------------------------------------------------------
        var surfaces = sharedCamera.arCoreSurfaces
        surfaces.add(readerSurface)
        cameraDevice.createReprocessableCaptureSession(
            InputConfiguration(cameraSize.width,cameraSize.height,ImageFormat.YUV_420_888),
            surfaces,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("AirmARk_Reader", "QRCode Check Capture Failed")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    // --- [SET PARAMETER] Setting Take Picture Parameter --------------------------------------------------
                    var captureReqest =
                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                    captureReqest.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START
                    )
                    for (s in surfaces) {
                        captureReqest.addTarget(s)
                    }

                    session?.setRepeatingRequest(previewRequestBuilder.build(), null, null)

                    // --- [TAKE-PICTURE!] WEEEEEEE ------------------------------------------------------------------------
                    session.capture(
                        captureReqest.build(),
                        object : CameraCaptureSession.CaptureCallback() {
                            // Decode And Notify
                            fun process(result: CaptureResult) {
                            }

                            override fun onCaptureProgressed(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                partialResult: CaptureResult
                            ) {
                                process(partialResult)
                                Log.i(
                                    "AirmARk_Reader#captureImage",
                                    "[NOTIFY] Capture Progressed."
                                )
                            }

                            override fun onCaptureCompleted(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                result: TotalCaptureResult
                            ) {
                                process(result)
                                Log.i(
                                    "AirmARk_Reader#captureImage",
                                    "[NOTIFY] Capture Created Completely."
                                )

                            }
                        },
                        UIHandler
                    )

                }

            },
            UIHandler
        )
    }

    /**
     * QRコードのでコードを行う
     * @param readReader 画像が使用可能になっている[ImageReader]
     * @return でコードしたQRコードの一覧．型はZXing独自．
     */
    private fun decodeImageToQRCode(readReader: ImageReader): Array<Result>? {
        // > [DEF-PROCESS] This is a process that give ZXing SourceImage when GOT IMAGE!! ------------------
        // --------------------------------------------ZXing is QRCode Reader ------------------------------
        // --- [CONVERT] Image to Bitmap -------------------------------------------------------------------
        Log.i("AirmARk_Reader#decodeImageToQRCode", "STARTED")
        var img = readReader.acquireLatestImage()
        val buf = img.planes[0].buffer
        val b = ByteArray(buf.remaining())
        buf.get(b)
        val bMap = BitmapFactory.decodeByteArray(b, 0, b.size)
        Log.i("AirmARk_Reader#decodeImageToQRCode", "[SUCCEED] Converting Image to Bitmap")

        // --- [CONVERT] Bitmap to ByteBitmap --------------------------------------------------------------
        val intArray = IntArray(bMap.width * bMap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source = RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        Log.i("AirmARk_Reader#decodeImageToQRCode", "[SUCCEED] Converting Bitmap to ByteBitmap")

        // --- [CLOSE] Release Memories of Image Instance --------------------------------------------------
        img.close()

        // --- [DECODE] ByteBitmap to QRCodeDatas ----------------------------------------------------------
        return try {
            val result = QRCodeMultiReader().decodeMultiple(bitmap)
            Log.i(
                "AirmARk_Reader#decodeImageToQRCode",
                "[SUCCEED] Decoded QRCodes! ${result.size} QRCode Founded."
            )

            result
        } catch (e: NotFoundException) {
            Log.i(
                "AirmARk_Reader#decodeImageToQRCode",
                "[SUCCEED] Decoded QRCodes! No QRCode Founded."
            )
            null
        }
    }

    /**
     * ZXingの型のでコード結果を独自の型に変換
     * @param results QRコードのデコード結果の配列([decodeImageToQRCode]の返り値)
     * @return ArrayList<IncludingData>
     */
    private fun convertResult(results: Array<Result>): ArrayList<IncludingCodeData> {
        // --- [CONVERT] ZXing to airmark_reader's OutputData ----------------------------------------------
        var resultArray = ArrayList<IncludingCodeData>()
        for (result in results) {
            Log.i("QRCodeInfo", "Text:${result.text}")
            for (point in result.resultPoints) {
                Log.e("QRCodePoint", "x:${point.x} / y:${point.y}")
            }
            resultArray.add(
                IncludingCodeData(
                    0.0, 0.0, 0.0, 0.0, result.text, URI("")
                )
            )
        }
        Log.i(
            "AirmARk_Reader#decodeImageToQRCode",
            "[SUCCEED] Converting Result(ZXing) to ArrayList<IncludingData>(airmark_reader)"
        )
        return resultArray
    }
}
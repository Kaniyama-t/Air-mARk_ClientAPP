package org.takuma_isec.airmark_reader

//import com.google.ar.core.ImageFormat
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import org.takuma_isec.airmark_reader.datas.IncludingCodeData
import java.net.URI


/**
 * QRCodeReader on ARCore[com.google.ar.core]
 * [WARNING!] This system is NOT Completely.
 *
 * @author Kaniyama_t
 */
class ARCodeReader(
    activity: Activity,
    listener: OnCodeReadListener,
    uithireadHandler: Handler?,
    backgroundHandler: Handler?,
    duration: Long
) : ARShareCameraLifecycle(activity, listener, uithreadHandler = uithireadHandler, backgroundHandler = backgroundHandler,duration = duration) {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BodyFunctions》AR's QRCode System

    private lateinit var mImageReader: ImageReader

    /**
     * 定期実行する関数
     * 本クラスは処理を関数ごとに分けているため、ここでは概略のみを確認できます。
     */
    @SuppressLint("MissingPermission")
    override fun process(camera: CameraDevice) {
        Log.i("AirmARk_Reader", "QRCodeCheck START")

        //backgroundHandler = Handler()

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

                            sharedSession.pause()
                            //camera.close()
                            if (resultData!!.isNotEmpty())
                            // resultData is NOT null
                                uithireadHandler?.post { listener.read(convertResult(resultData)) }
                            else
                            // resultData is NULL
                                uithireadHandler?.post { listener.read(ArrayList()) }
                        }
                    }
                )
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
        val wrappedCallback =
            sharedCamera.createARDeviceStateCallback(cameraCallback, backgroundHandler)
        // ※ 本来ならカメラPermission取得が必要(AR Coreにより取得済み)
        cameraManager.openCamera(cameraId, wrappedCallback, backgroundHandler)
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

        Log.i("AirmARk_reader", "captureImage Called.")
        mImageReader = ImageReader.newInstance(
            cameraSize.width,
            cameraSize.height,
            android.graphics.ImageFormat.JPEG,
            2
        )
        Log.i("AirmARk_reader", "Created ImageReader.")

        var readerSurface = mImageReader.surface

        // --- [CREATE SESSION] ----------------------------------------------------------------------------------------
        var surfaces = sharedCamera.arCoreSurfaces
        surfaces.add(readerSurface)
        Log.i("AirmARk_reader", "surface ArrayList prepared.")

        val readerWrapSessionCallback = sharedCamera.createARSessionStateCallback(object :
            CameraCaptureSession.StateCallback() {
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

                session?.setRepeatingRequest(captureReqest.build(), null, null)

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
                    backgroundHandler
                )

            }
        }, backgroundHandler)

        cameraDevice.createCaptureSession(surfaces, readerWrapSessionCallback, backgroundHandler)
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
package org.thelazybattley.macrotrack.features.photos.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.thelazybattley.macrotrack.domain.model.PHOTO_PICK_MAX
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

private const val CAPTURE_QUALITY = 92

@Composable
actual fun rememberPhotoCamera(): PhotoCamera {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val camera = remember { AndroidPhotoCamera(context = context) }
    DisposableEffect(key1 = lifecycleOwner) {
        camera.controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            camera.controller.unbind()
            camera.close()
        }
    }
    return camera
}

@Composable
actual fun PhotoCameraPreview(camera: PhotoCamera, modifier: Modifier) {
    val controller = (camera as AndroidPhotoCamera).controller
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                this.controller = controller
            }
        }
    )
}

private class AndroidPhotoCamera(private val context: Context) : PhotoCamera {
    val controller = LifecycleCameraController(context).apply {
        setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    }
    // Turning and compressing a full-size photo takes a moment, so it happens off the main thread.
    private val worker = Executors.newSingleThreadExecutor()

    private var isFrontSelected by mutableStateOf(value = false)

    override var isFrontFacing: Boolean
        get() = isFrontSelected
        set(value) {
            isFrontSelected = value
            controller.cameraSelector = if (value) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        }

    override fun capture(onResult: (ByteArray?) -> Unit) {
        val isMirrored = isFrontSelected
        val main = ContextCompat.getMainExecutor(context)
        controller.takePicture(worker, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bytes = runCatching { image.toUprightJpeg(isMirrored = isMirrored) }.getOrNull()
                image.close()
                main.execute { onResult(bytes) }
            }

            override fun onError(exception: ImageCaptureException) {
                main.execute { onResult(null) }
            }
        })
    }

    fun close() {
        worker.shutdown()
    }
}

private fun ImageProxy.toUprightJpeg(isMirrored: Boolean): ByteArray {
    val bitmap = toBitmap()
    val matrix = Matrix().apply {
        postRotate(imageInfo.rotationDegrees.toFloat())
        if (isMirrored) postScale(-1f, 1f)
    }
    val upright = if (matrix.isIdentity) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return ByteArrayOutputStream().also { upright.compress(Bitmap.CompressFormat.JPEG, CAPTURE_QUALITY, it) }.toByteArray()
}

@Composable
actual fun rememberPhotoPicker(): PhotoPicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = remember { AndroidPhotoPicker(context = context.applicationContext, scope = scope) }
    picker.launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = PHOTO_PICK_MAX)
    ) { uris -> picker.onPicked(uris = uris) }
    return picker
}

private class AndroidPhotoPicker(
    private val context: Context,
    private val scope: CoroutineScope
) : PhotoPicker {
    lateinit var launcher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>
    private var pending: ((List<ByteArray>) -> Unit)? = null

    // The system picker takes its limit when it's set up, so [max] is always PHOTO_PICK_MAX here.
    override fun pick(max: Int, onPicked: (List<ByteArray>) -> Unit) {
        pending = onPicked
        launcher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun onPicked(uris: List<Uri>) {
        val onPicked = pending ?: return
        pending = null
        scope.launch {
            val images = withContext(context = Dispatchers.IO) {
                uris.mapNotNull { uri ->
                    runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
                }
            }
            onPicked(images)
        }
    }
}

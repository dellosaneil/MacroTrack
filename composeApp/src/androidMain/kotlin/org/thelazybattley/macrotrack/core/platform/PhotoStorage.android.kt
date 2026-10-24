package org.thelazybattley.macrotrack.core.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.jpegDateTaken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

private const val FOLDER = "progress_photos"
private const val THUMBNAIL_SUFFIX = "_t"
private const val CUTOUT_SUFFIX = "_g"
private const val CUTOUT_EDGE = 900

/** Less of the photo than this being a person means there's no one to line up with. */
private const val MIN_PERSON_SHARE = 0.03
private const val JPEG_QUALITY = 88

/** Kept under no-backup files, so Android's cloud backup never copies the photos off the phone. */
class AndroidPhotoStorage(context: Context) : PhotoStorage {

    private val folder = File(context.noBackupFilesDir, FOLDER)

    override suspend fun store(bytes: ByteArray): StoredImage? = withContext(context = Dispatchers.IO) {
        runCatching {
            val exif = runCatching { ExifInterface(ByteArrayInputStream(bytes)) }.getOrNull()
            val upright = decodeUpright(bytes = bytes, maxEdge = PHOTO_MAX_EDGE, exif = exif) ?: return@runCatching null
            folder.mkdirs()
            val name = UUID.randomUUID().toString()
            File(folder, "$name.jpg").writeBytes(upright.toJpeg())
            File(folder, "$name$THUMBNAIL_SUFFIX.jpg").writeBytes(upright.scaledToEdge(edge = PHOTO_THUMBNAIL_EDGE).toJpeg())
            StoredImage(fileName = name, takenOn = exif?.dateTaken() ?: jpegDateTaken(bytes = bytes))
        }.getOrNull()
    }

    override suspend fun read(fileName: String, isThumbnail: Boolean): ByteArray? = withContext(context = Dispatchers.IO) {
        runCatching { file(fileName = fileName, isThumbnail = isThumbnail).readBytes() }.getOrNull()
    }

    override suspend fun crop(fileName: String, crop: CropRect): String? = withContext(context = Dispatchers.IO) {
        runCatching {
            val photo = BitmapFactory.decodeFile(file(fileName = fileName, isThumbnail = false).path) ?: return@runCatching null
            val x = (crop.left * photo.width).roundToInt().coerceIn(minimumValue = 0, maximumValue = photo.width - 1)
            val y = (crop.top * photo.height).roundToInt().coerceIn(minimumValue = 0, maximumValue = photo.height - 1)
            val width = (crop.width * photo.width).roundToInt().coerceIn(minimumValue = 1, maximumValue = photo.width - x)
            val height = (crop.height * photo.height).roundToInt().coerceIn(minimumValue = 1, maximumValue = photo.height - y)
            val cropped = Bitmap.createBitmap(photo, x, y, width, height)
            val name = UUID.randomUUID().toString()
            File(folder, "$name.jpg").writeBytes(cropped.toJpeg())
            File(folder, "$name$THUMBNAIL_SUFFIX.jpg").writeBytes(cropped.scaledToEdge(edge = PHOTO_THUMBNAIL_EDGE).toJpeg())
            name
        }.getOrNull()
    }

    override suspend fun readCutout(fileName: String): ByteArray? = withContext(context = Dispatchers.IO) {
        val cutout = File(folder, "$fileName$CUTOUT_SUFFIX.png")
        if (cutout.exists()) return@withContext cutout.readBytes()
        val bytes = runCatching { file(fileName = fileName, isThumbnail = false).readBytes() }.getOrNull() ?: return@withContext null
        val png = runCatching { cutOutPerson(bytes = bytes) }.getOrNull() ?: return@withContext null
        cutout.writeBytes(png)
        png
    }

    override suspend fun delete(fileNames: Collection<String>) = withContext(context = Dispatchers.IO) {
        fileNames.forEach { name ->
            file(fileName = name, isThumbnail = false).delete()
            file(fileName = name, isThumbnail = true).delete()
            File(folder, "$name$CUTOUT_SUFFIX.png").delete()
        }
    }

    override suspend fun fileNames(): Set<String> = withContext(context = Dispatchers.IO) {
        folder.listFiles().orEmpty()
            .map { it.nameWithoutExtension }
            .filterNot { it.endsWith(suffix = THUMBNAIL_SUFFIX) || it.endsWith(suffix = CUTOUT_SUFFIX) }
            .toSet()
    }

    private fun file(fileName: String, isThumbnail: Boolean) =
        File(folder, fileName + (if (isThumbnail) THUMBNAIL_SUFFIX else "") + ".jpg")
}

/** Decodes no bigger than needed for [maxEdge], then turns it the way the camera held it. */
private fun decodeUpright(bytes: ByteArray, maxEdge: Int, exif: ExifInterface?): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxEdge) sample *= 2
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return null
    val matrix = Matrix()
    when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> matrix.apply { postRotate(90f); postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> matrix.apply { postRotate(270f); postScale(-1f, 1f) }
    }
    val longEdge = max(bitmap.width, bitmap.height)
    if (longEdge > maxEdge) matrix.postScale(maxEdge / longEdge.toFloat(), maxEdge / longEdge.toFloat())
    return if (matrix.isIdentity) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

/** Keeps only the person, fading their edge, with everything else transparent; null when there's no one. */
private fun cutOutPerson(bytes: ByteArray): ByteArray? {
    val photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.scaledToEdge(edge = CUTOUT_EDGE) ?: return null
    val segmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder().setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build()
    )
    val mask = try {
        Tasks.await(segmenter.process(InputImage.fromBitmap(photo, 0)))
    } finally {
        segmenter.close()
    }
    val width = mask.width
    val height = mask.height
    if (width != photo.width || height != photo.height) return null
    val pixels = IntArray(size = width * height)
    photo.getPixels(pixels, 0, width, 0, 0, width, height)
    val buffer = mask.buffer.apply { rewind() }
    var person = 0
    for (index in pixels.indices) {
        val confidence = buffer.float
        if (confidence >= 0.5f) person++
        // Below 0.3 is background; above 0.7 is the person; in between, a soft edge.
        val alpha = ((confidence - 0.3f) / 0.4f).coerceIn(minimumValue = 0f, maximumValue = 1f)
        pixels[index] = (pixels[index] and 0x00FFFFFF) or ((alpha * 255).toInt() shl 24)
    }
    if (person < pixels.size * MIN_PERSON_SHARE) return null
    val cutout = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    return ByteArrayOutputStream().also { cutout.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
}

private fun Bitmap.scaledToEdge(edge: Int): Bitmap {
    val scale = edge / max(width, height).toFloat()
    if (scale >= 1f) return this
    return Bitmap.createScaledBitmap(this, (width * scale).roundToInt(), (height * scale).roundToInt(), true)
}

private fun Bitmap.toJpeg(): ByteArray =
    ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }.toByteArray()

/** "2026:10:18 07:42:10" → Oct 18, 2026. */
private fun ExifInterface.dateTaken(): LocalDate? {
    val text = getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: getAttribute(ExifInterface.TAG_DATETIME) ?: return null
    val parts = text.take(n = 10).split(":").map { it.toIntOrNull() ?: return null }
    if (parts.size != 3) return null
    return runCatching { LocalDate(year = parts[0], month = parts[1], day = parts[2]) }.getOrNull()
}

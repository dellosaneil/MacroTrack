@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package org.thelazybattley.macrotrack.core.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.thelazybattley.macrotrack.domain.model.jpegDateTaken
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.CIImage
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.CoreVideo.CVPixelBufferGetBaseAddress
import platform.CoreVideo.CVPixelBufferGetBytesPerRow
import platform.CoreVideo.CVPixelBufferGetHeight
import platform.CoreVideo.CVPixelBufferGetWidth
import platform.CoreVideo.CVPixelBufferLockBaseAddress
import platform.CoreVideo.CVPixelBufferRef
import platform.CoreVideo.CVPixelBufferUnlockBaseAddress
import platform.CoreVideo.kCVPixelBufferLock_ReadOnly
import platform.CoreVideo.kCVPixelFormatType_OneComponent8
import platform.Vision.VNGeneratePersonSegmentationRequest
import platform.Vision.VNGeneratePersonSegmentationRequestQualityLevelAccurate
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNPixelBufferObservation
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.setValue
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy
import kotlin.math.max
import kotlin.math.min

private const val FOLDER = "progress_photos"
private const val THUMBNAIL_SUFFIX = "_t"
private const val CUTOUT_SUFFIX = "_g"

/** Less of the photo than this being a person means there's no one to line up with. */
private const val MIN_PERSON_SHARE = 0.03
private const val JPEG_QUALITY = 0.88

/** Kept in Application Support and left out of iCloud backups, so the photos never leave the phone. */
class IosPhotoStorage : PhotoStorage {

    private val manager = NSFileManager.defaultManager

    private val folder: String by lazy {
        val base = manager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )?.path.orEmpty()
        val path = "$base/$FOLDER"
        manager.createDirectoryAtPath(path = path, withIntermediateDirectories = true, attributes = null, error = null)
        NSURL.fileURLWithPath(path = path).setResourceValue(value = NSNumber(bool = true), forKey = NSURLIsExcludedFromBackupKey, error = null)
        path
    }

    override suspend fun store(bytes: ByteArray): StoredImage? = withContext(context = Dispatchers.Default) {
        val image = UIImage.imageWithData(data = bytes.toNSData()) ?: return@withContext null
        val name = NSUUID().UUIDString
        val full = UIImageJPEGRepresentation(image = image.scaledToEdge(edge = PHOTO_MAX_EDGE), compressionQuality = JPEG_QUALITY)
        val thumbnail = UIImageJPEGRepresentation(image = image.scaledToEdge(edge = PHOTO_THUMBNAIL_EDGE), compressionQuality = JPEG_QUALITY)
        if (full == null || thumbnail == null) return@withContext null
        if (!full.writeToFile(path = path(fileName = name, isThumbnail = false), atomically = true)) return@withContext null
        thumbnail.writeToFile(path = path(fileName = name, isThumbnail = true), atomically = true)
        StoredImage(fileName = name, takenOn = jpegDateTaken(bytes = bytes))
    }

    override suspend fun read(fileName: String, isThumbnail: Boolean): ByteArray? = withContext(context = Dispatchers.Default) {
        NSData.dataWithContentsOfFile(path = path(fileName = fileName, isThumbnail = isThumbnail))?.toByteArray()
    }

    override suspend fun crop(fileName: String, crop: CropRect): String? = withContext(context = Dispatchers.Default) {
        val image = UIImage.imageWithContentsOfFile(path = path(fileName = fileName, isThumbnail = false)) ?: return@withContext null
        val cgImage = image.CGImage ?: return@withContext null
        val width = CGImageGetWidth(cgImage).toDouble()
        val height = CGImageGetHeight(cgImage).toDouble()
        val rect = CGRectMake(
            x = crop.left * width,
            y = crop.top * height,
            width = crop.width * width,
            height = crop.height * height
        )
        val croppedImage = CGImageCreateWithImageInRect(cgImage, rect) ?: return@withContext null
        val cropped = try {
            UIImage.imageWithCGImage(cgImage = croppedImage)
        } finally {
            CGImageRelease(croppedImage)
        }
        val name = NSUUID().UUIDString
        val full = UIImageJPEGRepresentation(image = cropped, compressionQuality = JPEG_QUALITY) ?: return@withContext null
        val thumbnail = UIImageJPEGRepresentation(image = cropped.scaledToEdge(edge = PHOTO_THUMBNAIL_EDGE), compressionQuality = JPEG_QUALITY)
            ?: return@withContext null
        if (!full.writeToFile(path = path(fileName = name, isThumbnail = false), atomically = true)) return@withContext null
        thumbnail.writeToFile(path = path(fileName = name, isThumbnail = true), atomically = true)
        name
    }

    override suspend fun readCutout(fileName: String): ByteArray? = withContext(context = Dispatchers.Default) {
        val cutoutPath = "$folder/$fileName$CUTOUT_SUFFIX.png"
        NSData.dataWithContentsOfFile(path = cutoutPath)?.let { return@withContext it.toByteArray() }
        val image = UIImage.imageWithContentsOfFile(path = path(fileName = fileName, isThumbnail = false)) ?: return@withContext null
        val png = cutOutPerson(image = image) ?: return@withContext null
        png.writeToFile(path = cutoutPath, atomically = true)
        png.toByteArray()
    }

    override suspend fun delete(fileNames: Collection<String>) = withContext(context = Dispatchers.Default) {
        fileNames.forEach { name ->
            manager.removeItemAtPath(path = path(fileName = name, isThumbnail = false), error = null)
            manager.removeItemAtPath(path = path(fileName = name, isThumbnail = true), error = null)
            manager.removeItemAtPath(path = "$folder/$name$CUTOUT_SUFFIX.png", error = null)
        }
    }

    override suspend fun fileNames(): Set<String> = withContext(context = Dispatchers.Default) {
        manager.contentsOfDirectoryAtPath(path = folder, error = null).orEmpty()
            .filterIsInstance<String>()
            .map { it.substringBeforeLast(delimiter = ".") }
            .filterNot { it.endsWith(suffix = THUMBNAIL_SUFFIX) || it.endsWith(suffix = CUTOUT_SUFFIX) }
            .toSet()
    }

    private fun path(fileName: String, isThumbnail: Boolean) =
        "$folder/$fileName${if (isThumbnail) THUMBNAIL_SUFFIX else ""}.jpg"
}

/** Keeps only the person, with everything else transparent, as a PNG; null when there's no one. */
private fun cutOutPerson(image: UIImage): NSData? {
    val cgImage = image.CGImage ?: return null
    val request = VNGeneratePersonSegmentationRequest().apply {
        qualityLevel = VNGeneratePersonSegmentationRequestQualityLevelAccurate
        outputPixelFormat = kCVPixelFormatType_OneComponent8
    }
    val handler = VNImageRequestHandler(cGImage = cgImage, options = emptyMap<Any?, Any>())
    if (!handler.performRequests(requests = listOf(request), error = null)) return null
    val mask = (request.results?.firstOrNull() as? VNPixelBufferObservation)?.pixelBuffer ?: return null
    if (personShare(mask = mask) < MIN_PERSON_SHARE) return null
    val photo = CIImage(cGImage = cgImage)
    val (photoWidth, photoHeight) = photo.extent.useContents { size.width to size.height }
    val maskImage = CIImage(cVPixelBuffer = mask)
    val (maskWidth, maskHeight) = maskImage.extent.useContents { size.width to size.height }
    val scaledMask = maskImage.imageByApplyingTransform(
        matrix = CGAffineTransformMakeScale(sx = photoWidth / maskWidth, sy = photoHeight / maskHeight)
    )
    val blend = CIFilter.filterWithName(name = "CIBlendWithMask") ?: return null
    blend.setValue(photo, forKey = "inputImage")
    blend.setValue(CIImage.emptyImage(), forKey = "inputBackgroundImage")
    blend.setValue(scaledMask, forKey = "inputMaskImage")
    val output = blend.outputImage ?: return null
    val rendered = CIContext().createCGImage(image = output, fromRect = photo.extent) ?: return null
    return try {
        UIImagePNGRepresentation(image = UIImage.imageWithCGImage(cgImage = rendered))
    } finally {
        CGImageRelease(rendered)
    }
}

/** How much of the mask is person, from a sample of its pixels. */
private fun personShare(mask: CVPixelBufferRef): Double {
    CVPixelBufferLockBaseAddress(mask, kCVPixelBufferLock_ReadOnly)
    try {
        val base = CVPixelBufferGetBaseAddress(mask)?.reinterpret<UByteVar>() ?: return 0.0
        val width = CVPixelBufferGetWidth(mask).toInt()
        val height = CVPixelBufferGetHeight(mask).toInt()
        val rowBytes = CVPixelBufferGetBytesPerRow(mask).toInt()
        var person = 0
        var sampled = 0
        for (y in 0 until height step 4) {
            for (x in 0 until width step 4) {
                sampled++
                if (base[y * rowBytes + x].toInt() >= 128) person++
            }
        }
        return if (sampled == 0) 0.0 else person.toDouble() / sampled
    } finally {
        CVPixelBufferUnlockBaseAddress(mask, kCVPixelBufferLock_ReadOnly)
    }
}

/** Redraws the image upright and no bigger than [edge] pixels on its long side. */
private fun UIImage.scaledToEdge(edge: Int): UIImage {
    val (width, height) = size.useContents { width to height }
    val scale = min(1.0, edge / max(width, height))
    val targetWidth = width * scale
    val targetHeight = height * scale
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { this.scale = 1.0 }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width = targetWidth, height = targetHeight), format = format)
    return renderer.imageWithActions { _ ->
        drawInRect(rect = CGRectMake(x = 0.0, y = 0.0, width = targetWidth, height = targetHeight))
    }
}

private fun ByteArray.toNSData(): NSData = if (isEmpty()) {
    NSData()
} else {
    usePinned { NSData.create(bytes = it.addressOf(index = 0), length = size.toULong()) }
}

private fun NSData.toByteArray(): ByteArray {
    val length = length.toInt()
    if (length == 0) return ByteArray(size = 0)
    return ByteArray(size = length).apply { usePinned { memcpy(it.addressOf(index = 0), bytes, this@toByteArray.length) } }
}

package com.example.dublintest.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dublintest.ml.ObjectDetectionHelper
import com.example.dublintest.databinding.FragmentCameraBinding
import com.example.dublintest.stickers.model.StickerAssetMap
import com.example.dublintest.stickers.ui.StickerOverlayManager
import com.example.dublintest.ui.profile.ProfileType
import com.example.tripi.stickers.ui.StickerPlacementManager
import com.example.dublintest.utils.BitmapUtils.rotateBitmap
import com.example.dublintest.utils.BitmapUtils.resizeWithAspectRatioAndPadding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.dublintest.ui.profile.ProfileViewModel

import androidx.fragment.app.activityViewModels
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore


class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var objectDetectorHelper: ObjectDetectionHelper
    private lateinit var stickerOverlayManager: StickerOverlayManager
    private lateinit var stickerPlacementManager: StickerPlacementManager


    @Volatile
    private var lastProcessedTimestampMs: Long = 0L
//    private val frameProcessingIntervalMs: Long = 200L
private val frameProcessingIntervalMs: Long = 200L
    private val modelInputSize = 320
    @Volatile
    private var skipFrames = 2

    private val profileViewModel: ProfileViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()
        objectDetectorHelper = ObjectDetectionHelper(requireContext())
//        StickerRepository.init(requireContext().applicationContext)


        stickerOverlayManager = StickerOverlayManager(
            container = binding.overlayContainer,
            appContext = requireContext(),
            konfettiView = binding.konfettiView,
            takePhotoButton = binding.takePhotoButton,
            coroutineScope = viewLifecycleOwner.lifecycleScope
        )

        stickerPlacementManager = StickerPlacementManager(binding.overlayContainer, stickerOverlayManager)


//        val assetMap = StickerAssetMap.loadFromJson(requireContext())
//        StickerManager.loadFromAssets(requireContext(), assetMap)

        StickerAssetMap.loadStickerData(requireContext())

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {

                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val currentTimeMs = System.currentTimeMillis()
                        if (currentTimeMs - lastProcessedTimestampMs >= frameProcessingIntervalMs) {
                            lastProcessedTimestampMs = currentTimeMs
                            processImageProxy(imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // First, build your analyzer
//            val imageAnalyzer = ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build()
//                .also { analysis ->
//                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
//                        try {
//                            if (skipFrames > 0) {
//                                skipFrames--
//                                Log.d(TAG, "Skipping warm-up frame")
//                                return@setAnalyzer  // Don't process, just return
//                            }
//
//                            val currentTimeMs = System.currentTimeMillis()
//                            if (currentTimeMs - lastProcessedTimestampMs >= frameProcessingIntervalMs) {
//                                lastProcessedTimestampMs = currentTimeMs
//                                processImageProxy(imageProxy)
//                            }
//                        } catch (e: Exception) {
//                            Log.e(TAG, "Analyzer error", e)
//                        } finally {
//                            imageProxy.close()
//                        }
//                    }
//
//                }
//
//            // THEN build preview (after analyzer)
//            val preview = Preview.Builder().build().also {
//                it.surfaceProvider = binding.viewFinder.surfaceProvider
//            }
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    this,
//                    cameraSelector,
//                    preview,
//                    imageAnalyzer
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "Use case binding failed", e)
//            }
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }


    private fun processImageProxy(imageProxy: ImageProxy) {
        try {

            val bitmap =
                YuvtoRGBConverter.convert(imageProxy)


            val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
            val resized = resizeWithAspectRatioAndPadding(rotated, modelInputSize)
            val results = objectDetectorHelper.detectFormatted(resized)

            Log.d(TAG, "Detected ${results.size} objects")


                if (isAdded && context != null) {
                    requireActivity().runOnUiThread {
                        if (!isAdded || context == null) return@runOnUiThread

                        stickerOverlayManager.clear()
                        val currentProfile = profileViewModel.selectedProfile.value ?: ProfileType.WOLF
                        stickerPlacementManager.showStickers(results, resized.width, resized.height, currentProfile)
                    }



            }

        } catch (e: Exception) {
            Log.e(TAG, "Image processing failed", e)
        } finally {
            // ✅ ALWAYS close, no matter what happens

                imageProxy.close()

        }
    }

//    private fun collectToFirestore(label: String) {
//        val info = StickerAssetMap.getStickerInfo(label) ?: return
//        val db = Firebase.firestore
//
//        info.domains.forEach { domain ->
//            db.collection("teams")
//                .document("current")
//                .update("progress.$domain", FieldValue.arrayUnion(label))
//                .addOnSuccessListener {
//                    Log.d("CameraFragment", "✅ Added '$label' to $domain")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("CameraFragment", "❌ Failed to add '$label' to $domain", e)
//                }
//        }
//    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
}

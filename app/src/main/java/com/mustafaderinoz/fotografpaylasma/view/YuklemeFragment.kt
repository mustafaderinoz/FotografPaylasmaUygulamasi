package com.mustafaderinoz.fotografpaylasma.view

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mustafaderinoz.fotografpaylasma.databinding.FragmentYuklemeBinding
import java.io.ByteArrayOutputStream
import android.util.Base64
import androidx.navigation.Navigation
import com.mustafaderinoz.fotografpaylasma.R

class YuklemeFragment : Fragment() {
    private var _binding: FragmentYuklemeBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        registerLounchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.yukleButton.setOnClickListener { yukleTiklandi(it) }
        binding.imageView.setOnClickListener { gorselSec(it) }
    }

    fun yukleTiklandi(view: View) {
        if (auth.currentUser != null) {
            if (secilenBitmap != null) {
                // Bitmap'i base64'e çevir ve 1MB altına sıkıştır
                val base64String = bitmapToBase64(secilenBitmap!!)

                if (base64String != null) {
                    val postMap = HashMap<String, Any>()
                    postMap.put("email", auth.currentUser!!.email.toString())
                    postMap.put("comment", binding.commentText.text.toString())
                    postMap.put("base64", base64String)
                    postMap.put("date", Timestamp.now())

                    // Firestore'a kaydet
                    db.collection("Posts").add(postMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Gönderi başarıyla yüklendi!", Toast.LENGTH_SHORT).show()
                            // Formu temizle
                            binding.commentText.text?.clear()
                            binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                            secilenBitmap = null
                            secilenGorsel = null
                            val action=YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Hata: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Görsel işlenirken hata oluştu", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Lütfen bir görsel seçin", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Giriş yapmanız gerekiyor", Toast.LENGTH_SHORT).show()
        }
    }

    // Bitmap'i base64'e çevir ve 1MB altına sıkıştır
    private fun bitmapToBase64(bitmap: Bitmap): String? {
        return try {
            var quality = 100
            var base64String: String
            var outputStream: ByteArrayOutputStream

            do {
                outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val byteArray = outputStream.toByteArray()
                base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

                // Base64 string boyutunu kontrol et (1MB = 1048576 bytes)
                val sizeInBytes = base64String.length

                if (sizeInBytes > 1048576) {
                    quality -= 5 // Kaliteyi düşür
                } else {
                    break
                }
            } while (quality > 10) // Minimum kalite %10

            if (base64String.length > 1048576) {
                Toast.makeText(requireContext(), "Görsel çok büyük, lütfen daha küçük bir görsel seçin", Toast.LENGTH_LONG).show()
                null
            } else {
                base64String
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun gorselSec(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Galeriye gitmek için izin vermeniz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",
                        View.OnClickListener {
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Galeriye gitmek için izin vermeniz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",
                        View.OnClickListener {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    fun registerLounchers() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorsel = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val intenToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch((intenToGallery))
            } else {
                Toast.makeText(requireContext(), "İzin reddetildi, izne ihtiyaç var", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
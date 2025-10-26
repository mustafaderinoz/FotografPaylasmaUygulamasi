package com.mustafaderinoz.fotografpaylasma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mustafaderinoz.fotografpaylasma.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private var isFabOpen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ana FAB tıklaması

        binding.fabMenu.setOnClickListener { menu_tiklandi(it) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





    fun menu_tiklandi(view: View){
        binding.fabMenu.setOnClickListener {
            if (isFabOpen) {
                closeFabMenu()
            } else {
                openFabMenu()
            }
        }

        // Alt menü tıklamaları
        binding.menuCikisYap.setOnClickListener {
            Toast.makeText(requireContext(), "Çıkış butonuna tıklandı", Toast.LENGTH_SHORT).show()
        }

        binding.menuEklemeYap.setOnClickListener {
            Toast.makeText(requireContext(), "Ekle butonuna tıklandı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFabMenu() {
        isFabOpen = true

        // Menüleri görünür yap ve animasyonla aç
        binding.menuCikisYap.visibility = View.VISIBLE
        binding.menuEklemeYap.visibility = View.VISIBLE

        binding.menuCikisYap.animate()
            .translationY(-180f)
            .alpha(1f)
            .setDuration(200)
            .start()

        binding.menuEklemeYap.animate()
            .translationY(-360f)
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    private fun closeFabMenu() {
        isFabOpen = false

        // Menüleri geri gizle
        binding.menuCikisYap.animate()
            .translationY(0f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction { binding.menuCikisYap.visibility = View.GONE }
            .start()

        binding.menuEklemeYap.animate()
            .translationY(0f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction { binding.menuEklemeYap.visibility = View.GONE }
            .start()
    }


}

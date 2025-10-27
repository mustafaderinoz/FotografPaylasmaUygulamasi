package com.mustafaderinoz.fotografpaylasma.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.mustafaderinoz.fotografpaylasma.adapter.PostAdapter
import com.mustafaderinoz.fotografpaylasma.databinding.FragmentFeedBinding
import com.mustafaderinoz.fotografpaylasma.model.Post

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private var isFabOpen = false
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    val postList:ArrayList<Post> =arrayListOf()
    private var adapter:PostAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
        // Ana FAB tıklaması
        binding.fabMenu.setOnClickListener { menu_tiklandi(it) }

        verileriAl()

        adapter=PostAdapter(postList)
        binding.feedRecylerView.layoutManager=LinearLayoutManager(requireContext())
        binding.feedRecylerView.adapter=adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun verileriAl(){
        db.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if(error!=null) println(error.localizedMessage)
            else {
                if(value != null && !value.isEmpty){
                        postList.clear()
                        val documents=value.documents

                        for (document in documents ){
                            val email=document.get("email") as String
                            val comment=document.get("comment") as String
                            val base64=document.get("base64") as String

                            val post= Post(email,comment,base64)
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
                }

            }
        }
    }

    fun menu_tiklandi(view: View) {
        if (isFabOpen) closeFabMenu()
        else openFabMenu()

        // Alt menü tıklamaları
        binding.menuCikisYap.setOnClickListener {
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToKullaniciFragment()
            Navigation.findNavController(view).navigate(action)
            Toast.makeText(requireContext(), "Çıkış yapıldı", Toast.LENGTH_SHORT).show()

        }

        binding.menuEklemeYap.setOnClickListener {
            isFabOpen = false
            val action=FeedFragmentDirections.actionFeedFragmentToYuklemeFragment()
            Navigation.findNavController(requireView()).navigate(action)

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

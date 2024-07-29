package com.example.helthcareappgroup11.user.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helthcareappgroup11.R
import com.example.helthcareappgroup11.models.Doctors
import com.example.helthcareappgroup11.user.adapters.DoctorItemAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserHomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var username: TextView
    private lateinit var auth: FirebaseAuth

    private lateinit var rView: RecyclerView

    private lateinit var doctorAdapterForUser: DoctorItemAdapter
    private val doctorList = mutableListOf<Doctors>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)

        username = view.findViewById(R.id.user_name)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        rView = view.findViewById(R.id.doctor_recycler_view)
        rView.layoutManager = LinearLayoutManager(context)

        database = FirebaseDatabase.getInstance().reference.child("doctors")
        auth = FirebaseAuth.getInstance()

        val options = FirebaseRecyclerOptions.Builder<Doctors>()
            .setQuery(database, Doctors::class.java)
            .build()

        doctorAdapterForUser = DoctorItemAdapter(options)
        rView.adapter = doctorAdapterForUser

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("UserHomeFragment", "Retrieving profile for userId: $userId")

            database.child("clients").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pUusername = snapshot.child("username").getValue(String::class.java)
                    if (pUusername != null) {
                        username.text = pUusername
                        Log.d("UserHomeFragment", "Username retrieved: $username")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to retrieve username",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("UserHomeFragment", "Username is null for userId: $userId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to retrieve data", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "No authenticated user", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        doctorAdapterForUser.startListening()
    }

    override fun onStop() {
        super.onStop()
        doctorAdapterForUser.stopListening()
    }

    private fun fetchDoctors() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                doctorList.clear()
                for (doctorSnapshot in snapshot.children) {
                    val doctor = doctorSnapshot.getValue(Doctors::class.java)
                    if (doctor != null) {
                        doctorList.add(doctor)
                    }
                }
                doctorAdapterForUser.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragmentUser", "Failed to load doctors", error.toException())
            }
        })
    }
}
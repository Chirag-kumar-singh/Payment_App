package com.example.payment_app.Activity

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.payment_app.Adapters.ContactsAdapter
import com.example.payment_app.Models.Contact
import com.example.payment_app.R

class PhoneNumberActivity : AppCompatActivity() {

    private lateinit var contactsAdapter: ContactsAdapter
    private var contactList: MutableList<Contact> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val searchViewContacts = findViewById<SearchView>(R.id.searchViewContacts)

        recyclerView.layoutManager = LinearLayoutManager(this)
        contactsAdapter = ContactsAdapter(contactList)
        recyclerView.adapter = contactsAdapter

        // Request contact permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            requestContactsPermission()
        }

        searchViewContacts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                contactsAdapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun requestContactsPermission() {
        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadContacts()
        } else {
            Toast.makeText(this, "Contact permission is required to display contacts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContacts() {
        val filterName = intent.getStringExtra("FILTER_NAME")?.trim()
        val contentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.let {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            if (nameIndex != -1 && phoneIndex != -1) {
                while (it.moveToNext()) {
                    val name = it.getString(nameIndex)
                    val phoneNumber = it.getString(phoneIndex)

                    // Apply filter if filterName is provided
                    if (filterName == null || name.contains(filterName, ignoreCase = true)) {
                        contactList.add(Contact(name, phoneNumber))
                    }
                }
            }

            it.close()
        }

        // Update the adapter and apply the filter if needed
        contactsAdapter.notifyDataSetChanged()

        filterName?.let {
            contactsAdapter.filter.filter(it)
        }
    }
}

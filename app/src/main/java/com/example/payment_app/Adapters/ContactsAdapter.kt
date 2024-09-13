package com.example.payment_app.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.payment_app.Activity.Payment_chat_screen
import com.example.payment_app.Models.Contact
import com.example.payment_app.R

class ContactsAdapter(private var contacts: MutableList<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>(), Filterable {

    private var contactsFiltered: MutableList<Contact> = contacts

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactsFiltered[position]
        holder.name.text = contact.name
        holder.phoneNumber.text = contact.phoneNumber

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, Payment_chat_screen::class.java).apply {
                putExtra("CONTACT_NAME", contact.name)
                putExtra("CONTACT_NUMBER", contact.phoneNumber)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return contactsFiltered.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query = charSequence?.toString()?.lowercase()

                contactsFiltered = if (query.isNullOrEmpty()) {
                    contacts
                } else {
                    contacts.filter {
                        it.name.lowercase().contains(query)
                    }.toMutableList()
                }

                val filterResults = FilterResults()
                filterResults.values = contactsFiltered
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                contactsFiltered = filterResults?.values as MutableList<Contact>
                notifyDataSetChanged()
            }
        }
    }

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.contact_name)
        val phoneNumber: TextView = view.findViewById(R.id.contact_phone_number)
    }
}

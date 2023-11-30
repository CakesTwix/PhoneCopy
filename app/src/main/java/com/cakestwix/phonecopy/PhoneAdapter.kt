package com.cakestwix.phonecopy

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cakestwix.phonecopy.databinding.ItemPhoneBinding


data class PhoneModel(
    val id: Int, // Slot Number
    val name: String, // SIM Provider
    val number: String, // Number, lol
)

class PhoneAdapter : RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder>() {

    var data: List<PhoneModel> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class PhoneViewHolder(val binding: ItemPhoneBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhoneAdapter.PhoneViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPhoneBinding.inflate(inflater, parent, false)

        return PhoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhoneAdapter.PhoneViewHolder, position: Int) {
        val sim = data[position]
        val context = holder.itemView.context

        // Copy button
        holder.binding.copy.setOnClickListener {
            context.copyToClipboard(sim.number)
        }

        // Share button
        holder.binding.share.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sim.number)
                type = "text/plain"
            }

            try {
                context.startActivity(sendIntent)
            } catch (e: ActivityNotFoundException) {
                // Define what your app should do if no activity can handle the intent.
            }
        }

        with(holder.binding) {
            simVendor.text = "${sim.name} (${sim.id})"
            simNumber.text = sim.number
        }
    }

    override fun getItemCount(): Int = data.size

    fun Context.copyToClipboard(text: CharSequence){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("clip",text)
        clipboard.setPrimaryClip(clip)
    }
}
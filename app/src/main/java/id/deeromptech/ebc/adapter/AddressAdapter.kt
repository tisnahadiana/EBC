package id.deeromptech.ebc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.databinding.AddressRvItemBinding
import id.deeromptech.ebc.util.Constants.SELECT_ADDRESS_FLAG

class AddressAdapter(
    val ADDRESS_CLICK_FLAG : String
) :
    RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder (val binding: AddressRvItemBinding) :
    ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Address>(){
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem.addressTitle == newItem.addressTitle
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            AddressRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    var selectedAddress = -1

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = differ.currentList[position]

        if(ADDRESS_CLICK_FLAG == SELECT_ADDRESS_FLAG) {
            if (selectedAddress == position) {
                holder.binding.buttonAddress.apply {
                    setBackgroundColor(resources.getColor(R.color.navi))
                    text = address.addressTitle
                    setTextColor(resources.getColor(R.color.white))
                }
            } else {
                holder.binding.buttonAddress.apply {
                    setBackgroundResource(R.drawable.unselected_button_background)
                    text = address.addressTitle
                    setTextColor(resources.getColor(R.color.black))
                }
            }

            holder.binding.buttonAddress.setOnClickListener {

                if (selectedAddress >= 0)
                    notifyItemChanged(selectedAddress)
                selectedAddress = holder.adapterPosition
                notifyItemChanged(selectedAddress)
                onBtnClick?.invoke(address)
            }

        }else {
            holder.binding.buttonAddress.apply {
                setBackgroundResource(R.drawable.unselected_button_background)
                text = address.addressTitle
                setTextColor(resources.getColor(R.color.black))
            }

            holder.binding.buttonAddress.setOnClickListener {
                onBtnClick?.invoke(address)
            }
        }

    }

    var onBtnClick : ((Address)->Unit)?=null
}
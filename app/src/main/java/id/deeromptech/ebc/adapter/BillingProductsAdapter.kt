package id.deeromptech.ebc.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.databinding.AddressRvItemBinding
import id.deeromptech.ebc.databinding.BillingProductsRvItemBinding
import id.deeromptech.ebc.helper.getProductPrice
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class BillingProductsAdapter : RecyclerView.Adapter<BillingProductsAdapter.BillingProductViewHolder>() {

    inner class BillingProductViewHolder (val binding: BillingProductsRvItemBinding) :
        RecyclerView.ViewHolder(binding.root){

        private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        fun bind(billingProduct: Cart){
            binding.apply {
                Glide.with(itemView).load(billingProduct.product.images[0]).into(imageCartProduct)
                tvProductCartName.text = billingProduct.quantity.toString()

                val priceAfterPercentage = billingProduct.product.offerPercentage.getProductPrice(billingProduct.product.price)
                tvProductCartName.text = "$ ${String.format("%.2f", priceAfterPercentage)}"

                val formattedPrice = "Rp. ${decimalFormat.format(billingProduct.product.price)}"
                tvProductCartName.text = formattedPrice
            }
        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Cart>(){
        override fun areItemsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem.product == newItem.product
        }

        override fun areContentsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductViewHolder {
        return BillingProductViewHolder(
            BillingProductsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BillingProductViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]
        holder.bind(billingProduct)
    }

    var onClick:((Address) -> Unit)? = null
}
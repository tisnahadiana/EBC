package id.deeromptech.ebc.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.databinding.BillingProductsSellerItemBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class BillingProductsSellerAdapter : RecyclerView.Adapter<BillingProductsSellerAdapter.BillingProductSellerViewHolder>() {

    inner class BillingProductSellerViewHolder (val binding: BillingProductsSellerItemBinding) :
        RecyclerView.ViewHolder(binding.root){

        private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        @SuppressLint("SetTextI18n")
        fun bind(billingProduct: Cart){
            binding.apply {
                Glide.with(itemView).load(billingProduct.product.images[0]).into(imageCartProduct)
                tvBillingProductQuantity.text = billingProduct.quantity.toString()

                val discountedPrice = billingProduct.product.price - (billingProduct.product.price * (billingProduct.product.offerPercentage!! / 100))
                val formattedPrice = "Rp. ${decimalFormat.format(discountedPrice)}"
                tvProductCartPrice.text = formattedPrice

                tvProductCartName.text = billingProduct.product.name
                tvSellerBilling.text = "Store : ${billingProduct.product.seller}"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductSellerViewHolder {
        return BillingProductSellerViewHolder(
            BillingProductsSellerItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BillingProductSellerViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]
        holder.bind(billingProduct)

        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClickProduct?.invoke(billingProduct.product)
        }
    }

    var onClick:((Address) -> Unit)? = null
    var onClickProduct: ((Product) -> Unit)? = null
}
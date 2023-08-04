package id.deeromptech.ebc.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Address
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.databinding.AddressRvItemBinding
import id.deeromptech.ebc.databinding.BillingProductsRvItemBinding
import id.deeromptech.ebc.databinding.ItemCartProductBinding
import id.deeromptech.ebc.helper.getProductPrice
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class BillingProductsAdapter : RecyclerView.Adapter<BillingProductsAdapter.BillingProductViewHolder>() {

    inner class BillingProductViewHolder (val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Cart>(){
        override fun areItemsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductViewHolder {
        return BillingProductViewHolder(
            ItemCartProductBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BillingProductViewHolder, position: Int) {
        val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))
        val product = differ.currentList[position]
        holder.binding.apply {
            btnPlus.visibility = View.GONE
            btnMinus.visibility = View.GONE
            line.visibility = View.GONE
            tvQuantity.visibility = View.GONE
//            cardView.setCardBackgroundColor(R.color.g_white)
            imageCartProduct.scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(holder.itemView).load(product.image).into(imageCartProduct)
            tvcartProductName.text = product.name

            val formattedPrice = if (product.newPrice != null && product.newPrice.isNotEmpty()) {
                // Format the newPrice if it is available
                decimalFormat.format(product.newPrice.toDouble())
            } else {
                // Format the original price
                decimalFormat.format(product.price.toDouble())
            }

            tvcartProductPrice.text = "Rp. $formattedPrice"
        }
    }

    var onPlusClick: ((Cart) -> Unit)? = null
    var onMinusesClick: ((Cart) -> Unit)? = null
    var onItemClick: ((Cart) -> Unit)? = null
}
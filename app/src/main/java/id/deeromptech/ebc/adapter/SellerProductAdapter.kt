package id.deeromptech.ebc.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.databinding.ItemSellerProductBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class SellerProductAdapter : RecyclerView.Adapter<SellerProductAdapter.SellerProductViewHolder>() {

    inner class SellerProductViewHolder(val binding: ItemSellerProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val decimalFormat =
            DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imgBestDeal)


                tvOldPrice.paintFlags= Paint.STRIKE_THRU_TEXT_FLAG
                if (product.offerPercentage == null)
                    tvNewPrice.visibility = View.INVISIBLE
                tvDealProductName.text = product.name

                val formattedOldPrice = "Rp. ${decimalFormat.format(product.price)}"
                tvOldPrice.text = formattedOldPrice

                val discountedPrice = product.price - (product.price * (product.offerPercentage!! / 100))
                val formattedPrice = "Rp. ${decimalFormat.format(discountedPrice)}"
                tvNewPrice.text = formattedPrice
            }

        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerProductViewHolder {
        return SellerProductViewHolder(
            ItemSellerProductBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SellerProductViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }

        holder.binding.btnDeleteProduct.setOnClickListener {
            onDelete?.invoke(product)
        }

        holder.binding.btnEditProduct.setOnClickListener {
            onUpdate?.invoke(product)
        }
    }

    var onClick: ((Product) -> Unit)? = null
    var onDelete: ((Product) -> Unit)? = null
    var onUpdate: ((Product) -> Unit)? = null
}
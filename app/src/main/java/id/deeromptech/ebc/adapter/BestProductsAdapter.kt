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
import id.deeromptech.ebc.databinding.ProductRvItemBinding
import id.deeromptech.ebc.util.Constants.IMAGES
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class BestProductsAdapter: RecyclerView.Adapter<BestProductsAdapter.BestProductsViewHolder>() {

    inner class BestProductsViewHolder(private val binding: ProductRvItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        fun bind(product: Product){
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imgProduct)
                product.offerPercentage?.let {
                    val remainingPrivePercentage = 1f - it
                    val priceAfterOffer = remainingPrivePercentage * product.price
                    val oldPrice = priceAfterOffer + 10000
                    val formattedPriceAfterOffer = "$ ${decimalFormat.format(priceAfterOffer)}"
                    val formattedOldPrice = "Rp. ${decimalFormat.format(oldPrice)}"
                    tvNewPrice.text = formattedPriceAfterOffer
                    tvPrice.text = formattedOldPrice
                    tvPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
                if (product.offerPercentage == null)
                    tvNewPrice.visibility = View.INVISIBLE
                tvName.text = product.name

                val formattedPrice = "Rp. ${decimalFormat.format(product.price)}"
                tvNewPrice.text = formattedPrice

            }
        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestProductsViewHolder {
        return BestProductsViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    var onClick:((Product) -> Unit)? = null
}
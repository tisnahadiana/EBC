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
import id.deeromptech.ebc.databinding.BestDealsRvItemBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class SearchRecyclerAdapter : RecyclerView.Adapter<SearchRecyclerAdapter.SearchViewHolder>() {
    var onItemClick: ((Product) -> Unit)? = null


    inner class SearchViewHolder(val binding: BestDealsRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val decimalFormat =
            DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imgBestDeal)

                if (product.offerPercentage == null) {
                    tvNewPrice.visibility = View.GONE
                    tvDealProductName.text = product.name
                    val formattedOldPrice = "Rp. ${decimalFormat.format(product.price)}"
                    tvOldPrice.text = formattedOldPrice
                } else {
                    tvDealProductName.text = product.name

                    val formattedOldPrice = "Rp. ${decimalFormat.format(product.price)}"
                    tvOldPrice.text = formattedOldPrice

                    val discountedPrice =
                        product.price - (product.price * (product.offerPercentage!! / 100))
                    val formattedPrice = "Rp. ${decimalFormat.format(discountedPrice)}"
                    tvNewPrice.text = formattedPrice
                    tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            BestDealsRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(differ.currentList[position])
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
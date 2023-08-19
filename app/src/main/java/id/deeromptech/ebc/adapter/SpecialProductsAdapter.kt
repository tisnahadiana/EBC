package id.deeromptech.ebc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.databinding.SpecialRvItemBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class SpecialProductsAdapter: RecyclerView.Adapter<SpecialProductsAdapter.SpecialProductsViewHolder>() {

    inner class SpecialProductsViewHolder(private val binding: SpecialRvItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        fun bind(product: Product){
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imgSpecialRv)
                tvSpecialName.text = product.name

                val discountedPrice = product.price - (product.price * (product.offerPercentage!! / 100))
                val formattedPrice = "Rp. ${decimalFormat.format(discountedPrice)}"
                tvSpecialPrice.text = formattedPrice
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialProductsViewHolder {
        return SpecialProductsViewHolder(
            SpecialRvItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SpecialProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    var onClick:((Product) -> Unit)? = null
}
package id.deeromptech.ebc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.databinding.SpecialRvItemBinding
import id.deeromptech.ebc.util.Constants.IMAGES
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class SpecialProductsAdapter: RecyclerView.Adapter<SpecialProductsAdapter.SpecialProductsViewHolder>() {

    inner class SpecialProductsViewHolder(val binding: SpecialRvItemBinding):
        RecyclerView.ViewHolder(binding.root)

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
        val images = product.images
        val image = (images!![IMAGES] as List<String>)[0]
        val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        holder.binding.apply {
            Glide.with(holder.itemView).load(image).into(imgSpecialRv)

            val formattedPrice = "Rp. ${decimalFormat.format(product.price)}"
            tvSpecialPrice.text = formattedPrice
            tvSpecialName.text = product.title
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }


        holder.binding.btnAddToCart.setOnClickListener {
            onAddToCartClick?.invoke(product)
        }
    }

    var onItemClick: ((Product) -> Unit)? = null

    var onAddToCartClick: ((Product) -> Unit)? = null
}
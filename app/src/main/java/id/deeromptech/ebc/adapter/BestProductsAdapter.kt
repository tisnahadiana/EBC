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

    var onItemClick: ((Product) -> Unit)? = null
    inner class BestProductsViewHolder(val binding: ProductRvItemBinding):
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
        val image = (product.images?.get(IMAGES) as List<String>)[0]
        val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))
        holder.binding.apply {
            Glide.with(holder.itemView).load(image).into(imgProduct)
            tvName.text = product.title

            val formattedTvPrice = "Rp. ${decimalFormat.format(product.price)}"
            tvPrice.text = formattedTvPrice

            tvNewPrice.visibility = View.GONE
        }

        product.newPrice?.let {
            if (product.newPrice.isNotEmpty() && product.newPrice != "0") {
                holder.binding.apply {
                    tvPrice.paintFlags = tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    val formattedNewPrice = "Rp. ${decimalFormat.format(product.newPrice)}"
                    tvNewPrice.text = formattedNewPrice
                    tvNewPrice.visibility = View.VISIBLE
                }
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(differ.currentList[position])
        }
    }
}
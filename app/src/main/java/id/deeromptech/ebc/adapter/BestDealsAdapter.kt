package id.deeromptech.ebc.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.data.local.Product
import id.deeromptech.ebc.databinding.BestDealsRvItemBinding
import id.deeromptech.ebc.util.Constants.IMAGES
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class BestDealsAdapter : RecyclerView.Adapter<BestDealsAdapter.BestDealsViewHolder>() {

    inner class BestDealsViewHolder(val binding: BestDealsRvItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestDealsViewHolder {
        return BestDealsViewHolder(
            BestDealsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestDealsViewHolder, position: Int) {
        val product = differ.currentList[position]
        val image = (product.images?.get(IMAGES) as List<String>)[0]
        val decimalFormat =
            DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        holder.binding.apply {
            Glide.with(holder.itemView).load(image).into(imgBestDeal)
            tvDealProductName.text = product.title

            val formattedNewPrice = "Rp. ${decimalFormat.format(product.newPrice)}"
            tvNewPrice.text = formattedNewPrice

            val formattedOldPrice = "Rp. ${decimalFormat.format(product.price)}"
            tvOldPrice.text = formattedOldPrice
            tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        holder.binding.btnSeeProduct.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }

    var onItemClick: ((Product) -> Unit)? = null
}
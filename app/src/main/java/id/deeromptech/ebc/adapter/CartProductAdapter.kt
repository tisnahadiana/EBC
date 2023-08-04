package id.deeromptech.ebc.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.databinding.ItemCartProductBinding
import id.deeromptech.ebc.helper.getProductPrice
import id.deeromptech.ebc.util.Constants.CART_FLAG
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class CartProductAdapter(
    private val itemFlag: String = CART_FLAG
): RecyclerView.Adapter<CartProductAdapter.CartProductsViewHolder>() {

    var onPlusClick: ((Cart) -> Unit)? = null
    var onMinusesClick: ((Cart) -> Unit)? = null
    var onItemClick: ((Cart) -> Unit)? = null

    inner class CartProductsViewHolder( val binding: ItemCartProductBinding):
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Cart>(){
        override fun areItemsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsViewHolder {
        return CartProductsViewHolder(
            ItemCartProductBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
        val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))
        val product = differ.currentList[position]
        holder.binding.apply {


            Glide.with(holder.itemView).load(product.image).into(imageCartProduct)
            tvcartProductName.text = product.name
            tvQuantity.text = product.quantity.toString()


            if (product.newPrice != null && product.newPrice.isNotEmpty() && product.newPrice != "0") {

                val formattedNewPrice = "Rp. ${decimalFormat.format(product.newPrice)}"
                tvcartProductPrice.text = formattedNewPrice
            } else {
                val formattedPrice = "Rp. ${decimalFormat.format(product.price)}"
                tvcartProductPrice.text = formattedPrice
            }

            if (itemFlag != CART_FLAG)
                holder.binding.apply {
                    btnPlus.visibility = View.INVISIBLE
                    btnMinus.visibility = View.INVISIBLE
                    tvQuantity.text = product.quantity.toString()
                }
            else {

                btnPlus.setOnClickListener {
                    onPlusClick!!.invoke(product)
                }

                btnMinus.setOnClickListener {
                    onMinusesClick!!.invoke(product)
                }


                holder.itemView.setOnClickListener {
                    onItemClick!!.invoke(product)
                }
            }
        }
    }
}
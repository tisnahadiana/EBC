package id.deeromptech.ebc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.deeromptech.ebc.data.local.Cart
import id.deeromptech.ebc.databinding.ItemCartProductBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class CartProductAdapter : RecyclerView.Adapter<CartProductAdapter.CartProductsViewHolder>() {

    inner class CartProductsViewHolder(val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val decimalFormat =
            DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))

        fun bind(cart: Cart) {
            binding.apply {
                Glide.with(itemView).load(cart.product.images[0]).into(imageCartProduct)
                tvcartProductName.text = cart.product.name
                tvQuantity.text = cart.quantity.toString()


                val discountedPrice =
                    cart.product.price - (cart.product.price * (cart.product.offerPercentage!! / 100))
                val formattedPrice = "Rp. ${decimalFormat.format(discountedPrice)}"
                tvcartProductPrice.text = formattedPrice

            }
        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Cart>() {
        override fun areItemsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: Cart, newItem: Cart): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

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
        val cart = differ.currentList[position]
        holder.bind(cart)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cart)
        }

        holder.binding.btnPlus.setOnClickListener {
            onPlusClick?.invoke(cart)
        }

        holder.binding.btnMinus.setOnClickListener {
            onMinusClick?.invoke(cart)
        }

        holder.binding.btnDeleteCart.setOnClickListener {
            onDeleteClick?.invoke(cart)
        }

        holder.binding.checkboxProduct.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedItems.add(cart)
            } else {
                checkedItems.remove(cart)
            }
            // Notify the listener in CartFragment about checked items change
            onCheckedItemsChanged?.invoke(checkedItems)
        }
        holder.binding.checkboxProduct.isChecked = checkedItems.contains(cart)

    }

    var onProductClick: ((Cart) -> Unit)? = null
    var onPlusClick: ((Cart) -> Unit)? = null
    var onMinusClick: ((Cart) -> Unit)? = null
    var onDeleteClick: ((Cart) -> Unit)? = null
    var onCheckedItemsChanged: ((Set<Cart>) -> Unit)? = null
    private val checkedItems = mutableSetOf<Cart>()
}
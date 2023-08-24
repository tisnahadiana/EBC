package id.deeromptech.ebc.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.data.local.OrderStatus
import id.deeromptech.ebc.data.local.getOrderStatus
import id.deeromptech.ebc.databinding.OrderItemBinding

class SellerOrderAdapter : RecyclerView.Adapter<SellerOrderAdapter.OrdersViewHolder>() {

    inner class OrdersViewHolder(val binding: OrderItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order){
            binding.apply {
                tvOrderId.text = order.orderId.toString()
                tvOrderDate.text = order.date
                val resources = itemView.resources

                if (order.orderStatus == "Canceled"){
                    btnDeleteOrder.visibility = View.VISIBLE
                }

                if (order.orderStatus == "Delivered"){
                    btnDeleteOrder.visibility = View.VISIBLE
                }

                val colorDrawable = when (order.orderStatus) {
                    "Ordered" -> {
                        ColorDrawable(resources.getColor(R.color.orang_yellow))
                    }
                    "Confirmed" ,"Shipped", "Delivered" -> {
                        ColorDrawable(resources.getColor(R.color.green))
                    }
                    else -> {
                        ColorDrawable(resources.getColor(R.color.red))
                    }
                }

                imageOrderState.setImageDrawable(colorDrawable)
            }
        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Order>(){
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.products == newItem.products
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(
            OrderItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.bind(order)

        holder.itemView.setOnClickListener {
            onClick?.invoke(order)
        }
        holder.binding.btnDeleteOrder.setOnClickListener {
            onDeleteClick?.invoke(order)
        }
    }

    var onClick:((Order) -> Unit)? = null
    var onDeleteClick:((Order) -> Unit)? = null
}
package id.deeromptech.ebc.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.deeromptech.ebc.R
import id.deeromptech.ebc.data.local.Order
import id.deeromptech.ebc.databinding.OrderItemBinding
import id.deeromptech.ebc.util.Constants.ORDER_CONFIRM_STATE
import id.deeromptech.ebc.util.Constants.ORDER_Delivered_STATE
import id.deeromptech.ebc.util.Constants.ORDER_PLACED_STATE
import id.deeromptech.ebc.util.Constants.ORDER_SHIPPED_STATE
import java.text.SimpleDateFormat

class AllOrdersAdapter : RecyclerView.Adapter<AllOrdersAdapter.OrdersViewHolder>() {

    inner class OrdersViewHolder(val binding: OrderItemBinding):
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Order>(){
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
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

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.binding.apply {
            val date = SimpleDateFormat("yyyy-MM-dd").format(order.date)
            tvOrderId.text = holder.itemView.context.resources.getText(R.string.order).toString()
                .plus(" #${order.id}")
            tvOrderDate.text = date

            when(order.state){
                ORDER_PLACED_STATE -> changeOrderStateColor(imageOrderState,imageOrderState.context.resources.getColor(R.color.orang_yellow))
                ORDER_CONFIRM_STATE -> changeOrderStateColor(imageOrderState,imageOrderState.context.resources.getColor(R.color.green))
                ORDER_SHIPPED_STATE -> changeOrderStateColor(imageOrderState,imageOrderState.context.resources.getColor(R.color.green))
                ORDER_Delivered_STATE -> changeOrderStateColor(imageOrderState,imageOrderState.context.resources.getColor(R.color.blue))
            }

        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(order)
        }
    }

    private fun changeOrderStateColor(imageView: ImageView, color:Int){
        imageView.imageTintList = ColorStateList.valueOf(color)
    }

    var onItemClick: ((Order) -> Unit)? = null
}
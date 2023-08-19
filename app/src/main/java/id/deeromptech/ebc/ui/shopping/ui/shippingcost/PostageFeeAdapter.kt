package id.deeromptech.ebc.ui.shopping.ui.shippingcost

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.deeromptech.ebc.data.model.CostPostageFee
import id.deeromptech.ebc.data.model.DiffUtilCost
import id.deeromptech.ebc.databinding.ItemCostBinding
import id.deeromptech.ebc.util.toRupiah

class PostageFeeAdapter : ListAdapter<CostPostageFee, PostageFeeAdapter.CostViewHolder>(DiffUtilCost()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CostViewHolder {
        val view = ItemCostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CostViewHolder, position: Int) {
        val item = getItem(position)
        holder.bindItem(item)
    }

    class CostViewHolder(private val binding: ItemCostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindItem(item: CostPostageFee) {
            binding.apply {
                codeTV.text = item.code
                serviceTV.text = item.service
                serviceDescriptionTV.text = item.description
                costValueTV.text = item.value?.toRupiah()
                estimationTV.text = item.etd
            }
        }
    }
}
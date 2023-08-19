package id.deeromptech.ebc.data.model

import androidx.recyclerview.widget.DiffUtil

class DiffUtilCost : DiffUtil.ItemCallback<CostPostageFee>() {
    override fun areItemsTheSame(oldItem: CostPostageFee, newItem: CostPostageFee): Boolean {
        return newItem.service == newItem.service
    }

    override fun areContentsTheSame(oldItem: CostPostageFee, newItem: CostPostageFee): Boolean {
        return newItem.service == newItem.service
    }
}
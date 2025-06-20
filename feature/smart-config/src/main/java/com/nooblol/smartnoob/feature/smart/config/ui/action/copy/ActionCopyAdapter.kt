/*
 * Copyright (C) 2023 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.nooblol.smartnoob.feature.smart.config.ui.action.copy

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.nooblol.smartnoob.core.ui.databinding.ItemListHeaderBinding
import com.nooblol.smartnoob.feature.smart.config.R
import com.nooblol.smartnoob.feature.smart.config.databinding.ItemActionBinding
import com.nooblol.smartnoob.feature.smart.config.ui.action.copy.ActionCopyModel.ActionCopyItem

/**
 * Adapter displaying all actions in a list.
 * @param onActionSelected Called when the user presses an action.
 */
class ActionCopyAdapter(
    private val onActionSelected: (ActionCopyItem.ActionItem) -> Unit
): ListAdapter<ActionCopyItem, RecyclerView.ViewHolder>(DiffUtilCallback) {

    override fun getItemViewType(position: Int): Int =
        when(getItem(position)) {
            is ActionCopyItem.HeaderItem -> R.layout.item_list_header
            is ActionCopyItem.ActionItem -> R.layout.item_action
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.item_list_header -> HeaderViewHolder(
                ItemListHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.item_action -> ActionViewHolder(
                ItemActionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Unsupported view type !")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.onBind(getItem(position) as ActionCopyItem.HeaderItem)
            is ActionViewHolder -> holder.onBind(getItem(position) as ActionCopyItem.ActionItem, onActionSelected)
        }
    }
}

/** DiffUtil Callback comparing two items when updating the [ActionCopyAdapter] list. */
object DiffUtilCallback: DiffUtil.ItemCallback<ActionCopyItem>(){
    override fun areItemsTheSame(oldItem: ActionCopyItem, newItem: ActionCopyItem): Boolean =
        when {
            oldItem is ActionCopyItem.HeaderItem && newItem is ActionCopyItem.HeaderItem -> true
            oldItem is ActionCopyItem.ActionItem && newItem is ActionCopyItem.ActionItem ->
                oldItem.uiAction.action.id == newItem.uiAction.action.id
            else -> false
        }

    override fun areContentsTheSame(oldItem: ActionCopyItem, newItem: ActionCopyItem): Boolean = oldItem == newItem
}

/**
 * View holder displaying a header in the [ActionCopyAdapter].
 * @param viewBinding the view binding for this header.
 */
class HeaderViewHolder(
    private val viewBinding: ItemListHeaderBinding,
) : RecyclerView.ViewHolder(viewBinding.root) {

    fun onBind(header: ActionCopyItem.HeaderItem) {
        viewBinding.textHeader.setText(header.title)
    }
}

/**
 * View holder displaying an action in the [ActionCopyAdapter].
 * @param viewBinding the view binding for this item.
 */
class ActionViewHolder(private val viewBinding: ItemActionBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    /**
     * Bind this view holder as a action item.
     *
     * @param item the item to be represented by this view holder.
     * @param actionClickedListener listener notified upon user click on this item.
     */
    fun onBind(item: ActionCopyItem.ActionItem, actionClickedListener: (ActionCopyItem.ActionItem) -> Unit) {
        viewBinding.apply {
            root.setOnClickListener { actionClickedListener.invoke(item) }

            actionName.visibility = View.VISIBLE
            actionTypeIcon.setImageResource(item.uiAction.icon)
            actionName.text = item.uiAction.name
            actionDetails.apply {
                text = item.uiAction.description

                val typedValue = TypedValue()
                val actionColorAttr = if (item.uiAction.haveError) R.attr.colorError else R.attr.colorOnSurfaceVariant
                root.context.theme.resolveAttribute(actionColorAttr, typedValue, true)
                setTextColor(typedValue.data)
            }
        }
    }
}
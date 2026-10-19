package com.s8139858.assignment2.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.s8139858.assignment2.R
import com.s8139858.assignment2.databinding.ItemEntityBinding
import com.s8139858.assignment2.domain.model.Entity

/**
 * The Adapter connects data to the RecyclerView and controls how each row is shown.
 *
 * RecyclerView reuses (recycles) a small pool of row views while scrolling;
 * the ViewHolder caches view lookups so no findViewById runs per scroll frame.
 * ListAdapter + DiffUtil calculates the minimal set of changes (insert/remove/
 * update) on a background thread, so list updates animate instead of a full reload.
 * Rows intentionally show the summary only — the description is for Details.
 */
class EntityAdapter(
    private val onEntityClick: (Entity) -> Unit,
) : ListAdapter<Entity, EntityAdapter.EntityViewHolder>(EntityDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val binding = ItemEntityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntityViewHolder(binding, onEntityClick)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EntityViewHolder(
        private val binding: ItemEntityBinding,
        onEntityClick: (Entity) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentEntity: Entity? = null

        init {
            binding.root.setOnClickListener { currentEntity?.let(onEntityClick) }
        }

        fun bind(entity: Entity) {
            currentEntity = entity
            binding.textEntityTitle.text = entity.summaryTitle
            binding.textEntitySubtitle.text = entity.summarySubtitle
            binding.textEntitySubtitle.isVisible = entity.summarySubtitle.isNotBlank()
            binding.root.contentDescription =
                binding.root.context.getString(R.string.cd_entity_item, entity.summaryTitle)
        }
    }

    private object EntityDiffCallback : DiffUtil.ItemCallback<Entity>() {
        override fun areItemsTheSame(oldItem: Entity, newItem: Entity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Entity, newItem: Entity): Boolean =
            oldItem == newItem
    }
}

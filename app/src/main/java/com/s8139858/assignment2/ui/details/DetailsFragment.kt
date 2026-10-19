package com.s8139858.assignment2.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.s8139858.assignment2.R
import com.s8139858.assignment2.databinding.FragmentDetailsBinding
import com.s8139858.assignment2.databinding.ItemDetailRowBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Shows every field of the selected entity (as labelled rows) plus its
 * description in a scrollable Material card layout.
 */
@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailsBinding.bind(view)

        val entity = viewModel.entity
        binding.textDetailsTitle.text = entity.summaryTitle

        binding.containerFields.removeAllViews()
        entity.fields.forEach { field ->
            val row = ItemDetailRowBinding.inflate(layoutInflater, binding.containerFields, false)
            row.textFieldLabel.text = field.label
            row.textFieldValue.text = field.value
            binding.containerFields.addView(row.root)
        }

        binding.textDescription.text =
            entity.description ?: getString(R.string.details_no_description)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.s8139858.assignment2.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.s8139858.assignment2.R
import com.s8139858.assignment2.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Renders [DashboardUiState]: loading, content, empty and error-with-retry. */
@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private val entityAdapter = EntityAdapter { entity ->
        findNavController().navigate(
            DashboardFragmentDirections.actionDashboardFragmentToDetailsFragment(entity),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        binding.recyclerEntities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = entityAdapter
        }
        binding.buttonRetry.setOnClickListener { viewModel.retry() }

        // Collect UiState only while the view is STARTED (visible); collection is
        // cancelled on STOPPED and restarted on START, so no wasted work or leaks.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: DashboardUiState) {
        binding.progressDashboard.isVisible = state is DashboardUiState.Loading
        binding.groupContent.isVisible = state is DashboardUiState.Success
        binding.textEmpty.isVisible = state is DashboardUiState.Empty
        binding.groupError.isVisible = state is DashboardUiState.Error

        when (state) {
            is DashboardUiState.Success -> {
                binding.textEntityTotal.text =
                    getString(R.string.dashboard_entity_total, state.entityTotal)
                entityAdapter.submitList(state.entities)
            }
            is DashboardUiState.Error ->
                binding.textError.setText(state.messageRes)
            else -> Unit
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

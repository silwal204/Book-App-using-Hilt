package com.s8139858.assignment2.ui.login

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.s8139858.assignment2.R
import com.s8139858.assignment2.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Renders [LoginUiState] and forwards user events to [LoginViewModel]. */
@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.editUsername.doAfterTextChanged { viewModel.onUsernameChanged() }
        binding.editPassword.doAfterTextChanged { viewModel.onPasswordChanged() }
        binding.buttonLogin.setOnClickListener {
            viewModel.login(
                username = binding.editUsername.text?.toString().orEmpty(),
                password = binding.editPassword.text?.toString().orEmpty(),
            )
        }

        // lifecycleScope is tied to the Fragment view; repeatOnLifecycle(STARTED)
        // collects the flows only while the view is visible and cancels collection
        // when it is stopped — the safe, leak-free way to observe Flows from UI.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    private fun render(state: LoginUiState) {
        binding.layoutUsername.error = state.usernameError?.let(::getString)
        binding.layoutPassword.error = state.passwordError?.let(::getString)
        binding.progressLogin.isVisible = state.isLoading
        binding.textColdStartHint.isVisible = state.showColdStartHint
        binding.buttonLogin.isEnabled = !state.isLoading
        binding.editUsername.isEnabled = !state.isLoading
        binding.editPassword.isEnabled = !state.isLoading
    }

    private fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.NavigateToDashboard -> findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToDashboardFragment(event.keypass),
            )
            is LoginEvent.ShowError ->
                Snackbar.make(binding.root, event.messageRes, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

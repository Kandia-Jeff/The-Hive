package com.example.thehive.ui.browse

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.thehive.R
import com.example.thehive.databinding.FragmentHomeBinding
import com.example.thehive.ui.common.ScreenState
import com.example.thehive.ui.common.setScreenState
import com.example.thehive.ui.common.showMessage

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)

        binding.emptyView.emptyMessage.text = "No homes match your filters"
        binding.errorView.retryButton.setOnClickListener {
            showMessage("Retry tapped")
        }

        // Change this to LOADING / EMPTY / ERROR to see each state
        setScreenState(
            ScreenState.EMPTY,
            binding.contentView,
            binding.loadingView.root,
            binding.emptyView.root,
            binding.errorView.root
        )
    }
}
package com.delafleur.mxt.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.delafleur.mxt.CameraUtil
import com.delafleur.mxt.R
import com.delafleur.mxt.data.CameracaptureViewModel
import com.delafleur.mxt.databinding.FragmentCameracaptureBinding
import com.delafleur.mxt.util.ProcessImageAnalyzer

class CameracaptureFragment : Fragment() {
        private val viewModel: CameracaptureViewModel by viewModels()

        private var _binding: FragmentCameracaptureBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentCameracaptureBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            CameraUtil.startCamera(
                requireContext(),
                ProcessImageAnalyzer(
                    {
                        runOnUiThread {
                            _binding?.imageView?.setImageBitmap(
                                it
                            )
                        }
                    },
                    binding.previewView,
                    viewModel.params
                ),
                binding.previewView.surfaceProvider
            )


        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        private fun Fragment.runOnUiThread(action: () -> Unit) {
            if (!isAdded) return
           activity?.runOnUiThread(action)
        }
    }

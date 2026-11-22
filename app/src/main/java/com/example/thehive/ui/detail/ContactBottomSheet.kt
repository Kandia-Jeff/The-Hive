package com.example.thehive.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.thehive.databinding.BottomSheetContactBinding
import com.example.thehive.ui.common.showMessage
import com.example.thehive.util.ContactHelper
import com.example.thehive.util.EnquiryMessageBuilder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ContactBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetContactBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private var location: String = ""
    private var email: String = ""
    private var phone: String = ""
    private var senderName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE, "")
            location = it.getString(ARG_LOCATION, "")
            email = it.getString(ARG_EMAIL, "")
            phone = it.getString(ARG_PHONE, "")
            senderName = it.getString(ARG_SENDER_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = EnquiryMessageBuilder.build(title, location, senderName)
        binding.tvMessagePreview.text = message
        binding.tvLandlordInfo.text = "Inquiring about: $title ($location)"

        binding.btnEmailChannel.setOnClickListener {
            ContactHelper.sendEmail(
                context = requireContext(),
                email = email,
                subject = "Enquiry: $title",
                body = message,
                onError = { error -> showMessage(error) }
            )
            dismiss()
        }

        binding.btnSmsChannel.setOnClickListener {
            ContactHelper.sendSms(
                context = requireContext(),
                phone = phone,
                message = message,
                onError = { error -> showMessage(error) }
            )
            dismiss()
        }

        binding.btnWhatsAppChannel.setOnClickListener {
            ContactHelper.sendWhatsApp(
                context = requireContext(),
                phone = phone,
                message = message,
                onError = { error -> showMessage(error) }
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ContactBottomSheet"

        private const val ARG_TITLE = "title"
        private const val ARG_LOCATION = "location"
        private const val ARG_EMAIL = "email"
        private const val ARG_PHONE = "phone"
        private const val ARG_SENDER_NAME = "sender_name"

        fun newInstance(
            title: String,
            location: String,
            email: String,
            phone: String,
            senderName: String? = null
        ): ContactBottomSheet {
            return ContactBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_LOCATION, location)
                    putString(ARG_EMAIL, email)
                    putString(ARG_PHONE, phone)
                    putString(ARG_SENDER_NAME, senderName)
                }
            }
        }
    }
}

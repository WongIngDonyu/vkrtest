package com.example.vkr.presentation.screens.manageevent

import android.net.Uri
import com.example.vkr.data.model.EventEntity

interface ManageEventContract {
    data class ViewState(
        val event: EventEntity? = null,
        val teamName: String = "",
        val photoUris: List<Uri> = emptyList()
    )

    interface View {
        fun updateState(state: ViewState)
        fun goBack()
        fun openImagePicker()
    }

    interface Presenter {
        fun onInit(eventId: Int)
        fun onFinishEvent()
        fun onImagePicked(uris: List<Uri>)
        fun onBackClicked()
    }
}

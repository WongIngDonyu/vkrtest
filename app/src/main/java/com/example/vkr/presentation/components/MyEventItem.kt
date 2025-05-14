package com.example.vkr.presentation.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.vkr.R
import com.example.vkr.data.model.EventEntity
import java.io.File

@Composable
fun MyEventItem(event: EventEntity, onClick: () -> Unit, onDelete: (() -> Unit)? = null) {
    val isFinished = event.isFinished
    val titleColor = if (isFinished) Color.Gray else Color.Unspecified
    val dateColor = if (isFinished) Color.LightGray else Color.Gray
    val painter = if (!event.imageUri.isNullOrBlank()) {
        rememberAsyncImagePainter(Uri.parse(event.imageUri))
    } else {
        painterResource(id = R.drawable.testew)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isFinished) { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = titleColor
            )
            Text(
                event.dateTime,
                style = MaterialTheme.typography.bodySmall,
                color = dateColor
            )
            if (isFinished) {
                Text(
                    text = "Завершено",
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (onDelete != null && !isFinished) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = Color.Red
                )
            }
        }
    }
}
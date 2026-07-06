package com.job2day.nazaarabox.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.core.VideoServer
import com.job2day.nazaarabox.ui.theme.NazaaraBoxPrimary
import com.job2day.nazaarabox.ui.theme.NazaaraBoxCardBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerBottomSheet(
    title: String,
    icon: ImageVector,
    options: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableIntStateOf(initialIndex) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NazaaraBoxCardBackground,
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Icon(icon, contentDescription = null, tint = NazaaraBoxPrimary)
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            options.forEachIndexed { index, option ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = option,
                            color = if (selected == index) NazaaraBoxPrimary else Color.White,
                            fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    trailingContent = {
                        if (selected == index) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NazaaraBoxPrimary)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = index
                            onDismiss()
                        },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastBottomSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NazaaraBoxCardBackground,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Cast to device", color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                text = "No devices found. Make sure your device is on the same Wi-Fi network.",
                modifier = Modifier.padding(top = 12.dp),
                color = Color.Gray,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerBottomSheet(
    servers: List<VideoServer>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NazaaraBoxCardBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
            )
        },
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                CustomIconWidget(iconName = "dns_rounded", size = 18.dp, color = NazaaraBoxPrimary)
                Text(
                    text = "Select Server",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
            servers.forEachIndexed { index, server ->
                val isActive = index == selectedIndex
                ListItem(
                    headlineContent = {
                        Text(
                            text = server.label,
                            color = if (isActive) NazaaraBoxPrimary else Color.White,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isActive) NazaaraBoxPrimary.copy(alpha = 0.12f)
                                    else Color.White.copy(alpha = 0.05f),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(server.icon, fontSize = 18.sp)
                        }
                    },
                    trailingContent = {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NazaaraBoxPrimary),
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(index)
                            onDismiss()
                        },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuSheet(
    title: String,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NazaaraBoxCardBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            ListItem(
                headlineContent = { Text("Share", color = Color.White) },
                modifier = Modifier.clickable {
                    onShare()
                    onDismiss()
                },
            )
        }
    }
}

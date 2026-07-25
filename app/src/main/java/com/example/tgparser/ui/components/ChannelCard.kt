package com.example.tgparser.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tgparser.model.Channel

@Composable
fun ChannelCard(
    channel: Channel,
    onClick: () -> Unit
) {

    val preview = channel.lastMessage.let {
        if (it.length > 33) it.take(30) + "..." else it
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
    ) {

        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = channel.name.first().uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )

                }

            }


            Spacer(
                modifier = Modifier.width(12.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium
                )


                Text(
                    text = channel.username,
                    style = MaterialTheme.typography.bodySmall
                )


                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium
                )

            }

            Column(
                modifier = Modifier
            ) {
                Text(
                    text = channel.lastUpdate,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 18.sp
                )

                Text(
                    text = "${channel.lastRefreshTime} ⟲",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 18.sp
                )
            }

        }

    }

}
package com.calorieai.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun <T : Enum<T>> EnumSelector(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    itemDescription: ((T) -> String)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(horizontal = 16.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (itemDescription != null) 72.dp else 56.dp)
                    .selectable(
                        selected = (item == selectedItem),
                        onClick = { onItemSelected(item) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (item == selectedItem),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = itemLabel(item),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    itemDescription?.let { desc ->
                        Text(
                            text = desc(item),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <T : Enum<T>> EnumSelectorCard(
    title: String,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    itemDescription: ((T) -> String)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        EnumSelector(
            items = items,
            selectedItem = selectedItem,
            onItemSelected = onItemSelected,
            itemLabel = itemLabel,
            itemDescription = itemDescription
        )
    }
}

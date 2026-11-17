package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PhonebookContact
import com.example.ui.theme.OnPrimaryWhite
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Outline
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.Saffron
import com.example.ui.theme.SurfaceContainerLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhonebookPickerSheet(
    contacts: List<PhonebookContact>,
    targetTypeLabel: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onImportSelected: (List<PhonebookContact>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    var selectedKeys by remember(contacts) {
        mutableStateOf(emptySet<String>())
    }

    val filtered = remember(contacts, search) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) contacts
        else contacts.filter {
            it.name.lowercase().contains(q) ||
                it.phoneNumber.lowercase().contains(q) ||
                it.phoneNumber.filter(Char::isDigit).contains(q)
        }
    }

    val allFilteredSelected = filtered.isNotEmpty() && filtered.all { it.lookupKey in selectedKeys }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Saffron.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = "Phonebook",
                            tint = OnSurface
                        )
                    }
                    Column {
                        Text(
                            text = "Phonebook contacts",
                            style = MaterialTheme.typography.headlineMedium,
                            color = OnSurface
                        )
                        Text(
                            text = "Select people to add as $targetTypeLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("close_phonebook_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close phonebook")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search name or phone number") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant)
                },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phonebook_search_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filtered.size} ${if (filtered.size == 1) "contact" else "contacts"} found",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                TextButton(
                    onClick = {
                        selectedKeys = if (allFilteredSelected) {
                            selectedKeys - filtered.map { it.lookupKey }.toSet()
                        } else {
                            selectedKeys + filtered.map { it.lookupKey }
                        }
                    },
                    modifier = Modifier.testTag("phonebook_select_all_button")
                ) {
                    Text(
                        text = if (allFilteredSelected) "Clear selection" else "Select all",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlack
                    )
                }
            }

            HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            tint = Outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (contacts.isEmpty()) {
                                "No contacts with phone numbers found on this device"
                            } else {
                                "No matching contacts"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filtered, key = { it.lookupKey }) { item ->
                        val checked = item.lookupKey in selectedKeys
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedKeys = if (checked) {
                                        selectedKeys - item.lookupKey
                                    } else {
                                        selectedKeys + item.lookupKey
                                    }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KuraAvatar(name = item.name, size = 44.dp, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = OnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.phoneNumber.isNotEmpty()) {
                                    Text(
                                        text = item.phoneNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedKeys = if (isChecked) {
                                        selectedKeys + item.lookupKey
                                    } else {
                                        selectedKeys - item.lookupKey
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryBlack,
                                    checkmarkColor = OnPrimaryWhite
                                )
                            )
                        }
                        HorizontalDivider(color = Color.Black.copy(alpha = 0.04f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val selectedItems = remember(contacts, selectedKeys) {
                contacts.filter { it.lookupKey in selectedKeys }
            }
            Button(
                onClick = { onImportSelected(selectedItems) },
                enabled = selectedItems.isNotEmpty() && !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlack,
                    contentColor = OnPrimaryWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(52.dp)
                    .testTag("confirm_phonebook_import_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedItems.isEmpty()) {
                        "Select contacts to import"
                    } else {
                        "Import ${selectedItems.size} ${if (selectedItems.size == 1) "contact" else "contacts"}"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

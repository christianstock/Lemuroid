package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyAdvanceModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyAdvanceSkin
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkinManager

@Composable
fun GbaSkinSelectionScreen(
    skinManager: GbaSkinManager,
    onSkinSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedSkin = skinManager.getSelectedSkinFlow().collectAsState(GameBoyAdvanceSkin.INDIGO)
    val allSkins = skinManager.getAllSkins()
    var selectedModelFilter by remember { mutableStateOf< GameBoyAdvanceModel?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Game Boy Advance Themes & Models",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        // Model Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedModelFilter == null,
                onClick = { selectedModelFilter = null },
                label = { Text("All Models") }
            )
            GameBoyAdvanceModel.entries.forEach { model ->
                FilterChip(
                    selected = selectedModelFilter == model,
                    onClick = {
                        selectedModelFilter = if (selectedModelFilter == model) null else model
                    },
                    label = { Text(model.displayName) }
                )
            }
        }

        val modelsToDisplay = if (selectedModelFilter != null) {
            listOf(selectedModelFilter!!)
        } else {
            GameBoyAdvanceModel.entries
        }

        modelsToDisplay.forEach { model ->
            val modelSkins = allSkins.filter { it.model == model }
            if (modelSkins.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Model Section Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = model.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "${model.widthMm} × ${model.heightMm} mm • ${model.preferredOrientation.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Grid of Skins for this Model
                    modelSkins.chunked(2).forEach { rowSkins ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowSkins.forEach { skin ->
                                GbaSkinCard(
                                    skin = skin,
                                    isSelected = selectedSkin.value.id == skin.id,
                                    onSelect = { onSkinSelected(skin.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowSkins.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GbaSkinCard(
    skin: GameBoyAdvanceSkin,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp, 60.dp)
                    .background(
                        color = skin.caseColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = skin.buttonColor,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = skin.buttonColor,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            Text(
                text = skin.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

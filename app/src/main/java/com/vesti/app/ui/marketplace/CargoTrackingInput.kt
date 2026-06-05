package com.vesti.app.ui.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors

/**
 * Kargo Firmaları ve doğrulama kuralları:
 * - Yurtiçi Kargo: 12 hane
 * - Aras Kargo: 13 hane, sadece rakamlardan oluşur
 * - MNG Kargo: 12 hane
 * - PTT Kargo: 13 hane (harf ve rakam karışık)
 * - Sürat Kargo: 14 hane
 */
data class CargoCompany(
    val id: String,
    val nameTr: String,
    val nameEn: String,
    val requiredLength: Int,
    val numericOnly: Boolean = false,
    val description: String // açıklama
)

val CARGO_COMPANIES = listOf(
    CargoCompany("yurtici", "Yurtiçi Kargo", "Yurtiçi Cargo", 12, false, "12 hane"),
    CargoCompany("aras", "Aras Kargo", "Aras Cargo", 13, true, "13 hane, sadece rakam"),
    CargoCompany("mng", "MNG Kargo", "MNG Cargo", 12, false, "12 hane"),
    CargoCompany("ptt", "PTT Kargo", "PTT Cargo", 13, false, "13 hane, harf+rakam"),
    CargoCompany("surat", "Sürat Kargo", "Sürat Cargo", 14, false, "14 hane")
)

fun validateTrackingNumber(company: CargoCompany, trackingNo: String): Pair<Boolean, String> {
    if (trackingNo.isEmpty()) {
        return Pair(false, "Takip numarası boş olamaz")
    }
    if (trackingNo.length != company.requiredLength) {
        return Pair(false, "${company.requiredLength} haneli olmalı (şu an: ${trackingNo.length} hane)")
    }
    if (company.numericOnly && !trackingNo.all { it.isDigit() }) {
        return Pair(false, "${company.nameTr} için takip numarası sadece rakamlardan oluşmalı")
    }
    return Pair(true, "Geçerli takip numarası ✓")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargoTrackingInput(
    onTrackingSubmit: (company: String, trackingNo: String) -> Unit
) {
    var selectedCompany by remember { mutableStateOf<CargoCompany?>(null) }
    var trackingNo by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = VestiColors.Primary, modifier = Modifier.size(20.dp))
            Text(
                AppConfig.t("Kargo Takip Numarası", "Cargo Tracking Number"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = VestiColors.TextMain
            )
        }

        // 1. Adım: Kargo Firması Seçimi
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                AppConfig.t("Önce kargo firmasını seçin", "First select the cargo company"),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Box {
                OutlinedTextField(
                    value = selectedCompany?.let {
                        if (AppConfig.language == "en") it.nameEn else it.nameTr
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(AppConfig.t("Kargo Firması", "Cargo Company")) },
                    placeholder = { Text(AppConfig.t("Firma seçin...", "Select company...")) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (selectedCompany != null) VestiColors.Primary else Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDropdown = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VestiColors.Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                )

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    CARGO_COMPANIES.forEach { company ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        if (AppConfig.language == "en") company.nameEn else company.nameTr,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        company.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            onClick = {
                                selectedCompany = company
                                trackingNo = ""
                                validationResult = null
                                showDropdown = false
                            },
                            leadingIcon = {
                                if (selectedCompany?.id == company.id) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VestiColors.Primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            }
        }

        // 2. Adım: Takip No Girişi (Sadece firma seçilince aktif olur)
        if (selectedCompany != null) {
            val company = selectedCompany!!
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    AppConfig.t(
                        "${company.nameTr} için ${company.requiredLength} haneli${if (company.numericOnly) ", sadece rakam" else ""} takip no girin",
                        "Enter ${company.requiredLength}-digit${if (company.numericOnly) ", numbers only" else ""} tracking no for ${company.nameEn}"
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = trackingNo,
                    onValueChange = { new ->
                        // Maksimum uzunluk sınırı
                        if (new.length <= company.requiredLength) {
                            // Sadece rakam gerektiren firmalar için filtre uygula
                            trackingNo = if (company.numericOnly) {
                                new.filter { it.isDigit() }
                            } else {
                                new.filter { it.isLetterOrDigit() }
                            }
                            validationResult = if (trackingNo.isNotEmpty()) {
                                validateTrackingNumber(company, trackingNo)
                            } else null
                        }
                    },
                    label = { Text(AppConfig.t("Takip Numarası", "Tracking Number")) },
                    placeholder = { Text("${company.requiredLength} " + AppConfig.t("hane", "digits")) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (company.numericOnly) KeyboardType.Number else KeyboardType.Text
                    ),
                    singleLine = true,
                    supportingText = {
                        val current = trackingNo.length
                        val required = company.requiredLength
                        Text(
                            "$current / $required",
                            color = when {
                                current == required -> Color(0xFF10B981)
                                current > 0 -> Color(0xFFF59E0B)
                                else -> Color.Gray
                            }
                        )
                    },
                    trailingIcon = {
                        validationResult?.let { (isValid, _) ->
                            Icon(
                                if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = when {
                            validationResult?.first == true -> Color(0xFF10B981)
                            validationResult?.first == false -> Color(0xFFEF4444)
                            else -> VestiColors.Primary
                        },
                        unfocusedBorderColor = when {
                            validationResult?.first == true -> Color(0xFF10B981).copy(alpha = 0.5f)
                            validationResult?.first == false -> Color(0xFFEF4444).copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        }
                    )
                )

                // Validation mesajı
                validationResult?.let { (isValid, message) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            message,
                            fontSize = 12.sp,
                            color = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Gönder Butonu
            Button(
                onClick = {
                    val (isValid, _) = validateTrackingNumber(company, trackingNo)
                    if (isValid) {
                        onTrackingSubmit(company.nameTr, trackingNo)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = validationResult?.first == true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VestiColors.Primary,
                    disabledContainerColor = VestiColors.Primary.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    AppConfig.t("Kargo Takibini Başlat", "Start Cargo Tracking"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

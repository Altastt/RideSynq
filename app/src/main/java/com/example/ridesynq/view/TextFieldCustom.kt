package com.example.ridesynq.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ridesynq.R
import com.example.ridesynq.models.AutoresizedText


@Composable
fun TextFieldCustom(
    value: String,                       // Текущее значение
    onValueChange: (String) -> Unit,     // Коллбэк изменения
    label: String,                       // Лейбл поля (ранее placeholder)
    modifier: Modifier = Modifier,
    isError: Boolean = false,            // Флаг ошибки
    supportingText: @Composable (() -> Unit)? = null, // Текст поддержки/ошибки
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default, // Опции клавиатуры
    singleLine: Boolean = true           // Однострочное поле по умолчанию
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(), // Растягиваем по ширине
        label = { Text(label) },
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp) // Стандартное скругление
    )
}

@Composable
fun TextFieldEmail(
    value: String,
    onValueChange: (String) -> Unit,
    label: String, // Лейбл поля
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(), // Растягиваем по ширине
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Клавиатура для Email
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        shape = RoundedCornerShape(12.dp) // Стандартное скругление
    )
}

@Composable
fun TextFieldPass(
    value: String,                      // Текущее значение этого поля
    onValueChange: (String) -> Unit,    // Коллбэк при изменении значения
    placeholder: String,                // Текст плейсхолдера/лейбла
    modifier: Modifier = Modifier,      // Модификатор для настройки внешнего вида
    isError: Boolean = false,           // Флаг для отображения состояния ошибки
    supportingText: @Composable (() -> Unit)? = null // Опциональный текст под полем (для ошибок/подсказок)
) {
    var showPassword by remember { mutableStateOf(false) }

    OutlinedTextField( // Используем OutlinedTextField для единообразия
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(), // По умолчанию растягиваем по ширине
        label = { Text(placeholder) }, // Используем label для лучшего UX
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    painter = painterResource(if (showPassword) R.drawable.show_pass else R.drawable.hide_pass),
                    contentDescription = if (showPassword) "Hide Password" else "Show Password",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        isError = isError, // Передаем состояние ошибки
        supportingText = supportingText, // Передаем текст поддержки/ошибки
        shape = RoundedCornerShape(12.dp) // Умеренное скругление углов
        // Убираем кастомные цвета индикаторов, OutlinedTextField сам обрабатывает isError
        // colors = TextFieldDefaults.colors(...)
    )
}

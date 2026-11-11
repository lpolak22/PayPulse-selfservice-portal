package com.example.paypulse.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.example.paypulse.R

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
        trailingIcon = if (onClear != null && query.isNotEmpty()) ({ ClearButton(onClear) }) else null,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = searchActions(onSearch)
    )
}

@Composable
private fun searchActions(onSearch: (() -> Unit)?): KeyboardActions {
    val keyboard = LocalSoftwareKeyboardController.current
    return onSearch?.let { search ->
        KeyboardActions(onSearch = {
            keyboard?.hide()
            search()
        })
    } ?: KeyboardActions.Default
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(painter = painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.search_clear))
    }
}

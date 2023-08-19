package com.example.ui


@ViewMarker
expect fun Text(viewNode: ViewNode, text: String)

@ViewMarker
expect fun Row(viewNode: ViewNode, children: @ViewMarker (ViewNode) -> Unit)
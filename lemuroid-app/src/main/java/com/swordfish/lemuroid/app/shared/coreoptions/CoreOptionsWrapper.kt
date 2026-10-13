package com.swordfish.lemuroid.app.shared.coreoptions

import java.io.Serializable

data class CoreOptionsWrapper(
    val options: List<LemuroidCoreOption>,
) : Serializable

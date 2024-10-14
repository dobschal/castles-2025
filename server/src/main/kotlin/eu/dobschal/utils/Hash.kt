package eu.dobschal.utils

import java.security.MessageDigest

fun hash(plainText: String, algorithm: String = "SHA-512"): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    val digest = messageDigest.digest(plainText.toByteArray())
    val stringBuilder = StringBuilder()
    for (i in digest.indices) {
        stringBuilder.append(((digest[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
    }
    return stringBuilder.toString()
}

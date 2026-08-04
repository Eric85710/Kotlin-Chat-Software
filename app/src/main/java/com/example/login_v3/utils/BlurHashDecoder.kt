package com.example.login_v3.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.withSign

object BlurHashDecoder {

    fun decode(blurHash: String?, width: Int, height: Int, punch: Float = 1f): Bitmap? {
        if (blurHash == null || blurHash.length < 6) return null

        val numComponents = decode83(blurHash, 0, 1)
        val nx = (numComponents % 9) + 1
        val ny = (numComponents / 9) + 1

        if (blurHash.length != 4 + 2 * nx * ny) return null

        val maxAc = (decode83(blurHash, 1, 2) + 1) / 166f
        val colors = Array(nx * ny) { i ->
            if (i == 0) {
                val value = decode83(blurHash, 2, 6)
                decodeDc(value)
            } else {
                val value = decode83(blurHash, 4 + i * 2, 4 + i * 2 + 2)
                decodeAc(value, maxAc * punch)
            }
        }

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (j in 0 until ny) {
                    for (i in 0 until nx) {
                        val basis = cos(Math.PI * x * i / width) * cos(Math.PI * y * j / height)
                        val color = colors[i + j * nx]
                        r += color[0] * basis.toFloat()
                        g += color[1] * basis.toFloat()
                        b += color[2] * basis.toFloat()
                    }
                }
                pixels[x + y * width] = Color.rgb(clamp(r), clamp(g), clamp(b))
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun decode83(str: String, start: Int, end: Int): Int {
        var res = 0
        for (i in start until end) {
            val c = str[i]
            val digit = charMap[c] ?: 0
            res = res * 83 + digit
        }
        return res
    }

    private fun decodeDc(value: Int): FloatArray {
        val r = (value shr 16) / 255f
        val g = ((value shr 8) and 255) / 255f
        val b = (value and 255) / 255f
        return floatArrayOf(srgbToLinear(r), srgbToLinear(g), srgbToLinear(b))
    }

    private fun decodeAc(value: Int, maxAc: Float): FloatArray {
        val r = value / (19 * 19)
        val g = (value / 19) % 19
        val b = value % 19
        return floatArrayOf(
            signedPow2((r - 9) / 9f) * maxAc,
            signedPow2((g - 9) / 9f) * maxAc,
            signedPow2((b - 9) / 9f) * maxAc
        )
    }

    private fun srgbToLinear(v: Float): Float {
        return if (v <= 0.04045f) v / 12.92f else ((v + 0.055f) / 1.055f).pow(2.4f)
    }

    private fun linearToSrgb(v: Float): Float {
        val f = v.coerceIn(0f, 1f)
        return if (f <= 0.0031308f) f * 12.92f else (1.055f * f.pow(1 / 2.4f) - 0.055f)
    }

    private fun clamp(v: Float): Int {
        return (linearToSrgb(v) * 255).toInt()
    }

    private fun signedPow2(v: Float): Float {
        return v.pow(2f).withSign(v)
    }

    private val charMap = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#$%*+,-.:;=?@[]^_{|}~"
        .mapIndexed { i, c -> c to i }
        .toMap()
}

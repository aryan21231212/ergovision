package com.ergovision.app.math

import com.ergovision.app.data.model.Point3D
import kotlin.math.acos
import kotlin.math.sqrt

object VectorMath {

    fun subtract(a: Point3D, b: Point3D): Point3D {
        return Point3D(a.x - b.x, a.y - b.y, a.z - b.z)
    }

    fun dotProduct(a: Point3D, b: Point3D): Float {
        return (a.x * b.x) + (a.y * b.y) + (a.z * b.z)
    }

    fun magnitude(v: Point3D): Float {
        return sqrt((v.x * v.x) + (v.y * v.y) + (v.z * v.z))
    }

    /**
     * Computes the angle in degrees between two 3D vectors.
     */
    fun angleBetween(v1: Point3D, v2: Point3D): Float {
        val mag1 = magnitude(v1)
        val mag2 = magnitude(v2)
        if (mag1 == 0f || mag2 == 0f) return 0f

        val cosine = (dotProduct(v1, v2) / (mag1 * mag2)).coerceIn(-1.0f, 1.0f)
        return Math.toDegrees(acos(cosine.toDouble())).toFloat()
    }
}

package com.kotlinBinaryHandler

import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

/**
 * Created by SuperRainbowNinja on 19/01/2018.
 */

fun serInt(stream: OutputStream, value: Int) {
    stream.write(value)
    stream.write(value shr 8)
    stream.write(value shr 16)
    stream.write(value shr 24)
}

fun getInt(stream: InputStream) : Int {
    var num = stream.read()
    num = num or (stream.read() shl 8)
    num = num or (stream.read() shl 16)
    num = num or (stream.read() shl 24)
    return num
}

class CharSerializer<T>(
        override val name: String,
        val getter : (IDependentSerializer<T>, T) -> Char,
        val setter : (IDependentSerializer<T>, T, Char) -> Unit
) : IIndependentSerializer<T> {
    override fun serialize(stream: OutputStream, data: T) {
        stream.write(getter(this, data).toInt())
    }

    override fun deserialize(stream: InputStream, data: T) {
        setter(this, data, stream.read().toChar())
    }
}

class IntFactory(override var name : String) : IFactory {
    override var parent: ICompoundFactory? = null

    override fun isConstant(): Boolean =
            value != null

    var value : Int? = null

    override fun setValue(const : ConstantProperty) {
        value = (const as? ConsInt)?.num?.toInt() ?: throw Exception("")
    }

    override fun <T: Any> create(
            valueType: KClass<*>,
            getter : (IDependentSerializer<T>, T) -> Any,
            setter : (IDependentSerializer<T>, T, Any) -> Unit
    ) : IDependentSerializer<T> {
        val value = value
        return if (value == null) {
            if (valueType == Int::class) {
                IntSerializer(name,
                        (getter as (IDependentSerializer<T>, T) -> Int),
                        (setter as (IDependentSerializer<T>, T, Int) -> Unit)
                )
            } else {
                throw Exception()
            }
        } else {
            ConstIntSerializer(name, value)
        }
    }
}

class IntSerializer<T>(
        name: String,
        getter : (IDependentSerializer<T>, T) -> Int,
        setter : (IDependentSerializer<T>, T, Int) -> Unit
) : NumSerializer<T, Int>(name, getter, setter) {
    override fun serialize(stream: OutputStream, data: T) =
            serInt(stream, getter(this, data))

    override fun deserialize(stream: InputStream, data: T) =
            setter(this, data, getInt(stream))
}

class ConstIntSerializer<T>(
        name : String,
        num : Int
) : ConstNumSerializer<T, Int>(name, num) {
    override fun serialize(stream: OutputStream, data: T) =
            serInt(stream, Num)

    override fun deserialize(stream: InputStream): Int =
            getInt(stream)
}
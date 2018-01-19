package com.kotlinBinaryHandler

import java.io.InputStream
import java.io.OutputStream

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

class CharSerializer(name: String) : NumSerializer(name) {
    override fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>): IDependentSerializer = CharSerializer(name)

    override fun getConstant(name: String, num: Number): IIndependentSerializer = ConstCharSerializer(name, num.toChar())

    override fun serialize(stream: OutputStream) = stream.write(_num.toInt())

    override fun deserialize(stream: InputStream) {
        _num = stream.read().toChar()
        //println(num)
    }

    override var value: Any
        get() = _num
        set(value) {
            _num = value as? Char ?: throw Exception("Provided number serializer with none number object")
        }

    override var num: Number
        get() = _num.toInt()
        set(value) {
            _num = value.toChar()
        }

    var _num : Char = 'a'

    class ConstCharSerializer(name: String, var _num : Char) : ConstNumSerializer(name) {
        override fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>): IDependentSerializer = this

        override var num: Number
            get() = _num.toInt()
            set(value) {
                _num = value.toChar()
            }

        override fun serialize(stream: OutputStream) = stream.write(_num.toInt())

        override fun deserialize(stream: InputStream) {
            val input = stream.read()
            checkConstant(input)
        }

        override fun getConstant(name: String, num: Number): IIndependentSerializer  = ConstCharSerializer(name, num.toChar())
    }
}

class IntSerializer(name: String) : NumSerializer(name) {
    override fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>): IDependentSerializer = IntSerializer(name)

    override fun getConstant(name: String, num: Number): IIndependentSerializer = ConstIntSerializer(name, num.toInt())

    override fun serialize(stream: OutputStream) = serInt(stream, _num)

    override fun deserialize(stream: InputStream) {
        _num = getInt(stream)
        //println(num)
    }

    override var num: Number
        get() = _num
        set(value) {
            _num = value.toInt()
        }

    var _num : Int = 0

    class ConstIntSerializer(name: String, var _num : Int) : ConstNumSerializer(name) {
        override fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>): IDependentSerializer = this

        override var num: Number
            get() = _num
            set(value) {
                _num = value.toInt()
            }

        override fun serialize(stream: OutputStream) = serInt(stream, _num)

        override fun deserialize(stream: InputStream) {
            val input = getInt(stream)
            checkConstant(input)
        }

        override fun getConstant(name: String, num: Number): IIndependentSerializer  = ConstIntSerializer(name, num.toInt())
    }
}
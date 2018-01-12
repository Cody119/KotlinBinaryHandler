package com.kotlinBinaryHandler

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by SuperRainbowNinja on 11/01/2018.
 */


interface IDependentSerializer {
    val name: String
    var value: Any
    //fun resolveDependency(others : Array<IBinarySerializer>, refs: ArrayList<Int>)
    fun serialize(stream: OutputStream)
    fun deserialize(stream: InputStream)
}

interface IIndependentSerializer : IDependentSerializer {
    //override fun resolveDependency(others: Array<IBinarySerializer>, refs: ArrayList<Int>) {}
}

interface ILengthProxy {
    var length: Int
}

interface LengthProxySerializer : ILengthProxy, IDependentSerializer

interface LengthProxyProvider {
    fun getProxy() : LengthProxySerializer
}

interface ConstantProvider {
    fun getConstant() : IDependentSerializer
    fun with(value: ConstantProperty) : ConstantProvider
}

interface Constant

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

class ConsArraySerializer(override val name: String, val serializer: IIndependentSerializer) : IIndependentSerializer, ConstantProvider {
    override var value: Any
        get() = array
        set(value) {
            array = value as Array<Any>
        }

    var array: Array<Any> = Array(0, {})

    override fun serialize(stream: OutputStream) {

    }

    override fun deserialize(stream: InputStream) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConstant(): IDependentSerializer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun with(value: ConstantProperty): ConstantProvider {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class VarArraySerializer(override val name: String, val serializer: IIndependentSerializer, val lengthGetter: ILengthProxy) : IDependentSerializer {
    init {
//        if (lengthGetter is Constant) throw Exception("Provided var array with constant length, use constant array instead")
    }

    override var value: Any
        get() = array
        set(value) {
            array = value as? Array<Any> ?: throw Exception("Provided array serializer with none array object")
            lengthGetter.length = array.size
        }

    var array: Array<Any> = Array(0, {})

    override fun serialize(stream: OutputStream) {
        array.forEach {
            serializer.value = it
            serializer.serialize(stream)
        }
    }

    override fun deserialize(stream: InputStream) {
        array = Array(lengthGetter.length, {
            serializer.deserialize(stream)
            serializer.value
        })
    }

}

class IntSerializer(override val name: String) : IIndependentSerializer, ConstantProvider, LengthProxyProvider, LengthProxySerializer {
    override fun with(value: ConstantProperty): ConstantProvider {
        num = (value as? ConsInt)?.num?.toInt() ?: throw Exception("Wrong type provided to Int")
        return this
    }

    override var length: Int
        get() = num
        set(value) {
            num = value
        }
    override var value: Any
        get() = num
        set(value) {
            num = value as? Int ?: throw Exception("Provided number serializer with none number object")
        }

    var num: Int = 0

    override fun serialize(stream: OutputStream) = serInt(stream, num)


    override fun deserialize(stream: InputStream) {
        num = getInt(stream)
    }

    override fun getProxy(): LengthProxySerializer = this

    class ConstIntSerializer(override val name: String, val num: Int) : IIndependentSerializer, LengthProxyProvider, LengthProxySerializer, Constant {
        override var value: Any
            get() = num
            set(value) = throw Exception("Tried to change value of constant")

        override fun serialize(stream: OutputStream) = serInt(stream, num)

        override fun deserialize(stream: InputStream) {
            val input = getInt(stream)
            if (input != num) {
                throw Exception("Magic number $num not found")
            }
        }

        override var length: Int
            get() = num
            set(value) {
                if (length != value) throw Exception("Tried to serialize array with wrong length")
            }

        override fun getProxy(): LengthProxySerializer = this

        //override fun getConstant(): DependentSerializer = this

    }

    override fun getConstant(): IDependentSerializer = ConstIntSerializer(name, num)

}

class CustomSerializer(override val name: String, val serializers: Array<IDependentSerializer>) : IIndependentSerializer {
    override var value: Any
        get() = array
        set(value) {
            array = value as Array<Any>
        }

    var array: Array<Any> = Array(0, {})

    fun serialize(stream: OutputStream, data: Array<Any>) {
        array = data
        serialize(stream)
    }

    override fun serialize(stream: OutputStream) {
        for ((i, property) in serializers.withIndex()) {
            if (property !is Constant)
                property.value = array[i]
        }
        serializers.forEach {
            it.serialize(stream)
        }
    }

    override fun deserialize(stream: InputStream) {
        array = Array(serializers.size, {
            serializers[it].deserialize(stream)
            serializers[it].value
        })
    }

}
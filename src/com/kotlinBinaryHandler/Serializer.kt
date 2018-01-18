package com.kotlinBinaryHandler

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by SuperRainbowNinja on 11/01/2018.
 */


interface IDependentSerializer {
    val name: String
    var value: Any
    fun serialize(stream: OutputStream)
    fun deserialize(stream: InputStream)
    fun getConstant() : IIndependentSerializer
    fun with(value: ConstantProperty) : IIndependentSerializer
    //TODO copy method
}

interface IIndependentSerializer : IDependentSerializer

interface ILengthProxy {
    var length: Int
}

interface LengthProxySerializer : ILengthProxy, IDependentSerializer

interface LengthProxyProvider {
    fun getProxy() : LengthProxySerializer
}

//TODO semi constant or rename to constant injector and remove assumption that it returns a constant

interface Constant {
    fun compare(o: Constant) {}
}

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

//class ConsArraySerializer(override val name: String, val serializer: IIndependentSerializer, lengthIn: Int) : IIndependentSerializer, ConstantProvider {
//    var length: Int = lengthIn
//        private set
//
//    override var value: Any
//        get() = array
//        set(value) {
//            if (value is Array<*>) {
//                if (array.size < length) throw Exception("Tried to serialise array that was to small")
//                array = value as Array<Any>
//            } else {
//                throw Exception("wrong type")
//            }
//        }
//
//    var array: Array<Any> = Array(length, {})
//
//    override fun serialize(stream: OutputStream) {
//        array.forEach {
//            serializer.value = it
//            serializer.serialize(stream)
//        }
//    }
//
//    override fun deserialize(stream: InputStream) {
//        array = Array(length, {
//            serializer.deserialize(stream)
//            serializer.value
//        })
//    }
//
//    override fun getConstant(): IIndependentSerializer {
//        if (serializer is ConstantProvider) {
//            return  ConsArray(name, Array(array.size, {
//                serializer.value = array[it]
//                serializer.getConstant()
//            }))
//        } else {
//            throw Exception("")
//        }
//    }
//
//    override fun with(value: ConstantProperty): IIndependentSerializer {
//        if (serializer is ConstantProvider && value is ConsCompound) {
//           return  ConsArray(name, Array(value.arr.size, {serializer.with(value.arr[it])}))
//        } else {
//            throw Exception("")
//        }
//    }
//
//    class ConsArray(override val name: String, var array: Array<IDependentSerializer>) : IIndependentSerializer, Constant {
//        override var value: Any
//            get() = Array(array.size, {array[it].value})
//            set(value) = throw Exception("Tried to change value of constant")
//
//        override fun serialize(stream: OutputStream) {
//            array.forEach {
//                it.serialize(stream)
//            }
//        }
//
//        override fun deserialize(stream: InputStream) {
//            array.forEach {
//                it.deserialize(stream)
//            }
//        }
//    }
//
//}

class VarArraySerializer(override val name: String, val serializer: IIndependentSerializer, val lengthGetter: ILengthProxy) : IDependentSerializer {
    init {
//        if (lengthGetter is Constant) throw Exception("Provided var array with constant length, use constant array instead")
    }

    override var value: Any
        get() = array
        set(value) {
            if (value is Array<*>) {
                if (lengthGetter !is Constant || lengthGetter.length == value.size) {
                    array = value as Array<Any>
                    lengthGetter.length = array.size
                } else {
                    throw Exception("Provided array object with wrong length")
                }
            } else {
                throw Exception("Provided array serializer with none array object")
            }
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

    override fun getConstant(): IIndependentSerializer = ConsArray(name, Array(array.size, {
        serializer.value = array[it]
        serializer.getConstant()
    }))

    override fun with(value: ConstantProperty): IIndependentSerializer {
        if (value is ConsCompound) {
            if (lengthGetter is Constant && lengthGetter.length != value.arr.size) {
                throw Exception("Array restriction to small to fit passed struct")
            }

            return  ConsArray(name, Array(lengthGetter.length, {
                serializer.with(value.arr[it])
            }))
        } else {
            throw Exception("")
        }
    }

    class ConsArray(override val name: String, var array: Array<IDependentSerializer>) : IIndependentSerializer, Constant {
        override fun getConstant(): IIndependentSerializer {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun with(value: ConstantProperty): IIndependentSerializer {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override var value: Any
            get() = Array(array.size, {array[it].value})
            set(value) = throw Exception("Tried to change value of constant")

        override fun serialize(stream: OutputStream) {
            array.forEach {
                it.serialize(stream)
            }
        }

        override fun deserialize(stream: InputStream) {
            array.forEach {
                it.deserialize(stream)
            }
        }
    }
}

class IntSerializer(override val name: String) : IIndependentSerializer, LengthProxyProvider, LengthProxySerializer {
    override fun with(value: ConstantProperty): IIndependentSerializer {
        return ConstIntSerializer(name,
            (value as? ConsInt)?.num?.toInt() ?: throw Exception("Wrong type provided to Int")
        )
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
        //println(num)
    }

    override fun getProxy(): LengthProxySerializer = this

    class ConstIntSerializer(override val name: String, val num: Int) : IIndependentSerializer, LengthProxyProvider, LengthProxySerializer, Constant {
        override fun getConstant(): IIndependentSerializer {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun with(value: ConstantProperty): IIndependentSerializer {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

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

    override fun getConstant(): IIndependentSerializer = ConstIntSerializer(name, num)

}

class CustomSerializer(override val name: String, val serializers: Array<IDependentSerializer>) : IIndependentSerializer {
    override fun with(value: ConstantProperty): IIndependentSerializer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConstant(): IIndependentSerializer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
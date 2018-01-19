package com.kotlinBinaryHandler

import java.io.InputStream
import java.io.OutputStream
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by SuperRainbowNinja on 11/01/2018.
 *
 * TODO potentially partial constant creation from compound terms (constant injector)
 * TODO special case for compound terms?
 * TODO proper naming for inner terms
 */


interface IDependentSerializer {
    val name: String
    var value: Any
    fun serialize(stream: OutputStream)
    fun deserialize(stream: InputStream)
    fun getConstant() : IIndependentSerializer
    fun with(value: ConstantProperty) : IIndependentSerializer
    fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>) : IDependentSerializer
}

interface IIndependentSerializer : IDependentSerializer

interface ILengthProxy {
    var length: Int
}

interface LengthProxySerializer : ILengthProxy, IDependentSerializer

//interface LengthProxyProvider {
//    fun getProxy() : LengthProxySerializer
//}


interface Constant {
    fun compare(o: Constant) {}
}

class VarArraySerializer(override val name: String, val serializer: IIndependentSerializer, val lengthGetter: ILengthProxy) : IDependentSerializer {
    override fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>): IDependentSerializer =
        VarArraySerializer(
                name,
                serializer.copy(ser, names) as IIndependentSerializer,
                if (lengthGetter is Constant) {
                    lengthGetter
                } else {
                    ser[names[(lengthGetter as IDependentSerializer).name] ?: throw Exception("")] as ILengthProxy
                }
        )

    override var value: Any
        get() = array
        set(value) {
            when (value) {
                is Array<*> ->
                    if (lengthGetter !is Constant || lengthGetter.length == value.size) {
                        array = value as Array<Any>
                        lengthGetter.length = array.size
                    } else {
                        throw Exception("Provided array object with wrong length")
                    }
                is CharSequence ->
                    if (lengthGetter !is Constant || lengthGetter.length == value.length) {
                        array = Array(value.length, value::get)
                        lengthGetter.length = array.size
                    } else {
                        throw Exception("Provided array object with wrong length")
                    }
                is List<*> ->
                    if (lengthGetter !is Constant || lengthGetter.length == value.size) {
                        array = Array((value as List<Any>).size, value::get)
                        lengthGetter.length = array.size
                    } else {
                        throw Exception("Provided array object with wrong length")
                    }
                else -> throw Exception("Provided array serializer with none array object")
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
        //For the moment constants are constant and shouldn't change any internal values
        override fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>): IDependentSerializer = this

        override fun getConstant(): IIndependentSerializer = this

        override fun with(value: ConstantProperty): IIndependentSerializer {
            return if (value is ConsCompound) {
                ConsArray(name, Array(array.size, {
                    array[it].with(value.arr[it])
                }))
            } else {
                throw Exception("")
            }
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

abstract class NumSerializer(override val name: String) : IIndependentSerializer, LengthProxySerializer {
    override fun with(value: ConstantProperty): IIndependentSerializer {
        return when (value) {
            is ConsFloat -> {
                getConstant(name, value.num)
            }
            is ConsInt -> {
                getConstant(name, value.num)
            }
            else -> {
                throw Exception("Wrong type provided to Number")
            }
        }
    }

    override var length: Int
        get() = num.toInt()
        set(value) {
            num = value
        }
    override var value: Any
        get() = num
        set(value) {
            num = value as? Int ?: throw Exception("Provided number serializer with none number object")
        }

    abstract var num: Number

    //override fun getProxy(): LengthProxySerializer = this

    abstract class ConstNumSerializer(override val name: String) : IIndependentSerializer, LengthProxySerializer, Constant {
        abstract var num: Number

        override fun getConstant(): IIndependentSerializer = this

        abstract fun getConstant(name: String, num: Number) : IIndependentSerializer

        override fun with(value: ConstantProperty): IIndependentSerializer {
            return when (value) {
                is ConsFloat -> {
                    getConstant(name, value.num)
                }
                is ConsInt -> {
                    getConstant(name, value.num)
                }
                else -> {
                    throw Exception("Wrong type provided to Number")
                }
            }
        }

        override var value: Any
            get() = num
            set(value) = throw Exception("Tried to change value of constant")

        fun checkConstant(input: Number) {
            if (input != num) {
                throw Exception("Magic number $num not found")
            }
        }

        override var length: Int
            get() = num.toInt()
            set(value) {
                if (length != value) throw Exception("Tried to serialize array with wrong length")
            }

        //override fun getProxy(): LengthProxySerializer = this
    }

    override fun getConstant(): IIndependentSerializer = getConstant(name, num)

    abstract fun getConstant(name: String, num: Number) : IIndependentSerializer

}

class CustomSerializer(override val name: String, val serializers: Array<IDependentSerializer>, val nameMap: HashMap<String, Int>) : IIndependentSerializer {
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

    operator fun get(name: String) : IDependentSerializer {
        return this[nameMap[name] ?: throw Exception("tried to get ")]
    }

    operator fun get(index: Int) : IDependentSerializer {
        return serializers[index]
    }

    override fun copy(ser: List<IDependentSerializer>, names: HashMap<String, Int>): CustomSerializer {
        val newSerializers = ArrayList<IDependentSerializer>()
        serializers.forEach {
            newSerializers.add(it.copy(newSerializers, nameMap))
        }
        return CustomSerializer(
                name,
                Array(newSerializers.size, newSerializers::get),
                nameMap.clone() as HashMap<String, Int>
        )
    }

}
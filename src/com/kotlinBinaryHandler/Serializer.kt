package com.kotlinBinaryHandler

import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

/**
 * Created by SuperRainbowNinja on 11/01/2018.
 *
 * TODO potentially partial constant creation from compound terms (constant injector)
 * TODO special case for compound terms?
 * TODO proper naming for inner terms
 */


interface IDependentSerializer<T> {
    val name: String
    //var value: Any
    fun serialize(stream: OutputStream, data : T)
    fun deserialize(stream: InputStream, data : T)
    //fun getConstant() : IIndependentSerializer<T>
}

interface IIndependentSerializer<T> : IDependentSerializer<T>

interface ILengthProxy<in T> {
    fun getLength(data : T) : Int
}

interface LengthProxySerializer<T> : ILengthProxy<T>, IDependentSerializer<T>


//TODO need 2 confirm getter and setter accept and return correct values
interface IFactory {
    val name: String

    var parent: ICompoundFactory?

    fun setValue(const : ConstantProperty)

    fun isConstant() : Boolean

    //fun resolveRefrences(members : Map<String, IFactory>) {}

    //TODO these getters r wrong lol, they need 2 be generic
    fun <T: Any> create(
            valueType: KClass<*>,
            getter : (IDependentSerializer<T>, T) -> Any,
            setter : (IDependentSerializer<T>, T, Any) -> Unit
    ) : IDependentSerializer<T>
}

interface ICompoundFactory: IFactory {
    val factoryMap: Map<String, IFactory>
    fun <T: Any > create(valueType: KClass<T>): IDependentSerializer<T>
}


interface Constant {
    fun compare(o: Constant) {}
}
/*
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
*/

class ArraySerializer<T>(
        override val name: String,
        val serializer: IIndependentSerializer<Any>,
        val getter : (IDependentSerializer<T>, T) -> Array<Any>,
        val setter : (IDependentSerializer<T>, T, Array<Any>) -> Unit,
        val lengthGetter: ILengthProxy<T>
) : IDependentSerializer<T>, ILengthProxy<T> {
    override fun serialize(stream: OutputStream, data: T) {
        val x = getter(this, data)
        x.forEach {
            serializer.serialize(stream, it)
        }

    }

    override fun deserialize(stream: InputStream, data: T) {
        val x = Array<Any>(lengthGetter.getLength(data), {

        })
    }

    override fun getLength(data: T): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

abstract class NumSerializer<T, N : Number>(
        override val name: String,
        val getter : (IDependentSerializer<T>, T) -> N,
        val setter : (IDependentSerializer<T>, T, N) -> Unit
) : IIndependentSerializer<T>, ILengthProxy<T> {

    override fun getLength(data : T) : Int = getter(this, data).toInt()
}

abstract class ConstNumSerializer<T, out N : Number>(
        override val name: String,
        val Num : N
) : IIndependentSerializer<T>, ILengthProxy<T>, Constant {
    override fun getLength(data: T): Int = Num.toInt()

    abstract fun deserialize(stream: InputStream) : N
    override fun deserialize(stream: InputStream, data: T) {
        if (Num != deserialize(stream)) {
            throw Error("Serialized number wrong")
        }
    }
}

fun <T, E> nGet(x: IDependentSerializer<T>, y: T): E =
        throw Exception("Used ")

fun <T, E> nSet(x: IDependentSerializer<T>, y: T, z: E): Unit =
        throw Exception("Used ")

class CompoundSerializerFactory(
        override val name : String,
        override val factoryMap: Map<String, IFactory>,
        val type : String,
        val factorys: Array<IFactory>) : ICompoundFactory {

    init { factorys.forEach { it.parent = this } }

    override var parent: ICompoundFactory? = null

    override fun isConstant(): Boolean =
            factorys.all { it.isConstant() }

    fun create(name : String) : IFactory {
        return CompoundSerializerFactory(name, factoryMap, type, factorys)
    }

    override fun <T: Any> create(
            valueType: KClass<*>,
            getter: (IDependentSerializer<T>, T) -> Any,
            setter: (IDependentSerializer<T>, T, Any) -> Unit
    ): IDependentSerializer<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
//TODO abstract the creation of getters and setters
    override fun <T: Any > create(valueType: KClass<T>): IDependentSerializer<T> {
        val map = HashMap<String, KMutableProperty1<T,Any>>()
        for (x in valueType.memberProperties) {
            val y = x
            //TODO Throw some error checking in this
            if (y is KMutableProperty1<*,*>) {
                map[y.name] = (y as KMutableProperty1<T,Any>)
            }
        }
        val ar = Array(factorys.size, {
            val x = factorys[it]
            val y = map[x.name]
            if (y != null) {
                x.create<T>(
                        y.returnType.jvmErasure,
                        {_, receiver -> y.get(receiver)},
                        {_, receiver, value -> y.set(receiver, value)}
                )
            } else {
                throw Exception()
            }
        })

        return CompoundSerializer(name, ar)
    }

    override fun setValue(const: ConstantProperty) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //Copy the array
    }

}

class CompoundSerializer1<T>(
        override val name: String,
        val serializers: Array<IDependentSerializer<Any>>,
        val getter: (IDependentSerializer<T>, T) -> Any,
        val setter: (IDependentSerializer<T>, T, Any) -> Unit
) : IDependentSerializer<T> {
    override fun serialize(stream: OutputStream, data: T) {
        val data = getter(this, data)
        serializers.forEach {
            it.serialize(stream, data)
        }
    }
//TODO the getter needs 2 create the class maybe?
    override fun deserialize(stream: InputStream, data: T) {
        val data = getter(this, data)
        serializers.forEach {
            it.deserialize(stream, data)
        }
    }
}

class CompoundSerializer<T>(override val name: String, val serializers: Array<IDependentSerializer<T>>) : IDependentSerializer<T> {
    override fun serialize(stream: OutputStream, data: T) {
        serializers.forEach {
            it.serialize(stream, data)
        }
    }

    override fun deserialize(stream: InputStream, data: T) {
        serializers.forEach {
            it.deserialize(stream, data)
        }
    }
}
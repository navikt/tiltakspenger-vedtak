package no.nav.tiltakspenger.vedtak.helper

import java.util.concurrent.atomic.AtomicBoolean

class DirtyCheckingList<E>(
    private val wrappedList: MutableList<E>,
    private val isDirty: AtomicBoolean,
) : MutableList<E> by wrappedList {

    override fun clear() {
        isDirty.set(true)
        wrappedList.clear()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        isDirty.set(true)
        return wrappedList.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        isDirty.set(true)
        return wrappedList.addAll(index, elements)
    }

    override fun add(index: Int, element: E) {
        isDirty.set(true)
        wrappedList.add(index, element)
    }

    override fun add(element: E): Boolean {
        isDirty.set(true)
        return wrappedList.add(element)
    }

    override fun removeAt(index: Int): E {
        isDirty.set(true)
        return wrappedList.removeAt(index)
    }

    override fun set(index: Int, element: E): E {
        isDirty.set(true)
        return wrappedList.set(index, element)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        isDirty.set(true)
        return wrappedList.retainAll(elements)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        isDirty.set(true)
        return wrappedList.removeAll(elements)
    }

    override fun remove(element: E): Boolean {
        isDirty.set(true)
        return wrappedList.remove(element)
    }

    override fun indexOf(element: E): Int {
        TODO("Not yet implemented")
    }
}


fun multiply(sets: MutableList<Set<String>>): Set<String> {
    if (sets.size == 0) return setOf()
    if (sets.size == 1) return sets.first()
    var prev: Set<String> = sets.first()
    sets.remove(sets.first())
    var cur = mutableSetOf<String>()
    sets.forEach { set ->
        cur = mutableSetOf()
        prev.forEach { str ->
            if (set.size == 0) cur.add(str) else
                set.forEach {
                    cur.add(str + it)
                }
        }
        prev = cur
    }
    return cur
}

fun multiply(sets: MutableList<Set<String>>, k: Int) = multiply(sets).map {
    if (it.length > k) it.substring(0, k) else it
}.toSet()

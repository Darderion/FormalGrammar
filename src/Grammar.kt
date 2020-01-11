
fun constructGrammar(str: String): Grammar {
    val arr = str.split(',')
    val nonterminals = arr.filter { it.contains("->") }.map {
        it.substring(0, it.indexOf("->")).trim()[0]
    }.toHashSet()
    val terminals = str.toCharArray().filter { !nonterminals.contains(it) }.filter {
        it != '-' && it != '>'
    }.toHashSet()
    val s = if (nonterminals.contains('S')) 'S' else nonterminals.first()
    val k = arr[arr.size - 1].trim().toInt()
    val strRules = arr.filter { it.contains("->") }
    val rules = hashMapOf<Char, HashSet<String>>()
    nonterminals.forEach {
        rules[it] = hashSetOf()
    }
    strRules.forEach {
        rules[it.substring(0, it.indexOf("->")).trim()[0]]!!.add(
            it.substring(it.indexOf('>') + 1).trim()
        )
    }
    return Grammar(terminals, nonterminals, rules, s, k)
}

class Grammar(
    val terminals: HashSet<Char>,
    val nonterminals: HashSet<Char>,
    val rules: HashMap<Char, HashSet<String>>,
    val entry: Char = nonterminals.first(),
    private var k: Int = 1) {

    fun isLLk(k: Int): Boolean {
        if (this.k != k) {
            setK(k)
        }
        return isLLk()
    }

    fun isLLk(): Boolean {
        rules.filter { it.value.size > 1 }.map { rule ->
            val s = sigma(rule.key)
            rule.value.forEach { A ->
                rule.value.forEach { B ->
                    if (A != B) {
                        s.forEach {
                            if (multiply(
                                    mutableListOf(
                                        first(A), it
                                    ), k
                                ).intersect(
                                    multiply(
                                        mutableListOf(
                                            first(B), it), k
                                        )
                                    ).isNotEmpty()
                                ) {
                                return false
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    fun first(seq: String) = multiply(seq.map {
        first(it)
    }.toMutableList(), k)

    fun first(symb: Char): Set<String> {
        var prev = setOf(symb.toString())
        val cur = mutableSetOf<String>()
        for(i in 0..k+1) {
            cur.clear()
            rules.forEach { sym ->
                sym.value.forEach { str ->
                    prev.forEach {
                        cur.add(it.applyRule(sym.key, str))
                    }
                }
            }
            prev = cur.toSet()
        }
        return cur.map { if (it.length > k) it.substring(0, k) else it }.filter {str ->
            nonterminals.filter { str.contains(it) }.isEmpty()
        }.toSet()
    }

    fun follow(symb: Char): HashSet<String> {
        val set = hashSetOf<String>()
        sigma(symb).forEach {
            set.addAll(it)
        }
        return set
    }

    fun follow(str: String) = follow(str[str.length - 1])

    var sigma_i: HashMap<Triple<Char, Char, Int>, Set<Set<String>>> = hashMapOf()

    fun sigma(A: Char, B: Char, step: Int): Set<Set<String>> {
        if (sigma_i[Triple(A, B, step)] != null) {
            return sigma_i[Triple(A, B, step)]!!
        }
        val hs = hashSetOf<Set<String>>()
        if (step > 0) hs.addAll(sigma(A, B, step - 1))
        if (rules[A] == null) return setSigma(A, B, step, hs)
        if (step == 0) {
            if (rules[A]!!.filter { it.contains(B) }.isEmpty()) return setSigma(A, B, step, hs)
            rules[A]!!.filter { it.contains(B) }.forEach {
                for(i in it.indices) {
                    if (it[i] == B) hs.add(first(it.substring(i + 1)))
                }
                return setSigma(A, B, step, hs)
            }
        }
        rules[A]!!.forEach { rule ->
            for(i in rule.indices) {
                if (nonterminals.contains(rule[i])) {
                    sigma(rule[i], B, step - 1).forEach {
                        hs.add(
                            multiply(
                                mutableListOf(
                                    it,
                                    first(rule.substring(i + 1))
                                ), k
                            )
                        )
                    }
                }
            }
        }
        return setSigma(A, B, step, hs)
    }

    fun setSigma(A: Char, B: Char, step: Int, sigma: Set<Set<String>>): Set<Set<String>> {
        sigma_i[Triple(A, B, step)] = sigma.toSet()
        return sigma.toSet()
    }

    fun sigma(symb: Char): Set<Set<String>> {
        var step = -1
        var k1 = 0
        var k2 = -1
        while (k1 != k2) {
            step++
            k1 = k2
            k2 = 0
            for(A in nonterminals) {
                for (B in nonterminals) {
                    k2 += sigma(A, B, step).size
                }
            }
        }
        return sigma('S', symb, step)
    }

    fun setK(k: Int) {
        if (this.k != k) {
            this.k = k
            sigma_i = hashMapOf()
        }
    }
}

fun String.applyRule(symb: Char, str: String) = this.replaceFirst(symb.toString(), str)

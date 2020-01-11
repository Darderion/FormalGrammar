
fun main() {
    // Example of a grammar-constructing string:
    //      S -> aaA, A -> aAc, A -> bBc, B -> bBc, B -> bc, 2
    val g = constructGrammar(readLine().toString())
    println(g.isLLk())
}

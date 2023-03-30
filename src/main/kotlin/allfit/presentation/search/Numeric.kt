package allfit.presentation.search

enum class NumericOperator(val symbol: String, val comparator: (Int, Int) -> Boolean) {
    Equals("=", { x, y -> x == y }),
    NotEquals("!=", { x, y -> x != y }),
    GreaterEquals(">=", { x, y -> x >= y }),
    LowerEquals("<=", { x, y -> x <= y });

    companion object {
        fun bySymbol(search: String) = NumericOperator.values().firstOrNull { it.symbol == search }
            ?: error("Invalid numeric operator symbol: '$search'")
    }
}

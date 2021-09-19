class Tokenizer(
    private val doubleDelimiter: Char = '.',
    private val argumentsDelimiter: Char = ','
) {

    companion object {
        val allFunctions = mapOf(
            "cos" to Token.Function.Cos,
            "sin" to Token.Function.Sin,
            "tan" to Token.Function.Tan,
            "ln" to Token.Function.Ln,
            "log" to Token.Function.Log
        )

        private val digitChars = ('0'..'9').toSet()
        private val letterChars = ('A'..'Z').toSet() + ('a'..'z').toSet() + '_'
        private val functionKeys = allFunctions.keys
    }

    fun tokenize(expression: String): List<Token> {
        val result = mutableListOf<Token>()

        var ix = 0

        while (ix < expression.length) {
            var nextToken: Token? = null
            val symbol = expression[ix]
            val restOfExpression = expression.substring(ix)

            when (symbol) {
                in digitChars -> {
                    var lastIxOfNumber = restOfExpression.indexOfFirst { it !in (digitChars + doubleDelimiter) }
                    if (lastIxOfNumber == -1) lastIxOfNumber = restOfExpression.length
                    val strNum = restOfExpression.substring(0, lastIxOfNumber)
                    val parsedNumber = requireNotNull(strNum.toDoubleOrNull()) { "error parsing number '$strNum'" }
                    result.add(Token.Operand.Num(parsedNumber))
                    ix += lastIxOfNumber - 1
                }
                in letterChars -> {
                    val functionUsed = functionKeys.find { restOfExpression.startsWith("$it(") }
                    if (functionUsed != null) {
                        nextToken = allFunctions[functionUsed]
                        ix += functionUsed.length - 1
                    } else {
                        var lastIxOfVar = restOfExpression.indexOfFirst {
                            it !in letterChars && it !in digitChars
                        }
                        if (lastIxOfVar == -1) lastIxOfVar = restOfExpression.length

                        nextToken = Token.Operand.Variable(restOfExpression.substring(0, lastIxOfVar))
                        ix += lastIxOfVar - 1
                    }
                }
                argumentsDelimiter -> nextToken = Token.Function.Delimiter
                '+' -> nextToken = if (supposedToBeUnaryOperator(result)) Token.Operator.UnaryPlus else Token.Operator.Sum
                '-' -> nextToken = if (supposedToBeUnaryOperator(result)) Token.Operator.UnaryMinus else Token.Operator.Sub
                '*' -> nextToken = Token.Operator.Mult
                '/' -> nextToken = Token.Operator.Div
                '^' -> nextToken = Token.Operator.Pow
                '(' -> nextToken = Token.Bracket.Left
                ')' -> nextToken = Token.Bracket.Right
            }

            if (nextToken != null) result.add(nextToken)
            ix++
        }

        return result
    }

    private fun supposedToBeUnaryOperator(result: MutableList<Token>): Boolean {
        return result.isEmpty() ||
                result.last() !is Token.Operand.Num &&
                result.last() !is Token.Bracket.Right &&
                result.last() !is Token.Operand.Variable
    }
}
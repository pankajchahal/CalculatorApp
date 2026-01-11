package com.example.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayDeque

class MainActivity : AppCompatActivity() {
    private lateinit var display: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        display = findViewById(R.id.display)

        // Numeric buttons
        val numIds = listOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot)
        for (id in numIds) findViewById<Button>(id).setOnClickListener { appendToExpression((it as Button).text.toString()) }

        // Operators
        findViewById<Button>(R.id.btnAdd).setOnClickListener { appendOperator("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { appendOperator("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { appendOperator("*") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { appendOperator("/") }

        // Special actions
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnClear).setOnLongClickListener { clearAll(); true }
        findViewById<Button>(R.id.btnBack).setOnClickListener { backspace() }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { applyPercent() }
        findViewById<Button>(R.id.btnPlusMinus).setOnClickListener { toggleSign() }
        findViewById<Button>(R.id.btnEq).setOnClickListener { evaluateAndShow() }
        // memory
        findViewById<Button>(R.id.btnMC).setOnClickListener { memoryClear() }
        findViewById<Button>(R.id.btnMR).setOnClickListener { memoryRecall() }
        findViewById<Button>(R.id.btnMPlus).setOnClickListener { memoryAdd() }
        findViewById<Button>(R.id.btnMMinus).setOnClickListener { memorySubtract() }
        // extra functions
        findViewById<Button>(R.id.btnSqrt).setOnClickListener { applySqrt() }
        findViewById<Button>(R.id.btnRecip).setOnClickListener { applyReciprocal() }
        findViewById<Button>(R.id.btnOpen).setOnClickListener { appendToExpression("(") }
        findViewById<Button>(R.id.btnClose).setOnClickListener { appendToExpression(")") }
    }

    private fun appendToExpression(s: String) {
        val cur = display.text.toString()
        if (cur == "0") display.text = s else display.append(s)
    }

    private fun appendOperator(op: String) {
        val cur = display.text.toString()
        if (cur.isEmpty()) return
        val last = cur.last()
        if (last == '+' || last == '-' || last == '*' || last == '/' || last == '.') {
            // replace operator
            display.text = cur.dropLast(1) + op
        } else {
            display.append(op)
        }
    }

    private fun clearAll() {
        display.text = "0"
    }

    private fun backspace() {
        val cur = display.text.toString()
        if (cur.length <= 1) display.text = "0" else display.text = cur.dropLast(1)
    }

    private fun applyPercent() {
        // apply percent to the last number: convert x -> x/100
        val expr = display.text.toString()
        val (prefix, number) = splitLastNumber(expr)
        val v = number.toDoubleOrNull() ?: return
        val out = (v / 100.0).toString().removeSuffix(".0")
        display.text = prefix + out
    }

    private fun toggleSign() {
        val expr = display.text.toString()
        val (prefix, number) = splitLastNumber(expr)
        if (number.isEmpty()) return
        val v = number.toDoubleOrNull() ?: return
        val out = if (v == 0.0) "0" else ( -v ).toString().removeSuffix(".0")
        display.text = prefix + out
    }

    private fun splitLastNumber(expr: String): Pair<String, String> {
        if (expr.isEmpty()) return Pair("", "")
        var i = expr.length - 1
        while (i >= 0 && (expr[i].isDigit() || expr[i] == '.')) i--
        val prefix = expr.substring(0, i + 1)
        val number = expr.substring(i + 1)
        return Pair(prefix, number)
    }

    private fun evaluateAndShow() {
        val expr = display.text.toString()
        try {
            val result = evaluateExpression(expr)
            display.text = if (result == null || result.isNaN()) "Error" else result.toString().removeSuffix(".0")
        } catch (e: Exception) {
            display.text = "Error"
        }
    }

    // Memory
    private var memory: Double = 0.0
    private fun memoryClear() { memory = 0.0 }
    private fun memoryRecall() { display.text = memory.toString().removeSuffix(".0") }
    private fun memoryAdd() {
        val v = display.text.toString().toDoubleOrNull() ?: return
        memory += v
    }
    private fun memorySubtract() {
        val v = display.text.toString().toDoubleOrNull() ?: return
        memory -= v
    }

    private fun applySqrt() {
        val expr = display.text.toString()
        val (prefix, number) = splitLastNumber(expr)
        val v = number.toDoubleOrNull() ?: return
        if (v < 0) { display.text = "Error"; return }
        val out = kotlin.math.sqrt(v).toString().removeSuffix(".0")
        display.text = prefix + out
    }

    private fun applyReciprocal() {
        val expr = display.text.toString()
        val (prefix, number) = splitLastNumber(expr)
        val v = number.toDoubleOrNull() ?: return
        if (v == 0.0) { display.text = "Error"; return }
        val out = (1.0 / v).toString().removeSuffix(".0")
        display.text = prefix + out
    }

    // Shunting-yard algorithm -> evaluate RPN
    private fun evaluateExpression(expr: String): Double? {
        val tokens = tokenize(expr)
        if (tokens.isEmpty()) return 0.0

        val output = ArrayDeque<String>()
        val ops = ArrayDeque<String>()

        fun precedence(op: String) = when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            else -> 0
        }

        var i = 0
        while (i < tokens.size) {
            val t = tokens[i]
            when {
                t.toDoubleOrNull() != null -> output.addLast(t)
                t == "(" -> ops.addLast(t)
                t == ")" -> {
                    while (ops.isNotEmpty() && ops.last() != "(") output.addLast(ops.removeLast())
                    if (ops.isNotEmpty() && ops.last() == "(") ops.removeLast()
                }
                else -> {
                    while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(t)) output.addLast(ops.removeLast())
                    ops.addLast(t)
                }
            }
            i++
        }
        while (ops.isNotEmpty()) output.addLast(ops.removeLast())

        // Evaluate RPN
        val stack = ArrayDeque<Double>()
        for (tk in output) {
            val num = tk.toDoubleOrNull()
            if (num != null) stack.addLast(num) else {
                if (stack.size < 2) return Double.NaN
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (tk) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b == 0.0) Double.NaN else a / b
                    else -> return Double.NaN
                }
                stack.addLast(res)
            }
        }
        return if (stack.size == 1) stack.last() else Double.NaN
    }

    private fun tokenize(expr: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) { sb.append(expr[i]); i++ }
                    out.add(sb.toString())
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' -> {
                    // handle unary minus: if '-' at start or after '(', treat as part of number
                    if (c == '-' && (out.isEmpty() || out.last() == "(")) {
                        // read number with leading minus
                        i++
                        val sb = StringBuilder("-")
                        while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) { sb.append(expr[i]); i++ }
                        out.add(sb.toString())
                    } else {
                        out.add(c.toString()); i++
                    }
                }
                else -> return emptyList()
            }
        }
        return out
    }
}
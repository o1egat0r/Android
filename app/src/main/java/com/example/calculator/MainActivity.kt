package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var textView: TextView? = null
    private var expression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)

        findViewById<TextView>(R.id.button_0).setOnClickListener { setTextFields("0") }
        findViewById<TextView>(R.id.button_1).setOnClickListener { setTextFields("1") }
        findViewById<TextView>(R.id.button_2).setOnClickListener { setTextFields("2") }
        findViewById<TextView>(R.id.button_3).setOnClickListener { setTextFields("3") }
        findViewById<TextView>(R.id.button_4).setOnClickListener { setTextFields("4") }
        findViewById<TextView>(R.id.button_5).setOnClickListener { setTextFields("5") }
        findViewById<TextView>(R.id.button_6).setOnClickListener { setTextFields("6") }
        findViewById<TextView>(R.id.button_7).setOnClickListener { setTextFields("7") }
        findViewById<TextView>(R.id.button_8).setOnClickListener { setTextFields("8") }
        findViewById<TextView>(R.id.button_9).setOnClickListener { setTextFields("9") }
        findViewById<TextView>(R.id.button_dot).setOnClickListener { setTextFields(".") }
        findViewById<TextView>(R.id.button_parentheses_open).setOnClickListener { setTextFields("(") }
        findViewById<TextView>(R.id.button_parentheses_close).setOnClickListener { setTextFields(")") }
        findViewById<TextView>(R.id.button_plus).setOnClickListener { setTextFields("+") }
        findViewById<TextView>(R.id.button_minus).setOnClickListener { setTextFields("-") }
        findViewById<TextView>(R.id.button_multiply).setOnClickListener { setTextFields("*") }
        findViewById<TextView>(R.id.button_divide).setOnClickListener { setTextFields("/") }

        findViewById<Button>(R.id.button_AC).setOnClickListener {
            expression = ""
            textView?.text = ""
        }

        findViewById<Button>(R.id.button_backspace).setOnClickListener {
            expression = expression.dropLast(1)
            textView?.text = expression
        }

        findViewById<Button>(R.id.button_equals).setOnClickListener {
            expression = eval(expression).toString()
            textView?.text = expression
        }
    }

    private fun setTextFields(value: String) {
        expression += value
        textView?.text = expression
    }

    private fun eval(expr: String): Double {
        return try {
            val parts = expr.split('+', '-', '*', '/')

            val a = parts[0].toDouble()
            val b = parts[1].toDouble()
            val oper = expr.first { it in "+-*/" }

            when (oper) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> a / b
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }



}

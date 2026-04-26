package com.example.tictactoe

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var resetButton: Button
    private lateinit var botSwitch: SwitchCompat
    private lateinit var boardButtons: List<Button>
    private var defaultCellTint: ColorStateList? = null
    private var defaultCellTextColors: ColorStateList? = null

    private val board = Array(9) { "" }
    private var currentPlayer = "X"
    private var gameFinished = false
    private var playVsBot = false

    private val winLines = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.tvStatus)
        resetButton = findViewById(R.id.btnReset)
        botSwitch = findViewById(R.id.switchBot)

        boardButtons = listOf(
            findViewById(R.id.btn0),
            findViewById(R.id.btn1),
            findViewById(R.id.btn2),
            findViewById(R.id.btn3),
            findViewById(R.id.btn4),
            findViewById(R.id.btn5),
            findViewById(R.id.btn6),
            findViewById(R.id.btn7),
            findViewById(R.id.btn8)
        )
        defaultCellTint = boardButtons.firstOrNull()?.backgroundTintList
        defaultCellTextColors = boardButtons.firstOrNull()?.textColors

        boardButtons.forEachIndexed { index, button ->
            button.setOnClickListener { onCellClicked(index) }
        }

        botSwitch.setOnCheckedChangeListener { _, isChecked ->
            playVsBot = isChecked
            resetGame()
        }

        resetButton.setOnClickListener { resetGame() }
        updateStatus()
    }

    private fun onCellClicked(index: Int) {
        if (gameFinished || board[index].isNotEmpty() || (playVsBot && currentPlayer == "O")) return

        applyMove(index, currentPlayer)
        if (finishIfNeeded(currentPlayer)) {
            return
        }
        togglePlayer()
        updateStatus()

        if (playVsBot && currentPlayer == "O" && !gameFinished) {
            makeBotMove()
        }
    }

    private fun makeBotMove() {
        val move = chooseBestBotMove() ?: return
        applyMove(move, "O")
        if (finishIfNeeded("O")) {
            return
        }
        togglePlayer()
        updateStatus()
    }

    private fun chooseBestBotMove(): Int? {
        val emptyCells = board.indices.filter { board[it].isEmpty() }
        if (emptyCells.isEmpty()) return null

        findCriticalMove("O")?.let { return it }
        findCriticalMove("X")?.let { return it }

        if (board[4].isEmpty()) return 4

        val corners = listOf(0, 2, 6, 8).filter { board[it].isEmpty() }
        if (corners.isNotEmpty()) return corners.random()

        return emptyCells.random()
    }

    private fun findCriticalMove(player: String): Int? {
        return winLines.asSequence()
            .mapNotNull { line ->
                val occupiedByPlayer = line.count { board[it] == player }
                val empty = line.firstOrNull { board[it].isEmpty() }
                if (occupiedByPlayer == 2 && empty != null) empty else null
            }
            .firstOrNull()
    }

    private fun applyMove(index: Int, player: String) {
        board[index] = player
        val moveColor = if (player == "X") {
            ContextCompat.getColor(this, R.color.cell_x_color)
        } else {
            ContextCompat.getColor(this, R.color.cell_o_color)
        }

        boardButtons[index].apply {
            text = player
            setTextColor(moveColor)
        }
    }

    private fun finishIfNeeded(player: String): Boolean {
        val winningLine = findWinningLine(player)
        if (winningLine != null) {
            highlightWinningLine(winningLine)
            statusText.text = getString(R.string.player_won, player)
            gameFinished = true
            return true
        }
        if (board.all { it.isNotEmpty() }) {
            statusText.text = getString(R.string.draw)
            gameFinished = true
            return true
        }
        return false
    }

    private fun findWinningLine(player: String): List<Int>? {
        return winLines.firstOrNull { line ->
            line.all { board[it] == player }
        }
    }

    private fun highlightWinningLine(line: List<Int>) {
        val winColor = ContextCompat.getColor(this, android.R.color.holo_green_dark)
        line.forEach { index ->
            boardButtons[index].backgroundTintList = ColorStateList.valueOf(winColor)
        }
    }

    private fun togglePlayer() {
        currentPlayer = if (currentPlayer == "X") "O" else "X"
    }

    private fun resetGame() {
        board.indices.forEach { board[it] = "" }
        boardButtons.forEach { button ->
            button.text = ""
            button.backgroundTintList = defaultCellTint
            defaultCellTextColors?.let { button.setTextColor(it) }
        }
        currentPlayer = "X"
        gameFinished = false
        updateStatus()
    }

    private fun updateStatus() {
        if (playVsBot && currentPlayer == "O") {
            statusText.text = getString(R.string.bot_thinking)
        } else {
            statusText.text = getString(R.string.turn_of_player, currentPlayer)
        }
    }
}

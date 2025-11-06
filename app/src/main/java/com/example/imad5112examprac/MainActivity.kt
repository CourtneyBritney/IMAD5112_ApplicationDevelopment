package com.example.imad5112examprac



//import android.os.Bundle
//import android.text.Editable
//import android.text.InputFilter
//import android.text.TextWatcher
//import android.view.View
//import android.widget.*
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    // Parallel arrays
    private val movieTitles  = arrayListOf("Inception", "Black Panther", "Spirited Away")
    private val movieGenres  = arrayListOf("Sci-Fi", "Action", "Animation")
    private val movieRatings = arrayListOf(5, 4, 5)            // integers 1–5
    private val movieReviews = arrayListOf(
        "Mind-bending and brilliant.",
        "Cultural milestone with heart.",
        "Pure magic; timeless."
    )

    // Limits
    private val MAX_TITLE_LEN   = 60
    private val MAX_GENRE_LEN   = 30
    private val MAX_REVIEW_LEN  = 240
    private val MAX_RATING      = 5

    // Views (late init for convenience)
    private lateinit var screenSplash: LinearLayout
    private lateinit var screenMenu: LinearLayout
    private lateinit var screenAdd: LinearLayout
    private lateinit var screenList: LinearLayout

    private lateinit var menuAddBtn: Button
    private lateinit var menuViewBtn: Button

    private lateinit var titleEt: EditText
    private lateinit var genreEt: EditText
    private lateinit var ratingEt: EditText
    private lateinit var reviewEt: EditText
    private lateinit var addBtn: Button
    private lateinit var gotoListBtn: Button

    private lateinit var listView: ListView
    private lateinit var avgText: TextView
    private lateinit var clearAllBtn: Button
    private lateinit var backFromAddBtn: Button
    private lateinit var backFromListBtn: Button

    private fun showScreen(target: View) {
        screenSplash.visibility = if (target == screenSplash) View.VISIBLE else View.GONE
        screenMenu.visibility   = if (target == screenMenu)   View.VISIBLE else View.GONE
        screenAdd.visibility    = if (target == screenAdd)    View.VISIBLE else View.GONE
        screenList.visibility   = if (target == screenList)   View.VISIBLE else View.GONE
    }

    private fun updateMenuButtons() {
        // Only enable View Reviews when we actually have items
        menuViewBtn.isEnabled = movieTitles.isNotEmpty()
        gotoListBtn.isEnabled = movieTitles.isNotEmpty()
    }

    private fun updateAverage() {
        // Accurate average using Double; round to one decimal
        val avg = if (movieRatings.isEmpty()) 0.0
        else {
            val sum = movieRatings.fold(0) { acc, v -> acc + v }
            (round((sum.toDouble() / movieRatings.size) * 10.0) / 10.0)
        }
        avgText.text = "Average rating: ${"%.1f".format(avg)} (${movieRatings.size} movie${if (movieRatings.size == 1) "" else "s"})"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Splash + Menu
        screenSplash = findViewById(R.id.screenSplash)
        screenMenu   = findViewById(R.id.screenMenu)

        // Add screen
        screenAdd = findViewById(R.id.screenAdd)
        titleEt   = findViewById(R.id.etTitle)
        genreEt   = findViewById(R.id.etGenre)
        ratingEt  = findViewById(R.id.etRating)
        reviewEt  = findViewById(R.id.etReview)
        addBtn    = findViewById(R.id.btnAddMovie)
        gotoListBtn = findViewById(R.id.btnViewReviewsFromAdd)
        backFromAddBtn = findViewById(R.id.btnBackFromAdd)

        // List screen
        screenList = findViewById(R.id.screenList)
        listView   = findViewById(R.id.listView)
        avgText    = findViewById(R.id.tvAverage)
        clearAllBtn = findViewById(R.id.btnClearAll)
        backFromListBtn = findViewById(R.id.btnBackFromList)

        // Menu buttons
        menuAddBtn = findViewById(R.id.btnMenuAdd)
        menuViewBtn = findViewById(R.id.btnMenuView)

        // Input hard limits
        titleEt.filters  = arrayOf(InputFilter.LengthFilter(MAX_TITLE_LEN))
        genreEt.filters  = arrayOf(InputFilter.LengthFilter(MAX_GENRE_LEN))
        ratingEt.filters = arrayOf(InputFilter.LengthFilter(1)) // 1–5
        reviewEt.filters = arrayOf(InputFilter.LengthFilter(MAX_REVIEW_LEN))

        // Splash then Menu
        showScreen(screenSplash)
        Handler(Looper.getMainLooper()).postDelayed({
            showScreen(screenMenu)
            updateMenuButtons()
        }, 1500)

        // Clear error as user types
        fun watcherFor(et: EditText) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!et.text.isNullOrBlank()) et.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        listOf(titleEt, genreEt, ratingEt, reviewEt).forEach { it.addTextChangedListener(watcherFor(it)) }

        fun focusError(field: EditText, msg: String) {
            field.error = msg
            field.requestFocus()
            field.setSelection(field.text?.length ?: 0)
        }

        fun isLettersSpaces(s: String) = s.matches(Regex("^[\\p{L}][\\p{L} .,&-]*[\\p{L}]$"))
        fun hasVowel(s: String) = s.contains(Regex("[AEIOUaeiou]"))

        fun validate(): Boolean {
            val title  = titleEt.text.toString().trim()
            val genre  = genreEt.text.toString().trim()
            val rateTx = ratingEt.text.toString().trim()
            val review = reviewEt.text.toString().trim()

            if (title.isEmpty() || title.length !in 2..MAX_TITLE_LEN || !isLettersSpaces(title) || !hasVowel(title)) {
                focusError(titleEt, "Enter a real movie title (letters/spaces, 2–$MAX_TITLE_LEN).")
                return false
            }
            if (genre.isEmpty() || genre.length !in 3..MAX_GENRE_LEN || !isLettersSpaces(genre) || !hasVowel(genre)) {
                focusError(genreEt, "Enter a valid genre (e.g., Drama) 3–$MAX_GENRE_LEN.")
                return false
            }
            if (rateTx.isEmpty()) {
                focusError(ratingEt, "Enter a rating 1–5.")
                return false
            }
            val rating = rateTx.toIntOrNull()
            if (rating == null || rating !in 1..MAX_RATING) {
                focusError(ratingEt, "Rating must be a whole number 1–5.")
                return false
            }
            if (review.isEmpty()) {
                focusError(reviewEt, "Write a short review.")
                return false
            }
            if (review.length > MAX_REVIEW_LEN) {
                focusError(reviewEt, "Keep reviews under $MAX_REVIEW_LEN characters.")
                return false
            }

            val dup = movieTitles.indices.any { i ->
                movieTitles[i].equals(title, true) && movieGenres[i].equals(genre, true)
            }
            if (dup) {
                Toast.makeText(this, "That movie in that genre is already reviewed.", Toast.LENGTH_LONG).show()
                focusError(titleEt, "Duplicate entry.")
                return false
            }
            return true
        }

        fun refreshListAndAverage() {
            val items = ArrayList<String>(movieTitles.size)
            for (i in movieTitles.indices) {
                items.add("★${movieRatings[i]}  ${movieTitles[i]}  (${movieGenres[i]})\n${movieReviews[i]}")
            }
            listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
            updateAverage()
        }

        // --- Menu navigation ---
        menuAddBtn.setOnClickListener { showScreen(screenAdd) }
        menuViewBtn.setOnClickListener {
            if (movieTitles.isEmpty()) {
                Toast.makeText(this, "No reviews yet. Add your first movie.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            refreshListAndAverage()
            showScreen(screenList)
        }

        // --- Add screen actions ---
        addBtn.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val title  = titleEt.text.toString().trim()
            val genre  = genreEt.text.toString().trim()
            val rating = ratingEt.text.toString().trim().toInt()
            val review = reviewEt.text.toString().trim()

            movieTitles.add(title)
            movieGenres.add(genre)
            movieRatings.add(rating)
            movieReviews.add(review)

            Toast.makeText(this, "Saved review for \"$title\" ($genre) ★$rating", Toast.LENGTH_LONG).show()

            // Clear inputs and enable review navigation
            titleEt.text.clear(); genreEt.text.clear(); ratingEt.text.clear(); reviewEt.text.clear()
            titleEt.requestFocus()
            updateMenuButtons()
        }

        gotoListBtn.setOnClickListener {
            if (movieTitles.isEmpty()) {
                Toast.makeText(this, "No reviews yet. Add your first movie.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            refreshListAndAverage()
            showScreen(screenList)
        }

        backFromAddBtn.setOnClickListener { showScreen(screenMenu) }

        // --- List screen actions ---
        clearAllBtn.setOnClickListener {
            if (movieTitles.isEmpty()) {
                Toast.makeText(this, "There’s nothing to clear.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Clear All Reviews?")
                .setMessage("This will remove every movie review. Continue?")
                .setPositiveButton("Yes, Clear All") { _, _ ->
                    movieTitles.clear(); movieGenres.clear(); movieRatings.clear(); movieReviews.clear()
                    listView.adapter = null
                    updateAverage()
                    updateMenuButtons()
                    Toast.makeText(this, "All reviews cleared.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        backFromListBtn.setOnClickListener { showScreen(screenMenu) }
    }
}

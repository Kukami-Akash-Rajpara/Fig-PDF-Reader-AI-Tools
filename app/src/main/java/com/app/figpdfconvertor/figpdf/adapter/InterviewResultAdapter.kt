package com.app.figpdfconvertor.figpdf.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.model.InterviewResult

class InterviewResultAdapter(
    private val list: List<InterviewResult>
) : RecyclerView.Adapter<InterviewResultAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtQuestion: TextView = itemView.findViewById(R.id.txtQuestion)
        val txtAnswer: TextView = itemView.findViewById(R.id.txtAnswer)
        val txtScore: TextView = itemView.findViewById(R.id.txtScore)
        val txtQueCount: TextView = itemView.findViewById(R.id.txtQueCount)
        val txtEvaluation: TextView = itemView.findViewById(R.id.txtEvaluation)
        val dropdown: ImageView = itemView.findViewById(R.id.dropdown)
        /*val txtStrengths: TextView = itemView.findViewById(R.id.txtStrengths)
        val txtWeaknesses: TextView = itemView.findViewById(R.id.txtWeaknesses)
        val txtToneFeedback: TextView = itemView.findViewById(R.id.txtToneFeedback)
        val txtRecommendation: TextView = itemView.findViewById(R.id.txtRecommendation)*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interview_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = list[position]
        holder.txtQuestion.text = "${item.question}"
        holder.txtAnswer.text = "${item.answer}"
        holder.txtScore.text = "Score: ${item.score}/10"

// Change text color based on score
        if (item.score >= 5) {
            holder.txtScore.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        } else {
            holder.txtScore.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        }

        holder.txtQueCount.text = "Question ${position + 1}:"
        holder.dropdown.setOnClickListener {
            if (holder.txtEvaluation.visibility == View.GONE) {
                holder.txtEvaluation.visibility = View.VISIBLE
                holder.dropdown.setImageResource(R.drawable.ic_arrow_up) // change to up arrow
            } else {
                holder.txtEvaluation.visibility = View.GONE
                holder.dropdown.setImageResource(R.drawable.ic_drop_down_arow1) // change back to down arrow
            }
        }
        val evaluationText = listOfNotNull(
            item.strengths.takeIf { !it.isNullOrBlank() }?.let { "Strengths: $it" },
            item.weaknesses.takeIf { !it.isNullOrBlank() }?.let { "Weaknesses: $it" },
            item.communicationToneFeedback.takeIf { !it.isNullOrBlank() }?.let { "Tone Feedback: $it" },
            item.recommendation.takeIf { !it.isNullOrBlank() }?.let { "Recommendation: $it" }
        )

        if (evaluationText.isNotEmpty()) {
            val spannable = SpannableString(evaluationText.joinToString("\n"))
            var start = 0

            for (line in evaluationText) {
                val end = start + line.length

                // Add bullet
                spannable.setSpan(BulletSpan(15), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                // Bold the label before colon
                val colonIndex = line.indexOf(":")
                if (colonIndex != -1) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        start + colonIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                start = end + 1 // +1 for newline
            }

            holder.txtEvaluation.text = spannable
        //    holder.txtEvaluation.visibility = View.VISIBLE
        } else {
            holder.txtEvaluation.visibility = View.GONE
        }


        /*holder.txtStrengths.text = "Strengths: ${item.strengths}"
        holder.txtWeaknesses.text = "Weaknesses: ${item.weaknesses}"
        holder.txtToneFeedback.text = "Tone Feedback: ${item.communicationToneFeedback}"
        holder.txtRecommendation.text = "Recommendation: ${item.recommendation}"*/
    }

    override fun getItemCount(): Int = list.size
}

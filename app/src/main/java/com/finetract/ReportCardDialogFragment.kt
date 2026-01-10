package com.finetract

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ReportCardDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_report_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val report = ReportCardManager.generateReport(requireContext())
        if (report == null) {
            // Should usually not happen if logic checked before showing, but close if so
            dismiss()
            return
        }

        // Bind Data
        view.findViewById<TextView>(R.id.tv_grade).text = report.grade
        view.findViewById<TextView>(R.id.tv_attendance).text = "${report.disciplineDays} / ${report.totalDays} Days"
        view.findViewById<TextView>(R.id.tv_remark).text = "\"${report.remark}\""
        view.findViewById<TextView>(R.id.tv_tip).text = report.tip

        // Subjects Table
        val container = view.findViewById<LinearLayout>(R.id.container_subjects)
        container.removeAllViews()

        for ((subject, status) in report.subjectGrades) {
            if (status == "-") continue // Skip empty subjects

            val row = LinearLayout(context)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, 8, 0, 8)
            
            val tvName = TextView(context)
            tvName.text = subject
            tvName.setTextColor(android.graphics.Color.parseColor("#5D4037"))
            tvName.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            
            val tvStatus = TextView(context)
            tvStatus.text = status.uppercase()
            tvStatus.textSize = 12f
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD)
            
            if (status == "Good") {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#388E3C")) // Green
            } else {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F")) // Red
            }
            
            row.addView(tvName)
            row.addView(tvStatus)
            container.addView(row)
        }

        view.findViewById<View>(R.id.btn_dismiss).setOnClickListener {
            ReportCardManager.markReportShown(requireContext())
            dismiss()
        }
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

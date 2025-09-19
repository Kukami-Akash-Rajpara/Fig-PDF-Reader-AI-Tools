package com.app.figpdfconvertor.figpdf.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.activity.PDFViewerActivity
import com.app.figpdfconvertor.figpdf.activity.SummeryViewActivity
import com.app.figpdfconvertor.figpdf.adapter.TxtFileAdapter
import com.app.figpdfconvertor.figpdf.databinding.FragmentHistoryBinding
import com.app.figpdfconvertor.figpdf.adapter.PdfFileAdapter
import com.app.figpdfconvertor.figpdf.model.PdfFileItem
import com.app.figpdfconvertor.figpdf.utils.ImageToPDFOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import java.io.File

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val pdfItems = mutableListOf<PdfFileItem>()
    private var mPdfOptions: ImageToPDFOptions? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        mPdfOptions = ImageToPDFOptions()
    }

    private fun setupTabs() {
        val tabs = listOf(
            getString(R.string.pdf_converter),
            getString(R.string.pdf_summarizer),
            getString(R.string.ai_ppt_maker),
            getString(R.string.resume_analyzer)
        )

        tabs.forEachIndexed { index, title ->
            val tab = binding.tabHistory.newTab()
            val customView = layoutInflater.inflate(R.layout.item_tab, null)
            val tabText = customView.findViewById<MaterialTextView>(R.id.tabText)
            tabText.text = title

            if (index == 0) tabText.setTypeface(
                ResourcesCompat.getFont(
                    requireContext(),
                    R.font.roboto_semi_bold
                )
            )

            tab.customView = customView
            binding.tabHistory.addTab(tab)
        }

        binding.tabHistory.getTabAt(0)?.let { firstTab ->
            firstTab.select()
            loadPdfFiles(getPdfOutputFolder())
        }

        binding.tabHistory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabText = tab.customView?.findViewById<TextView>(R.id.tabText)
                tabText?.setTypeface(
                    ResourcesCompat.getFont(
                        requireContext(),
                        R.font.roboto_semi_bold
                    )
                )

                when (tab.position) {
                    0 -> { // PDF Tab
                        val files = getPdfFilesFromFolder(getPdfOutputFolder())
                        if (files.isEmpty()) {
                            binding.rvHistory.visibility = View.GONE
                            binding.llNoData.visibility = View.VISIBLE
                        } else {
                            binding.rvHistory.visibility = View.VISIBLE
                            binding.llNoData.visibility = View.GONE
                            binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
                            binding.rvHistory.adapter = PdfFileAdapter(
                                mPdfOptions,files as MutableList<PdfFileItem>,requireActivity(),requireActivity().supportFragmentManager,
                                onShareClick = { fileItem ->
                                    sharePdfFile(fileItem.file) // share functionality
                                },
                                onItemClick = { fileItem ->
                                    val intent =
                                        Intent(requireActivity(), PDFViewerActivity::class.java)
                                    intent.putExtra(
                                        "pdf_path",
                                        fileItem.file.absolutePath
                                    ) // pass PDF path
                                    startActivity(intent)
                                }
                            )
                        }
                    }  // PDF only
                    1 -> { // TXT Tab
                        val files = getTxtFilesFromFolder(getPdfSummarizerFolder())
                        if (files.isEmpty()) {
                            binding.rvHistory.visibility = View.GONE
                            binding.llNoData.visibility = View.VISIBLE
                        } else {
                            binding.rvHistory.visibility = View.VISIBLE
                            binding.llNoData.visibility = View.GONE
                            binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
                            binding.rvHistory.adapter = TxtFileAdapter(files) { fileItem ->
                                val intent =
                                    Intent(requireActivity(), SummeryViewActivity::class.java)
                                intent.putExtra(
                                    "txt_path",
                                    fileItem.file.absolutePath
                                ) // pass the full path
                                startActivity(intent)
                            }
                        }
                    }// TXT only
                    2, 3 -> {
                        binding.rvHistory.visibility = View.GONE
                        binding.llNoData.visibility = View.VISIBLE
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabText = tab.customView?.findViewById<TextView>(R.id.tabText)
                tabText?.setTypeface(
                    ResourcesCompat.getFont(
                        requireContext(),
                        R.font.roboto_regular
                    )
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun getPdfSummarizerFolder(): File {
        val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appName: String? = getString(R.string.app_name)
        val summarizerFolder = File(docs, "$appName/PDF Summarizer")
        if (!summarizerFolder.exists()) summarizerFolder.mkdirs()
        Log.e("HistoryFragment", "Summarizer folder path: ${summarizerFolder.absolutePath}")
        return summarizerFolder
    }

    private fun loadPdfFiles(folder: File) {
        pdfItems.clear()
        val files = getPdfFilesFromFolder(folder)
        Log.e("HistoryFragment", "Found PDFs: ${files.map { it.name }}")
        pdfItems.addAll(files)

        if (pdfItems.isEmpty()) {
            binding.rvHistory.visibility = View.GONE
            binding.llNoData.visibility = View.VISIBLE
        } else {
            binding.rvHistory.visibility = View.VISIBLE
            binding.llNoData.visibility = View.GONE

            binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
            binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
            binding.rvHistory.adapter = PdfFileAdapter(
                mPdfOptions,files as MutableList<PdfFileItem>,requireActivity(),requireActivity().supportFragmentManager,
                onShareClick = { fileItem ->
                    sharePdfFile(fileItem.file) // share functionality
                },
                onItemClick = { fileItem ->
                    val intent = Intent(requireActivity(), PDFViewerActivity::class.java)
                    intent.putExtra("pdf_path", fileItem.file.absolutePath) // pass PDF path
                    startActivity(intent)
                }
            )
        }
    }


    private fun sharePdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getPdfOutputFolder(): File {
        val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appName: String? = getString(R.string.app_name)
        val appFolder = File(docs, "$appName/ImageToPDF")
        if (!appFolder.exists()) appFolder.mkdirs()
        Log.e("HistoryFragment", "PDF folder path: ${appFolder.absolutePath}")
        return appFolder
    }

    private fun getTxtFilesFromFolder(folder: File): List<PdfFileItem> {
        if (!folder.exists()) return emptyList()

        val files = folder.listFiles { file -> file.isFile && file.extension.equals("txt", true) }
            ?: emptyArray()
        val list = mutableListOf<PdfFileItem>()

        for (file in files) {
            list.add(
                PdfFileItem(
                    file = file,
                    name = file.name,
                    thumbnail = null, // no preview for txt
                    createdTime = file.lastModified(),
                    size = file.length()
                )
            )
        }

        Log.e("HistoryFragment", "TXT files found: ${list.size}")
        return list
    }

    private fun getPdfFilesFromFolder(folder: File): List<PdfFileItem> {
        if (!folder.exists()) return emptyList()

        val files = folder.listFiles { file -> file.isFile && file.extension.equals("pdf", true) }
            ?: emptyArray()
        val list = mutableListOf<PdfFileItem>()

        for (file in files) {
            var thumbnail: Bitmap? = null
            try {
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    thumbnail =
                        Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                }
                renderer.close()
                fd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            list.add(
                PdfFileItem(
                    file = file,
                    name = file.name,
                    thumbnail = thumbnail,
                    createdTime = file.lastModified(),
                    size = file.length()
                )
            )
        }

        Log.e("HistoryFragment", "PDF files found: ${list.size}")
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

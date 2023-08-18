package com.example.downloadmanagerdemo

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.downloadmanagerdemo.databinding.FragmentFirstBinding
import java.io.File


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var fileName: String? = null
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            downloadFile()
        }
    }

    var dm: DownloadManager? = null

    private val TAG = FirstFragment::class.java.simpleName
    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            // your code
            Log.d(TAG, "onReceive() called with: ctxt = $ctxt, intent = $intent")
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            openFile(fileName, id)
        }
    }

    protected fun openFile(fileName: String?, id: Long) {
//        val install = Intent(Intent.ACTION_VIEW)
//        install.setDataAndType(Uri.fromFile(File(fileName)), "MIME-TYPE")
//        startActivity(install)

        val fileIntent = Intent(Intent.ACTION_VIEW)
        var downloadDirectory: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        (downloadDirectory.absolutePath + "/" + fileName).toUri()
        // Grabs the Uri for the file that was downloaded.

        // Grabs the Uri for the file that was downloaded.
        val mostRecentDownload1: Uri? = dm?.getUriForDownloadedFile(id)
        val mostRecentDownload: Uri = (downloadDirectory.absolutePath + "/" + fileName).toUri()
//        val mostRecentDownload: Uri = (context?.cacheDir?.absolutePath + "/" + fileName).toUri()
        // DownloadManager stores the Mime Type. Makes it really easy for us.
        // DownloadManager stores the Mime Type. Makes it really easy for us.
        val mimeType: String? = dm?.getMimeTypeForDownloadedFile(id)

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName.toString()
        )
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.downloadmanagerdemo" + ".fileprovider",
            file
        )

        fileIntent.setDataAndType(
            uri,
            mimeType
        )
//        fileIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            requireContext().startActivity(fileIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(), "No handler for this type of file.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun downloadFile() {
        requireActivity().registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        // https://docs.google.com/gview?embedded=true&url=https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf
        val link =
            "https://freetestdata.com/wp-content/uploads/2022/11/Free_Test_Data_10.5MB_PDF.pdf".toUri()

        val r = DownloadManager.Request(link)

        fileName = link.lastPathSegment

        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setNotificationVisibility(VISIBILITY_VISIBLE + VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .allowScanningByMediaScanner()

//        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        dm = requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
        dm!!.enqueue(r)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
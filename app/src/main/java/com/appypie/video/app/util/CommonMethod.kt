package com.appypie.video.app.util

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appypie.video.app.R
import com.appypie.video.app.util.Constants.RANDOM_ALLOWED_CHARACTERS
import com.appypie.video.app.util.Constants.meetingData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.share_layout.view.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class CommonMethod {


    private fun isValidUrl(url: String): Boolean {
        val p = Patterns.WEB_URL
        val m = p.matcher(url.toLowerCase())
        return m.matches()
    }

    companion object {


        fun validateEditText(editText: AppCompatEditText?, message: String, textInputLayout: TextInputLayout) {

            editText!!.requestFocus()
            textInputLayout.error = message

            editText.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    showError(textInputLayout, (v as EditText).text, message)
                }
            }


            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    showError(textInputLayout, s, message)
                }
            })
        }

        fun showError(textInputLayout: TextInputLayout, s: Editable, message: String) {

            if (!TextUtils.isEmpty(s)) {
                textInputLayout.error = null
            } else {
                textInputLayout.error = message
            }
        }


        @SuppressLint("SetTextI18n")
        fun showDatePicker(context: Context, textView: TextView) {
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                    context, R.style.DatePickerDialogThemes,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                        var month = "" + (monthOfYear + 1)
                        if ((monthOfYear + 1) < 10) {
                            month = "0" + (monthOfYear + 1)
                        }

                        textView.text = "$dayOfMonth/$month/$year"
                    }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
            //datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }


        fun convertMinuteToHour(duration: Int): String {

            val hours: Int = duration / 60
            val minutes: Int = duration % 60

            return when {
                hours == 0 -> {
                    String.format("%02d Minutes", minutes)
                }
                minutes == 0 -> {
                    String.format("%d Hour", hours)
                }
                else -> {
                    String.format("%d Hour, %02d Minutes", hours, minutes)
                }
            }


        }


        fun addFragment(activity: FragmentActivity, fragment: Fragment, container: Int) {
            activity.supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.fragment_slide_right_enter,
                            R.anim.fragment_slide_right_exit,
                            R.anim.fragment_slide_left_enter,
                            R.anim.fragment_slide_left_exit)
                    .add(container, fragment)
                    .commit()

        }

        fun replaceFragment(activity: FragmentActivity, fragment: Fragment, container: Int) {
            activity.supportFragmentManager
                    .beginTransaction().setCustomAnimations(
                            R.anim.fragment_slide_left_enter,
                            R.anim.fragment_slide_left_exit,
                            R.anim.fragment_slide_right_enter,
                            R.anim.fragment_slide_right_exit)
                    .addToBackStack(null)
                    .replace(container, fragment)
                    .commit()

        }

        @SuppressLint("SetTextI18n")
        fun showDatePickerEditText(context: Context, editText: EditText) {
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                    context, R.style.DatePickerDialogThemes,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        var month = "" + (monthOfYear + 1)
                        if ((monthOfYear + 1) < 10) {
                            month = "0" + (monthOfYear + 1)
                        }

                        var day = "" + dayOfMonth
                        if (dayOfMonth < 10) {
                            day = "0$dayOfMonth"
                        }

                        editText.setText("$day/$month/$year")
                    }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }

        @SuppressLint("SetTextI18n")
        fun showMonthYearPicker(context: Context, textView: TextView) {
            try {
                val c = Calendar.getInstance()
                val mYear = c.get(Calendar.YEAR)
                val mMonth = c.get(Calendar.MONTH)
                val mDay = c.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                        context,/* R.style.DatePickerDialogThemes*/AlertDialog.THEME_HOLO_LIGHT,
                        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                            var monthString = ""
                            if ((monthOfYear + 1) < 10) {
                                monthString = "0" + (monthOfYear + 1)
                            } else {
                                monthString = (monthOfYear + 1).toString()
                            }

                            textView.text = "$monthString/$year"
                        }, mYear, mMonth, mDay
                )

                (datePickerDialog.datePicker.findViewById<View>(
                        Resources.getSystem().getIdentifier(
                                "day",
                                "id",
                                "android"
                        )
                ) as View).visibility = View.GONE

                datePickerDialog.show()
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        val DISPLAY_MESSAGE_ACTION = "com.codecube.broking.gcm"

        val EXTRA_MESSAGE = "message"

        //public static final String DATE_FORMAT = "dd/MM/yyyy";  //or use "M/d/yyyy"
        val DATE_FORMAT = "yyyy-dd-MM"  //or use "M/d/yyyy"

        fun isYesterday(date: Long): Boolean {
            val now = Calendar.getInstance()
            val cdate = Calendar.getInstance()
            cdate.timeInMillis = date

            now.add(Calendar.DATE, -1)

            return (now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                    && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                    && now.get(Calendar.DATE) == cdate.get(Calendar.DATE))
        }

        fun isToday(date: Long): Boolean {
            return DateUtils.isToday(date)
        }

        fun isTablet(context: Context): Boolean {
            val xlarge =
                    context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == 4
            val large =
                    context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
            return xlarge || large
        }

        fun hasInternetConnection(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (wifiNetwork != null && wifiNetwork.isConnected) {
                return true
            }
            val mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (mobileNetwork != null && mobileNetwork.isConnected) {
                return true
            }
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }


        fun call(context: Context, number: String) {
            try {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$number")
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        fun mail(context: Context, mailId: String) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto: $mailId")
            context.startActivity(Intent.createChooser(emailIntent, "Send Mail"))
        }

        fun isTodayOrYesterday(date: Long): String {
            var str = ""
            val now = Calendar.getInstance()
            val tomorrow = Calendar.getInstance()
            val cdate = Calendar.getInstance()
            cdate.timeInMillis = date

            now.add(Calendar.DATE, -1)
            tomorrow.add(Calendar.DATE, +1)

            if (DateUtils.isToday(date)) {
                str = "Today"

            } else if (now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                    && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                    && now.get(Calendar.DATE) == cdate.get(Calendar.DATE)
            ) {
                str = "Yesterday"
            } else if (tomorrow.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                    && tomorrow.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                    && tomorrow.get(Calendar.DATE) == cdate.get(Calendar.DATE)
            ) {
                str = "Tomorrow"
            }
            return str
        }

        fun rupeesFormat(value: String): String {
            var value = value
            value = value.replace(",", "")
            val lastDigit = value[value.length - 1]
            var result = ""
            val len = value.length - 1
            var nDigits = 0
            for (i in len - 1 downTo 0) {
                result = value[i] + result
                nDigits++
                if (nDigits % 2 == 0 && i > 0) {
                    result = ",$result"
                }
            }
            return result + lastDigit
        }


        fun displayMessage(context: Context, message: String) {
            val intent = Intent(DISPLAY_MESSAGE_ACTION)
            intent.putExtra(EXTRA_MESSAGE, message)
            context.sendBroadcast(intent)
        }

        fun displayOnlyTime(str: String): String {
            val d = Date()
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            return sdf.format(str)
        }


        fun dateToString(dates: Date): String? {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            var dateTime: String? = null
            try {
                val date: Date
                date = Date()
                dateTime = dateFormat.format(date)
                println("Current Date Time : $dateTime")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return dateTime
        }

        fun displayOnlyDate(): String {
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return df.format(c)
        }

        fun getCurrentDate(): String {
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat("EEEE,dd MMM yyyy", Locale.getDefault())
            return df.format(c)
        }

        fun getCurrentDateWithoutDay(): String {
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat(",dd MMM yyyy", Locale.getDefault())
            return df.format(c)
        }


        fun getTomorrowDateWithoutDay(): String {

            val calendar = Calendar.getInstance()
            val today = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = calendar.time

            val df = SimpleDateFormat(", dd MMM yyyy", Locale.getDefault())
            val formattedDate = df.format(tomorrow)
            return formattedDate


        }

        fun convertDate(strDate: String?): String? {
            if (strDate == null || strDate.trim { it <= ' ' }.isEmpty()) {
                return ""
            }
            try {
                /* var spf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)*/
                var spf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val newDate = spf.parse(strDate)
                spf = SimpleDateFormat("EEEE,dd MMM yyyy", Locale.getDefault())
                return spf.format(newDate!!)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return strDate
        }

        // Find todays date
        val currentTimeStamp: String?
            get() {
                return try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    /* val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())*/
                    /* val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)*/
                    dateFormat.format(Date())
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

        fun getFormattedDateFromTimestamp(timestampInMilliSeconds: Long): String? {
            val date = Date()
            date.time = timestampInMilliSeconds
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(date)
        }

        fun isPastDate(startDate: String): Boolean {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val strDate = sdf.parse(startDate)
            return System.currentTimeMillis() > strDate!!.time
        }


        fun isValidToken(startDate: String): Boolean {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val strDate = sdf.parse(startDate)
                System.currentTimeMillis() < strDate!!.time
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }


        fun convertDateToTime(strDate: String?): String? {
            if (strDate == null || strDate.trim { it <= ' ' }.isEmpty()) {
                return ""
            }
            try {
                /* var spf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)*/
                var spf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss a", Locale.getDefault())
                val newDate = spf.parse(strDate)
                spf = SimpleDateFormat("HH:mm a", Locale.getDefault())
                return spf.format(newDate!!)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return strDate
        }


        fun convertDateFormat(ourDates: String): String? {
            var ourDate: String? = ourDates
            ourDate = try {

                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val value: Date = formatter.parse(ourDate)
                val dateFormatter = SimpleDateFormat("EEEE,dd MMM yyyy HH:mm", Locale.getDefault()) //this format changeable
                dateFormatter.timeZone = TimeZone.getDefault()
                dateFormatter.format(value)

                //Log.d("ourDate", ourDate);
            } catch (e: Exception) {
                "00-00-0000 00:00"
            }
            return ourDate
        }


        fun formatTimeZone(tz: TimeZone): String {
            return try {
                val gmt: String = TimeZone.getTimeZone(tz.id).getDisplayName(true, TimeZone.SHORT)
                gmt + " " + tz.id
            } catch (e: Exception) {
                ""
            }
        }


        fun hideSoftKeyboard(activity: Activity) {
            val inputMethodManager =
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isActive) {
                if (activity.currentFocus != null) {
                    inputMethodManager.hideSoftInputFromWindow(
                            activity.currentFocus!!.windowToken,
                            0
                    )
                }
            }
        }

        fun showLocationAlert(context: Context) {

            val handler = Handler()
            handler.postDelayed({
                try {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Please Enable GPS !").setCancelable(false)
                            .setPositiveButton("Settings") { dialog, id ->
                                dialog.dismiss()
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                    builder.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 1000)


        }

        fun showAlert(message: String, context: Context) {
            val builder = AlertDialog.Builder(context, R.style.AlertDialogCustom)
            builder.setCancelable(true)
            builder.setMessage(message).setCancelable(false)
                    .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
            try {
                builder.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo: NetworkInfo?
            try {
                netInfo = cm.activeNetworkInfo
                if (netInfo != null && netInfo.isConnectedOrConnecting) {
                    return true
                }
            } catch (e: Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            return false
        }


        fun showMessage(context: Context, messsage: String) {
            Toast.makeText(context, messsage, Toast.LENGTH_SHORT).show()
        }


        fun setRecyclerView(context: Context, recyclerView: RecyclerView) {
            //recyclerView.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = RecyclerView.VERTICAL
            recyclerView.layoutManager = layoutManager

        }

        fun setHRecyclerView(context: Context, recyclerView: RecyclerView) {
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = RecyclerView.HORIZONTAL
            recyclerView.layoutManager = layoutManager
        }

        fun setGridRecyclerView(context: Activity, recyclerView: RecyclerView, count: Int) {
            val layoutManager = GridLayoutManager(context, count)
            /* layoutManager.orientation = RecyclerView.VERTICAL*/
            recyclerView.layoutManager = layoutManager
            /* recyclerView.addItemDecoration(GridItemDecoration(10, 2))*/
        }


        fun isValidEmaillId(email: String): Boolean {
            val PATTERN = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,4}"
            val PATTERN1 = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{1,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|1[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|1[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|1[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")

            return Pattern.compile(PATTERN1).matcher(email).matches()
        }

        fun isValidMobile(phone: String?): Boolean {
            return if (phone == null || phone.length < 10 || phone.length > 16) {
                false
            } else {
                Patterns.PHONE.matcher(phone).matches()
            }
        }


        fun isEmpty(text: String, editText: EditText, errorMsg: String): Boolean {
            if (TextUtils.isEmpty(text.trim())) {
                editText.requestFocus()
                editText.error = errorMsg
                return true
            }
            return false

        }

        fun isNetworkAvailable(ctx: Context): Boolean {
            val connectivityManager =
                    ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }


        /*   public static void showDatePicker(Context context, final TextView textView) {

        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.DatePickerDialogThemes, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                textView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }*/

        /*get the difference number of days between two dates
     * start is start Date
     * end is end Date
     * */
        fun getCountOfDays(start: String, end: String): String {
            var numberOfDays: Long = 0
            try {
                val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
                val startDate: Date
                val endDate: Date
                numberOfDays = 0

                startDate = dateFormat.parse(start)
                endDate = dateFormat.parse(end)
                numberOfDays = getUnitBetweenDates(startDate, endDate, TimeUnit.DAYS)

                println("days are :$numberOfDays")
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return "" + numberOfDays
        }

        private fun getUnitBetweenDates(startDate: Date, endDate: Date, unit: TimeUnit): Long {
            val timeDiff = endDate.time - startDate.time
            return unit.convert(timeDiff, TimeUnit.MILLISECONDS)
        }


        @SuppressLint("SimpleDateFormat")
        fun getHoursFromTimes(fromTime: String, toTime: String): String {
            val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
            val date1: Date
            val date2: Date
            val days: Int
            var hours = 0
            val min: Int

            try {
                date1 = simpleDateFormat.parse(fromTime)
                date2 = simpleDateFormat.parse(toTime)

                val difference = date2.time - date1.time
                days = (difference / (1000 * 60 * 60 * 24)).toInt()
                hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
                min =
                        (difference - (1000 * 60 * 60 * 24 * days).toLong() - (1000 * 60 * 60 * hours).toLong()).toInt() / (1000 * 60)
                hours = if (hours < 0) -hours else hours
                println("Hours" + " :: " + hours + "min :" + min)
            } catch (e: ParseException) {
                e.printStackTrace()
            }


            return "" + hours
        }

        fun getByteArrayfromURL(d: Drawable): ByteArray {

            var mUserData = byteArrayOf()
            val bitmap = (d as BitmapDrawable).bitmap

            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            mUserData = bytes.toByteArray()

            return mUserData
        }


        fun URLtoByteArray2(toDownload: String): ByteArray {

            val img = Base64.decode(toDownload, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(img, 0, img.size)

            return img
        }


        fun hideKeyboard(context: Activity?) {

            if (context != null) {
                val view = context.currentFocus
                if (view != null) {
                    val inputManager =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(
                            view.windowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        }


        fun drawableFromUrl(url: String): Bitmap? {
            var x: Bitmap? = null

            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val input = connection.inputStream

                x = BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            //return new BitmapDrawable(x);
            return x
        }


        fun getPath(activity: Activity, uri: Uri): String {
            var cursor = activity.contentResolver.query(uri, null, null, null, null)
            cursor!!.moveToFirst()
            var document_id = cursor.getString(0)
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
            cursor.close()

            cursor = activity.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Images.Media._ID + " = ? ",
                    arrayOf(document_id),
                    null
            )
            cursor!!.moveToFirst()
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
            cursor.close()

            return path
        }


        @Throws(IOException::class)
        fun getBytes(inputStream: InputStream): ByteArray {
            val byteBuffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            val len = inputStream.read(buffer)
            while (len != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            return byteBuffer.toByteArray()
        }

        fun getBitmapFromURL(src: String): Bitmap? {
            try {
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                return BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                // Log exception
                return null
            }

        }


        @Throws(MalformedURLException::class)
        fun extractYoutubeId(url: String): String {
            return "http://img.youtube.com/vi/" + getYoutubeVideoIdFromUrl(url) + "/0.jpg"

            /*String query = new URL(url).getQuery();
        String[] param = query.split("&");
        String id = null;
        for (String row : param) {
            String[] param1 = row.split("=");
            if (param1[0].equals("v")) {
                id = param1[1];
            }
        }
        return id;*/
        }

        fun getYoutubeVideoIdFromUrl(inUrl: String): String? {
            if (inUrl.toLowerCase().contains("youtu.be")) {
                return inUrl.substring(inUrl.lastIndexOf("/") + 1)
            }
            val pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
            val compiledPattern = Pattern.compile(pattern)
            val matcher = compiledPattern.matcher(inUrl)
            return if (matcher.find()) {
                matcher.group()
            } else null
        }


        /*  public static void TermsAndConditions(final Context context, String url) {

        final ProgressD[] progressD = new ProgressD[1];
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.terms_conditions);

        final WebView webview = dialog.findViewById(R.id.webview);
        webview.loadUrl(url);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressD[0] = ProgressD.show(context, context.getString(R.string.connecting), false);
                webview.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                progressD[0].dismiss();
                webview.setVisibility(View.VISIBLE);
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }*/


        fun getMapDirection(context: Context) {

            /*  String myLatitude = data.lat;
        String myLongitude = data.lng;
        String labelLocation = data.location;
        String location1 = "http://maps.google.com/maps?q=" + myLatitude + "," + myLongitude + "(" + labelLocation + ")&iwloc=A&hl=es";*/
            val location2 = "geo:<28.468558>,<77.495651>?q=<28.468558>,<77.495651>(Label+Name)"
            val gmmIntentUri = Uri.parse(location2)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            mapIntent.setClassName(
                    "com.google.android.apps.maps",
                    "com.google.android.maps.MapsActivity"
            )
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            }


        }


        /*    public static void zoomImage(final Context context, String url) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.zoom_image_dialog);
        final ImageView imageView = dialog.findViewById(R.id.imageView);
        Picasso.with(context).load(url).placeholder(R.mipmap.avatar_male).into(imageView);
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }*/

        fun isAppRunning(context: Context): Boolean {
            val activityManager =
                    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val procInfos = activityManager.runningAppProcesses
            if (procInfos != null) {
                for (processInfo in procInfos) {
                    if (processInfo.processName == context.packageName) {
                        return true
                    }
                }
            }
            return false
        }


        fun isMeetingCompleted(status: String): Boolean {

            if (status == "Completed") {
                return true
            }
            return false
        }


        fun getCompleteAddressString(
                context: Context,
                LATITUDE: Double,
                LONGITUDE: Double
        ): String {
            var addressLine = ""
            val geocoder: Geocoder
            var addresses: List<Address>? = null
            geocoder = Geocoder(context, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
                val address = addresses!![0].getAddressLine(0)
                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName
                addressLine = address
            } catch (e: IOException) {
                e.printStackTrace()
            }

            Log.e("Complete", "Address is : $addressLine")
            return addressLine
        }


        fun showMapDirection(context: Context, latitude: Double, longitude: Double, title: String) {
            try {
                if (CommonMethod.isOnline(context)) {
                    /* String desLocation = "&daddr=" + latitude + "," + longitude;
                String currLocation = "saddr=" + SavedPreferences.getActiveInstance(context).getLatitude() + "," + SavedPreferences.getActiveInstance(context).getLongitude();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + currLocation + desLocation + "&dirflg=d"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                context.startActivity(intent);*/

                    val strUri = "http://maps.google.com/maps?q=loc:$latitude,$longitude ($title)"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
                    intent.setClassName(
                            "com.google.android.apps.maps",
                            "com.google.android.maps.MapsActivity"
                    )
                    context.startActivity(intent)


                }
            } catch (e: Exception) {
                Log.e("Common Method :", "Error when showing google map directions, E: $e")
            }

        }


        fun finishFragment(fragment: Fragment, activity: FragmentActivity?) {
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.remove(fragment)
        }


        fun convertStringToByte(image: String): ByteArray {
            return Base64.decode(image, Base64.DEFAULT)
        }

        fun spinnerText(parent: AdapterView<*>) {
            (parent.getChildAt(0) as TextView).setTextColor(Color.DKGRAY)
            (parent.getChildAt(0) as TextView).textSize = 14f
        }

        fun convertByteToString(bytes: ByteArray): String {
            return resizeBase64Image(Base64.encodeToString(bytes, Base64.DEFAULT))
        }

        fun resizeBase64Image(base64image: String): String {
            val encodeByte = Base64.decode(base64image.toByteArray(), Base64.DEFAULT)
            val options = BitmapFactory.Options()
            options.inPurgeable = true
            var image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size, options)


            if (image.height <= 400 && image.width <= 400) {
                return base64image
            }
            image = Bitmap.createScaledBitmap(image, 800, 800, false)

            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, baos)

            val b = baos.toByteArray()
            System.gc()
            return Base64.encodeToString(b, Base64.NO_WRAP)

        }

        fun setBase64Image(imageView: ImageView, base64String: String) {
            if (!base64String.isEmpty()) {
                val bytes = Base64.decode(base64String, Base64.DEFAULT)
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            }
        }


        fun convert12HrsFormat(format: String, time: String?): String {
            var time12Hour: String = time.toString()
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                val dateObj = sdf.parse(time!!)
                time12Hour = SimpleDateFormat("K:mm a", Locale.getDefault()).format(dateObj!!)
                if (time12Hour.toCharArray()[0] == '0') {
                    time12Hour = "12" + time12Hour.substring(1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return time12Hour
        }


        fun isLocationEnabled(context: Context): Boolean {
            var locationMode = 0
            val locationProviders: String
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    locationMode = Settings.Secure.getInt(
                            context.contentResolver,
                            Settings.Secure.LOCATION_MODE
                    )
                } catch (e: Settings.SettingNotFoundException) {
                    e.printStackTrace()
                    return false
                }

                return locationMode != Settings.Secure.LOCATION_MODE_OFF

            } else {
                locationProviders =
                        Settings.Secure.getString(
                                context.contentResolver,
                                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
                        )
                return !TextUtils.isEmpty(locationProviders)
            }
        }


        fun clearNotifications(context: Context) {
            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }


        fun setLanguage(activity: Activity, lang: String) {
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val resources = activity.resources
            val configuration = resources.configuration
            if (Build.VERSION.SDK_INT >= 17) {
                configuration.setLocale(locale)
            } else {
                configuration.locale = locale
            }
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        fun print(text: String) {
            Log.e("###-->", text)
        }


        fun currentCountry(context: Context): String {
            val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            return locale.getDisplayCountry(locale)
        }


        fun showFinishAlert(msg: String?, context: Activity?) {
            val alertDialog = AlertDialog.Builder(context, R.style.DatePickerDialogThemes)
            alertDialog.setMessage(msg)
            alertDialog.setCancelable(false)

            alertDialog.setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
                context?.finish()
            }

            // Showing Alert Message
            alertDialog.show()

        }


        fun roundOffDecimal(number: Float): String? {
            val df = DecimalFormat("0.00")
            return df.format(number).replace(",", ".")
        }

        fun shareIntent(context: Activity) {

            try {
                val meetingLink = "<a href=${meetingData?.meetingLink!!}>${meetingData?.meetingLink!!}</a>"
                var date = ""
                var timeZone = ""
                var time = ""
                var description = ""

                if (meetingData.meetingType != "Personal") {
                    val tz = TimeZone.getDefault()
                    tz.id = meetingData?.timeZone!!
                    date = convertDate(meetingData?.startDate).toString()
                    timeZone = formatTimeZone(tz)
                    time = meetingData?.startTime!! + " (" + convertMinuteToHour(meetingData?.duration!!) + ")"
                    description = meetingData?.description.toString()
                }

                val subject = "Meeting scheduled for " + meetingData?.topic
                val text = "Hi there,\n\n" +
                        "You are invited to a scheduled meeting.\n\n" +
                        "Meeting Name: " + meetingData?.topic!! + "\n\n" +
                        "Meeting description: " + description + "\n\n" +
                        "When: " + date + "\n" + timeZone + "\n" + time + "\n\n" +
                        "Please Use this URL to join:\n" + meetingData?.meetingLink!! + "\n\n" +
                        "Your meeting credentials:\n" +
                        "Meeting ID: " + meetingData?.meetingId + "\n" +
                        "Password: " + meetingData?.meetingPassword!! + "\n\n" +
                        "Note: You can directly join the meeting through the link and make sure to install the app on your device."


                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SENDTO
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                sendIntent.putExtra(Intent.EXTRA_TEXT, text)


                val bottomSheetDialog = BottomSheetDialog(context)

                val sheetView: View = context.layoutInflater.inflate(R.layout.share_layout, null)

                bottomSheetDialog.setContentView(sheetView)

                bottomSheetDialog.window!!.attributes.windowAnimations = R.style.dialog_animation


                sheetView.layout_copy_url.setOnClickListener {
                    val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val myClip = ClipData.newPlainText("text", meetingData.meetingLink)
                    myClipboard?.setPrimaryClip(myClip!!)
                    Toast.makeText(context, "Link Copied", Toast.LENGTH_SHORT).show();
                }

                sheetView.layout_message.setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.putExtra("sms_body", subject + "\n\n" + text)
                        intent.data = Uri.parse("sms:")
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }

                sheetView.layout_email.setOnClickListener {
                    sendIntent.type = "message/rfc822"
                    sendIntent.data = Uri.parse("mailto:")
                    context.startActivity(Intent.createChooser(sendIntent, "Share Via : "))
                }

                bottomSheetDialog.show()


            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


        fun shareOnMeeting(context: Activity) {
            try {
                val meetingLink = "<a href=${meetingData.meetingLink}>${meetingData.meetingLink}</a>"
                val subject = "Please join the meeting in progress : " + meetingData.topic
                val text = "Hi there,\n\n" +
                        "Join the ongoing meeting. Please Use this URL to join:\n\n" + meetingData.meetingLink + "\n\n" +
                        "Your meeting credentials:\n" +
                        "Meeting ID: " + meetingData.meetingId + "\n" +
                        "Password: " + meetingData.meetingPassword + "\n\n" +
                        "Note: You can directly join the meeting through the link and make sure to install the app on your device."

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SENDTO
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                sendIntent.putExtra(Intent.EXTRA_TEXT, text)


                val bottomSheetDialog = BottomSheetDialog(context)

                val sheetView: View = context.layoutInflater.inflate(R.layout.share_layout, null)

                bottomSheetDialog.setContentView(sheetView)

                bottomSheetDialog.window!!.attributes.windowAnimations = R.style.dialog_animation


                sheetView.layout_copy_url.setOnClickListener {
                    val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val myClip = ClipData.newPlainText("text", meetingData.meetingLink)
                    myClipboard?.setPrimaryClip(myClip!!)
                    Toast.makeText(context, "Link Copied", Toast.LENGTH_SHORT).show();
                }

                sheetView.layout_message.setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.putExtra("sms_body", subject + "\n\n" + text)
                        intent.data = Uri.parse("sms:")
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }

                sheetView.layout_email.setOnClickListener {
                    sendIntent.type = "message/rfc822"
                    sendIntent.data = Uri.parse("mailto:")
                    context.startActivity(Intent.createChooser(sendIntent, "Share Via : "))
                }

                bottomSheetDialog.show()


            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


        fun shareIntentCall(context: Context, str: String) {
            try {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, str)
                sendIntent.type = "text/plain"
                context.startActivity(Intent.createChooser(sendIntent, "Share Via : "))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun getTokenHeaderMap(): HashMap<String, String> {
            val headerMap = HashMap<String, String>()
            headerMap["Accept"] = Constants.ACCEPT
            headerMap["Authorization"] = Constants.AUTH_TOKEN_HEADER
            headerMap["Host"] = Constants.HOST
            headerMap["Content-Type"] = Constants.CONTENT_TYPE
            return headerMap
        }

        fun getHeaderMap(): HashMap<String, String> {
            val headerMap = HashMap<String, String>()
            headerMap["Accept"] = Constants.ACCEPT
            headerMap["Authorization"] = "Bearer " + AppPrefs.getString(Constants.ACCESS_TOKEN)
            headerMap["Host"] = Constants.HOST
            headerMap["Content-Type"] = Constants.CONTENT_TYPE
            return headerMap
        }


        fun showToast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }

        fun callBrowserIntent(context: Context, s: String) {
            try {
                var url = s
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://$url"

                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun getRandomString(sizeOfRandomString: Int): String {
            val random = Random()
            val sb = StringBuilder(sizeOfRandomString)
            for (i in 0 until sizeOfRandomString)
                sb.append(RANDOM_ALLOWED_CHARACTERS[random.nextInt(RANDOM_ALLOWED_CHARACTERS.length)])
            return sb.toString()
        }


        private fun getDeviceName(context: Context) {
            Log.e("Build.MANUFACTURER---", Build.MANUFACTURER)
            val manufacturer = "xiaomi"
            if (manufacturer == Build.MANUFACTURER) {
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.type = "text/plain"
                context.startActivity(emailIntent)
            }
        }


        fun showNetworkAlert(context: Context?) {
            try {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setMessage(Constants.SERVER_ERROR)
                alertDialog.setCancelable(true)

                alertDialog.setPositiveButton("Settings") { dialog, which ->
                    if (isOnline(context!!)) {
                        dialog.dismiss()
                    } else {
                        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        context.startActivity(intent)
                        dialog.dismiss()
                    }
                }
                alertDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}

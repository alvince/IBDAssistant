package cn.alvince.ibdassistant.main.model

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cn.alvince.ibdassistant.R

enum class NavItem(val tag: String, private @StringRes val label: Int, @DrawableRes val icon: Int) {

    Home("Home_Slice_Alarm", R.string.home_nav_alarm, R.drawable.ic_time_auto_24_fill),

    Toolbox("Home_Slice_Toolbox", R.string.home_nav_toolbox, R.drawable.ic_service_toolbox_24_fill),

    Mine("Home_Slice_Profile", R.string.home_nav_profile, R.drawable.ic_assignment_ind_24_fill),

    ;

    fun labelText(context: Context) = context.getString(label)
}

package com.byagowi.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.CalendarAdapter;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.GregorianCalendarEvent;
import com.byagowi.persiancalendar.entity.IslamicCalendarEvent;
import com.byagowi.persiancalendar.entity.PersianCalendarEvent;
import com.byagowi.persiancalendar.enums.CalendarTypeEnum;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.AthanActivity;
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;

public class CalendarFragment extends Fragment
        implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private ViewPager monthViewPager;

    private Calendar calendar = Calendar.getInstance();

    private Coordinate coordinate;

    private PrayTimesCalculator prayTimesCalculator;
    private TextView imsakTextView;
    private TextView fajrTextView;
    private TextView dhuhrTextView;
    private TextView asrTextView;
    private TextView maghribTextView;
    private TextView ishaTextView;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private TextView midnightTextView;

    private TextView weekDayName;
    private TextView gregorianDate;
    private TextView gregorianDateDay;
    private TextView gregorianDateLinear;
    private TextView islamicDate;
    private TextView islamicDateDay;
    private TextView islamicDateLinear;
    private TextView shamsiDate;
    private TextView shamsiDateDay;
    private TextView shamsiDateLinear;
    private TextView eventTitle;
    private TextView eventMessage;
    private TextView holidayTitle;
    private TextView today;
    private AppCompatImageView todayIcon;
    private AppCompatImageView warnUserIcon;

    private AppCompatImageView moreCalendar;
    private AppCompatImageView moreOwghat;
    private AppCompatImageView owghatIcon;


    private CardView owghat;
    private CardView event;

    private LinearLayoutCompat imsakLayout;
    private LinearLayoutCompat fajrLayout;
    private LinearLayoutCompat sunriseLayout;
    private LinearLayoutCompat dhuhrLayout;
    private LinearLayoutCompat asrLayout;
    private LinearLayoutCompat sunsetLayout;
    private LinearLayoutCompat maghribLayout;
    private LinearLayoutCompat ishaLayout;
    private LinearLayoutCompat midnightLayout;

    private CardView calendarsCard;

    private int viewPagerPosition;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        viewPagerPosition = 0;

        imsakLayout = view.findViewById(R.id.imsakLayout);
        fajrLayout = view.findViewById(R.id.fajrLayout);
        sunriseLayout = view.findViewById(R.id.sunriseLayout);
        dhuhrLayout = view.findViewById(R.id.dhuhrLayout);
        asrLayout = view.findViewById(R.id.asrLayout);
        sunsetLayout = view.findViewById(R.id.sunsetLayout);
        maghribLayout = view.findViewById(R.id.maghribLayout);
        ishaLayout = view.findViewById(R.id.ishaLayout);
        midnightLayout = view.findViewById(R.id.midnightLayout);

        gregorianDate = view.findViewById(R.id.gregorian_date);
        gregorianDateDay = view.findViewById(R.id.gregorian_date_day);
        gregorianDateLinear = view.findViewById(R.id.gregorian_date_linear);
        islamicDate = view.findViewById(R.id.islamic_date);
        islamicDateDay = view.findViewById(R.id.islamic_date_day);
        islamicDateLinear = view.findViewById(R.id.islamic_date_linear);
        shamsiDate = view.findViewById(R.id.shamsi_date);
        shamsiDateDay = view.findViewById(R.id.shamsi_date_day);
        shamsiDateLinear = view.findViewById(R.id.shamsi_date_linear);
        weekDayName = view.findViewById(R.id.week_day_name);
        today = view.findViewById(R.id.today);
        todayIcon = view.findViewById(R.id.today_icon);
        warnUserIcon = view.findViewById(R.id.warn_user_icon);
        today.setVisibility(View.GONE);
        todayIcon.setVisibility(View.GONE);

        imsakTextView = view.findViewById(R.id.imsak);
        fajrTextView = view.findViewById(R.id.fajr);
        dhuhrTextView = view.findViewById(R.id.dhuhr);
        asrTextView = view.findViewById(R.id.asr);
        maghribTextView = view.findViewById(R.id.maghrib);
        ishaTextView = view.findViewById(R.id.isgha);
        sunriseTextView = view.findViewById(R.id.sunrise);
        sunsetTextView = view.findViewById(R.id.sunset);
        midnightTextView = view.findViewById(R.id.midnight);

        moreCalendar = view.findViewById(R.id.more_calendar);

        moreOwghat = view.findViewById(R.id.more_owghat);

        eventTitle = view.findViewById(R.id.event_title);
        eventMessage = view.findViewById(R.id.event_message);
        holidayTitle = view.findViewById(R.id.holiday_title);

        owghat = view.findViewById(R.id.owghat);
        owghatIcon = view.findViewById(R.id.owghat_icon);
        event = view.findViewById(R.id.cardEvent);

        monthViewPager = view.findViewById(R.id.calendar_pager);

        coordinate = Utils.getCoordinate(getContext());
        prayTimesCalculator = new PrayTimesCalculator(Utils.getCalculationMethod());
        monthViewPager.setAdapter(new CalendarAdapter(getChildFragmentManager(),
                Utils.isRTL(getContext())));
        CalendarAdapter.gotoOffset(monthViewPager, 0);

        monthViewPager.addOnPageChangeListener(this);

        owghat.setOnClickListener(this);
        today.setOnClickListener(this);
        todayIcon.setOnClickListener(this);
        gregorianDate.setOnClickListener(this);
        gregorianDateLinear.setOnClickListener(this);
        islamicDate.setOnClickListener(this);
        islamicDateDay.setOnClickListener(this);
        islamicDateLinear.setOnClickListener(this);
        shamsiDate.setOnClickListener(this);
        shamsiDateDay.setOnClickListener(this);
        shamsiDateLinear.setOnClickListener(this);

        calendarsCard = view.findViewById(R.id.calendars_card);
        calendarsCard.setOnClickListener(this);

        warnUserIcon.setVisibility(View.GONE);
        gregorianDateLinear.setVisibility(View.GONE);
        islamicDateLinear.setVisibility(View.GONE);
        shamsiDateLinear.setVisibility(View.GONE);

        String cityName = Utils.getCityName(getContext(), false);
        if (!TextUtils.isEmpty(cityName)) {
            ((TextView) view.findViewById(R.id.owghat_text))
                    .append(" (" + cityName + ")");
        }

        // This will immediately be replaced by the same functionality on fragment but is here to
        // make sure enough space is dedicated to actionbar's title and subtitle, kinda hack anyway
        AbstractDate today = Utils.getTodayOfCalendar(Utils.getMainCalendar());
        Utils.setActivityTitleAndSubtitle(getActivity(), Utils.getMonthName(today),
                Utils.formatNumber(today.getYear()));

        // Easter egg to test AthanActivity
        owghatIcon.setOnLongClickListener(v -> {
            startActivity(new Intent(getContext(), AthanActivity.class));
            return true;
        });

        return view;
    }

    void changeMonth(int position) {
        monthViewPager.setCurrentItem(monthViewPager.getCurrentItem() + position, true);
    }

    void selectDay(long jdn) {
        PersianDate persianDate = DateConverter.jdnToPersian(jdn);
        weekDayName.setText(Utils.getWeekDayName(persianDate));
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        IslamicDate hijriDate = DateConverter.civilToIslamic(civilDate, Utils.getIslamicOffset());

        shamsiDateLinear.setText(Utils.toLinearDate(persianDate));
        shamsiDateDay.setText(Utils.formatNumber(persianDate.getDayOfMonth()));
        shamsiDate.setText(Utils.getMonthName(persianDate) + "\n" + Utils.formatNumber(persianDate.getYear()));

        gregorianDateLinear.setText(Utils.toLinearDate(civilDate));
        gregorianDateDay.setText(Utils.formatNumber(civilDate.getDayOfMonth()));
        gregorianDate.setText(Utils.getMonthName(civilDate) + "\n" + Utils.formatNumber(civilDate.getYear()));

        islamicDateLinear.setText(Utils.toLinearDate(hijriDate));
        islamicDateDay.setText(Utils.formatNumber(hijriDate.getDayOfMonth()));
        islamicDate.setText(Utils.getMonthName(hijriDate) + "\n" + Utils.formatNumber(hijriDate.getYear()));

        if (Utils.getTodayJdn() == jdn) {
            today.setVisibility(View.GONE);
            todayIcon.setVisibility(View.GONE);
            if (Utils.isIranTime())
                weekDayName.setText(weekDayName.getText() + " (" + getString(R.string.iran_time) + ")");
        } else {
            today.setVisibility(View.VISIBLE);
            todayIcon.setVisibility(View.VISIBLE);
        }

        setOwghat(civilDate);
        showEvent(jdn);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void addEventOnCalendar(long jdn) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        CivilDate civil = DateConverter.jdnToCivil(jdn);

        intent.putExtra(CalendarContract.Events.DESCRIPTION, Utils.dayTitleSummary(
                Utils.getDateFromJdnOfCalendar(Utils.getMainCalendar(), jdn)));

        Calendar time = Calendar.getInstance();
        time.set(civil.getYear(), civil.getMonth() - 1, civil.getDayOfMonth());

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

        startActivity(intent);
    }

    private int maxSupportedYear = 1397;

    private void showEvent(long jdn) {
        List<AbstractEvent> events = Utils.getEvents(jdn);
        String holidays = Utils.getEventsTitle(events, true);
        String nonHolidays = Utils.getEventsTitle(events, false);

        event.setVisibility(View.GONE);
        holidayTitle.setVisibility(View.GONE);
        eventMessage.setVisibility(View.GONE);
        eventTitle.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(holidays)) {
            holidayTitle.setText(holidays);
            holidayTitle.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(nonHolidays)) {
            eventTitle.setText(nonHolidays);
            eventTitle.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }

        String messageToShow = "";
        if (Utils.getToday().getYear() > maxSupportedYear)
            messageToShow = getString(R.string.shouldBeUpdated);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
        if (enabledTypes.size() == 0) {
            if (!TextUtils.isEmpty(messageToShow))
                messageToShow += "\n";
            messageToShow += getString(R.string.warn_if_events_not_set);
        }

        if (!TextUtils.isEmpty(messageToShow)) {
            warnUserIcon.setVisibility(View.VISIBLE);
            eventMessage.setText(messageToShow);
            eventMessage.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }
    }

    private void setOwghat(CivilDate civilDate) {
        if (coordinate == null) {
            owghat.setVisibility(View.GONE);
            return;
        }

        calendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = calendar.getTime();

        Map<PrayTime, Clock> prayTimes = prayTimesCalculator.calculate(date, coordinate);

        imsakTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.IMSAK)));
        fajrTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.FAJR)));
        sunriseTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNRISE)));
        dhuhrTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.DHUHR)));
        asrTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.ASR)));
        sunsetTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNSET)));
        maghribTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.MAGHRIB)));
        ishaTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.ISHA)));
        midnightTextView.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.MIDNIGHT)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.calendars_card:
                boolean isOpenCalendarCommand = gregorianDateLinear.getVisibility() != View.VISIBLE;

                moreCalendar.setImageResource(isOpenCalendarCommand
                        ? R.drawable.ic_keyboard_arrow_up
                        : R.drawable.ic_keyboard_arrow_down);
                gregorianDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                islamicDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                shamsiDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);

                break;

            case R.id.owghat:

                boolean isOpenOwghatCommand = sunriseLayout.getVisibility() == View.GONE;

                moreOwghat.setImageResource(isOpenOwghatCommand
                        ? R.drawable.ic_keyboard_arrow_up
                        : R.drawable.ic_keyboard_arrow_down);
                imsakLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                sunriseLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                asrLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                sunsetLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                ishaLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                midnightLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);

                break;

            case R.id.today:
            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.shamsi_date:
            case R.id.shamsi_date_day:
                Utils.copyToClipboard(getContext(), shamsiDateDay.getText() + " " +
                        shamsiDate.getText().toString().replace("\n", " "));
                break;

            case R.id.shamsi_date_linear:
                Utils.copyToClipboard(getContext(), shamsiDateLinear.getText());
                break;

            case R.id.gregorian_date:
            case R.id.gregorian_date_day:
                Utils.copyToClipboard(getContext(), gregorianDateDay.getText() + " " +
                        gregorianDate.getText().toString().replace("\n", " "));
                break;

            case R.id.gregorian_date_linear:
                Utils.copyToClipboard(getContext(), gregorianDateLinear.getText());
                break;

            case R.id.islamic_date:
            case R.id.islamic_date_day:
                Utils.copyToClipboard(getContext(), islamicDateDay.getText() + " " +
                        islamicDate.getText().toString().replace("\n", " "));
                break;

            case R.id.islamic_date_linear:
                Utils.copyToClipboard(getContext(), islamicDateLinear.getText());
                break;
        }
    }

    private void bringTodayYearMonth() {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        CalendarAdapter.gotoOffset(monthViewPager, 0);

        selectDay(DateConverter.persianToJdn(Utils.getToday()));
    }

    public void bringDate(long jdn) {
        CalendarTypeEnum mainCalendar = Utils.getMainCalendar();
        AbstractDate today = Utils.getTodayOfCalendar(mainCalendar);
        AbstractDate date = Utils.getDateFromJdnOfCalendar(mainCalendar, jdn);
        viewPagerPosition =
                (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();
        CalendarAdapter.gotoOffset(monthViewPager, viewPagerPosition);

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, date.getDayOfMonth());

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        selectDay(jdn);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                CalendarAdapter.positionToOffset(position));
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        today.setVisibility(View.VISIBLE);
        todayIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.calendar_menu_button, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchAutoComplete.setAdapter(new ArrayAdapter<>(getContext(),
                R.layout.suggestion, android.R.id.text1, Utils.allEnabledEventsTitles));
        searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            Object ev = Utils.allEnabledEvents.get(Utils.allEnabledEventsTitles.indexOf(
                    (String) parent.getItemAtPosition(position)));
            PersianDate todayPersian = Utils.getToday();
            long todayJdn = DateConverter.persianToJdn(todayPersian);
            IslamicDate todayIslamic = DateConverter.jdnToIslamic(todayJdn);
            CivilDate todayCivil = DateConverter.jdnToCivil(todayJdn);

            if (ev instanceof PersianCalendarEvent) {
                PersianDate date = ((PersianCalendarEvent) ev).getDate();
                bringDate(DateConverter.persianToJdn(todayPersian.getYear() +
                                (date.getMonth() < todayPersian.getMonth() ? 1 : 0),
                        date.getMonth(), date.getDayOfMonth()));
            } else if (ev instanceof IslamicCalendarEvent) {
                IslamicDate date = ((IslamicCalendarEvent) ev).getDate();
                bringDate(DateConverter.islamicToJdn(todayIslamic.getYear() +
                                (date.getMonth() < todayIslamic.getMonth() ? 1 : 0),
                        date.getMonth(), date.getDayOfMonth()));
            } else if (ev instanceof GregorianCalendarEvent) {
                CivilDate date = ((GregorianCalendarEvent) ev).getDate();
                bringDate(DateConverter.civilToJdn(todayCivil.getYear() +
                                (date.getMonth() < todayCivil.getMonth() ? 1 : 0),
                        date.getMonth(), date.getDayOfMonth()));
            }
            searchView.onActionViewCollapsed();
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_to:
                SelectDayDialog dialog = new SelectDayDialog();
                dialog.show(getChildFragmentManager(), SelectDayDialog.class.getName());
                break;
            default:
                break;
        }
        return true;
    }

    public int getViewPagerPosition() {
        return viewPagerPosition;
    }
}
